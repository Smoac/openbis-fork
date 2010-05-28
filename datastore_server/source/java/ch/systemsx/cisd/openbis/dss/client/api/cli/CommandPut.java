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

import java.io.InputStream;
import java.io.PrintStream;

import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDssDTO;

/**
 * Command that lists files in the data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class CommandPut extends AbstractCommand
{
    private static class CommandPutArguments extends GlobalArguments
    {
        public String getStorageProcess()
        {
            return getArguments().get(0);
        }

        public String getFilePath()
        {
            return getArguments().get(1);
        }

        @Override
        public boolean isComplete()
        {
            if (false == super.isComplete())
                return false;

            if (getArguments().size() < 2)
                return false;

            return true;
        }
    }

    private static class CommandPutExecutor
    {
        private final CommandPutArguments arguments;

        private final IDssServiceRpcGeneric dssService;

        CommandPutExecutor(IDssServiceRpcGeneric dssService, CommandPutArguments arguments)
        {
            this.arguments = arguments;
            this.dssService = dssService;
        }

        int execute()
        {
            NewDataSetDssDTO newDataSet = getNewDataSet();
            dssService.putDataSet("", newDataSet);

            return 0;
        }

        private NewDataSetDssDTO getNewDataSet()
        {
            String storageProcessName = arguments.getStorageProcess();
            String filePath = arguments.getFilePath();
            filePath.length();
            InputStream fileInputStream = null;
            return new NewDataSetDssDTO(storageProcessName, fileInputStream);
        }
    }

    private final CommandPutArguments arguments;

    CommandPut()
    {
        arguments = new CommandPutArguments();
        parser = new CmdLineParser(arguments);
    }

    public int execute(String[] args) throws UserFailureException, EnvironmentFailureException
    {
        parser.parseArgument(args);

        // Show help and exit
        if (arguments.isHelp())
        {
            printUsage(System.out);
            return 0;
        }

        // Show usage and exit
        if (arguments.isComplete() == false)
        {
            printUsage(System.err);
            return 1;
        }

        IDssComponent component = login(arguments);
        IDssServiceRpcGeneric dssService = component.getDefaultDssService();
        return new CommandPutExecutor(dssService, arguments).execute();
    }

    public String getName()
    {
        return "put";
    }

    /**
     * Print usage information about the command.
     */
    @Override
    public void printUsage(PrintStream out)
    {
        out.println(getUsagePrefixString() + " [options] <storage process> <path>");
        parser.printUsage(out);
        out.println("  Example : " + getCommandCallString() + " "
                + parser.printExample(ExampleMode.ALL) + " <storage process> <path>");
    }
}
