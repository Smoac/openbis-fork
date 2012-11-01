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

package ch.systemsx.cisd.cifex.rpc.client.cli;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.cifex.rpc.client.encryption.OpenPGPSymmetricKeyEncryption;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A command for encrypting a file into an OpenPGP container.
 * 
 * @author Bernd Rinn
 */
public class EncryptCommand extends AbstractCommand
{

    private static final String NAME = "encrypt";

    private static EncryptCommand instance;

    private static class Parameters extends MinimalParameters
    {
        private File inFile;

        private File outFile;

        @Option(name = "p", longName = "passphrase", metaVar = "STRING", usage = "The pass phrase to use for encryption.")
        private String passphrase;

        @Option(name = "g", longName = "generate-passphrase", metaVar = "FLAG", usage = "Automatically generate a passphrase (incompatible with -p).", skipForExample=true)
        private boolean generatePassphrase;

        @Option(name = "s", longName = "short-passphrase", metaVar = "FLAG", usage = "Create a short and quite memorizable password (implies -g).", skipForExample=true)
        private boolean shortPassphrase;
        
        @Option(name = "O", longName = "overwrote-output-file", metaVar = "FLAG", usage = "Whether an already existing output file should be silently overwritten.")
        private boolean overwriteOutputFile;

        public Parameters(String[] args)
        {
            super(args, NAME, "<file>");
            if (getArgs().size() != 1 || (getPassphrase() != null && isGeneratePassphrase()))
            {
                printHelp(true);
            }
            inFile = new File(getArgs().get(0));
            outFile = new File(getArgs().get(0) + OpenPGPSymmetricKeyEncryption.PGP_FILE_EXTENSION);
        }

        public File getInFile()
        {
            return inFile;
        }

        public File getOutFile()
        {
            return outFile;
        }

        public boolean isOverwriteOutputFile()
        {
            return overwriteOutputFile;
        }

        public String getPassphrase()
        {
            return passphrase;
        }

        public boolean isGeneratePassphrase()
        {
            return generatePassphrase || shortPassphrase;
        }

        public boolean isShortPassphrase()
        {
            return shortPassphrase;
        }
    }

    /** Returns the unique instance of this class. */
    public final static synchronized EncryptCommand getInstance()
    {
        if (instance == null)
        {
            instance = new EncryptCommand();
        }
        return instance;
    }

    private EncryptCommand()
    {
        super(NAME);
    }

    private String getPassphraseOrExit(final Parameters parameters)
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
            System.err.println("No passphrase has been specified, exiting.");
            System.exit(1);
        }
        String passphraseRepeat = tryGetPassphrase("Passphrase (repeat): ", parameters.getPassphrase());
        if (passphrase.equals(passphraseRepeat) == false)
        {
            System.err.println("The two passphrases do not match, exiting.");
            System.exit(1);
        }
        return passphrase;
    }

    @Override
    public int execute(String[] arguments) throws UserFailureException, EnvironmentFailureException
    {
        final Parameters parameters = new Parameters(arguments);
        final String passphrase = getPassphraseOrExit(parameters);
        OpenPGPSymmetricKeyEncryption.encrypt(parameters.getInFile(), parameters.getOutFile(),
                passphrase, parameters.isOverwriteOutputFile());

        return 0;
    }

}
