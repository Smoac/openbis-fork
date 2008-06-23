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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.FileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathCopier;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * The main class of the Datamover.
 * 
 * @author Bernd Rinn
 */
public class Main
{
    private static final String DEFAULT_COMMAND = "start";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, Main.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, Main.class);
    
    private static final Map<String, ICommand> commands = new HashMap<String, ICommand>();

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

    private static final Runnable loggingShutdownHook = new Runnable()
        {

            //
            // Runnable
            //

            public final void run()
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Datamover is shutting down.");
                }
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
        Runtime.getRuntime().addShutdownHook(new Thread(loggingShutdownHook, "Shutdown Hook"));
    }

    /**
     * performs a self-test.
     */
    private static void selfTest(final Parameters parameters)
    {
        final String msgStart = "Datamover self test failed:";
        try
        {
            final ArrayList<IFileStore> stores = new ArrayList<IFileStore>();
            final FileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
            stores.add(parameters.getIncomingStore(factory));
            final IFileStore bufferStore =
                    FileStoreFactory.createLocal(parameters.getBufferDirectoryPath(),
                            Parameters.BUFFER_KIND_DESC, factory);
            stores.add(bufferStore);
            stores.add(parameters.getOutgoingStore(factory));
            if (parameters.tryGetManualInterventionDir() != null)
            {
                final IFileStore dummyStore =
                        FileStoreFactory.createLocal(parameters.tryGetManualInterventionDir(),
                                "manual intervention", factory);
                stores.add(dummyStore);
            }
            if (parameters.tryGetExtraCopyDir() != null)
            {
                final IFileStore dummyStore =
                        FileStoreFactory.createLocal(parameters.tryGetExtraCopyDir(), "extra-copy",
                                factory);
                stores.add(dummyStore);
            }
            final IPathCopier copyProcess = factory.getCopier(false);
            SelfTest.check(copyProcess, stores.toArray(IFileStore.EMPTY_ARRAY));
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

    @Private
    static ITerminable startupServer(final Parameters parameters, final LocalBufferDirs bufferDirs)
    {
        final IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        return DataMover.start(parameters, factory, bufferDirs);
    }

    private static void startupServer(final Parameters parameters)
    {
        final IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        DataMover.start(parameters, factory);
    }

    public static void main(final String[] args)
    {
        commands.put("status", new StatusCommand(true));
        commands.put("mstatus", new StatusCommand(false));
        commands.put(DEFAULT_COMMAND, new ServerStartupCommand());
        initLog();
        final Parameters parameters = new Parameters(args);
        String command = DEFAULT_COMMAND;
        if (parameters.getArgs().size() > 0)
        {
            command = parameters.getArgs().get(0);
        }
        ICommand cmd = commands.get(command);
        if (cmd == null)
        {
            System.out.println("Unknown command '" + command + "'.");
            System.out.println("Allowed commands are " + commands.keySet());
        }
        cmd.execute(parameters);
    }
    
    private static final class ServerStartupCommand implements ICommand
    {
        public void execute(Parameters parameters)
        {
            printInitialLogMessage(parameters);
            selfTest(parameters);
            startupServer(parameters);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Datamover ready and waiting for data.");
            }
        }
    }

}
