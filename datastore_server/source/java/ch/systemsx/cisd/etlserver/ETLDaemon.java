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
import java.io.FilenameFilter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.concurrent.TimerUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.filesystem.FaultyPathDirectoryScanningHandler;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IDirectoryScanningHandler;
import ch.systemsx.cisd.common.filesystem.PathPrefixPrepender;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkDirectoryScanningHandler;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
import ch.systemsx.cisd.common.utilities.IStopSignaler;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.SystemExit;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.IWebService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * The main class of the ETL server.
 * 
 * @author Bernd Rinn
 */
public final class ETLDaemon
{
    static final String STOREROOT_DIR_KEY = "storeroot-dir";

    static final String NOTIFY_SUCCESSFUL_REGISTRATION = "notify-successful-registration";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ETLDaemon.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, ETLDaemon.class);

    private static final File shredderQueueFile = new File(".shredder");

    private static final UncaughtExceptionHandler loggingExceptionHandler =
            new UncaughtExceptionHandler()
                {

                    //
                    // UncaughtExceptionHandler
                    //

                    public final void uncaughtException(final Thread t, final Throwable e)
                    {
                        notificationLog.error("An exception has occurred [thread: '" + t.getName()
                                + "'].", e);
                    }
                };

    @Private
    static IExitHandler exitHandler = SystemExit.SYSTEM_EXIT;

    private static void initLog()
    {
        LogInitializer.init();
        Thread.setDefaultUncaughtExceptionHandler(loggingExceptionHandler);
    }

    private static void printInitialLogMessage(final Parameters parameters)
    {
        operationLog.info("Data Store Server is starting up.");
        for (final String line : BuildAndEnvironmentInfo.INSTANCE.getEnvironmentInfo())
        {
            operationLog.info(line);
        }
        parameters.log();
    }

    private static boolean checkListShredder(final String[] args)
    {
        if (args.length > 0 && args[0].equals("--show-shredder"))
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
            return true;
        } else
        {
            return false;
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
            if (IWebService.VERSION != serviceVersion)
            {
                throw new ConfigurationFailureException(
                        "This client has the wrong service version for the server (client: "
                                + IWebService.VERSION + ", server: " + serviceVersion + ").");
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
                System.err.printf(msgStart + " [%s: %s]\n", e.getClass().getSimpleName(), e
                        .getMessage());
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
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Timer task interruption is operational.");
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
        final Map<String, IProcessorFactory> processorFactories =
                new LinkedHashMap<String, IProcessorFactory>();
        final Map<String, Properties> processorProperties = parameters.getProcessorProperties();
        for (final Map.Entry<String, Properties> entry : processorProperties.entrySet())
        {
            processorFactories.put(entry.getKey(), StandardProcessorFactory
                    .create(entry.getValue()));
        }
        final ThreadParameters[] threads = parameters.getThreads();
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        final Properties properties = parameters.getProperties();
        final boolean notifySuccessfulRegistration = getNotifySuccessfulRegistration(properties);
        final HighwaterMarkWatcher highwaterMarkWatcher =
                new HighwaterMarkWatcher(getHighwaterMark(properties));
        for (final ThreadParameters threadParameters : threads)
        {
            createProcessingThread(parameters, threadParameters, openBISService,
                    processorFactories, highwaterMarkWatcher, notifySuccessfulRegistration);
        }
    }

    private final static File getStoreRootDir(final Properties properties)
    {
        return FileUtilities.normalizeFile(new File(PropertyUtils.getMandatoryProperty(properties,
                STOREROOT_DIR_KEY)));
    }

    @Private
    final static void migrateStoreRootDir(final File storeRootDir,
            final DatabaseInstancePE databaseInstancePE)
    {
        final File[] instanceDirs =
                storeRootDir.listFiles((FilenameFilter) new NameFileFilter("Instance_"
                        + databaseInstancePE.getCode()));
        final int size = instanceDirs.length;
        assert size == 0 || size == 1 : "Wrong size of instance directories.";
        final String absolutePath = storeRootDir.getAbsolutePath();
        if (size == 0)
        {
            operationLog.info(String.format("No instance directory has been renamed "
                    + "in store root directory '%s'.", absolutePath));
        } else
        {
            final File instanceDir = instanceDirs[0];
            final File newName = new File(storeRootDir, "Instance_" + databaseInstancePE.getUuid());
            instanceDir.renameTo(newName);
            operationLog.info(String.format("Following instance directory '%s' has been "
                    + "renamed to '%s' in store root directory '%s'.", instanceDir.getName(),
                    newName.getName(), absolutePath));
        }
    }

    @Private
    final static List<File> findFiles(final File root, String prefix, int maxDepth)
    {
        ArrayList<File> files = new ArrayList<File>();
        if (maxDepth == 0)
        {
            if (root.getName().startsWith(prefix))
            {
                files.add(root);
            }
        } else
        {
            if (root.isDirectory())
            {
                for (File file : root.listFiles())
                {
                    files.addAll(findFiles(file, prefix, maxDepth - 1));
                }
            }
        }
        return files;
    }

    @Private
    final static void migrateDataStoreByRenamingObservableTypeToDataSetType(final File root)
    {
        final String observableTypeDirPrefix = "ObservableType_";
        final String dataSetTypeDirPrefix = "DataSetType_";
        final String observableTypeFilePrefix = "observable_type";
        final String dataSetTypeFilePrefix = "data_set_type";

        for (File file : findFiles(root, observableTypeDirPrefix, 5))
        {
            final File newName =
                    new File(file.getAbsolutePath().replaceFirst(observableTypeDirPrefix,
                            dataSetTypeDirPrefix));
            file.renameTo(newName);
        }
        for (File file : findFiles(root, observableTypeFilePrefix, 10))
        {
            final File newName =
                    new File(file.getAbsolutePath().replaceFirst(observableTypeFilePrefix,
                            dataSetTypeFilePrefix));
            file.renameTo(newName);
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
            final ThreadParameters threadParameters,
            final IEncapsulatedOpenBISService authorizedLimsService,
            final Map<String, IProcessorFactory> processorFactories,
            final HighwaterMarkWatcher highwaterMarkWatcher,
            final boolean notifySuccessfulRegistration)
    {
        final File incomingDataDirectory = threadParameters.getIncomingDataDirectory();
        final IETLServerPlugin plugin = threadParameters.getPlugin();
        final Properties properties = parameters.getProperties();
        final File storeRootDir = getStoreRootDir(properties);
        migrateStoreRootDir(storeRootDir, authorizedLimsService.getHomeDatabaseInstance());
        migrateDataStoreByRenamingObservableTypeToDataSetType(storeRootDir);
        plugin.getStorageProcessor().setStoreRootDirectory(storeRootDir);
        final Properties mailProperties = parameters.getMailProperties();
        final TransferredDataSetHandler pathHandler =
                new TransferredDataSetHandler(threadParameters.tryGetGroupCode(), plugin,
                        authorizedLimsService, mailProperties, highwaterMarkWatcher,
                        notifySuccessfulRegistration);
        pathHandler.setProcessorFactories(processorFactories);
        final HighwaterMarkDirectoryScanningHandler directoryScanningHandler =
                createDirectoryScanningHandler(pathHandler, highwaterMarkWatcher,
                        incomingDataDirectory, storeRootDir, processorFactories.values());
        final DirectoryScanningTimerTask dataMonitorTask =
                createDirectoryScanningTimerTask(incomingDataDirectory, pathHandler,
                        directoryScanningHandler);
        selfTest(incomingDataDirectory, authorizedLimsService, pathHandler);
        final String timerThreadName =
                threadParameters.getThreadName() + " - Incoming Data Monitor";
        final Timer workerTimer = new Timer(timerThreadName);
        workerTimer.schedule(dataMonitorTask, 0L, parameters.getCheckIntervalMillis());
        addShutdownHookForCleanup(workerTimer, pathHandler, parameters.getShutdownTimeOutMillis(),
                threadParameters.getThreadName());
    }

    private static DirectoryScanningTimerTask createDirectoryScanningTimerTask(
            final File incomingDataDirectory, final TransferredDataSetHandler pathHandler,
            final HighwaterMarkDirectoryScanningHandler directoryScanningHandler)
    {
        IOFileFilter filter = FileFilterUtils.prefixFileFilter(Constants.IS_FINISHED_PREFIX);
        return new DirectoryScanningTimerTask(incomingDataDirectory, filter, pathHandler,
                directoryScanningHandler);
    }

    private static void addShutdownHookForCleanup(final Timer workerTimer,
            final TransferredDataSetHandler mover, final long timeoutMillis, final String threadName)
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
                                Math.max(timeoutMillis / 2, timeoutMillis
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
            final File incomingDataDirectory, final File storeRootDir,
            final Iterable<IProcessorFactory> processorFactories)
    {
        final IDirectoryScanningHandler faultyPathHandler =
                new FaultyPathDirectoryScanningHandler(incomingDataDirectory, stopSignaler);
        final List<File> list = new ArrayList<File>();
        list.add(incomingDataDirectory);
        for (final IProcessorFactory processorFactory : processorFactories)
        {
            final PathPrefixPrepender pathPrefixPrepender =
                    processorFactory.getPathPrefixPrepender();
            File file = pathPrefixPrepender.tryGetDirectoryForAbsolutePaths();
            if (file != null)
            {
                list.add(file);
            }
            file = pathPrefixPrepender.tryGetDirectoryForRelativePaths();
            if (file != null)
            {
                list.add(file);
            }
        }
        return new HighwaterMarkDirectoryScanningHandler(faultyPathHandler, highwaterMarkWatcher,
                list.toArray(new File[0]));
    }

    public final static void main(final String[] args)
    {
        if (checkListShredder(args))
        {
            System.exit(0);
        }
        initLog();
        final Parameters parameters = new Parameters(args);
        TimingParameters.setDefault(parameters.getTimingParameters());
        QueueingPathRemoverService.start(shredderQueueFile);
        printInitialLogMessage(parameters);
        startupServer(parameters);
        operationLog.info("Data Store Server ready and waiting for data.");
    }

}
