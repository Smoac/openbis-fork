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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedFileInputStream;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;

/**
 * Command that lists files in the data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class CommandPut extends AbstractCommand
{
    private static class CommandPutArguments extends GlobalArguments
    {
        @Option(name = "t", longName = "type", usage = "Set the data set type")
        private String dataSetType;

        public String getDataSetType()
        {
            return dataSetType;
        }

        public DataSetOwnerType getOwnerType()
        {
            return DataSetOwnerType.valueOf(getArguments().get(0).toString().toUpperCase());
        }

        public String getOwnerIdentifier()
        {
            return getArguments().get(1);
        }

        public String getFilePath()
        {
            return getArguments().get(1);
        }

        public File getFile()
        {
            return new File(getFilePath());
        }

        @Override
        public boolean isComplete()
        {
            if (false == super.isComplete())
                return false;

            if (getArguments().size() < 3)
                return false;

            try
            {
                DataSetOwnerType.valueOf(getArguments().get(1).toString().toUpperCase());
            } catch (IllegalArgumentException e)
            {
                return false;
            }

            return true;
        }
    }

    private static class CommandPutExecutor
    {
        private final CommandPutArguments arguments;

        private final IDssComponent component;

        CommandPutExecutor(IDssComponent dssService, CommandPutArguments arguments)
        {
            this.arguments = arguments;
            this.component = dssService;
        }

        int execute()
        {
            try
            {
                NewDataSetDTO newDataSet = getNewDataSet();
                if (newDataSet.getFileInfos().isEmpty())
                {
                    File file = arguments.getFile();
                    if (false == file.exists())
                    {
                        System.err.println("Data set file does not exist");
                    } else if (false == file.isDirectory())
                    {
                        System.err.println("Must select a directory to upload.");
                    } else
                    {
                        System.err.println("Data set is empty.");
                    }
                    return -1;
                }
                ConcatenatedFileInputStream fileInputStream =
                        new ConcatenatedFileInputStream(true, getFilesForFileInfos(arguments
                                .getFilePath(), newDataSet.getFileInfos()));
                IDataSetDss dataSet = component.putDataSet(newDataSet, fileInputStream);
                System.out.println("Registered new data set " + dataSet.getCode());
            } catch (IOException e)
            {
                e.printStackTrace();
                return -1;
            }

            return 0;
        }

        private NewDataSetDTO getNewDataSet() throws IOException
        {
            // Get the owner
            // That the owner type is valid has already been checked by CmdPutArguments#isComplete
            DataSetOwnerType ownerType = arguments.getOwnerType();
            String ownerIdentifier = arguments.getOwnerIdentifier();
            DataSetOwner owner = new NewDataSetDTO.DataSetOwner(ownerType, ownerIdentifier);

            // Get the file infos
            String filePath = arguments.getFilePath();
            File file = new File(filePath);
            ArrayList<FileInfoDssDTO> fileInfos = getFileInfosForPath(filePath, file);

            // Get the parent
            String parentNameOrNull = (file.isDirectory()) ? file.getName() : null;

            NewDataSetDTO dataSet = new NewDataSetDTO(owner, parentNameOrNull, fileInfos);
            // Set the data set type (may be null)
            dataSet.setDataSetTypeOrNull(arguments.getDataSetType());
            return dataSet;
        }

        private ArrayList<FileInfoDssDTO> getFileInfosForPath(String path, File file)
                throws IOException
        {
            ArrayList<FileInfoDssDTO> fileInfos = new ArrayList<FileInfoDssDTO>();
            if (false == file.exists())
            {
                return fileInfos;
            }
            if (false == file.isDirectory())
            {
                return fileInfos;
            }
            FileInfoDssBuilder builder = new FileInfoDssBuilder(path, path);
            builder.appendFileInfosForFile(file, fileInfos, true);
            return fileInfos;
        }

        private List<File> getFilesForFileInfos(String filePath, List<FileInfoDssDTO> fileInfos)
        {
            ArrayList<File> files = new ArrayList<File>();
            File parent = new File(filePath);
            if (false == parent.isDirectory())
            {
                return Collections.singletonList(parent);
            }

            for (FileInfoDssDTO fileInfo : fileInfos)
            {
                files.add(new File(parent, fileInfo.getPathInDataSet()));
            }

            return files;
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
        if (null == component)
        {
            return 1;
        }
        return new CommandPutExecutor(component, arguments).execute();
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
        out.println(getUsagePrefixString()
                + " [options] <data set type> <owner type> <owner> <path>");
        parser.printUsage(out);
        out.println("  Examples : ");
        out.println("     " + getCommandCallString() + parser.printExample(ExampleMode.ALL)
                + " HCS_IMAGE EXPERIMENT <experiment identifier> <path>");
        out.println("     " + getCommandCallString() + parser.printExample(ExampleMode.ALL)
                + " HCS_IMAGE SAMPLE <sample identifier> <path>");
    }
}
