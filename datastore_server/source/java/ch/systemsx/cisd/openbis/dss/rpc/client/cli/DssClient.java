/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.rpc.client.cli;

import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang.StringUtils;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.MasqueradingException;
import ch.systemsx.cisd.common.exceptions.SystemExitException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.SystemExit;
import ch.systemsx.cisd.openbis.dss.component.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.component.IDssComponent;
import ch.systemsx.cisd.openbis.dss.component.impl.DssComponent;

/**
 * The dss command which supports
 * <ul>
 * <li>ls &mdash; list files in a data set</li>
 * <li>get &mdash; get files in a data set</li>
 * </ul>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssClient
{
    static
    {
        // Disable any logging output.
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    private final GlobalArguments arguments;

    private final CmdLineParser parser;

    private final CommandFactory commandFactory;

    private final IExitHandler exitHandler;

    private DssClient()
    {
        this.exitHandler = SystemExit.SYSTEM_EXIT;
        this.arguments = new GlobalArguments();
        this.parser = new CmdLineParser(arguments);
        this.commandFactory = new CommandFactory();
    }

    private void runWithArgs(String[] args)
    {
        try
        {
            parser.parseArgument(args);
        } catch (CmdLineException e)
        {
            printUsage(System.err);
            exitHandler.exit(1);
        }

        // Show help and exit
        if (arguments.isHelp())
        {
            printHelp(System.out);
            exitHandler.exit(0);
        }

        // Show usage and exit
        if (arguments.isComplete() == false)
        {
            printUsage(System.err);
            exitHandler.exit(1);
        }

        // Login to DSS
        IDssComponent component = loginOrDie();

        int resultCode = 0;

        try
        {
            // Get the data set
            IDataSetDss dataSet = component.getDataSet(arguments.getDataSetCode());

            // Find the command and run it
            ICommand cmd = commandFactory.tryCommandForName(arguments.getCommand(), dataSet);
            if (null == cmd)
            {
                printUsage(System.err);
                resultCode = 1;
            } else
            {
                String[] cmdArgs = new String[arguments.getCommandArguments().size()];
                arguments.getCommandArguments().toArray(cmdArgs);
                resultCode = cmd.execute(cmdArgs);
            }
        } catch (final InvalidSessionException ex)
        {
            System.err
                    .println("Your session is no longer valid. Please login again. [server said: '"
                            + ex.getMessage() + "']");
            resultCode = 1;
        } catch (final UserFailureException ex)
        {
            System.err.println();
            System.err.println(ex.getMessage());
            resultCode = 1;
        } catch (final EnvironmentFailureException ex)
        {
            System.err.println();
            System.err.println(ex.getMessage() + " (environment failure)");
            resultCode = 1;
        } catch (final RemoteConnectFailureException ex)
        {
            System.err.println();
            System.err.println("Remote server cannot be reached (environment failure)");
            resultCode = 1;
        } catch (final RemoteAccessException ex)
        {
            System.err.println();
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
            resultCode = 1;
        } catch (final SystemExitException e)
        {
            resultCode = 1;
        } catch (MasqueradingException e)
        {
            System.err.println(e);
            resultCode = 1;
        } catch (IllegalArgumentException e)
        {
            System.err.println(e.getMessage());
            resultCode = 1;
        } catch (final Exception e)
        {
            System.err.println();
            e.printStackTrace();
            resultCode = 1;
        } finally
        {
            // Cleanup
            component.logout();
        }

        exitHandler.exit(resultCode);
    }

    /**
     * Log in to openBIS or exit if login fails.
     */
    private IDssComponent loginOrDie()
    {
        try
        {
            IDssComponent component = new DssComponent(arguments.getServerBaseUrl());
            component.login(arguments.getUsername(), arguments.getPassword());
            return component;
        } catch (final InvalidSessionException ex)
        {
            System.err
                    .println("Your session is no longer valid. Please login again. [server said: '"
                            + ex.getMessage() + "']");
            exitHandler.exit(1);
        } catch (final UserFailureException ex)
        {
            System.err.println();
            System.err.println(ex.getMessage());
            exitHandler.exit(1);
        } catch (final EnvironmentFailureException ex)
        {
            System.err.println();
            System.err.println(ex.getMessage() + " (environment failure)");
            exitHandler.exit(1);
        } catch (final RemoteConnectFailureException ex)
        {
            System.err.println();
            System.err.println("Remote server cannot be reached (environment failure)");
            exitHandler.exit(1);
        } catch (final RemoteAccessException ex)
        {
            System.err.println();
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
        } catch (MasqueradingException e)
        {
            System.err.println(e);
            exitHandler.exit(1);
        } catch (final Exception e)
        {
            System.err.println();
            e.printStackTrace();
            exitHandler.exit(1);
        }

        // never reached
        return null;
    }

    private void printHelp(PrintStream out)
    {
        if (arguments.hasCommand())
        {
            commandFactory.printHelpForName(arguments.getCommand(), getProgramCallString(), out);
        } else
        {
            printUsage(out);
        }
    }

    private String getProgramCallString()
    {
        return "dss_client.sh";
    }

    private void printUsage(PrintStream out)
    {
        out.println("usage: " + getProgramCallString()
                + " [options...] -- DATA_SET_CODE COMMAND [ARGS]");
        out
                .println(" (Note: it is necessary to add two dashes \"--\" after options have been specified and before the dataset code.)");
        List<String> commands = commandFactory.getKnownCommands();
        out.println("\nCommands:");
        for (String cmd : commands)
        {
            out.print(" ");
            out.println(cmd);
        }
        out.print("\n");
        out.println("Options:");
        parser.printUsage(out);
    }

    public static void main(String[] args)
    {
        DssClient newMe = new DssClient();
        newMe.runWithArgs(args);
    }

}
