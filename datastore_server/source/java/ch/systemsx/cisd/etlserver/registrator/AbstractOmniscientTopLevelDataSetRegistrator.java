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

package ch.systemsx.cisd.etlserver.registrator;

import static ch.systemsx.cisd.etlserver.IStorageProcessor.STORAGE_PROCESSOR_KEY;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.AbstractTopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.IRollbackDelegate;
import ch.systemsx.cisd.etlserver.DataSetRegistrationRollbacker;
import ch.systemsx.cisd.etlserver.DataStrategyStore;
import ch.systemsx.cisd.etlserver.IDataStrategyStore;
import ch.systemsx.cisd.etlserver.IPostRegistrationAction;
import ch.systemsx.cisd.etlserver.IPreRegistrationAction;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.PropertiesBasedETLServerPlugin;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.utils.PostRegistrationExecutor;
import ch.systemsx.cisd.etlserver.utils.PreRegistrationExecutor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractOmniscientTopLevelDataSetRegistrator extends
        AbstractTopLevelDataSetRegistrator implements IRollbackDelegate
{
    static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            AbstractOmniscientTopLevelDataSetRegistrator.class);

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractOmniscientTopLevelDataSetRegistrator.class);

    protected static class OmniscientTopLevelDataSetRegistratorState
    {
        private final TopLevelDataSetRegistratorGlobalState globalState;

        private final IStorageProcessor storageProcessor;

        private final ReentrantLock registrationLock;

        private final IFileOperations fileOperations;

        private final IPreRegistrationAction preRegistrationAction;

        private final IPostRegistrationAction postRegistrationAction;

        private final IDataStrategyStore dataStrategyStore;

        private final MarkerFileUtility markerFileUtility;

        private final DatabaseInstance homeDatabaseInstance;

        private OmniscientTopLevelDataSetRegistratorState(
                TopLevelDataSetRegistratorGlobalState globalState,
                IStorageProcessor storageProcessor, ReentrantLock registrationLock,
                IFileOperations fileOperations)
        {
            this.globalState = globalState;
            this.storageProcessor = storageProcessor;
            this.registrationLock = registrationLock;
            this.fileOperations = fileOperations;
            this.dataStrategyStore =
                    new DataStrategyStore(globalState.getOpenBisService(),
                            globalState.getMailClient());
            this.preRegistrationAction =
                    PreRegistrationExecutor.create(globalState.getPreRegistrationScriptOrNull());
            this.postRegistrationAction =
                    PostRegistrationExecutor.create(globalState.getPostRegistrationScriptOrNull());
            this.markerFileUtility =
                    new MarkerFileUtility(operationLog, notificationLog, fileOperations,
                            storageProcessor);
            this.homeDatabaseInstance = globalState.getOpenBisService().getHomeDatabaseInstance();
        }

        public TopLevelDataSetRegistratorGlobalState getGlobalState()
        {
            return globalState;
        }

        public IStorageProcessor getStorageProcessor()
        {
            return storageProcessor;
        }

        public ReentrantLock getRegistrationLock()
        {
            return registrationLock;
        }

        public IFileOperations getFileOperations()
        {
            return fileOperations;
        }

        public IPreRegistrationAction getPreRegistrationAction()
        {
            return preRegistrationAction;
        }

        public IPostRegistrationAction getPostRegistrationAction()
        {
            return postRegistrationAction;
        }

        public IDataStrategyStore getDataStrategyStore()
        {
            return dataStrategyStore;
        }

        public MarkerFileUtility getMarkerFileUtility()
        {
            return markerFileUtility;
        }

        public DatabaseInstance getHomeDatabaseInstance()
        {
            return homeDatabaseInstance;
        }
    }

    private final OmniscientTopLevelDataSetRegistratorState state;

    private boolean stopped;

    /**
     * @param globalState
     */
    protected AbstractOmniscientTopLevelDataSetRegistrator(
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);

        state =
                new OmniscientTopLevelDataSetRegistratorState(globalState,
                        PropertiesBasedETLServerPlugin.create(IStorageProcessor.class, globalState
                                .getThreadParameters().getThreadProperties(),
                                STORAGE_PROCESSOR_KEY, true), new ReentrantLock(),
                        FileOperations.getMonitoredInstanceForCurrentThread());

    }

    public OmniscientTopLevelDataSetRegistratorState getRegistratorState()
    {
        return state;
    }

    public Lock getRegistrationLock()
    {
        return state.registrationLock;
    }

    public void handle(File incomingDataSetFileOrIsFinishedFile)
    {
        if (stopped)
        {
            return;
        }
        final File isFinishedFile = incomingDataSetFileOrIsFinishedFile;
        final File incomingDataSetFile;
        final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction;

        if (getGlobalState().isUseIsFinishedMarkerFile())
        {
            incomingDataSetFile =
                    state.getMarkerFileUtility().getIncomingDataSetPathFromMarker(isFinishedFile);
            cleanAfterwardsAction = new IDelegatedActionWithResult<Boolean>()
                {
                    public Boolean execute()
                    {
                        return state.getMarkerFileUtility().deleteAndLogIsFinishedMarkerFile(
                                isFinishedFile);
                    }
                };
        } else
        {
            incomingDataSetFile = incomingDataSetFileOrIsFinishedFile;
            cleanAfterwardsAction = new IDelegatedActionWithResult<Boolean>()
                {
                    public Boolean execute()
                    {
                        return true; // do nothing
                    }
                };
        }

        DataSetRegistrationService service =
                new DataSetRegistrationService(this, cleanAfterwardsAction);

        handleDataSet(incomingDataSetFile, service);
        service.commit();
    }

    public boolean isStopped()
    {
        return stopped;
    }

    public boolean isRemote()
    {
        return true;
    }

    //
    // ISelfTestable
    //
    public final void check() throws ConfigurationFailureException, EnvironmentFailureException
    {
        new TopLevelDataSetChecker(operationLog, state.storageProcessor, state.fileOperations)
                .runCheck();
    }

    /**
     * For subclasses to override.
     */
    public void rollback(DataSetRegistrationAlgorithm registrationAlgorithm, Throwable throwable)
    {
        updateStopped(throwable instanceof InterruptedExceptionUnchecked);

        new DataSetRegistrationRollbacker(stopped, registrationAlgorithm,
                registrationAlgorithm.getIncomingDataSetFile(), notificationLog, operationLog,
                throwable).doRollback();
    }

    public void registerDataSetInApplicationServer(DataSetInformation dataSetInformation,
            NewExternalData data) throws Throwable
    {
        getGlobalState().getOpenBisService().registerDataSet(dataSetInformation, data);
    }

    /**
     * Update the value of stopped using the argument.
     * <p>
     * To be called by subclasses.
     */
    protected void updateStopped(boolean update)
    {
        stopped |= update;
    }

    /**
     * For subclasses to override.
     */
    protected abstract void handleDataSet(File dataSetFile, DataSetRegistrationService service);
}
