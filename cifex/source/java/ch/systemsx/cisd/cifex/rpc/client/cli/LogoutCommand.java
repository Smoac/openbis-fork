/*
 * Copyright 2007 ETH Zuerich, CISD
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

import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Logout command.
 * 
 * @author Franz-Josef Elmer
 */
public final class LogoutCommand extends AbstractCommand
{
    private static final String NAME = "logout";

    private static LogoutCommand instance;

    /** Not to be instantiated outside of this class. */
    private LogoutCommand()
    {
        super(NAME);
    }

    /** Returns the unique instance of this class. */
    public final static synchronized LogoutCommand getInstance()
    {
        if (instance == null)
        {
            instance = new LogoutCommand();
        }
        return instance;
    }

    public final int execute(String[] arguments) throws UserFailureException,
            EnvironmentFailureException
    {
        if (SESSION_TOKEN_FILE.exists())
        {
            try
            {
                String sessionToken = FileUtilities.loadToString(SESSION_TOKEN_FILE).trim();
                final MinimalParameters parameters = new MinimalParameters(arguments, NAME);
                parameters.assertArgsEmpty();
                final ICIFEXComponent cifexOrNull = tryGetComponent();
                if (cifexOrNull != null)
                {
                    cifexOrNull.logout(sessionToken);
                }
                SESSION_TOKEN_FILE.delete();
                if (cifexOrNull == null)
                {
                    return 2;
                }
                System.out.println("Successfully logged out.");
            } catch (InvalidSessionException ex)
            {
                // Ignore InvalidSessionException, as we logout anyway.
                SESSION_TOKEN_FILE.delete();
                return 0;
            }
        }
        return 0;
    }
}