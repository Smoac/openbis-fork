/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.client.cli;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.SystemExitException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.SystemExit;

/**
 * The command line client for CIFEX.
 * 
 * @author Bernd Rinn
 */
public class CIFEXClient
{

    @Private
    static IExitHandler exitHandler = SystemExit.SYSTEM_EXIT;

    /** Creates the set of available <code>ICommand</code> implementations. */
    private final static Map<String, ICommand> commands = createCommands();

    /** Initializes the client. */
    private final static void init()
    {
        // Disable any logging output. This can also be realized using following statement:
        // Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure(new NullAppender());
    }

    private final static void printUsage()
    {
        final String cmd = MinimalParameters.getCommandPrepender() + " <command>";
        System.err.println("Usage: " + cmd + " <options> ");
        System.err.println();
        System.err.println("Possible commands are: ");
        System.err.println();
        printCommands();
        System.err.println();
        System.err.println("Type '" + cmd + " --help' for help on a specific command");
    }

    private final static void printCommands()
    {
        for (final String command : commands.keySet())
        {
            System.err.println(command);
        }
    }

    private final static void printVersion()
    {
        System.err.println(MinimalParameters.getCommandPrepender() + " version "
                + BuildAndEnvironmentInfo.INSTANCE.getFullVersion() + " (Service version "
                + ICIFEXRPCService.VERSION + ")");
    }

    public static void main(final String[] args) throws Exception
    {
        init();
        if (args.length == 1 && "--version".equals(args[0]))
        {
            printVersion();
            exitHandler.exit(1);
        }
        final Set<String> commandNames = commands.keySet();
        if (args.length == 0 || commandNames.contains(args[0]) == false)
        {
            printUsage();
            exitHandler.exit(1);
        }
        final String[] newArgs = (String[]) ArrayUtils.remove(args, 0);
        final ICommand command = commands.get(args[0]);
        try
        {
            exitHandler.exit(command.execute(newArgs));
        } catch (final UserFailureException ex)
        {
            System.err.println(ex.getMessage() + " (user fault)");
            exitHandler.exit(1);
        } catch (final EnvironmentFailureException ex)
        {
            System.err.println(ex.getMessage() + " (environment failure)");
            exitHandler.exit(1);
        } catch (final RemoteConnectFailureException ex)
        {
            System.err.println("Remote server cannot be reached (environment failure)");
            exitHandler.exit(1);
        } catch (final RemoteAccessException ex)
        {
            final Throwable cause = ex.getCause();
            if (cause != null)
            {
                if (cause instanceof UnknownHostException)
                {
                    System.err.println(String.format(
                            "Given host '%s' can not be reached  (environment failure)", cause
                                    .getMessage()));
                } else if (cause instanceof IllegalArgumentException)
                {
                    System.err.println(cause.getMessage());
                } else if (cause instanceof SSLHandshakeException)
                {
                    final String property = "javax.net.ssl.trustStore";
                    System.err.println(String.format(
                            "Validation of SSL certificate failed [%s=%s] (configuration failure)",
                            property, StringUtils.defaultString(System.getProperty(property))));
                } else
                {
                    ex.printStackTrace();
                }
            } else
            {
                ex.printStackTrace();
            }
            exitHandler.exit(1);
        } catch (final SystemExitException e)
        {
            exitHandler.exit(1);
        } catch (final Exception e)
        {
            e.printStackTrace();
            exitHandler.exit(1);
        }
    }

    /**
     * Returns all <code>ICommand</code> implementations.
     * <p>
     * The keys are the command names you use to load a given <code>ICommand</code> implementation.
     * </p>
     */
    private final static Map<String, ICommand> createCommands()
    {
        final Map<String, ICommand> map = new TreeMap<String, ICommand>();
        registerCommand(map, LoginCommand.getInstance());
        registerCommand(map, LogoutCommand.getInstance());
        registerCommand(map, ListDownloadFilesCommand.getInstance());
        registerCommand(map, FileDownloadCommand.getInstance());
        registerCommand(map, FileUploadCommand.getInstance());
        return map;
    }

    private static void registerCommand(final Map<String, ICommand> map, final ICommand command)
    {
        final String name = command.getName();
        assert map.containsKey(name) == false : "A command named '" + name
                + "' has already been registered";
        map.put(name, command);
    }

}
