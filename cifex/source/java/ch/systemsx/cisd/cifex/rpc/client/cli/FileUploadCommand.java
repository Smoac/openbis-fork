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
import java.util.Collections;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.Uploader;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A command to perform file uploads to CIFEX.
 * 
 * @author Bernd Rinn
 */
public class FileUploadCommand extends AbstractCommandWithSessionToken
{

    private static final String NAME = "upload";

    private static FileUploadCommand instance;

    private Parameters parameters;

    private static class Parameters extends MinimalParameters
    {

        @Option(name = "c", longName = "comment", metaVar = "STRING", usage = "A comment to send to recipients of the download invitation.")
        private String comment;

        @Option(name = "r", longName = "recipients", metaVar = "STRING", usage = "Comma separated list of recipients.")
        private String recipients;

        private File file;

        public Parameters(String[] args)
        {
            super(args, NAME, "<file>");
            if (getArgs().size() != 1)
            {
                printHelp(true);
            }
            file = new File(getArgs().get(0));
            comment = StringUtils.trimToEmpty(comment);
            recipients = StringUtils.trimToEmpty(recipients);
        }

        String getComment()
        {
            return comment;
        }

        String getRecipients()
        {
            return recipients;
        }

        File getFile()
        {
            return file;
        }

    }

    private FileUploadCommand()
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
    public final static synchronized FileUploadCommand getInstance()
    {
        if (instance == null)
        {
            instance = new FileUploadCommand();
        }
        return instance;
    }

    @Override
    protected int execute(String sessionToken, ICIFEXRPCService service, String[] args)
            throws UserFailureException, EnvironmentFailureException
    {
        final Uploader uploader = new Uploader(service, sessionToken);
        addConsoleProgressListener(uploader);
        uploader.upload(Collections.singletonList(getParameters().getFile()), getParameters()
                .getRecipients(), getParameters().getComment());
        return 0;
    }

}
