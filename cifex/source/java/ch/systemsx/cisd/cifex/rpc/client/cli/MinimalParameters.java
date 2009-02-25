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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.args4j.Argument;
import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.common.utilities.SystemExit;

/**
 * Super class of all command parameter classes.
 * <p>
 * Currently know options are:
 * <ul>
 * <li><code>server-base-url</code> (abbr.: <code>s</code>)</li>
 * <li><code>help</code> (abbr.: <code>h</code>)</li>
 * </ul>
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public class MinimalParameters
{
    private final static String CIFEX_WINDOWS_COMMAND_PREPEND = "cifex.bat ";

    private final static String CIFEX_UNIX_COMMAND_PREPEND = "cifex.sh ";

    private static final String ARGS_NOT_EMPTY = "No arguments allowed.";

    @Argument()
    private final List<String> args = new ArrayList<String>();

    private final CmdLineParser parser;

    private final IExitHandler exitHandler;

    private final String programCall;

    private final String genericArgs;

    public MinimalParameters(final String[] args, final String commandName)
    {
        this(args, commandName, StringUtils.EMPTY);
    }

    public MinimalParameters(final String[] args, final String commandName, final String genericArgs)
    {
        this(args, commandName, genericArgs, SystemExit.SYSTEM_EXIT);
    }

    public MinimalParameters(final String[] args, final String commandName,
            final String genericArgs, final IExitHandler exitHandler)
    {
        parser = new CmdLineParser(this);
        this.exitHandler = exitHandler;
        this.programCall = getCommandPrepender() + commandName;
        this.genericArgs = genericArgs;
        try
        {
            parser.parseArgument(args);
        } catch (final CmdLineException ex)
        {
            throw new UserFailureException(ex.getMessage(), ex);
        }
    }

    public static String getCommandPrepender()
    {
        if (OSUtilities.isUnix())
        {
            return CIFEX_UNIX_COMMAND_PREPEND;
        } else
        {
            return CIFEX_WINDOWS_COMMAND_PREPEND;
        }
    }

    @Option(name = "h", longName = "help", usage = "Show this help text", skipForExample = true)
    public void printHelp(final boolean exit)
    {
        parser.printHelp(programCall, "[option [...]]", genericArgs, ExampleMode.ALL);
        if (exit)
        {
            exitHandler.exit(0);
        }
    }

    public final List<String> getArgs()
    {
        return Collections.unmodifiableList(args);
    }

    /**
     * Call this method to ensure that no arguments are given to the program.
     * 
     * <p>
     * <b>Note: This method does not return but exits the program if there have been arguments
     * provided!</b>
     */
    public final void assertArgsEmpty()
    {
        if (args.size() != 0)
        {
            System.err.println(ARGS_NOT_EMPTY);
            printHelp(true);
        }
    }

}
