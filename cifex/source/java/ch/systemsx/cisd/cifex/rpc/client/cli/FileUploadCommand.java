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
import java.util.ArrayList;
import java.util.List;

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

        @Option(name = "n", longName = "name", usage = "Name of the file as reported to the server (only allowed when exactly one file is given).")
        private String name;

        @Option(name = "p", longName = "passphrase", metaVar = "STRING", usage = "The pass phrase to use for encryption.")
        private String passphrase;

        @Option(name = "g", longName = "generate-passphrase", metaVar = "FLAG", usage = "Automatically generate a passphrase (incompatible with -p).", skipForExample = true)
        private boolean generatePassphrase;

        @Option(name = "s", longName = "short-passphrase", metaVar = "FLAG", usage = "Create a short and quite memorizable password (implies -g).", skipForExample = true)
        private boolean shortPassphrase;

        @Option(name = "O", longName = "overwrote-output-file", metaVar = "FLAG", usage = "Whether an already existing output file for the local encrypted file should be silently overwritten (only used if encryption is enabled).")
        private boolean overwriteOutputFile;

        private List<FileWithOverrideName> files;

        public Parameters(String[] args)
        {
            super(args, NAME, "<file> [<file>...]");
            if (getArgs().isEmpty() || (getPassphrase() != null && isGeneratePassphrase())
                    || (getArgs().size() > 1 && name != null))
            {
                printHelp(true);
            }
            files = new ArrayList<FileWithOverrideName>(getArgs().size());
            if (name != null)
            {
                files.add(new FileWithOverrideName(new File(getArgs().get(0)), name));
            } else
            {
                for (String filename : getArgs())
                {
                    files.add(new FileWithOverrideName(new File(filename), null));
                }
            }
            comment = StringUtils.trimToEmpty(comment);
            recipients = StringUtils.trimToEmpty(recipients);
        }

        public boolean isEncrypt()
        {
            return encrypt || passphrase != null || generatePassphrase || shortPassphrase;
        }

        public String getPassphrase()
        {
            return passphrase;
        }

        public boolean isGeneratePassphrase()
        {
            return generatePassphrase || isShortPassphrase();
        }

        public boolean isShortPassphrase()
        {
            return shortPassphrase;
        }

        public boolean isOverwriteOutputFile()
        {
            return overwriteOutputFile;
        }

        String getComment()
        {
            return comment;
        }

        String getRecipients()
        {
            return recipients;
        }

        List<FileWithOverrideName> getFiles()
        {
            return files;
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
        if (parameters.isShortPassphrase())
        {
            final String passphrase = generatePassphrase(true);
            System.out.println("Password is: " + passphrase);
            return passphrase;
        } else if (parameters.isGeneratePassphrase())
        {
            final String passphrase = generatePassphrase(false);
            System.out.println("Passphrase is: " + passphrase);
            return passphrase;
        }
        String passphrase = tryGetPassphrase("Passphrase: ", parameters.getPassphrase());
        if (StringUtils.isBlank(passphrase))
        {
            System.err.println("No password has been specified, exiting.");
            System.exit(1);
        }
        String passphraseRepeat =
                tryGetPassphrase("Passphrase (repeat): ", parameters.getPassphrase());
        if (passphrase.equals(passphraseRepeat) == false)
        {
            System.err.println("The two passphrases do not match, exiting.");
            System.exit(1);
        }
        return passphrase;
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
        addConsoleProgressListener(uploader, getParameters().getFiles().size() > 1, getParameters()
                .beQuiet());

        final List<FileWithOverrideName> files;
        if (getParameters().isEncrypt())
        {
            final String passphrase = getPassphraseOrExit();
            files = new ArrayList<FileWithOverrideName>(getParameters().getFiles().size());
            for (FileWithOverrideName file : getParameters().getFiles())
            {
                final File encryptedFile =
                        OpenPGPSymmetricKeyEncryption.encrypt(file.getOriginalFile(), file
                                .getEncryptedFile(), passphrase, getParameters()
                                .isOverwriteOutputFile());
                file = new FileWithOverrideName(encryptedFile, file.tryGetOverrideName());
                files.add(file);
            }
        } else
        {
            files = getParameters().getFiles();
        }
        uploader.upload(files, getParameters().getRecipients(), getParameters().getComment());
        return 0;
    }

}
