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
import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.datamover.rsync.RsyncCopier;
import ch.systemsx.cisd.datamover.xcopy.XcopyCopier;

/**
 * The main class of the datamover.
 * 
 * @author Bernd Rinn
 */
public class Main
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, Main.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, Main.class);

    private static final UncaughtExceptionHandler loggingExceptionHandler = new UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                notificationLog.error("An exception has occurred [thread: '" + t.getName() + "'].", e);
            }
        };

    private static void initLog()
    {
        LogInitializer.init();
        Thread.setDefaultUncaughtExceptionHandler(loggingExceptionHandler);
    }

    private static void printInitialLogMessage(final Parameters parameters)
    {
        operationLog.info("datamover is starting up.");
        for (String line : BuildAndEnvironmentInfo.INSTANCE.getEnvironmentInfo())
        {
            operationLog.info(line);
        }
        parameters.log();
    }

    private static IPathCopier suggestPathCopier(Parameters parameters, boolean requiresDeletionBeforeCreation)
    {
        final File rsyncExecutable = findRsyncExecutable(parameters.getRsyncExecutable());
        final File sshExecutable = findSshExecutable(parameters.getSshExecutable());
        if (rsyncExecutable != null)
        {
            return new RsyncCopier(rsyncExecutable, sshExecutable, requiresDeletionBeforeCreation);
        } else if (OSUtilities.isWindows())
        {
            return new XcopyCopier(OSUtilities.findExecutable("xcopy"), requiresDeletionBeforeCreation);
        } else
        {
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

    /**
     * performs a self-test.
     */
    private static void selfTest(final Parameters parameters)
    {
        try
        {
            IPathCopier copyProcess = suggestPathCopier(parameters, false);
            SelfTest.check(copyProcess, parameters.getIncomingStore(), parameters.getBufferStore(), parameters
                    .getOutgoingStore(), parameters.getManualInterventionStore());
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
    }

    /**
     * Returns the path copier
     */
    private static IPathCopier getPathCopier(Parameters parameters, File destinationDirectory)
    {
        IPathCopier copyProcess = suggestPathCopier(parameters, false);
        boolean requiresDeletionBeforeCreation =
                SelfTest.requiresDeletionBeforeCreation(copyProcess, destinationDirectory);
        return suggestPathCopier(parameters, requiresDeletionBeforeCreation);
    }

    private static void startupServer(final Parameters parameters)
    {
        selfTest(parameters);
        final IFileSysOperationsFactory operations = new IFileSysOperationsFactory()
            {
                public IPathLastChangedChecker getChecker()
                {
                    return new FSPathLastChangedChecker();
                }

                public IPathCopier getCopier(File destinationDirectory)
                {
                    return getPathCopier(parameters, destinationDirectory);
                }

                public IPathRemover getRemover()
                {
                    return new FSPathRemover();
                }
            };

        final MonitorStarter starter = new MonitorStarter(parameters, operations);
        starter.start();
    }

    public static void main(String[] args)
    {
        initLog();
        final Parameters parameters = new Parameters(args);
        printInitialLogMessage(parameters);
        startupServer(parameters);
        operationLog.info("datamover ready and waiting for data.");
    }

}
