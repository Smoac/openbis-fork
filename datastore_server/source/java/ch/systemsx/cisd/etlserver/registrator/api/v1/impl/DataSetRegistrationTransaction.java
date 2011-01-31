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

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentImmutable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * The implementation of a transaction. This class is designed to be used in one thread.
 * <p>
 * A transaction tracks commands that are invoked on it so they can be reverted (rolledback) if
 * necessary.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationTransaction<T extends DataSetInformation> implements
        IDataSetRegistrationTransaction
{
    // Keeps track of steps that have been executed and may need to be reverted. Elements are kept
    // in the order they need to be reverted.
    private final RollbackStack rollbackStack;

    // The directory to use as "local" for paths
    private final File workingDirectory;

    // The directory in which new data sets get staged
    private final File stagingDirectory;

    // The registration service that owns this transaction
    private final DataSetRegistrationService registrationService;

    // The interface to openBIS
    private final IEncapsulatedOpenBISService openBisService;

    private final IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory;

    private final ArrayList<DataSet<T>> registeredDataSets = new ArrayList<DataSet<T>>();

    public DataSetRegistrationTransaction(File rollBackStackParentFolder, File workingDirectory,
            File stagingDirectory, DataSetRegistrationService registrationService,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
    {
        this(new RollbackStack(new File(rollBackStackParentFolder, "rollBackQueue1"), new File(
                rollBackStackParentFolder, "rollBackQueue2")), workingDirectory, stagingDirectory,
                registrationService, registrationDetailsFactory);
    }
    
    DataSetRegistrationTransaction(RollbackStack rollbackStack, File workingDirectory,
            File stagingDirectory, DataSetRegistrationService registrationService,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
    {
        this.rollbackStack = rollbackStack;
        this.workingDirectory = workingDirectory;
        this.stagingDirectory = stagingDirectory;
        this.registrationService = registrationService;
        this.openBisService =
                this.registrationService.getRegistratorState().getGlobalState().getOpenBisService();
        this.registrationDetailsFactory = registrationDetailsFactory;
    }

    public IDataSet createNewDataSet()
    {
        // Create registration details for the new data set
        DataSetRegistrationDetails<T> registrationDetails =
                registrationDetailsFactory.createDataSetRegistrationDetails();

        // Request a code, so we can keep the staging file name and the data set code in sync
        String dataSetCode = generateDataSetCode(registrationDetails);
        registrationDetails.getDataSetInformation().setDataSetCode(dataSetCode);

        // Create a directory for the data set
        File stagingFolder = new File(stagingDirectory, dataSetCode);
        MkdirsCommand cmd = new MkdirsCommand(stagingFolder.getAbsolutePath());
        executeCommand(cmd);

        DataSet<T> dataSet = new DataSet<T>(registrationDetails, stagingFolder);
        registeredDataSets.add(dataSet);
        return dataSet;
    }

    public ISampleImmutable getSample(String sampleIdentifierString)
    {
        SampleIdentifier sampleIdentifier =
                new SampleIdentifierFactory(sampleIdentifierString).createIdentifier();
        return new Sample(openBisService.tryGetSampleWithExperiment(sampleIdentifier));
    }

    public ISample getSampleForUpdate(String sampleIdentifierString)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ISample createNewSample(String sampleIdentifierString)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IExperimentImmutable getExperiment(String experimentIdentifierString)
    {
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(experimentIdentifierString).createIdentifier();
        ExperimentImmutable experiment =
                new ExperimentImmutable(openBisService.tryToGetExperiment(experimentIdentifier));
        return experiment;
    }

    public IExperiment getExperimentForUpdate(String experimentIdentifierString)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IExperiment createNewExperiment(String experimentIdentifierString)
    {
        // TODO Auto-generated method stub
        return null;
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

        File dataSetFolder = dataSet.getDataSetFolder();
        File dstFile = new File(dataSetFolder, dstInDataset);

        FileUtilities.checkInputFile(srcFile);

        MoveFileCommand cmd =
                new MoveFileCommand(srcFile.getParentFile().getAbsolutePath(), srcFile.getName(),
                        dstFile.getParentFile().getAbsolutePath(), dstFile.getName());
        executeCommand(cmd);
        return dstFile.getAbsolutePath();
    }

    public String createNewDirectory(IDataSet dst, String dirName)
    {
        @SuppressWarnings("unchecked")
        DataSet<T> dataSet = (DataSet<T>) dst;
        File dataSetFolder = dataSet.getDataSetFolder();
        File dstFile = new File(dataSetFolder, dirName);
        MkdirsCommand cmd = new MkdirsCommand(dstFile.getAbsolutePath());
        executeCommand(cmd);
        return null;
    }

    public String createNewFile(IDataSet dst, String fileName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteFile(String src)
    {
        // TODO Auto-generated method stub

    }

    /**
     * Commit the transaction
     */
    public void commit()
    {
        for (DataSet<T> dataSet : registeredDataSets)
        {
            registrationService.queueDataSetRegistration(dataSet.getDataSetFolder(),
                    dataSet.getRegistrationDetails());
        }
    }

    /**
     * Rollback any commands that have been executed. Rollback is done in the reverse order of
     * execution.
     */
    public void rollback()
    {
        rollbackStack.rollbackAll();
    }

    /**
     * Execute the command and add it to the list of commands that have been executed.
     */
    protected void executeCommand(ITransactionalCommand cmd)
    {
        rollbackStack.pushAndExecuteCommand(cmd);
    }

    /**
     * Generate a data set code for the registration details. Just calls openBisService to get a
     * data set code by default.
     * 
     * @return A data set code
     */
    protected String generateDataSetCode(DataSetRegistrationDetails<T> registrationDetails)
    {
        return openBisService.createDataSetCode();
    }

}
