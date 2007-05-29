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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.io.FileFilter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Timer;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.common.utilities.RegexFileFilter;
import ch.systemsx.cisd.common.utilities.RegexFileFilter.PathType;
import ch.systemsx.cisd.datamover.DirectoryScanningTimerTask.IPathHandler;
import ch.systemsx.cisd.datamover.rsync.RsyncCopier;
import ch.systemsx.cisd.datamover.xcopy.XcopyCopier;

/**
 * The main class of the raw data mover.
 * 
 * @author Bernd Rinn
 */
public class Main
{

    private static final FileFilter ALWAYS_TRUE_FILTER = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return true;
            }
        };

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, Main.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, Main.class);

    private static final UncaughtExceptionHandler loggingExceptionHandler = new UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                notificationLog.error("An exception has occurred.", e);
            }
        };

    private static void initLog()
    {
        LogInitializer.init();
        Thread.setDefaultUncaughtExceptionHandler(loggingExceptionHandler);
    }

    private static void printInitialLogMessage(final Parameters parameters)
    {
        operationLog.info("Data mover is starting up.");
        for (String line : BuildAndEnvironmentInfo.INSTANCE.getEnvironmentInfo())
        {
            operationLog.info(line);
        }
        parameters.log();
    }

    private static IPathCopier getPathCopier(Parameters parameters, boolean requiresDeletionBeforeCreation)
    {
        final File rsyncExecutable = findRsyncExecutable(parameters.getRsyncExecutable());
        final File sshExecutable = findSshExecutable(parameters.getSshExecutable());
        if (rsyncExecutable != null)
        {
            return new RsyncCopier(rsyncExecutable, sshExecutable, requiresDeletionBeforeCreation);
        } else if (OSUtilities.isWindows())
        {
            return new XcopyCopier(OSUtilities.findExecutable("xcopy"), requiresDeletionBeforeCreation);
        } else {
            throw new ConfigurationFailureException("Unable to find a copy engine.");
        }
    }

    private static File findRsyncExecutable(final String rsyncExecutablePath)
    {
        final File rsyncExecutable;
        if (rsyncExecutablePath != null)
        {
            rsyncExecutable = new File(rsyncExecutablePath);
        } else if (OSUtilities.isWindows() == false)
        {
            rsyncExecutable = OSUtilities.findExecutable("rsync");
        } else
        {
            rsyncExecutable = null;
        }
        if (rsyncExecutable != null && OSUtilities.executableExists(rsyncExecutable) == false)
        {
            throw ConfigurationFailureException.fromTemplate("Cannot find rsync executable '%s'.", rsyncExecutable
                    .getAbsoluteFile());
        }
        return rsyncExecutable;
    }

    private static File findSshExecutable(String sshExecutablePath)
    {
        final File sshExecutable;
        if (sshExecutablePath != null)
        {
            if (sshExecutablePath.length() > 0)
            {
                sshExecutable = new File(sshExecutablePath);
            } else
            // Explicitely disable tunneling via ssh on the command line.
            {
                sshExecutable = null;
            }
        } else
        {
            sshExecutable = OSUtilities.findExecutable("ssh");
        }
        if (sshExecutable != null && OSUtilities.executableExists(sshExecutable) == false)
        {
            throw ConfigurationFailureException.fromTemplate("Cannot find ssh executable '%s'.", sshExecutable
                    .getAbsoluteFile());
        }
        return sshExecutable;
    }

    private static void startupServer(final Parameters parameters)
    {
        final File localDataDirectory = parameters.getLocalDataDirectory();
        final File localTemporaryDirectory = parameters.getLocalTemporaryDirectory();
        final File remoteDataDirectory = parameters.getRemoteDataDirectory();
        final String remoteHost = parameters.getRemoteHost();
        IPathCopier temporaryCopyProcess = null; // Convince Eclipse compiler that the variable has been initialized.

        try
        {
            temporaryCopyProcess = getPathCopier(parameters, false); // This is part of the self-test.
            SelfTest.check(localDataDirectory, localTemporaryDirectory, remoteDataDirectory, remoteHost,
                    temporaryCopyProcess);
        } catch (HighLevelException e)
        {
            System.err.printf("Self test failed: [%s: %s]\n", e.getClass().getSimpleName(), e.getMessage());
            System.exit(1);
        } catch (RuntimeException e)
        {
            System.err.println("Self test failed:");
            e.printStackTrace();
            System.exit(1);
        }
        if (SelfTest.requiresDeletionBeforeCreation(temporaryCopyProcess, localTemporaryDirectory, remoteDataDirectory))
        {
            temporaryCopyProcess = getPathCopier(parameters, true);
        }
        final IPathCopier copyProcess = temporaryCopyProcess;

        final IFileSystemOperations operations = new IFileSystemOperations()
            {
                public IPathLastChangedChecker getChecker()
                {
                    return new FSPathLastChangedChecker();
                }

                public IPathCopier getCopier()
                {
                    return copyProcess;
                }

                public IPathRemover getRemover()
                {
                    return new FSPathRemover();
                }
            };
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(remoteDataDirectory, operations, copyProcess, parameters);
        final IPathHandler remoteMover =
                new RemotePathMover(remoteDataDirectory, remoteHost, monitor, operations, parameters);
        final DirectoryScanningTimerTask remoteMovingTask =
                new DirectoryScanningTimerTask(localTemporaryDirectory, ALWAYS_TRUE_FILTER, remoteMover);
        final RegexFileFilter cleansingFilter = new RegexFileFilter();
        if (parameters.getCleansingRegex() != null)
        {
            cleansingFilter.add(PathType.FILE, parameters.getCleansingRegex());
        }
        final IPathHandler localPathMover =
                new CleansingPathHandlerDecorator(cleansingFilter, new IntraFSPathMover(localTemporaryDirectory));
        final DirectoryScanningTimerTask localMovingTask =
                new DirectoryScanningTimerTask(localDataDirectory, new QuietPeriodFileFilter(parameters, operations),
                        localPathMover);
        final Timer remoteMovingTimer = new Timer("Remote Mover");
        final Timer localMovingTimer = new Timer("Local Mover");

        localMovingTimer.schedule(localMovingTask, 0, parameters.getCheckIntervalMillis());
        // Implementation notes:
        // 1. The startup of the remote moving task is delayed for half the time of the check interval. Thus the local
        // moving task should have enough time to finith its job.
        // 2. The remote moving task is scheduled at fixed rate. The rationale behind this is that if new items are
        // added
        // to the local temp directory while the remote timer task has been running for a long time, busy moving data to
        // remote, the task shoulnd't sit idle for the check time when there is actually work to do.
        remoteMovingTimer.scheduleAtFixedRate(remoteMovingTask, parameters.getCheckIntervalMillis() / 2, parameters
                .getCheckIntervalMillis());
    }

    public static void main(String[] args)
    {
        initLog();
        final Parameters parameters = new Parameters(args);
        printInitialLogMessage(parameters);
        startupServer(parameters);
        operationLog.info("Data mover ready and waiting for data.");
    }

}
