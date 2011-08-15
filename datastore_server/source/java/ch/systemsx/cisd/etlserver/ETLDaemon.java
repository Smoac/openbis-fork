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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.concurrent.TimerUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask.IScannedStore;
import ch.systemsx.cisd.common.filesystem.FaultyPathDirectoryScanningHandler;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IDirectoryScanningHandler;
import ch.systemsx.cisd.common.filesystem.IStoreItemFilter;
import ch.systemsx.cisd.common.filesystem.LastModificationChecker;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.filesystem.QuietPeriodFileFilter;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkDirectoryScanningHandler;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
import ch.systemsx.cisd.common.utilities.IStopSignaler;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.SystemExit;
import ch.systemsx.cisd.etlserver.validation.DataSetValidator;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IncomingShareIdProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.QueueingDataSetStatusUpdaterService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * The main class of the ETL server.
 * 
 * @author Bernd Rinn
 */
public final class ETLDaemon
{
    public static final File shredderQueueFile = new File(".shredder");

    public static final File updaterQueueFile = new File(".updater");

    static final String NOTIFY_SUCCESSFUL_REGISTRATION = "notify-successful-registration";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ETLDaemon.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            ETLDaemon.class);

    @Private
    static IExitHandler exitHandler = SystemExit.SYSTEM_EXIT;

    private static void printInitialLogMessage(final Parameters parameters)
    {
        operationLog.info("Data Store Server is starting up.");
        for (final String line : BuildAndEnvironmentInfo.INSTANCE.getEnvironmentInfo())
        {
            operationLog.info(line);
        }
        parameters.log();
    }

    public static void listShredder()
    {
        final List<File> shredderItems =
                QueueingPathRemoverService.listShredderItems(shredderQueueFile);
        if (shredderItems.isEmpty())
        {
            System.out.println("Shredder is empty.");
        } else
        {
            System.out.println("Found " + shredderItems.size() + " items in shredder:");
            for (final File f : shredderItems)
            {
                System.out.println(f.getAbsolutePath());
            }
        }
    }

    public static void listUpdaterQueue()
    {
        final List<DataSetCodesWithStatus> items =
                QueueingDataSetStatusUpdaterService.listItems(updaterQueueFile);
        if (items.isEmpty())
        {
            System.out.println("Updater queue is empty.");
        } else
        {
            System.out.println("Found " + items.size() + " items in updater:");
            for (final DataSetCodesWithStatus item : items)
            {
                System.out.println(item);
            }
        }
    }

    private static void selfTest(final File incomingDirectory,
            final IEncapsulatedOpenBISService service, final ISelfTestable... selfTestables)
    {
        final String msgStart = "Data Store Server self test failed:";
        ISelfTestable currentSelfTestableOrNull = null;
        try
        {
            checkFullyAccesible(incomingDirectory);
            final int serviceVersion = service.getVersion();
            if (IServer.VERSION != serviceVersion)
            {
                throw new ConfigurationFailureException(
                        "This client has the wrong service version for the server (client: "
                                + IServer.VERSION + ", server: " + serviceVersion + ").");
            }
            for (final ISelfTestable selfTestableOrNull : selfTestables)
            {
                if (selfTestableOrNull != null)
                {
                    currentSelfTestableOrNull = selfTestableOrNull;
                    selfTestableOrNull.check();
                }
            }
        } catch (final HighLevelException e)
        {
            if (currentSelfTestableOrNull != null && currentSelfTestableOrNull.isRemote())
            {
                notificationLog.error("Self test on self-testable "
                        + currentSelfTestableOrNull.getClass().getSimpleName()
                        + " failed. This it relies on a remote resource which might become "
                        + "available at at later time, we keep the server running anyway.", e);
            } else
            {
                System.err.printf(msgStart + " [%s: %s]\n", e.getClass().getSimpleName(),
                        e.getMessage());
                exitHandler.exit(1);
            }
        } catch (final RuntimeException e)
        {
            System.err.println(msgStart);
            e.printStackTrace();
            exitHandler.exit(1);
        }
        if (TimerUtilities.isOperational())
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Timer task interruption is operational.");
            }
        } else
        {
            operationLog.warn("Timer task interruption is not operational. "
                    + "No clean up can be performed on extraordinary shutdown.");
        }

    }

    private static void checkFullyAccesible(final File directory)
            throws ConfigurationFailureException
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Checking source directory '" + directory.getAbsolutePath() + "'.");
        }
        final String errorMessage =
                FileUtilities.checkDirectoryFullyAccessible(directory, "source");
        if (errorMessage != null)
        {
            throw new ConfigurationFailureException(errorMessage);
        }
    }

    private static void startupServer(final Parameters parameters)
    {
        final ThreadParameters[] threads = parameters.getThreads();
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        final Properties properties = parameters.getProperties();
        final boolean notifySuccessfulRegistration = getNotifySuccessfulRegistration(properties);
        final HighwaterMarkWatcher highwaterMarkWatcher =
                new HighwaterMarkWatcher(getHighwaterMark(properties));
        IDataSetValidator dataSetValidator = new DataSetValidator(properties);
        final Properties mailProperties = Parameters.createMailProperties(properties);
        final IMailClient mailClient = new MailClient(mailProperties);
        final IDataSourceQueryService dataSourceQueryService =
                ServiceProvider.getDataSourceQueryService();
        File storeRootDir = DssPropertyParametersUtil.getStoreRootDir(parameters.getProperties());
        List<String> incomingShares = new ArrayList<String>();
        for (final ThreadParameters threadParameters : threads)
        {
            File incomingDataDirectory = threadParameters.getIncomingDataDirectory();
            String shareId =
                    SegmentedStoreUtils.findIncomingShare(incomingDataDirectory, storeRootDir,
                            new Log4jSimpleLogger(operationLog));
            incomingShares.add(shareId);
            operationLog.info("[" + threadParameters.getThreadName() + "]: Data sets drop into '"
                    + incomingDataDirectory + "' will be stored in share " + shareId + ".");
            createProcessingThread(parameters, threadParameters, shareId, openBISService,
                    highwaterMarkWatcher, mailClient, dataSetValidator, dataSourceQueryService,
                    notifySuccessfulRegistration);
        }
        IncomingShareIdProvider.add(incomingShares);
        mailClient.sendTestEmail();
    }

    @Private
    final static void migrateStoreRootDir(final File storeRootDir,
            final DatabaseInstance databaseInstance)
    {
        final File[] instanceDirs =
                storeRootDir.listFiles((FilenameFilter) new NameFileFilter("Instance_"
                        + databaseInstance.getCode()));
        final int size = instanceDirs.length;
        assert size == 0 || size == 1 : "Wrong size of instance directories.";
        final String absolutePath = storeRootDir.getAbsolutePath();
        if (size == 0)
        {
            if (operationLog.isDebugEnabled())
                operationLog.debug(String.format("No instance directory has been renamed "
                        + "in store root directory '%s'.", absolutePath));
        } else
        {
            final File instanceDir = instanceDirs[0];
            final File newName = new File(storeRootDir, "Instance_" + databaseInstance.getUuid());
            instanceDir.renameTo(newName);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("Following instance directory '%s' has been "
                        + "renamed to '%s' in store root directory '%s'.", instanceDir.getName(),
                        newName.getName(), absolutePath));
            }
        }
    }

    private final static long getHighwaterMark(final Properties properties)
    {
        return PropertyUtils.getLong(properties,
                HostAwareFileWithHighwaterMark.HIGHWATER_MARK_PROPERTY_KEY, -1L);
    }

    private final static boolean getNotifySuccessfulRegistration(final Properties properties)
    {
        return PropertyUtils.getBoolean(properties, NOTIFY_SUCCESSFUL_REGISTRATION, false);
    }

    private final static void createProcessingThread(final Parameters parameters,
            final ThreadParameters threadParameters, String shareId,
            final IEncapsulatedOpenBISService authorizedLimsService,
            final HighwaterMarkWatcher highwaterMarkWatcher, final IMailClient mailClient,
            final IDataSetValidator dataSetValidator,
            IDataSourceQueryService dataSourceQueryService,
            final boolean notifySuccessfulRegistration)
    {
        final File incomingDataDirectory = threadParameters.getIncomingDataDirectory();
        final ITopLevelDataSetRegistrator pathHandler =
                createTopLevelDataSetRegistrator(parameters.getProperties(), threadParameters,
                        shareId, authorizedLimsService, mailClient, dataSetValidator,
                        dataSourceQueryService, notifySuccessfulRegistration);
        final HighwaterMarkDirectoryScanningHandler directoryScanningHandler =
                createDirectoryScanningHandler(pathHandler, highwaterMarkWatcher,
                        incomingDataDirectory, threadParameters.reprocessFaultyDatasets());
        FileFilter fileFilter =
                createFileFilter(incomingDataDirectory, threadParameters.useIsFinishedMarkerFile(),
                        parameters);
        final DirectoryScanningTimerTask dataMonitorTask =
                new DirectoryScanningTimerTask(incomingDataDirectory, fileFilter, pathHandler,
                        directoryScanningHandler);
        selfTest(incomingDataDirectory, authorizedLimsService, pathHandler);
        final String timerThreadName =
                threadParameters.getThreadName() + " - Incoming Data Monitor";
        final Timer workerTimer = new Timer(timerThreadName);
        workerTimer.schedule(dataMonitorTask, 0L, parameters.getCheckIntervalMillis());
        addShutdownHookForCleanup(workerTimer, pathHandler, parameters.getShutdownTimeOutMillis(),
                threadParameters.getThreadName());
    }

    public static ITopLevelDataSetRegistrator createTopLevelDataSetRegistrator(
            final Properties properties, final ThreadParameters threadParameters, String shareId,
            final IEncapsulatedOpenBISService openBISService, final IMailClient mailClient,
            final IDataSetValidator dataSetValidator,
            IDataSourceQueryService dataSourceQueryService,
            final boolean notifySuccessfulRegistration)
    {
        final File storeRootDir = DssPropertyParametersUtil.getStoreRootDir(properties);
        migrateStoreRootDir(storeRootDir, openBISService.getHomeDatabaseInstance());
        String dssCode = DssPropertyParametersUtil.getDataStoreCode(properties);
        TopLevelDataSetRegistratorGlobalState globalState =
                new TopLevelDataSetRegistratorGlobalState(dssCode, shareId, storeRootDir,
                        openBISService, mailClient, dataSetValidator, dataSourceQueryService,
                        notifySuccessfulRegistration, threadParameters);

        ITopLevelDataSetRegistrator registrator =
                ClassUtils.create(ITopLevelDataSetRegistrator.class, threadParameters
                        .getTopLevelDataSetRegistratorClass(TransferredDataSetHandler.class),
                        globalState);

        return registrator;
    }

    /**
     * Create a top-level data set registrator with explicit control of all parameters.
     */
    public static ITopLevelDataSetRegistrator createTopLevelDataSetRegistrator(
            final Properties properties, final ThreadParameters threadParameters, String shareId,
            final IEncapsulatedOpenBISService openBISService, final IMailClient mailClient,
            final IDataSetValidator dataSetValidator,
            IDataSourceQueryService dataSourceQueryService,
            final boolean notifySuccessfulRegistration, boolean useIsFinishedMarkerFile,
            boolean deleteUnidentified, String preRegistrationScriptOrNull,
            String postRegistrationScriptOrNull, String[] validationScriptsOrNull,
            Class<?> defaultTopLevelDataSetRegistratorClass)
    {
        final File storeRootDir = DssPropertyParametersUtil.getStoreRootDir(properties);
        migrateStoreRootDir(storeRootDir, openBISService.getHomeDatabaseInstance());
        String dssCode = DssPropertyParametersUtil.getDataStoreCode(properties);
        TopLevelDataSetRegistratorGlobalState globalState =
                new TopLevelDataSetRegistratorGlobalState(dssCode, shareId, storeRootDir,
                        openBISService, mailClient, dataSetValidator, dataSourceQueryService,
                        notifySuccessfulRegistration, threadParameters, useIsFinishedMarkerFile,
                        deleteUnidentified, preRegistrationScriptOrNull,
                        postRegistrationScriptOrNull, validationScriptsOrNull);

        ITopLevelDataSetRegistrator registrator =
                ClassUtils
                        .create(ITopLevelDataSetRegistrator.class,
                                threadParameters
                                        .getTopLevelDataSetRegistratorClass(defaultTopLevelDataSetRegistratorClass),
                                globalState);

        return registrator;
    }

    private static FileFilter createFileFilter(File incomingDataDirectory,
            boolean useIsFinishedMarkerFile, Parameters parameters)
    {
        if (useIsFinishedMarkerFile)
        {
            return FileFilterUtils.prefixFileFilter(Constants.IS_FINISHED_PREFIX);
        } else
        {
            return createQuietPeriodFilter(incomingDataDirectory, parameters);
        }
    }

    private static FileFilter createQuietPeriodFilter(final File incomingDataDirectory,
            Parameters parameters)
    {
        int ignoredErrorCountBeforeNotification = 3;
        LastModificationChecker lastModificationChecker =
                new LastModificationChecker(incomingDataDirectory);
        final IStoreItemFilter quietPeriodFilter =
                new QuietPeriodFileFilter(lastModificationChecker,
                        parameters.getQuietPeriodMillis(), ignoredErrorCountBeforeNotification);
        return new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    assert pathname.getParentFile().getAbsolutePath()
                            .equals(incomingDataDirectory.getAbsolutePath()) : "The file should come to the filter only from the incoming directory";

                    StoreItem storeItem = new StoreItem(pathname.getName());
                    return quietPeriodFilter.accept(storeItem);
                }
            };
    }

    private static void addShutdownHookForCleanup(final Timer workerTimer,
            final ITopLevelDataSetRegistrator mover, final long timeoutMillis,
            final String threadName)
    {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        if (operationLog.isInfoEnabled())
                        {
                            operationLog.info("Requesting shutdown lock of thread '" + threadName
                                    + "'.");
                        }
                        final long startTimeMillis = System.currentTimeMillis();
                        final boolean lockOK =
                                mover.getRegistrationLock().tryLock(timeoutMillis,
                                        TimeUnit.MILLISECONDS);
                        final long timeoutLeftMillis =
                                Math.max(timeoutMillis / 2,
                                        timeoutMillis
                                                - (System.currentTimeMillis() - startTimeMillis));
                        if (lockOK == false)
                        {
                            operationLog.error("Failed to get lock for shutdown of thread '"
                                    + threadName + "'.");
                        }
                        try
                        {
                            if (operationLog.isInfoEnabled())
                            {
                                operationLog.info(String.format("Initiating shutdown sequence "
                                        + "[maximal shutdown time: %ds].",
                                        2 * timeoutLeftMillis / 1000));
                            }
                            final boolean shutdownOK =
                                    TimerUtilities.tryShutdownTimer(workerTimer, timeoutLeftMillis);
                            operationLog.log(shutdownOK ? Level.INFO : Level.ERROR,
                                    "Worker thread shutdown, status="
                                            + (shutdownOK ? "OK" : "FAILED"));
                        } finally
                        {
                            if (lockOK)
                            {
                                mover.getRegistrationLock().unlock();
                            }
                        }
                        operationLog.warn("Shutting down shredder(s)");
                        QueueingPathRemoverService.stopAndWait(timeoutMillis);
                    } catch (final InterruptedException ex)
                    {
                        throw new InterruptedExceptionUnchecked(ex);
                    } finally
                    {
                        if (operationLog.isInfoEnabled())
                        {
                            operationLog.info("Shutting down now.");
                        }
                    }
                }
            }, threadName + " - Shutdown Handler"));
    }

    private final static HighwaterMarkDirectoryScanningHandler createDirectoryScanningHandler(
            final IStopSignaler stopSignaler, final HighwaterMarkWatcher highwaterMarkWatcher,
            final File incomingDataDirectory, boolean reprocessFaultyDatasets)
    {
        final IDirectoryScanningHandler faultyPathHandler =
                createFaultyPathHandler(stopSignaler, incomingDataDirectory,
                        reprocessFaultyDatasets);
        return new HighwaterMarkDirectoryScanningHandler(faultyPathHandler, highwaterMarkWatcher,
                incomingDataDirectory);
    }

    private static IDirectoryScanningHandler createFaultyPathHandler(
            final IStopSignaler stopSignaler, final File incomingDataDirectory,
            boolean reprocessFaultyDatasets)
    {
        if (reprocessFaultyDatasets)
        {
            return createDummyFaultyPathHandler();
        } else
        {
            return new FaultyPathDirectoryScanningHandler(incomingDataDirectory, stopSignaler);
        }
    }

    // returns the handler which does not check if the path was faulty
    private static IDirectoryScanningHandler createDummyFaultyPathHandler()
    {
        return new IDirectoryScanningHandler()
            {

                public void init(IScannedStore scannedStore)
                {
                    // do nothing
                }

                public void beforeHandle(IScannedStore scannedStore)
                {
                    // do nothing
                }

                public Status finishItemHandle(IScannedStore scannedStore, StoreItem storeItem)
                {
                    return Status.OK;
                }

                public HandleInstruction mayHandle(IScannedStore scannedStore, StoreItem storeItem)
                {
                    return HandleInstruction.PROCESS;
                }

            };
    }

    public final static void main(final String[] args)
    {
        Parameters parameters = null;
        try
        {
            parameters = new Parameters(args);
        } catch (ConfigurationFailureException ex)
        {
            operationLog.error(
                    "Cannot launch the server, bacause of the misconfiguraton: " + ex.getMessage(),
                    ex);
            System.exit(1);
        }
        run(parameters);
    }

    private static void run(final Parameters parameters)
    {
        TimingParameters.setDefault(parameters.getTimingParameters());
        if (QueueingPathRemoverService.isRunning() == false)
        {
            QueueingPathRemoverService.start(shredderQueueFile);
        }
        if (QueueingDataSetStatusUpdaterService.isRunning() == false)
        {
            QueueingDataSetStatusUpdaterService.start(updaterQueueFile);
        }
        printInitialLogMessage(parameters);
        startupServer(parameters);
        MaintenanceTaskUtils.startupMaintenancePlugins(parameters.getMaintenancePlugins());
        operationLog.info("Data Store Server ready and waiting for data.");
    }

    /**
     * Runs ETL Daemon for system testing: Replaces default {@link IExitHandler} by a one which
     * throws an {@link AssertionError}.
     */
    public static void runForTesting(String[] args)
    {
        exitHandler = new IExitHandler()
            {
                public void exit(int exitCode)
                {
                    throw new AssertionError("Unexpected exit: " + exitCode);
                }
            };
        Parameters parameters = new Parameters(args, exitHandler);
        run(parameters);
    }

}
