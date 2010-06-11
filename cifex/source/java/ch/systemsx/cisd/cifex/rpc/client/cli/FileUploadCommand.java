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
import ch.systemsx.cisd.cifex.rpc.client.FileWithOverrideName;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXUploader;
import ch.systemsx.cisd.cifex.rpc.client.encryption.OpenPGPSymmetricKeyEncryption;
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

        @Option(name = "q", longName = "quiet", usage = "Suppress progress reporting.")
        private boolean quiet;

        @Option(name = "E", longName = "encrypt", usage = "Encrypt file before uploading.")
        private boolean encrypt;

        @Option(name = "n", longName = "name", usage = "Name of the file as reported to the server.")
        private String name;

        @Option(name = "p", longName = "passphrase", metaVar = "STRING", usage = "The pass phrase to use for encryption.")
        private String passphrase;

        public boolean isEncrypt()
        {
            return encrypt || passphrase != null;
        }

        public String getPassphrase()
        {
            return passphrase;
        }

        private FileWithOverrideName file;

        public Parameters(String[] args)
        {
            super(args, NAME, "<file>");
            if (getArgs().size() != 1)
            {
                printHelp(true);
            }
            file = new FileWithOverrideName(new File(getArgs().get(0)), name);
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

        FileWithOverrideName getFile()
        {
            return file;
        }

        public boolean beQuiet()
        {
            return quiet;
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

    private String getPassphraseOrExit()
    {
        String passphrase = tryGetPassphrase(parameters.getPassphrase());
        if (StringUtils.isBlank(passphrase))
        {
            System.err.println("No password has been specified.");
            System.exit(1);
        }
        return passphrase;
    }

    private File getEncryptedFile(FileWithOverrideName clearFile)
    {
        final File encryptedFile;
        if (clearFile.tryGetOverrideName() == null)
        {
            encryptedFile =
                    new File(clearFile.getFile().getPath()
                            + OpenPGPSymmetricKeyEncryption.PGP_FILE_EXTENSION);
        } else
        {
            encryptedFile =
                    new File(clearFile.getFile().getAbsoluteFile().getParent(), clearFile
                            .tryGetOverrideName());
        }
        return encryptedFile;
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
        final ICIFEXUploader uploader = cifex.createUploader(sessionToken);
        addConsoleProgressListener(uploader, getParameters().beQuiet());
        FileWithOverrideName file = getParameters().getFile();

        if (getParameters().isEncrypt())
        {
            File encryptedFile = getEncryptedFile(file);
            final String passphrase = getPassphraseOrExit();
            encryptedFile =
                    OpenPGPSymmetricKeyEncryption
                            .encrypt(file.getFile(), encryptedFile, passphrase);
            file = new FileWithOverrideName(encryptedFile, file.tryGetOverrideName());
        }

        uploader.upload(Collections.singletonList(file), getParameters().getRecipients(),
                getParameters().getComment());
        return 0;
    }

}
