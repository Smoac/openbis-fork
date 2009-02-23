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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.Downloader;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A command to perform file downloads from CIFEX.
 * 
 * @author Bernd Rinn
 */
public class FileDownloadCommand extends AbstractCommandWithSessionToken
{

    private static final String NAME = "download";

    private static FileDownloadCommand instance;

    private Parameters parameters;
    
    private static final Pattern FILE_ID_LINK_PATTERN = Pattern.compile("fileId=([0-9]+)"); 

    private static class Parameters extends MinimalParameters
    {

        @Option(name = "d", longName = "directory", metaVar = "DIR", usage = "Directory to download the file to.")
        private File directory;

        @Option(name = "n", longName = "name", metaVar = "FILE", usage = "File name to use for the downloaded file (instead of the one stored in CIFEX).")
        private String name;

        private long fileID;

        public Parameters(String[] args)
        {
            super(args, NAME, "<cifex_file_id or cifex_link>");
            if (getArgs().size() != 1)
            {
                printHelp(true);
            }
            final String fileIdStr = getArgs().get(0);
            try
            {
                fileID = Long.parseLong(fileIdStr);
            } catch (NumberFormatException ex)
            {
                final Matcher fileIdLinkMatcher = FILE_ID_LINK_PATTERN.matcher(fileIdStr); 
                if (fileIdLinkMatcher.find())
                {
                    fileID = Long.parseLong(fileIdLinkMatcher.group(1));
                } else
                {
                    printHelp(true);
                }
            }
        }

        public File getDirectory()
        {
            return directory;
        }

        public String getName()
        {
            return name;
        }

        public long getFileID()
        {
            return fileID;
        }

    }

    private FileDownloadCommand()
    {
        super(NAME);
    }

    @Override
    protected Parameters getParameters()
    {
        if (parameters == null)
        {
            parameters = new Parameters(arguments);
        }
        return parameters;
    }

    /** Returns the unique instance of this class. */
    public final static synchronized FileDownloadCommand getInstance()
    {
        if (instance == null)
        {
            instance = new FileDownloadCommand();
        }
        return instance;
    }

    @Override
    protected int execute(String sessionToken, ICIFEXRPCService service, String[] args)
            throws UserFailureException, EnvironmentFailureException
    {
        final Downloader downloader = new Downloader(service, sessionToken);
        addConsoleProgressListener(downloader);
        downloader.download(getParameters().getFileID(), getParameters().getDirectory(),
                getParameters().getName());
        return 0;
    }

}
