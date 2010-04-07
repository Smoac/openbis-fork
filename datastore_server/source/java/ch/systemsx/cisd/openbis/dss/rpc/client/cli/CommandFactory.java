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
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.component.IDataSetDss;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class CommandFactory
{

    ICommand tryCommandForName(String name, IDataSetDss dataSet)
    {
        if ("ls".equals(name))
        {
            return new CommandLs(dataSet);
        } else if ("get".equals(name))
        {
            return new CommandGet(dataSet);
        }
        return null;
    }

    void printHelpForName(String name, String programCallString, PrintStream out)
    {
        if ("ls".equals(name))
        {
            CommandLs command = new CommandLs(null);
            command.printHelp(programCallString, out);
        } else if ("get".equals(name))
        {
            CommandGet command = new CommandGet(null);
            command.printHelp(programCallString, out);
        }
    }

    List<String> getKnownCommands()
    {
        String[] commands =
            { "ls", "get" };
        return Arrays.asList(commands);
    }
}
