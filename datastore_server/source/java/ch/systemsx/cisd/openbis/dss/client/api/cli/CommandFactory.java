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

package ch.systemsx.cisd.openbis.dss.client.api.cli;

import java.util.Arrays;
import java.util.List;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class CommandFactory
{
    private static enum Command
    {
        LS, GET, HELP
    }

    /**
     * Find the command that matches the name.
     */
    ICommand tryCommandForName(String name)
    {
        // special handling of "-h" and "--help"
        if ("-h".equals(name) || "--help".equals(name))
        {
            return new CommandHelp(this);
        }

        Command command = Command.valueOf(name.toUpperCase());
        if (null == command)
        {
            return null;
        }

        ICommand result;
        switch (command)
        {
            case LS:
                result = new CommandLs();
                break;
            case GET:
                result = new CommandGet();
                break;
            case HELP:
                result = new CommandHelp(this);
                break;
            default:
                result = null;
                break;
        }

        return result;
    }

    List<String> getKnownCommands()
    {
        String[] commands =
            { "ls", "get" };
        return Arrays.asList(commands);
    }
}
