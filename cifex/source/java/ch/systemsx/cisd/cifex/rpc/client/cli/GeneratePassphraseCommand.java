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

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A command for generating strong passphrases.
 * 
 * @author Bernd Rinn
 */
public class GeneratePassphraseCommand extends AbstractCommand
{

    private static final String NAME = "generate-passphrase";

    private static GeneratePassphraseCommand instance;

    private static class Parameters extends MinimalParameters
    {
        @Option(name = "s", longName = "short-pasphrase", metaVar = "FLAG", usage = "Create a short and quite memorizable password.")
        private boolean shortPassphrase;

        public Parameters(String[] args)
        {
            super(args, NAME, "");
            if (getArgs().size() != 0)
            {
                printHelp(true);
            }
        }

        public boolean isShortPassphrase()
        {
            return shortPassphrase;
        }
    }

    /** Returns the unique instance of this class. */
    public final static synchronized GeneratePassphraseCommand getInstance()
    {
        if (instance == null)
        {
            instance = new GeneratePassphraseCommand();
        }
        return instance;
    }

    private GeneratePassphraseCommand()
    {
        super(NAME);
    }

    @Override
    public int execute(String[] arguments) throws UserFailureException, EnvironmentFailureException
    {
        final Parameters parameters = new Parameters(arguments);
        System.out.println(generatePassphrase(parameters.isShortPassphrase()));
        
        return 0;
    }

}
