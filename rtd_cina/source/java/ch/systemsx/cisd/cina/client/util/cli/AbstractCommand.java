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

package ch.systemsx.cisd.cina.client.util.cli;

import java.io.PrintStream;

import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.cina.client.util.v1.CinaUtilitiesFacadeFactory;
import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.openbis.dss.client.api.cli.GlobalArguments;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ICommand;

/**
 * Superclass for cina command-line client commands.
 * <p>
 * Provides services used by most subclasses. In order for the AbstractCommand to work, subclasses
 * must do the following:
 * <ul>
 * <li>Set the parser ivar in their constructor
 * </ul>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractCommand<T extends GlobalArguments> implements ICommand
{
    // The parser for command-line arguments; set by subclasses in the constructor.
    protected final CmdLineParser parser;

    // The object that describes and receives the value of the arguments
    protected final T arguments;

    protected AbstractCommand(T arguments)
    {
        this.parser = new CmdLineParser(arguments);
        this.arguments = arguments;
    }

    /**
     * Print usage information about the command.
     */
    public void printUsage(PrintStream out)
    {
        out.println(getUsagePrefixString() + " [options]");
        parser.printUsage(out);
        out.println("  Example : " + getCommandCallString() + " "
                + parser.printExample(ExampleMode.ALL) + "");
    }

    /**
     * How is this command invoked from the command line? This is the program call string + command
     * name
     */
    protected String getCommandCallString()
    {
        return getProgramCallString() + " " + getName();
    }

    /**
     * Used for displaying help.
     */
    protected String getUsagePrefixString()
    {
        return "usage: " + getCommandCallString();
    }

    /**
     * How is this program invoked from the command line?
     */
    protected String getProgramCallString()
    {
        return "cina_client.sh";
    }

    /**
     * Creates the CinaUtilities facade object and logs into the server.
     */
    protected ICinaUtilities login()
    {
        ICinaUtilities component =
                CinaUtilitiesFacadeFactory.tryCreate(arguments.getUsername(),
                        arguments.getPassword(), arguments.getServerBaseUrl());
        if (null == component)
        {
            System.out.println("Username / password is invalid");
        }
        return component;
    }

    CmdLineParser getParser()
    {
        return parser;
    }

    T getArguments()
    {
        return arguments;
    }
}
