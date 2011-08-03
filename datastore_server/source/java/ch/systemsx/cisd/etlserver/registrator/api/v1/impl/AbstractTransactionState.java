/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.DynamicTransactionQuery;
import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IMaterial;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IProject;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISpace;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Abstract superclass for the states a DataSetRegistrationTransaction can be in.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractTransactionState<T extends DataSetInformation>
{
    protected final DataSetRegistrationTransaction<T> parent;

    protected AbstractTransactionState(DataSetRegistrationTransaction<T> parent)
    {
        this.parent = parent;
    }

    public abstract boolean isCommitted();

    public abstract boolean isRolledback();

    /**
     * The state where the transaction is still modifyiable.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class LiveTransactionState<T extends DataSetInformation> extends
            AbstractTransactionState<T>
    {
        // Keeps track of steps that have been executed and may need to be reverted. Elements are
        // kept in the order they need to be reverted.
        private final RollbackStack rollbackStack;

        // The directory to use as "local" for paths
        private final File workingDirectory;

        // The directory in which new data sets get staged
        private final File stagingDirectory;

        // The registration service that owns this transaction
        private final DataSetRegistrationService<T> registrationService;

        // The interface to openBIS
        private final IEncapsulatedOpenBISService openBisService;

        private final IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory;

        private final List<DataSet<T>> registeredDataSets = new ArrayList<DataSet<T>>();

        private final List<DataSetUpdatable> dataSetsToBeUpdated =
                new ArrayList<DataSetUpdatable>();

        private final List<Experiment> experimentsToBeRegistered = new ArrayList<Experiment>();

        private final List<Space> spacesToBeRegistered = new ArrayList<Space>();

        private final List<Project> projectsToBeRegistered = new ArrayList<Project>();

        private final List<Sample> samplesToBeRegistered = new ArrayList<Sample>();

        private final List<Sample> samplesToBeUpdated = new ArrayList<Sample>();

        private final List<Material> materialsToBeRegistered = new ArrayList<Material>();

        private final Map<String, DynamicTransactionQuery> queriesToCommit =
                new HashMap<String, DynamicTransactionQuery>();

        private String userIdOrNull = null;

        public LiveTransactionState(DataSetRegistrationTransaction<T> parent,
                RollbackStack rollbackStack, File workingDirectory, File stagingDirectory,
                DataSetRegistrationService<T> registrationService,
                IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
        {
            super(parent);
            this.rollbackStack = rollbackStack;
            this.workingDirectory = workingDirectory;
            this.stagingDirectory = stagingDirectory;
            this.registrationService = registrationService;
            this.openBisService =
                    this.registrationService.getRegistratorContext().getGlobalState()
                            .getOpenBisService();
            this.registrationDetailsFactory = registrationDetailsFactory;
        }

        public String getUserId()
        {
            return userIdOrNull;
        }

        public void setUserId(String userIdOrNull)
        {
            this.userIdOrNull = userIdOrNull;
        }

        public IDataSet createNewDataSet()
        {
            // Create registration details for the new data set
            DataSetRegistrationDetails<T> registrationDetails =
                    registrationDetailsFactory.createDataSetRegistrationDetails();

            return createNewDataSet(registrationDetails);
        }

        public IDataSet createNewDataSet(String dataSetType)
        {
            // Create registration details for the new data set
            DataSetRegistrationDetails<T> registrationDetails =
                    registrationDetailsFactory.createDataSetRegistrationDetails();
            registrationDetails.setDataSetType(dataSetType);

            return createNewDataSet(registrationDetails);
        }

        public IDataSet createNewDataSet(String dataSetType, String dataSetCode)
        {
            // Create registration details for the new data set
            DataSetRegistrationDetails<T> registrationDetails =
                    registrationDetailsFactory.createDataSetRegistrationDetails();
            registrationDetails.setDataSetType(dataSetType);

            return createNewDataSet(registrationDetails, dataSetCode);
        }

        public IDataSet createNewDataSet(DataSetRegistrationDetails<T> registrationDetails)
        {
            // Request a code, so we can keep the staging file name and the data set code in sync
            return createNewDataSet(registrationDetails, registrationDetails
                    .getDataSetInformation().getDataSetCode());
        }

        public IDataSet createNewDataSet(DataSetRegistrationDetails<T> registrationDetails,
                String specifiedCode)
        {
            final String dataSetCode;
            if (null == specifiedCode)
            {
                dataSetCode = generateDataSetCode(registrationDetails);
                registrationDetails.getDataSetInformation().setDataSetCode(dataSetCode);
            } else
            {
                dataSetCode = specifiedCode.toUpperCase();
            }
            registrationDetails.getDataSetInformation().setDataSetCode(dataSetCode);

            // Create a directory for the data set
            File stagingFolder = new File(stagingDirectory, dataSetCode);
            MkdirsCommand cmd = new MkdirsCommand(stagingFolder.getAbsolutePath());
            executeCommand(cmd);

            DataSet<T> dataSet =
                    registrationDetailsFactory.createDataSet(registrationDetails, stagingFolder);

            // If the registration details already contains a sample or experiment, set it on the
            // new data set.

            SampleIdentifier sampleId =
                    registrationDetails.getDataSetInformation().getSampleIdentifier();
            if (null != sampleId)
            {
                ISampleImmutable sample = parent.getSample(sampleId.toString());
                dataSet.setSample(sample);
            }
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                    registrationDetails.getDataSetInformation().tryToGetSample();
            if (sample != null)
            {
                dataSet.setSample(new SampleImmutable(sample));
            }
            ExperimentIdentifier experimentId =
                    registrationDetails.getDataSetInformation().getExperimentIdentifier();
            if (null != experimentId)
            {
                IExperimentImmutable exp = parent.getExperiment(experimentId.toString());
                dataSet.setExperiment(exp);
            }
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment =
                    registrationDetails.getDataSetInformation().tryToGetExperiment();
            if (null != experiment)
            {
                dataSet.setExperiment(new ExperimentImmutable(experiment));
            }

            List<String> parents =
                    registrationDetails.getDataSetInformation().getParentDataSetCodes();
            if (registrationDetails.getDataSetInformation().getParentDataSetCodes() != null)
            {
                dataSet.setParentDatasets(parents);
            }

            registeredDataSets.add(dataSet);
            return dataSet;
        }

        public IDataSetImmutable getDataSet(String dataSetCode)
        {
            ExternalData dataSet = openBisService.tryGetDataSet(dataSetCode);
            if (dataSet == null)
            {
                return null;
            } else
            {
                return new DataSetImmutable(dataSet);
            }
        }

        public IDataSetUpdatable getDataSetForUpdate(String dataSetCode)
        {
            // See if we already have an updatable version of the data set
            DataSetUpdatable result = findDataSetLocally(dataSetCode);
            if (result != null)
            {
                return result;
            }

            ExternalData dataSet = openBisService.tryGetDataSet(dataSetCode);
            if (dataSet == null)
            {
                return null;
            } else
            {
                result = new DataSetUpdatable(dataSet);
                dataSetsToBeUpdated.add(result);
                return result;
            }
        }

        private DataSetUpdatable findDataSetLocally(String dataSetCode)
        {
            // This is a slow implementation. Could be sped up by using a hashmap in the future.
            for (DataSetUpdatable dataSet : dataSetsToBeUpdated)
            {
                if (dataSet.getDataSetCode().equals(dataSetCode))
                {
                    return dataSet;
                }
            }
            return null;
        }

        public ISample getSampleForUpdate(String sampleIdentifierString)
        {
            SampleIdentifier sampleIdentifier =
                    new SampleIdentifierFactory(sampleIdentifierString).createIdentifier();

            // Check if we already have an updatable sample for this one
            Sample result = findSampleLocally(sampleIdentifier);
            if (result != null)
            {
                return result;
            }

            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                    openBisService.tryGetSampleWithExperiment(sampleIdentifier);

            if (sample != null)
            {
                result = new Sample(sample);
                samplesToBeUpdated.add(result);
            }

            return result;
        }

        private Sample findSampleLocally(SampleIdentifier sampleIdentifier)
        {
            String sampleIdentifierString = sampleIdentifier.toString();
            // This is a slow implementation. Could be sped up by using a hashmap in the future.
            for (Sample sample : samplesToBeUpdated)
            {
                if (sample.getSampleIdentifier().equals(sampleIdentifierString))
                {
                    return sample;
                }
            }
            return null;
        }

        public ISample createNewSample(String sampleIdentifierString, String sampleTypeCode)
        {
            String permId = openBisService.createPermId();
            Sample sample = new Sample(sampleIdentifierString, permId);
            sample.setSampleType(sampleTypeCode);
            samplesToBeRegistered.add(sample);
            return sample;
        }

        public IExperiment getExperimentForUpdate(String experimentIdentifierString)
        {
            throw new NotImplementedException();
        }

        public IExperiment createNewExperiment(String experimentIdentifierString,
                String experimentTypeCode)
        {
            String permId = openBisService.createPermId();
            Experiment experiment = new Experiment(experimentIdentifierString, permId);
            experiment.setExperimentType(experimentTypeCode);
            experimentsToBeRegistered.add(experiment);
            return experiment;
        }

        public ISpace createNewSpace(String spaceCode, String spaceAdminUserIdOrNull)
        {
            Space space = new Space(spaceCode, spaceAdminUserIdOrNull);
            spacesToBeRegistered.add(space);
            return space;
        }

        public IProject createNewProject(String projectIdentifier)
        {
            Project project = new Project(projectIdentifier);
            projectsToBeRegistered.add(project);
            return project;
        }

        public IMaterial createNewMaterial(String materialCode, String materialType)
        {
            Material material = new Material(materialCode, materialType);
            materialsToBeRegistered.add(material);
            return material;
        }

        public String moveFile(String src, IDataSet dst)
        {
            File srcFile = new File(src);
            return moveFile(src, dst, srcFile.getName());
        }

        public String moveFile(String src, IDataSet dst, String dstInDataset)
        {
            @SuppressWarnings("unchecked")
            DataSet<T> dataSet = (DataSet<T>) dst;

            // See if this is an absolute path
            File srcFile = new File(src);
            if (false == srcFile.exists())
            {
                // Try it relative
                srcFile = new File(workingDirectory, src);
            }

            File dataSetFolder = dataSet.getDataSetStagingFolder();
            File dstFile = new File(dataSetFolder, dstInDataset);

            FileUtilities.checkInputFile(srcFile);

            MoveFileCommand cmd =
                    new MoveFileCommand(srcFile.getParentFile().getAbsolutePath(),
                            srcFile.getName(), dstFile.getParentFile().getAbsolutePath(),
                            dstFile.getName());
            executeCommand(cmd);
            return dstFile.getAbsolutePath();
        }

        public String createNewDirectory(IDataSet dst, String dirName)
        {
            @SuppressWarnings("unchecked")
            DataSet<T> dataSet = (DataSet<T>) dst;
            File dataSetFolder = dataSet.getDataSetStagingFolder();
            File dstFile = new File(dataSetFolder, dirName);
            MkdirsCommand cmd = new MkdirsCommand(dstFile.getAbsolutePath());
            executeCommand(cmd);
            return dstFile.getAbsolutePath();
        }

        public String createNewFile(IDataSet dst, String fileName)
        {
            return createNewFile(dst, "/", fileName);
        }

        public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
        {
            @SuppressWarnings("unchecked")
            DataSet<T> dataSet = (DataSet<T>) dst;
            File dataSetFolder = dataSet.getDataSetStagingFolder();
            File dstFolder = new File(dataSetFolder, dstInDataset);
            File dstFile = new File(dstFolder, fileName);
            NewFileCommand cmd = new NewFileCommand(dstFile.getAbsolutePath());
            executeCommand(cmd);
            return dstFile.getAbsolutePath();
        }

        public DynamicTransactionQuery getDatabaseQuery(String dataSourceName)
        {
            DynamicTransactionQuery query = queriesToCommit.get(dataSourceName);
            if (null == query)
            {
                query = QueryTool.getQuery(DynamicTransactionQuery.class);
                queriesToCommit.put(dataSourceName, query);
            }
            return query;
        }

        public void deleteFile(String src)
        {
            throw new NotImplementedException();
        }

        /**
         * Commit the transaction.
         * 
         * @return true if any datasets has been commited
         */
        public boolean commit()
        {
            ArrayList<DataSetStorageAlgorithm<T>> algorithms =
                    new ArrayList<DataSetStorageAlgorithm<T>>(registeredDataSets.size());
            for (DataSet<T> dataSet : registeredDataSets)
            {
                File contents = dataSet.tryDataSetContents();
                DataSetRegistrationDetails<T> details = dataSet.getRegistrationDetails();

                // The experiment/sample does not yet exist
                if (experimentsToBeRegistered.contains(dataSet.getExperiment())
                        || samplesToBeRegistered.contains(dataSet.getSample()))
                {
                    algorithms.add(registrationService
                            .createStorageAlgorithmWithIdentifiedStrategy(contents, details));
                } else
                {
                    algorithms.add(registrationService.createStorageAlgorithm(contents, details));
                }
            }

            DataSetStorageAlgorithmRunner<T> runner =
                    new DataSetStorageAlgorithmRunner<T>(algorithms, parent, parent);
            List<DataSetInformation> datasets = runner.prepareAndRunStorageAlgorithms();
            for (DynamicTransactionQuery query : queriesToCommit.values())
            {
                query.commit();
                query.close(false);
            }

            return datasets.isEmpty() == false;
        }

        /**
         * Rollback any commands that have been executed. Rollback is done in the reverse order of
         * execution.
         */
        public void rollback()
        {
            rollbackStack.rollbackAll();
            registeredDataSets.clear();
            for (DynamicTransactionQuery query : queriesToCommit.values())
            {
                query.rollback();
                query.close(false);
            }
        }

        /**
         * Execute the command and add it to the list of commands that have been executed.
         */
        private void executeCommand(ITransactionalCommand cmd)
        {
            rollbackStack.pushAndExecuteCommand(cmd);
        }

        /**
         * Generate a data set code for the registration details. Just calls openBisService to get a
         * data set code by default.
         * 
         * @return A data set code
         */
        private String generateDataSetCode(DataSetRegistrationDetails<T> registrationDetails)
        {
            return openBisService.createDataSetCode();
        }

        AtomicEntityOperationDetails<T> createEntityOperationDetails(
                List<DataSetRegistrationInformation<T>> dataSetRegistrations)
        {

            List<NewSpace> spaceRegistrations = convertSpacesToBeRegistered();
            List<NewProject> projectRegistrations = convertProjectsToBeRegistered();
            List<NewExperiment> experimentRegistrations = convertExperimentsToBeRegistered();
            List<SampleUpdatesDTO> sampleUpdates = convertSamplesToBeUpdated();
            List<NewSample> sampleRegistrations = convertSamplesToBeRegistered();
            Map<String, List<NewMaterial>> materialRegistrations = convertMaterialsToBeRegistered();
            List<DataSetUpdatesDTO> dataSetUpdates = convertDataSetsToBeUpdated();

            // experiment updates not yet supported
            List<ExperimentUpdatesDTO> experimentUpdates = new ArrayList<ExperimentUpdatesDTO>();

            AtomicEntityOperationDetails<T> registrationDetails =
                    new AtomicEntityOperationDetails<T>(getUserId(), spaceRegistrations,
                            projectRegistrations, experimentUpdates, experimentRegistrations,
                            sampleUpdates, sampleRegistrations, materialRegistrations,
                            dataSetRegistrations, dataSetUpdates);
            return registrationDetails;
        }

        private List<NewProject> convertProjectsToBeRegistered()
        {
            List<NewProject> result = new ArrayList<NewProject>();
            for (Project apiProject : projectsToBeRegistered)
            {
                result.add(ConversionUtils.convertToNewProject(apiProject));
            }
            return result;
        }

        private List<NewSpace> convertSpacesToBeRegistered()
        {
            List<NewSpace> result = new ArrayList<NewSpace>();
            for (Space apiSpace : spacesToBeRegistered)
            {
                result.add(ConversionUtils.convertToNewSpace(apiSpace));
            }
            return result;
        }

        private List<NewExperiment> convertExperimentsToBeRegistered()
        {
            List<NewExperiment> result = new ArrayList<NewExperiment>();
            for (Experiment apiExperiment : experimentsToBeRegistered)
            {
                result.add(ConversionUtils.convertToNewExperiment(apiExperiment));
            }
            return result;
        }

        private List<NewSample> convertSamplesToBeRegistered()
        {
            List<NewSample> result = new ArrayList<NewSample>();
            for (Sample apiSample : samplesToBeRegistered)
            {
                result.add(ConversionUtils.convertToNewSample(apiSample));
            }
            return result;
        }

        private List<SampleUpdatesDTO> convertSamplesToBeUpdated()
        {
            List<SampleUpdatesDTO> result = new ArrayList<SampleUpdatesDTO>();
            for (Sample apiSample : samplesToBeUpdated)
            {
                result.add(ConversionUtils.convertToSampleUpdateDTO(apiSample));
            }
            return result;
        }

        private List<DataSetUpdatesDTO> convertDataSetsToBeUpdated()
        {
            List<DataSetUpdatesDTO> result = new ArrayList<DataSetUpdatesDTO>();
            for (DataSetUpdatable dataSet : dataSetsToBeUpdated)
            {
                result.add(ConversionUtils.convertToDataSetUpdatesDTO(dataSet));
            }
            return result;
        }

        private Map<String, List<NewMaterial>> convertMaterialsToBeRegistered()
        {
            Map<String, List<NewMaterial>> result = new HashMap<String, List<NewMaterial>>();
            for (Material material : materialsToBeRegistered)
            {
                NewMaterial converted = ConversionUtils.convertToNewMaterial(material);
                String materialType = material.getMaterialType();
                List<NewMaterial> materialsOfSameType = result.get(materialType);
                if (materialsOfSameType == null)
                {
                    materialsOfSameType = new ArrayList<NewMaterial>();
                    result.put(materialType, materialsOfSameType);
                }
                materialsOfSameType.add(converted);
            }
            return result;
        }

        @Override
        public boolean isCommitted()
        {
            return false;
        }

        @Override
        public boolean isRolledback()
        {
            return false;
        }
    }

    private static abstract class TerminalTransactionState<T extends DataSetInformation> extends
            AbstractTransactionState<T>
    {
        private final LiveTransactionState<T> liveState;

        protected TerminalTransactionState(LiveTransactionState<T> liveState)
        {
            super(liveState.parent);
            this.liveState = liveState;
            deleteStagingFolders();
            this.liveState.rollbackStack.discard();
        }

        private void deleteStagingFolders()
        {
            for (DataSet<T> dataSet : liveState.registeredDataSets)
            {
                dataSet.getDataSetStagingFolder().delete();
            }
        }

    }

    /**
     * State where the transaction has been committed.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class CommitedTransactionState<T extends DataSetInformation> extends
            TerminalTransactionState<T>
    {

        public CommitedTransactionState(LiveTransactionState<T> liveState)
        {
            super(liveState);
        }

        @Override
        public boolean isCommitted()
        {
            return true;
        }

        @Override
        public boolean isRolledback()
        {
            return false;
        }
    }

    static class RolledbackTransactionState<T extends DataSetInformation> extends
            TerminalTransactionState<T>
    {
        public RolledbackTransactionState(LiveTransactionState<T> liveState)
        {
            super(liveState);
        }

        @Override
        public boolean isCommitted()
        {
            return false;
        }

        @Override
        public boolean isRolledback()
        {
            return true;
        }
    }
}
