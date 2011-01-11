/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.IStorageProcessor.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.utils.PostRegistrationExecutor;
import ch.systemsx.cisd.etlserver.utils.PreRegistrationExecutor;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * The class that handles the incoming data set.
 * 
 * @author Bernd Rinn
 */
public final class TransferredDataSetHandler extends AbstractTopLevelDataSetRegistrator implements
        IDataSetHandler, IExtensibleDataSetHandler
{

    static final String TARGET_NOT_RELATIVE_TO_STORE_ROOT =
            "Target path '%s' is not relative to store root directory '%s'.";

    static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            TransferredDataSetHandler.class);

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            TransferredDataSetHandler.class);

    static final NamedDataStrategy ERROR_DATA_STRATEGY = new NamedDataStrategy(
            DataStoreStrategyKey.ERROR);

    private final IStoreRootDirectoryHolder storeRootDirectoryHolder;

    private final IEncapsulatedOpenBISService limsService;

    private final IDataStrategyStore dataStrategyStore;

    private final IDataSetInfoExtractor dataSetInfoExtractor;

    private final IFileOperations fileOperations;

    private final Lock registrationLock;

    private final ITypeExtractor typeExtractor;

    private final IStorageProcessor storageProcessor;

    private final IMailClient mailClient;

    private final String dssCode;

    private final boolean notifySuccessfulRegistration;

    private final boolean useIsFinishedMarkerFile;

    private boolean stopped = false;

    private boolean deleteUnidentified = false;

    private DatabaseInstance homeDatabaseInstance;

    private final IDataSetHandler dataSetHandler;

    private final IDataSetValidator dataSetValidator;

    private final IPreRegistrationAction preRegistrationAction;

    private final IPostRegistrationAction postRegistrationAction;

    /**
     * The designated constructor.
     * 
     * @param globalState
     */
    public TransferredDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);

        this.dssCode = globalState.getDssCode();
        assert dssCode != null : "Unspecified data store code";

        IETLServerPlugin plugin =
                ETLServerPluginFactory.getPluginForThread(globalState.getThreadParameters());
        assert plugin != null : "IETLServerPlugin implementation can not be null.";

        storeRootDirectoryHolder = plugin.getStorageProcessor();
        assert storeRootDirectoryHolder != null : "Given store root directory holder can not be null.";

        this.limsService = globalState.getOpenBisService();
        assert limsService != null : "IEncapsulatedLimsService implementation can not be null.";

        this.mailClient = globalState.getMailClient();
        assert mailClient != null : "IMailClient implementation can not be null.";

        this.dataSetInfoExtractor = plugin.getDataSetInfoExtractor();
        this.typeExtractor = plugin.getTypeExtractor();
        this.storageProcessor = plugin.getStorageProcessor();
        dataSetHandler = plugin.getDataSetHandler(this, limsService);
        if (dataSetHandler instanceof IDataSetHandlerWithMailClient)
        {
            ((IDataSetHandlerWithMailClient) dataSetHandler).initializeMailClient(mailClient);
        }
        this.dataSetValidator = globalState.getDataSetValidator();
        this.dataStrategyStore = new DataStrategyStore(this.limsService, mailClient);
        this.notifySuccessfulRegistration = globalState.isNotifySuccessfulRegistration();
        this.registrationLock = new ReentrantLock();
        this.fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
        this.useIsFinishedMarkerFile = globalState.isUseIsFinishedMarkerFile();
        this.deleteUnidentified = globalState.isDeleteUnidentified();
        this.preRegistrationAction =
                PreRegistrationExecutor.create(globalState.getPreRegistrationScriptOrNull());
        this.postRegistrationAction =
                PostRegistrationExecutor.create(globalState.getPostRegistrationScriptOrNull());

    }

    /**
     * A constructor used for testing.
     * 
     * @param globalState
     * @param plugin
     */
    TransferredDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState,
            IETLServerPlugin plugin)
    {
        super(globalState);

        this.dssCode = globalState.getDssCode();
        assert dssCode != null : "Unspecified data store code";

        assert plugin != null : "IETLServerPlugin implementation can not be null.";

        storeRootDirectoryHolder = plugin.getStorageProcessor();
        assert storeRootDirectoryHolder != null : "Given store root directory holder can not be null.";

        this.limsService = globalState.getOpenBisService();
        assert limsService != null : "IEncapsulatedLimsService implementation can not be null.";

        this.mailClient = globalState.getMailClient();
        assert mailClient != null : "IMailClient implementation can not be null.";

        this.dataSetInfoExtractor = plugin.getDataSetInfoExtractor();
        this.typeExtractor = plugin.getTypeExtractor();
        this.storageProcessor = plugin.getStorageProcessor();
        dataSetHandler = plugin.getDataSetHandler(this, limsService);
        if (dataSetHandler instanceof IDataSetHandlerWithMailClient)
        {
            ((IDataSetHandlerWithMailClient) dataSetHandler).initializeMailClient(mailClient);
        }
        this.dataSetValidator = globalState.getDataSetValidator();
        this.dataStrategyStore = new DataStrategyStore(this.limsService, mailClient);
        this.notifySuccessfulRegistration = globalState.isNotifySuccessfulRegistration();
        this.registrationLock = new ReentrantLock();
        this.fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
        this.useIsFinishedMarkerFile = globalState.isUseIsFinishedMarkerFile();
        this.deleteUnidentified = globalState.isDeleteUnidentified();
        this.preRegistrationAction =
                PreRegistrationExecutor.create(globalState.getPreRegistrationScriptOrNull());
        this.postRegistrationAction =
                PostRegistrationExecutor.create(globalState.getPostRegistrationScriptOrNull());

    }

    /**
     * Returns the lock one needs to hold before one interrupts a data set registration.
     */
    public Lock getRegistrationLock()
    {
        return registrationLock;
    }

    //
    // IPathHandler
    //

    public final void handle(final File file)
    {
        if (stopped)
        {
            return;
        }
        dataSetHandler.handleDataSet(file);
    }

    public List<DataSetInformation> handleDataSet(final File dataSet)
    {
        final TransferredDataSetHandlerDataSetRegistrationAlgorithm registrationHelper =
                createRegistrationHelper(dataSet);
        return new DataSetRegistrationAlgorithmRunner(registrationHelper).runAlgorithm();
    }

    public List<DataSetInformation> handleDataSet(final File dataSet,
            DataSetInformation dataSetInformation, IDataSetRegistrator registrator)
    {
        dataSetInformation.setInstanceCode(getHomeDatabaseInstance().getCode());
        dataSetInformation.setInstanceUUID(getHomeDatabaseInstance().getUuid());
        final TransferredDataSetHandlerDataSetRegistrationAlgorithm registrationHelper =
                createRegistrationHelper(dataSet, dataSetInformation, registrator);
        return new DataSetRegistrationAlgorithmRunner(registrationHelper).runAlgorithm();
    }

    public boolean isStopped()
    {
        return stopped;
    }

    //
    // ISelfTestable
    //

    public final void check() throws ConfigurationFailureException, EnvironmentFailureException
    {
        final File storeRootDirectory = storeRootDirectoryHolder.getStoreRootDirectory();
        storeRootDirectory.mkdirs();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Checking store root directory '"
                    + storeRootDirectory.getAbsolutePath() + "'.");
        }
        final String errorMessage =
                fileOperations.checkDirectoryFullyAccessible(storeRootDirectory, "store root");
        if (errorMessage != null)
        {
            if (fileOperations.exists(storeRootDirectory) == false)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "Store root directory '%s' does not exist.",
                        storeRootDirectory.getAbsolutePath());
            } else
            {
                throw new ConfigurationFailureException(errorMessage);
            }
        }
    }

    public boolean isRemote()
    {
        return true;
    }

    DatabaseInstance getHomeDatabaseInstance()
    {
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = limsService.getHomeDatabaseInstance();
        }
        return homeDatabaseInstance;
    }

    //
    // Helper class
    //

    private TransferredDataSetHandlerDataSetRegistrationAlgorithm createRegistrationHelper(File file)
    {
        if (useIsFinishedMarkerFile)
        {
            return createRegistrationHelperWithIsFinishedFile(file, null, null);
        } else
        {
            return createRegistrationHelperWithQuietPeriodFilter(file, null, null);
        }
    }

    private TransferredDataSetHandlerDataSetRegistrationAlgorithm createRegistrationHelper(
            File dataSet, DataSetInformation dataSetInformation, IDataSetRegistrator registrator)
    {
        if (useIsFinishedMarkerFile)
        {
            return createRegistrationHelperWithIsFinishedFile(dataSet, dataSetInformation,
                    registrator);
        } else
        {
            return createRegistrationHelperWithQuietPeriodFilter(dataSet, dataSetInformation,
                    registrator);
        }
    }

    private TransferredDataSetHandlerDataSetRegistrationAlgorithm createRegistrationHelperWithIsFinishedFile(
            final File isFinishedFile, final DataSetInformation dsInfo,
            IDataSetRegistrator registrator)
    {
        assert isFinishedFile != null : "Unspecified is-finished file.";
        final String name = isFinishedFile.getName();
        assert name.startsWith(IS_FINISHED_PREFIX) : "A finished file must starts with '"
                + IS_FINISHED_PREFIX + "'.";

        File incomingDataSetFile = getIncomingDataSetPathFromMarker(isFinishedFile);
        IDelegatedActionWithResult<Boolean> cleanAftrewardsAction =
                new IDelegatedActionWithResult<Boolean>()
                    {
                        public Boolean execute()
                        {
                            return deleteAndLogIsFinishedMarkerFile(isFinishedFile);
                        }
                    };
        if (null != registrator)
        {
            return new OverridingRegistrationHelper(this, incomingDataSetFile,
                    cleanAftrewardsAction, preRegistrationAction, postRegistrationAction,
                    registrator)
                {
                    @Override
                    protected DataSetInformation extractDataSetInformation(File incomingDataSetPath)
                    {
                        return dsInfo;
                    }

                };
        } else
        {
            return new RegistrationHelper(this, incomingDataSetFile, cleanAftrewardsAction,
                    preRegistrationAction, postRegistrationAction);
        }
    }

    private TransferredDataSetHandlerDataSetRegistrationAlgorithm createRegistrationHelperWithQuietPeriodFilter(
            File incomingDataSetFile, final DataSetInformation dsInfo,
            IDataSetRegistrator registrator)
    {
        IDelegatedActionWithResult<Boolean> cleanAftrewardsAction =
                new IDelegatedActionWithResult<Boolean>()
                    {
                        public Boolean execute()
                        {
                            return true; // do nothing
                        }
                    };
        if (registrator != null)
        {
            return new OverridingRegistrationHelper(this, incomingDataSetFile,
                    cleanAftrewardsAction, preRegistrationAction, postRegistrationAction,
                    registrator)
                {
                    @Override
                    protected DataSetInformation extractDataSetInformation(File incomingDataSetPath)
                    {
                        return dsInfo;
                    }

                };
        } else
        {
            return new RegistrationHelper(this, incomingDataSetFile, cleanAftrewardsAction,
                    preRegistrationAction, postRegistrationAction);
        }
    }

    /**
     * From given <var>isFinishedPath</var> gets the incoming data set path and checks it.
     * 
     * @return <code>null</code> if a problem has happened. Otherwise a useful and usable incoming
     *         data set path is returned.
     */
    private final File getIncomingDataSetPathFromMarker(final File isFinishedPath)
    {
        final File incomingDataSetPath =
                FileUtilities.removePrefixFromFileName(isFinishedPath, IS_FINISHED_PREFIX);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Getting incoming data set path '%s' from is-finished path '%s'",
                    incomingDataSetPath, isFinishedPath));
        }
        final String errorMsg =
                fileOperations.checkPathFullyAccessible(incomingDataSetPath, "incoming data set");
        if (errorMsg != null)
        {
            fileOperations.delete(isFinishedPath);
            throw EnvironmentFailureException.fromTemplate(String.format(
                    "Error moving path '%s' from '%s' to '%s': %s", incomingDataSetPath.getName(),
                    incomingDataSetPath.getParent(),
                    storeRootDirectoryHolder.getStoreRootDirectory(), errorMsg));
        }
        return incomingDataSetPath;
    }

    private boolean deleteAndLogIsFinishedMarkerFile(File isFinishedFile)
    {
        if (fileOperations.exists(isFinishedFile) == false)
        {
            return false;
        }
        final boolean ok = fileOperations.delete(isFinishedFile);
        final String absolutePath = isFinishedFile.getAbsolutePath();
        if (ok == false)
        {
            notificationLog.error(String.format("Removing file '%s' failed.", absolutePath));
        } else
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("File '%s' has been removed.", absolutePath));
            }
        }
        return ok;
    }

    private class RegistrationHelper extends TransferredDataSetHandlerDataSetRegistrationAlgorithm
    {

        /**
         * @param transferredDataSetHandler
         * @param incomingDataSetFile
         * @param cleanAftrewardsAction
         * @param postRegistrationAction
         */
        RegistrationHelper(TransferredDataSetHandler transferredDataSetHandler,
                File incomingDataSetFile,
                IDelegatedActionWithResult<Boolean> cleanAftrewardsAction,
                IPreRegistrationAction preRegistrationAction,
                IPostRegistrationAction postRegistrationAction)
        {
            super(incomingDataSetFile, cleanAftrewardsAction, preRegistrationAction,
                    postRegistrationAction);
        }

        // state accessors
        @Override
        protected IEncapsulatedOpenBISService getOpenBisService()
        {
            return limsService;
        }

        @Override
        protected ITypeExtractor getTypeExtractor()
        {
            return typeExtractor;
        }

        @Override
        protected IStorageProcessor getStorageProcessor()
        {
            return storageProcessor;
        }

        @Override
        protected IDataSetInfoExtractor getDataSetInfoExtractor()
        {
            return dataSetInfoExtractor;
        }

        @Override
        protected DatabaseInstance getHomeDatabaseInstance()
        {
            return TransferredDataSetHandler.this.getHomeDatabaseInstance();
        }

        @Override
        protected Logger getOperationLog()
        {
            return operationLog;
        }

        @Override
        protected Logger getNotificationLog()
        {
            return notificationLog;
        }

        @Override
        protected IMailClient getMailClient()
        {
            return mailClient;
        }

        @Override
        protected IFileOperations getFileOperations()
        {
            return fileOperations;
        }

        @Override
        protected String getDataStoreCode()
        {
            return dssCode;
        }

        @Override
        protected IDataStrategyStore getDataStrategyStore()
        {
            return dataStrategyStore;
        }

        @Override
        protected IDataSetValidator getDataSetValidator()
        {
            return dataSetValidator;
        }

        @Override
        protected Lock getRegistrationLock()
        {
            return TransferredDataSetHandler.this.getRegistrationLock();
        }

        @Override
        protected boolean shouldDeleteUnidentified()
        {
            return deleteUnidentified;
        }

        @Override
        protected boolean shouldNotifySuccessfulRegistration()
        {
            return notifySuccessfulRegistration;
        }

        @Override
        protected void rollback(final Throwable throwable) throws Error
        {
            stopped |= throwable instanceof InterruptedExceptionUnchecked;
            if (stopped)
            {
                Thread.interrupted(); // Ensure the thread's interrupted state is cleared.
                getOperationLog().warn(
                        String.format("Requested to stop registration of data set '%s'",
                                algorithm.getDataSetInformation()));
            } else
            {
                getNotificationLog().error(
                        String.format(algorithm.getErrorMessageTemplate(),
                                algorithm.getDataSetInformation()), throwable);
            }
            // Errors which are not AssertionErrors leave the system in a state that we don't
            // know and can't trust. Thus we will not perform any operations any more in this
            // case.
            if (throwable instanceof Error && throwable instanceof AssertionError == false)
            {
                throw (Error) throwable;
            }
            UnstoreDataAction action = algorithm.rollbackStorageProcessor(throwable);
            if (stopped == false)
            {
                if (action == UnstoreDataAction.MOVE_TO_ERROR)
                {
                    final File baseDirectory =
                            algorithm.createBaseDirectory(
                                    TransferredDataSetHandler.ERROR_DATA_STRATEGY,
                                    algorithm.getStoreRoot(), algorithm.getDataSetInformation());
                    algorithm.setBaseDirectoryHolder(new BaseDirectoryHolder(
                            TransferredDataSetHandler.ERROR_DATA_STRATEGY, baseDirectory,
                            incomingDataSetFile));
                    boolean moveInCaseOfErrorOk =
                            FileRenamer.renameAndLog(incomingDataSetFile, algorithm
                                    .getBaseDirectoryHolder().getTargetFile());
                    writeThrowable(throwable);
                    if (moveInCaseOfErrorOk)
                    {
                        algorithm.clean();
                    }
                } else if (action == UnstoreDataAction.DELETE)
                {
                    FileUtilities.deleteRecursively(incomingDataSetFile, new Log4jSimpleLogger(
                            TransferredDataSetHandler.operationLog));
                }
            }
        }
    }

    private class OverridingRegistrationHelper extends RegistrationHelper
    {

        private final IDataSetRegistrator registrator;

        /**
         * @param transferredDataSetHandler
         * @param incomingDataSetFile
         * @param cleanAftrewardsAction
         * @param postRegistrationAction
         */
        OverridingRegistrationHelper(TransferredDataSetHandler transferredDataSetHandler,
                File incomingDataSetFile,
                IDelegatedActionWithResult<Boolean> cleanAftrewardsAction,
                IPreRegistrationAction preRegistrationAction,
                IPostRegistrationAction postRegistrationAction, IDataSetRegistrator registrator)
        {
            super(transferredDataSetHandler, incomingDataSetFile, cleanAftrewardsAction,
                    preRegistrationAction, postRegistrationAction);
            this.registrator = registrator;
        }

        @Override
        protected boolean shouldDeleteUnidentified()
        {
            return true;
        }

        @Override
        protected boolean shouldNotifySuccessfulRegistration()
        {
            return false;
        }

        @Override
        protected void registerDataSetInApplicationServer(NewExternalData data) throws Throwable
        {
            registrator.registerDataSetInApplicationServer(data);
        }
    }
}
