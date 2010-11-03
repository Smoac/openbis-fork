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

import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.openbis.dss.client.api.cli.GlobalArguments;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ResultCode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractExecutor<A extends GlobalArguments>
{
    protected final A arguments;

    private final CmdLineParser parser;

    private final AbstractCinaCommand<A> command;

    AbstractExecutor(AbstractCinaCommand<A> command)
    {
        this.command = command;
        arguments = command.getArguments();
        parser = command.getParser();
    }

    final ResultCode execute(String[] args)
    {
        parser.parseArgument(args);

        // Show help and exit
        if (arguments.isHelp())
        {
            command.printUsage(System.out);
            return ResultCode.OK;
        }

        // Show usage and exit
        if (arguments.isComplete() == false)
        {
            command.printUsage(System.err);
            return ResultCode.INVALID_ARGS;
        }

        ICinaUtilities component = null;
        try
        {
            component = command.login();
            if (null == component)
            {
                return ResultCode.INVALID_UNAME_PASS;
            }
            return doExecute(component);
        } finally
        {
            // Cleanup
            if (null != component)
            {
                component.logout();
            }
        }

    }

    protected abstract ResultCode doExecute(ICinaUtilities component);
}
