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
 * A command for decrypting a file from an OpenPGP container.
 * 
 * @author Bernd Rinn
 */
public class DecryptCommand extends AbstractCommand
{

    private static final String NAME = "decrypt";

    private static DecryptCommand instance;

    private static class Parameters extends MinimalParameters
    {
        private File inFile;

        @Option(name = "o", longName = "output-file", metaVar = "STRING", usage = "The name of the decrypted file.")
        private String outFilename;

        @Option(name = "O", longName = "overwrote-output-file", metaVar = "FLAG", usage = "Whether an already existing output file should be silently overwritten.")
        private boolean overwriteOutputFile;

        @Option(name = "p", longName = "passphrase", metaVar = "STRING", usage = "The pass phrase to use for decryption.")
        private String passphrase;

        public Parameters(String[] args)
        {
            super(args, NAME, "<file>");
            if (getArgs().size() != 1)
            {
                printHelp(true);
            }
            inFile = new File(getArgs().get(0));
        }

        public File getInFile()
        {
            return inFile;
        }

        public String getOutFilename()
        {
            return outFilename;
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

    /** Returns the unique instance of this class. */
    public final static synchronized DecryptCommand getInstance()
    {
        if (instance == null)
        {
            instance = new DecryptCommand();
        }
        return instance;
    }

    private DecryptCommand()
    {
        super(NAME);
    }

    private String getPassphraseOrExit(final Parameters parameters)
    {
        String passphrase = tryGetPassphrase("Passphrase: ", parameters.getPassphrase());
        if (StringUtils.isBlank(passphrase))
        {
            System.err.println("No passphrase has been specified, exiting.");
            System.exit(1);
        }
        return passphrase;
    }

    @Override
    public int execute(String[] arguments) throws UserFailureException, EnvironmentFailureException
    {
        final Parameters parameters = new Parameters(arguments);
        String passphrase = getPassphraseOrExit(parameters);
        final File clearTextFile =
                OpenPGPSymmetricKeyEncryption.decrypt(parameters.getInFile(), parameters
                        .getOutFilename(), passphrase, parameters.isOverwriteOutputFile());
        if (parameters.getOutFilename() == null)
        {
            System.out.println("\nDecrypted file is '" + clearTextFile + "'.");
        }

        return 0;
    }

}
