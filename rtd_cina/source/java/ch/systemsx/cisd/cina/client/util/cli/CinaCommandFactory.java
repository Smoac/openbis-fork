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

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.client.api.cli.AbstractCommandFactory;
import ch.systemsx.cisd.openbis.dss.client.api.cli.CommandHelp;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ICommand;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaCommandFactory extends AbstractCommandFactory
{
    static final String PROGRAM_CALL_STRING = "cina_client.sh";

    private static enum Command
    {
        LISTSAMPS, GENCODE, LISTEXPS, GETREPLICA, HELP
    }

    public List<String> getKnownCommands()
    {
        String[] commands =
            { "listsamps", "gencode", "listexps", "getreplica" };
        return Arrays.asList(commands);
    }

    public ICommand getHelpCommand()
    {
        return new CommandHelp(this, PROGRAM_CALL_STRING);
    }

    public ICommand tryCommandForName(String name)
    {
        ICommand helpCommandOrNull = tryHelpCommandForName(name);
        if (null != helpCommandOrNull)
        {
            return helpCommandOrNull;
        }

        Command command;
        try
        {
            command = Command.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e)
        {
            return null;
        }

        ICommand result;
        switch (command)
        {
            case LISTSAMPS:
                result = new CommandSampleLister();
                break;
            case GENCODE:
                result = new CommandGenerateSampleCode();
                break;
            case LISTEXPS:
                result = new CommandExperimentLister();
                break;
            case GETREPLICA:
                result = new CommandGetReplica();
                break;
            case HELP:
                result = new CommandHelp(this, PROGRAM_CALL_STRING);
                break;
            default:
                result = null;
                break;
        }

        return result;
    }

}
