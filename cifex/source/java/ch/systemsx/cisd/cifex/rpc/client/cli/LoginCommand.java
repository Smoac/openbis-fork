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

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Login command.
 * 
 * @author Franz-Josef Elmer
 */
public final class LoginCommand extends AbstractCommand
{
    private static final String NAME = "login";

    private static LoginCommand instance;

    private static final class Parameters extends MinimalParameters
    {
        @Option(name = "u", longName = "username", usage = "User login name")
        private String username;

        @Option(name = "p", longName = "password", usage = "User login password")
        private String password;

        private final Credentials credentials;

        public Parameters(final String[] args)
        {
            super(args, NAME);
            credentials = new Credentials(username, password);
        }

        Credentials getCredentials()
        {
            return credentials;
        }
    }

    /** Not to be instantiated outside of this class. */
    private LoginCommand()
    {
        super(NAME);
    }

    /** Returns the unique instance of this class. */
    public final static synchronized LoginCommand getInstance()
    {
        if (instance == null)
        {
            instance = new LoginCommand();
        }
        return instance;
    }

    public final int execute(final String[] arguments) throws UserFailureException,
            EnvironmentFailureException
    {
        final Parameters parameters = new Parameters(arguments);
        final ICIFEXRPCService serviceOrNull = tryGetService();
        if (serviceOrNull == null)
        {
            return 2;
        }
        Credentials credentials = parameters.getCredentials();
        try
        {
            credentials = getCredentials(credentials);
        } catch (final IOException ex)
        {
            throw new EnvironmentFailureException("I/O Exception while getting credentials.", ex);
        }
        // Check <code>credentials</code>.
        if (credentials == null)
        {
            System.err.println("Credentials are not valid.");
            return 1;
        }
        final String sessionToken =
                serviceOrNull.login(credentials.getUserName(), credentials.getPassword());
        return processSessionToken(sessionToken, credentials.getUserName());
    }

    private final static int processSessionToken(final String sessionToken, final String user)
    {
        if (sessionToken == null)
        {
            System.out.println("Authentication failed for user " + user);
            return 1;
        } else
        {
            FileWriter writer = null;
            try
            {
                writer = new FileWriter(SESSION_TOKEN_FILE);
                writer.write(sessionToken);
                System.out.println("Successfully authenticated.");
                return 0;
            } catch (final IOException e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            } finally
            {
                IOUtils.closeQuietly(writer);
            }
        }
    }

    /**
     * Asks the user for username and password.
     * <p>
     * This method should only be called if initial credentials are not valid. Note that this method
     * does not return as long as input user name resp. password are <code>null</code>.
     * </p>
     */
    private final Credentials getCredentials(final Credentials initial) throws IOException
    {
        // userName
        String userName = initial.getUserName();
        if (StringUtils.isBlank(userName))
        {
            userName = getConsoleReader().readLine("User: ");
        }
        if (StringUtils.isBlank(userName))
        {
            System.err.println("No user name has been specified.");
            System.exit(1);
        }
        // password
        String password = initial.getPassword();
        if (StringUtils.isBlank(password))
        {
            password = getConsoleReader().readLine("Password: ", Character.valueOf('*'));
        }
        if (StringUtils.isBlank(password))
        {
            System.err.println("No password has been specified.");
            System.exit(1);
        }
        return new Credentials(userName, password);
    }

}
