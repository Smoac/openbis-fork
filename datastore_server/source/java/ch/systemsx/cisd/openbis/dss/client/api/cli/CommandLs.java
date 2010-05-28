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

import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * Comand that lists files in the data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class CommandLs extends AbstractCommand
{
    private static class CommandLsArguments extends DataSetArguments
    {
        @Option(name = "r", longName = "recursive", usage = "Recurse into directories")
        private boolean recursive = false;

        public boolean isRecursive()
        {
            return recursive;
        }
    }

    private static class CommandLsExecutor
    {
        private final CommandLsArguments arguments;

        private final IDataSetDss dataSet;

        CommandLsExecutor(IDataSetDss dataSet, CommandLsArguments arguments)
        {
            this.arguments = arguments;
            this.dataSet = dataSet;
        }

        int execute()
        {
            FileInfoDssDTO[] fileInfos = getFileInfos();
            printFileInfos(fileInfos);

            return 0;
        }

        private FileInfoDssDTO[] getFileInfos()
        {

            String path = getRequestedPath();
            return dataSet.listFiles(path, arguments.isRecursive());
        }

        private String getRequestedPath()
        {
            return arguments.getRequestedPath();
        }

        private void printFileInfos(FileInfoDssDTO[] fileInfos)
        {
            for (FileInfoDssDTO fileInfo : fileInfos)
            {
                StringBuilder sb = new StringBuilder();
                if (fileInfo.isDirectory())
                {
                    sb.append(" \t");
                } else
                {
                    sb.append(fileInfo.getFileSize());
                    sb.append("\t");
                }
                sb.append(fileInfo.getPathInDataSet());
                System.out.println(sb.toString());
            }
        }
    }

    private final CommandLsArguments arguments;

    CommandLs()
    {
        arguments = new CommandLsArguments();
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

        IDssComponent component = null;
        try
        {
            component = login(arguments);
            IDataSetDss dataSet = getDataSet(component, arguments);
            return new CommandLsExecutor(dataSet, arguments).execute();
        } finally
        {
            // Cleanup
            if (null != component)
            {
                component.logout();
            }
        }
    }

    public String getName()
    {
        return "ls";
    }
}
