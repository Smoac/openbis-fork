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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A command to delete a file from the CIFEX server.
 * 
 * @author Bernd Rinn
 */
public class FileDeletionCommand extends AbstractCommandWithSessionToken
{

    private static final String NAME = "delete";

    private static FileDeletionCommand instance;

    private Parameters parameters;

    private static final Pattern FILE_ID_LINK_PATTERN = Pattern.compile("fileId=([0-9]+)");

    private static class Parameters extends MinimalParameters
    {

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

        public long getFileID()
        {
            return fileID;
        }

    }

    private FileDeletionCommand()
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
    public final static synchronized FileDeletionCommand getInstance()
    {
        if (instance == null)
        {
            instance = new FileDeletionCommand();
        }
        return instance;
    }

    @Override
    protected boolean isHelpRequest(final String[] args)
    {
        arguments = args;
        return getParameters().isHelpRequest();
    }

    @Override
    protected int execute(String sessionToken, ICIFEXComponent cifex, String[] args)
            throws UserFailureException, EnvironmentFailureException
    {
        cifex.deleteFile(sessionToken, getParameters().getFileID());
        System.out.printf("File with id %d successfully deleted from server.\n", getParameters()
                .getFileID());
        return 0;
    }

}
