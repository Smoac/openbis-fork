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
import java.util.ArrayList;
import java.util.Timer;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.SystemExit;
import ch.systemsx.cisd.common.utilities.TriggeringTimerTask;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.FileStoreSelfTestables;
import ch.systemsx.cisd.datamover.filesystem.FileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * The main class of the Datamover.
 * 
 * @author Bernd Rinn
 */
public final class Main
{
    private static final String DATAMOVER_PID_FILE_NAME = "datamover.pid";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, Main.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, Main.class);

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

    private static void initLog()
    {
        LogInitializer.init();
        Thread.setDefaultUncaughtExceptionHandler(loggingExceptionHandler);
    }

    private static void printInitialLogMessage(final Parameters parameters)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Datamover is starting up.");
        }
        for (final String line : BuildAndEnvironmentInfo.INSTANCE.getEnvironmentInfo())
        {
            operationLog.info(line);
        }
        parameters.log();
    }

    /**
     * performs a self-test.
     */
    private static void selfTest(final Parameters parameters)
    {
        final ArrayList<IFileStore> stores = new ArrayList<IFileStore>();
        final FileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        stores.add(parameters.getIncomingStore(factory));
        final IFileStore bufferStore =
                FileStoreFactory.createLocal(parameters.getBufferDirectoryPath(),
                        Parameters.BUFFER_KIND_DESC, factory, false);
        stores.add(bufferStore);
        stores.add(parameters.getOutgoingStore(factory));
        if (parameters.tryGetManualInterventionDir() != null)
        {
            final IFileStore dummyStore =
                    FileStoreFactory.createLocal(parameters.tryGetManualInterventionDir(),
                            "manual intervention", factory, false);
            stores.add(dummyStore);
        }
        if (parameters.tryGetExtraCopyDir() != null)
        {
            final IFileStore dummyStore =
                    FileStoreFactory.createLocal(parameters.tryGetExtraCopyDir(), "extra-copy",
                            factory, false);
            stores.add(dummyStore);
        }
        final IPathCopier copyProcess = factory.getCopier(false);
        SelfTest.check(copyProcess, stores.toArray(IFileStore.EMPTY_ARRAY),
                FileStoreSelfTestables.getSelfTestables());
    }

    private final static void createShutdownHookTimer(final ITerminable terminable)
    {
        final TriggeringTimerTask shutdownHook =
                new TriggeringTimerTask(createDeleteOnExitFile(DataMover.SHUTDOWN_MARKER_FILENAME),
                        new DataMoverShutdownHook(terminable, SystemExit.SYSTEM_EXIT));
        new Timer("Shutdown Hook").schedule(shutdownHook, 0L, 5000L);
    }

    @Private
    static ITerminable startupServer(final Parameters parameters, final LocalBufferDirs bufferDirs)
    {
        final IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        return DataMover.start(parameters, factory, bufferDirs);
    }

    private final static void startupServer(final Parameters parameters)
    {
        final IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        createOutgoingTargetFile(parameters);
        createDeleteOnExitFile(DATAMOVER_PID_FILE_NAME);
        createShutdownHookTimer(DataMover.start(parameters, factory));
    }

    private final static void createOutgoingTargetFile(final Parameters parameters)
    {
        final HostAwareFileWithHighwaterMark outgoingTarget = parameters.getOutgoingTarget();
        FileUtilities.writeToFile(createDeleteOnExitFile(DataMover.OUTGOING_TARGET_LOCATION_FILE),
                outgoingTarget.getCanonicalPath());
    }

    private final static File createDeleteOnExitFile(final String fileName)
    {
        final File file = new File(fileName);
        file.deleteOnExit();
        return file;
    }

    public static void main(final String[] args)
    {
        String msgStart = "";
        try
        {
            initLog();
            msgStart = "Datamover parameters wrong:"; 
            final Parameters parameters = new Parameters(args);
            msgStart = "";
            printInitialLogMessage(parameters);
            msgStart = "Failure in starting up server";
            startupServer(parameters);
            msgStart = "Datamover self test failed:";
            selfTest(parameters);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Datamover ready and waiting for data.");
            }
        } catch (final HighLevelException e)
        {
            System.err.printf(msgStart + " [%s: %s]\n", e.getClass().getSimpleName(), e
                    .getMessage());
            System.exit(1);
        } catch (final RuntimeException e)
        {
            System.err.println(msgStart);
            e.printStackTrace();
            System.exit(1);
        }
    }

}
