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
import java.io.FilenameFilter;
import java.util.Date;
import java.util.List;
import java.util.Random;

import net.lemnik.eodsql.DynamicTransactionQuery;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPersistentMap;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.IEntityOperationService;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IMaterial;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IProject;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISpace;
import ch.systemsx.cisd.etlserver.registrator.api.v1.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState.CommitedTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState.LiveTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState.RecoveryPendingTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState.RolledbackTransactionState;
import ch.systemsx.cisd.etlserver.registrator.recovery.AutoRecoverySettings;
import ch.systemsx.cisd.etlserver.registrator.recovery.IDataSetStorageRecoveryManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IProjectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISpaceImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * The implementation of a transaction. This class is designed to be used in one thread.
 * <p>
 * A transaction tracks commands that are invoked on it so they can be reverted (rolledback) if
 * necessary.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationTransaction<T extends DataSetInformation> implements
        IDataSetRegistrationTransaction, DataSetStorageAlgorithmRunner.IRollbackDelegate<T>,
        DataSetStorageAlgorithmRunner.IDataSetInApplicationServerRegistrator<T>,
        DataSetRegistrationPersistentMap.IHolder
{
    private static final String ROLLBACK_QUEUE1_FILE_NAME_SUFFIX = "rollBackQueue1";

    private static final String ROLLBACK_QUEUE2_FILE_NAME_SUFFIX = "rollBackQueue2";

    private final static String ROLLBACK_STACK_FILE_NAME_DATE_FORMAT_PATTERN = "yyyyMMddHHmmssSSS";

    private final AutoRecoverySettings autoRecoverySettings;

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetRegistrationTransaction.class);

    public static synchronized RollbackStack[] findRollbackStacks(File rollBackStackParentFolder)
    {
        File[] rollbackQueue1Files = rollBackStackParentFolder.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(ROLLBACK_QUEUE1_FILE_NAME_SUFFIX);
                }
            });

        RollbackStack[] rollbackStacks = new RollbackStack[rollbackQueue1Files.length];

        for (int i = 0; i < rollbackQueue1Files.length; i++)
        {
            File rollbackStackQueue1 = rollbackQueue1Files[i];
            RollbackStack stack = createExistingRollbackStack(rollbackStackQueue1);
            rollbackStacks[i] = stack;
        }
        return rollbackStacks;
    }

    /**
     * Check if there are any uncompleted transactions and roll them back. To be called during
     * startup of a thread.
     */
    public static synchronized void rollbackDeadTransactions(File rollBackStackParentFolder)
    {

        RollbackStack[] stacks = findRollbackStacks(rollBackStackParentFolder);
        for (RollbackStack stack : stacks)
        {
            if (stack.isLockedState())
            {
                operationLog.info("Found rollback stack in locked state: "
                        + stack.getBackingFiles()[0] + ". Not Rolling back.");
            } else
            {
                operationLog.info("Found dead rollback stack: " + stack.getBackingFiles()[0]
                        + ". Rolling back.");

                try
                {
                    stack.rollbackAll();
                } catch (Throwable ex)
                {
                    // This should ever happen since rollbackAll should handle execptions, but is
                    // here
                    // as a safeguard.
                    operationLog.error("Encountered error rolling back transaction:", ex);
                }
                stack.discard();
            }
        }
    }

    /**
     * Create a new persistent rollback stack in the supplied folder.
     */
    private static RollbackStack createNewRollbackStack(File rollBackStackParentFolder)
    {
        // Add a random number to the prefix to distinguish between rollback stacks created in the
        // same millisecond.
        String fileNamePrefix =
                DateFormatUtils.format(new Date(), ROLLBACK_STACK_FILE_NAME_DATE_FORMAT_PATTERN)
                        + "-" + new Random().nextInt(10000) + "-";
        return new RollbackStack(new File(rollBackStackParentFolder, fileNamePrefix
                + ROLLBACK_QUEUE1_FILE_NAME_SUFFIX), new File(rollBackStackParentFolder,
                fileNamePrefix + ROLLBACK_QUEUE2_FILE_NAME_SUFFIX));
    }

    /**
     * Given a queue1 file, create an existing rollback stack
     */
    private static RollbackStack createExistingRollbackStack(File rollbackStackQueue1)
    {
        String rollbackStack1FileName = rollbackStackQueue1.getName();
        // Remove the ROLLBACK_QUEUE1_FILE_NAME_SUFFIX and append the
        // ROLLBACK_QUEUE2_FILE_NAME_SUFFIX
        String rollbackStack2FileName =
                rollbackStack1FileName.substring(0, rollbackStack1FileName.length()
                        - ROLLBACK_QUEUE1_FILE_NAME_SUFFIX.length())
                        + ROLLBACK_QUEUE2_FILE_NAME_SUFFIX;
        return new RollbackStack(rollbackStackQueue1, new File(rollbackStackQueue1.getParentFile(),
                rollbackStack2FileName));
    }

    private AbstractTransactionState<T> state;

    private DataSetRegistrationPersistentMap registrationContext;

    // The registration service that owns this transaction
    private final DataSetRegistrationService<T> registrationService;

    // The interface to openBIS
    private final IEncapsulatedOpenBISService openBisService;

    public DataSetRegistrationTransaction(File rollBackStackParentFolder, File workingDirectory,
            File stagingDirectory, DataSetRegistrationService<T> registrationService,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory,
            AutoRecoverySettings autoRecoverySettings)
    {
        this(createNewRollbackStack(rollBackStackParentFolder), workingDirectory, stagingDirectory,
                registrationService, registrationDetailsFactory, autoRecoverySettings);
    }

    DataSetRegistrationTransaction(RollbackStack rollbackStack, File workingDirectory,
            File stagingDirectory, DataSetRegistrationService<T> registrationService,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory,
            AutoRecoverySettings autoRecoverySettings)
    {
        state =
                new LiveTransactionState<T>(this, rollbackStack, workingDirectory,
                        stagingDirectory, registrationService, registrationDetailsFactory);
        this.registrationService = registrationService;
        this.openBisService =
                this.registrationService.getRegistratorContext().getGlobalState()
                        .getOpenBisService();
        this.registrationContext = new DataSetRegistrationPersistentMap();
        this.autoRecoverySettings = autoRecoverySettings;
    }

    public String getUserId()
    {
        return getStateAsLiveState().getUserId();
    }

    public void setUserId(String userIdOrNull)
    {
        getStateAsLiveState().setUserId(userIdOrNull);
    }

    public IDataSet createNewDataSet()
    {
        return getStateAsLiveState().createNewDataSet();
    }

    public IDataSet createNewDataSet(String dataSetType)
    {
        return getStateAsLiveState().createNewDataSet(dataSetType);
    }

    public IDataSet createNewDataSet(String dataSetType, String dataSetCode)
    {
        return getStateAsLiveState().createNewDataSet(dataSetType, dataSetCode);
    }

    public IDataSet createNewDataSet(DataSetRegistrationDetails<T> registrationDetails)
    {
        return getStateAsLiveState().createNewDataSet(registrationDetails);
    }

    public IDataSet createNewDataSet(IDataSetRegistrationDetailsFactory<T> factory,
            String dataSetType)
    {
        return getStateAsLiveState().createNewDataSet(factory, dataSetType, null);
    }

    public IDataSetImmutable getDataSet(String dataSetCode)
    {
        return getStateAsLiveState().getDataSet(dataSetCode);
    }

    public IDataSetUpdatable getDataSetForUpdate(String dataSetCode)
    {
        return getStateAsLiveState().getDataSetForUpdate(dataSetCode);
    }

    public ISampleImmutable getSample(String sampleIdentifierString)
    {
        SampleIdentifier sampleIdentifier =
                new SampleIdentifierFactory(sampleIdentifierString).createIdentifier();
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sampleOrNull =
                openBisService.tryGetSampleWithExperiment(sampleIdentifier);
        return (null == sampleOrNull) ? null : new SampleImmutable(sampleOrNull);
    }

    public ISample getSampleForUpdate(String sampleIdentifierString)
    {
        return getStateAsLiveState().getSampleForUpdate(sampleIdentifierString);
    }

    public IExperiment getExperimentForUpdate(String experimentIdentifierString)
    {
        return getStateAsLiveState().getExperimentForUpdate(experimentIdentifierString);
    }

    public ISample createNewSample(String sampleIdentifierString, String sampleTypeCode)
    {
        return getStateAsLiveState().createNewSample(sampleIdentifierString, sampleTypeCode);
    }

    public IExperimentImmutable getExperiment(String experimentIdentifierString)
    {
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(experimentIdentifierString).createIdentifier();
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experimentOrNull =
                openBisService.tryToGetExperiment(experimentIdentifier);
        return (null == experimentOrNull) ? null : new ExperimentImmutable(experimentOrNull);
    }

    public IExperiment createNewExperiment(String experimentIdentifierString,
            String experimentTypeCode)
    {
        return getStateAsLiveState().createNewExperiment(experimentIdentifierString,
                experimentTypeCode);
    }

    public IProject createNewProject(String projectIdentifier)
    {
        return getStateAsLiveState().createNewProject(projectIdentifier);
    }

    public IProjectImmutable getProject(String projectIdentifierString)
    {
        ProjectIdentifier projectIdentifier =
                new ProjectIdentifierFactory(projectIdentifierString).createIdentifier();
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project projectOrNull =
                openBisService.tryGetProject(projectIdentifier);
        return (null == projectOrNull) ? null : new ProjectImmutable(projectOrNull);
    }

    public ISpace createNewSpace(String spaceCode, String spaceAdminUserIdOrNull)
    {
        return getStateAsLiveState().createNewSpace(spaceCode, spaceAdminUserIdOrNull);
    }

    public ISpaceImmutable getSpace(String spaceCode)
    {
        SpaceIdentifier spaceIdentifier = new SpaceIdentifier(spaceCode);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space spaceOrNull =
                openBisService.tryGetSpace(spaceIdentifier);
        return (null == spaceOrNull) ? null : new SpaceImmutable(spaceOrNull);
    }

    public IMaterialImmutable getMaterial(String materialCode, String materialType)
    {
        MaterialIdentifier materialIdentifier = new MaterialIdentifier(materialCode, materialType);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material materialOrNull =
                openBisService.tryGetMaterial(materialIdentifier);
        return (null == materialOrNull) ? null : new MaterialImmutable(materialOrNull);
    }

    public IMaterial createNewMaterial(String materialCode, String materialType)
    {
        return getStateAsLiveState().createNewMaterial(materialCode, materialType);
    }

    public String moveFile(String src, IDataSet dst)
    {
        return getStateAsLiveState().moveFile(src, dst);
    }

    public String moveFile(String src, IDataSet dst, String dstInDataset)
    {
        return getStateAsLiveState().moveFile(src, dst, dstInDataset);
    }

    public String createNewDirectory(IDataSet dst, String dirName)
    {
        return getStateAsLiveState().createNewDirectory(dst, dirName);
    }

    public String createNewFile(IDataSet dst, String fileName)
    {
        return getStateAsLiveState().createNewFile(dst, fileName);
    }

    public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
    {
        return getStateAsLiveState().createNewFile(dst, dstInDataset, fileName);
    }

    public void deleteFile(String src)
    {
        getStateAsLiveState().deleteFile(src);
    }

    /**
     * Marked as deprecated, to prevent using this method directly. Instead it should only be used
     * implicitly as an implementation of the persistent map holder interface.
     */
    @Deprecated
    public DataSetRegistrationPersistentMap getPersistentMap()
    {
        return registrationContext;
    }

    /**
     * Commit the transaction. Does not throw exceptions if the commit fails on some datasets!
     * 
     * @return true if any datasets have been commited, false otherwise.
     */
    public boolean commit()
    {
        // No need to commit again
        if (state.isCommitted())
        {
            return false;
        }
        LiveTransactionState<T> liveState = getStateAsLiveState();
        boolean commitSucceeded = liveState.commit();

        // The attempt to commit the live state could have changed the state to rolledback
        if (state.isRolledback() || state.isRecoveryPending())
        {
            return false;
        }

        // Advance to the committed state.
        state = new CommitedTransactionState<T>(liveState);
        invokeDidCommitTransaction();

        return commitSucceeded;
    }

    private void invokeDidCommitTransaction()
    {
        try
        {
            registrationService.executePostCommit(this);
        } catch (Throwable t)
        {
            DssRegistrationLogger dssRegistrationLog = registrationService.getDssRegistrationLog();
            dssRegistrationLog.log("Post-storage action failed:");
            dssRegistrationLog.log(t.toString());
            operationLog.warn("Failed to invoke post transaction hook:" + t.getMessage(), t);
        }
    }

    /**
     * Rollback any commands that have been executed. Rollback is done in the reverse order of
     * execution.
     */
    public void rollback()
    {
        // No need to rollback again
        if (state.isRolledback() || state.isRecoveryPending())
        {
            return;
        }

        LiveTransactionState<T> liveState = getStateAsLiveState();
        liveState.rollback();

        state = new RolledbackTransactionState<T>(liveState);
    }

    /**
     * Delegate method called by the {@link DataSetStorageAlgorithmRunner}.
     */
    @Override
    public void didRollbackStorageAlgorithmRunner(DataSetStorageAlgorithmRunner<T> algorithm,
            Throwable ex, ErrorType errorType)
    {

        boolean useAutoRecovery = autoRecoverySettings == AutoRecoverySettings.USE_AUTO_RECOVERY;

        IDataSetStorageRecoveryManager storageRecoveryManager =
                registrationService.getRegistratorContext().getGlobalState()
                        .getStorageRecoveryManager();
        
            if (useAutoRecovery)
            {
                storageRecoveryManager.removeCheckpoint(algorithm);
            }
            rollback();
            registrationService.didRollbackTransaction(this, algorithm, ex, errorType);
    }

    @Override
    public void markReadyForRecovery(DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex)
    {
        registrationService.registerNonFatalError(ex);
        state = new RecoveryPendingTransactionState<T>(getStateAsLiveState());
    }
    
    /**
     * Delegate method called by the {@link DataSetStorageAlgorithmRunner}. This implementation asks
     * the DataSetRegistrationService to register not just the data sets, but perform any creation
     * or updates of Experiments and Samples as well.
     */
    public void registerDataSetsInApplicationServer(TechId registrationId,
            List<DataSetRegistrationInformation<T>> dataSetRegistrations) throws Throwable
    {
        AtomicEntityOperationDetails<T> registrationDetails =
                getStateAsLiveState().createEntityOperationDetails(registrationId,
                        dataSetRegistrations);
        IEntityOperationService<T> entityRegistrationService =
                registrationService.getEntityRegistrationService();

        entityRegistrationService.performOperationsInApplcationServer(registrationDetails);
    }

    public boolean isCommittedOrRolledback()
    {
        return isCommitted() || isRolledback();
    }

    public boolean isCommitted()
    {
        return state.isCommitted();
    }

    public boolean isRolledback()
    {
        return state.isRolledback();
    }

    public boolean isRecoveryPending()
    {
        return state.isRecoveryPending();
    }

    /**
     * Write something to the operation log.
     */
    public void logInfo(Object message)
    {
        operationLog.info(message);
    }

    /**
     * Return the state as live state. Throw an EnvironmentFailureException if this is not possible.
     */
    private LiveTransactionState<T> getStateAsLiveState()
    {
        try
        {
            LiveTransactionState<T> liveState = (LiveTransactionState<T>) state;
            return liveState;
        } catch (ClassCastException ex)
        {
            String message;
            if (state instanceof CommitedTransactionState)
            {
                message = "The transaction has already been committed";
            } else
            {
                message = "The transaction has already been rolledback";
            }
            throw new EnvironmentFailureException(message, ex);
        }
    }

    public ISearchService getSearchService()
    {
        return new SearchService(openBisService);
    }

    public DynamicTransactionQuery getDatabaseQuery(String dataSourceName)
            throws IllegalArgumentException
    {
        return getStateAsLiveState().getDatabaseQuery(dataSourceName);
    }

    void invokeDidEncounterSecondaryTransactionErrors(
            List<SecondaryTransactionFailure> encounteredErrors)
    {
        try
        {
            registrationService.didEncounterSecondaryTransactionErrors(this, encounteredErrors);
        } catch (Throwable t)
        {
            DataSetRegistrationTransaction.operationLog.warn(
                    "Failed to invoke secondary transaction error hook:" + t.getMessage(), t);
        }
    }

    public OmniscientTopLevelDataSetRegistratorState getRegistratorContext()
    {
        return registrationService.getRegistratorContext();
    }

    public AutoRecoverySettings getAutoRecoverySettings()
    {
        return autoRecoverySettings;
    }

    public IDataSetStorageRecoveryManager getStorageRecoveryManager()
    {
        return registrationService.getRegistratorContext().getGlobalState()
                .getStorageRecoveryManager();
    }

    public DataSetFile getIncomingDataSetFile()
    {
        return registrationService.getIncomingDataSetFile();
    }
}
