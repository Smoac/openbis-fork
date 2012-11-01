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

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXDownloader;
import ch.systemsx.cisd.cifex.rpc.client.encryption.OpenPGPSymmetricKeyEncryption;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A command to perform file downloads from CIFEX.
 * 
 * @author Bernd Rinn
 */
public class FileDownloadAllCommand extends AbstractCommandWithSessionToken
{

    private static final String NAME = "download_all";

    private static FileDownloadAllCommand instance;

    private Parameters parameters;

    private static class Parameters extends MinimalParameters
    {

        @Option(name = "d", longName = "directory", metaVar = "DIR", usage = "Directory to download the files to.")
        private File directory;

        @Option(name = "q", longName = "quiet", usage = "Suppress progress reporting.")
        private boolean quiet;

        @Option(name = "D", longName = "decrypt", usage = "Decrypt files after downloading.")
        private boolean decrypt;

        @Option(name = "p", longName = "passphrase", metaVar = "STRING", usage = "The pass phrase to use for encryption.")
        private String passphrase;

        @Option(name = "O", longName = "overwrote-output-file", metaVar = "FLAG", usage = "Whether an already existing output file for the local decrypted file should be silently overwritten (only used if decryption is enabled).")
        private boolean overwriteOutputFile;

        public Parameters(String[] args)
        {
            super(args, NAME, "");
            if (getArgs().isEmpty() == false)
            {
                printHelp(true);
            }
        }

        public File getDirectory()
        {
            if (directory == null)
            {
                return new File(".");
            }
            if (directory.isDirectory() == false)
            {
                directory.mkdirs();
            }
            return directory;
        }

        public boolean beQuiet()
        {
            return quiet;
        }

        public boolean isDecrypt()
        {
            return decrypt || passphrase != null;
        }

        public boolean isOverwriteOutputFile()
        {
            return overwriteOutputFile;
        }

        public String getPassphrase()
        {
            return passphrase;
        }

    }

    private FileDownloadAllCommand()
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
    public final static synchronized FileDownloadAllCommand getInstance()
    {
        if (instance == null)
        {
            instance = new FileDownloadAllCommand();
        }
        return instance;
    }

    private String getPassphraseOrExit(String label)
    {
        String passphrase = tryGetPassphrase(label, parameters.getPassphrase());
        if (StringUtils.isBlank(passphrase))
        {
            System.err.println("No password has been specified.");
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
        final ICIFEXDownloader downloader = cifex.createDownloader(sessionToken);
        addConsoleProgressListener(downloader, true, getParameters().beQuiet());

        final String passphrase;
        if (getParameters().isDecrypt())
        {
            passphrase = getPassphraseOrExit("Passphrase: ");
        } else
        {
            passphrase = null;
        }
        for (FileInfoDTO fileInfo : cifex.listDownloadFiles(sessionToken))
        {
            final File file =
                    downloader.download(fileInfo.getID(), getParameters().getDirectory(), null,
                            true);

            if (getParameters().isDecrypt())
            {
                File clearTextFile =
                        OpenPGPSymmetricKeyEncryption.decrypt(file, null, passphrase,
                                getParameters().isOverwriteOutputFile());
                System.out.println("\nDecrypted file is '" + clearTextFile + "'.");
            }
        }

        return 0;
    }

}
