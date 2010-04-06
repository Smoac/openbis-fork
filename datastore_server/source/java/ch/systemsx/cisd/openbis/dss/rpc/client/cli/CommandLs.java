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
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.args4j.Argument;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.component.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.rpc.shared.FileInfoDss;

/**
 * Comand that lists files in the data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class CommandLs extends AbstractCommand
{
    private static class CommandLsArguments
    {
        @Option(name = "r", longName = "recursive", usage = "Recurse into directories")
        private boolean recursive = false;

        @Argument
        private final List<String> arguments = new ArrayList<String>();

        public boolean isRecursive()
        {
            return recursive;
        }

        // Accessed via reflection
        @SuppressWarnings("unused")
        public void setRecursive(boolean recursive)
        {
            this.recursive = recursive;
        }

        public List<String> getArguments()
        {
            return arguments;
        }
    }

    private final CommandLsArguments arguments;

    private final CmdLineParser parser;

    private final IDataSetDss dataSet;

    /**
     * Constructor for the command. The dataSet may be null if this command will only be used to
     * print help.
     * 
     * @param dataSet
     */
    CommandLs(IDataSetDss dataSet)
    {
        arguments = new CommandLsArguments();
        parser = new CmdLineParser(arguments);
        this.dataSet = dataSet;
    }

    public int execute(String[] args) throws UserFailureException, EnvironmentFailureException
    {
        FileInfoDss[] fileInfos = getFileInfos(args);
        printFileInfos(fileInfos);

        return 0;
    }

    private FileInfoDss[] getFileInfos(String[] args)
    {
        parser.parseArgument(args);

        String path;
        if (arguments.getArguments().isEmpty())
        {
            path = "/";
        } else
        {
            path = arguments.getArguments().get(0);
        }

        return dataSet.listFiles(path, arguments.isRecursive());
    }

    private void printFileInfos(FileInfoDss[] fileInfos)
    {
        for (FileInfoDss fileInfo : fileInfos)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(fileInfo.getPath());
            sb.append(" -- ");
            if (fileInfo.isDirectory())
            {
                sb.append("Directory");
            } else
            {
                sb.append(fileInfo.getFileSize());
            }
            System.out.println(sb.toString());
        }
    }

    public String getName()
    {
        return "ls";
    }

    public void printHelp(PrintStream out)
    {
        out.println(getProgramCallString() + " [options] <path>");
        parser.printUsage(out);
        out.println("  Example : " + getProgramCallString() + " "
                + parser.printExample(ExampleMode.ALL));
    }
}
