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

import java.util.Timer;
import java.util.TimerTask;

import ch.systemsx.cisd.cifex.rpc.client.ClientConfigurationFiles;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Abstract super class of all commands which need a session token.
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractCommandWithSessionToken extends AbstractCommand
{

    private static final long ONE_MINUTE = 60 * 1000L;

    private static String tryCheckAndGetSessionToken()
    {
        if (ClientConfigurationFiles.SESSION_TOKEN_FILE.exists())
        {
            return FileUtilities.loadToString(ClientConfigurationFiles.SESSION_TOKEN_FILE).trim();
        }
        System.err.println("You are not logged in. Please call '"
                + MinimalParameters.getCommandPrepender() + "login' to start a session.");
        return null;
    }

    private MinimalParameters parameters;

    protected String[] arguments;

    AbstractCommandWithSessionToken(String name)
    {
        super(name);
    }

    /**
     * Method that must be implemented by subclasses.
     * 
     * @param sessionToken never <code>null</code>.
     */
    protected abstract int execute(final String sessionToken, final ICIFEXComponent cifexComponent,
            final String[] args) throws UserFailureException, EnvironmentFailureException;

    /**
     * Should show the help and return <code>true</code> if the execution is a request for help (
     * <code>--help</code>).
     */
    protected abstract boolean isHelpRequest(final String[] args);

    protected MinimalParameters getParameters()
    {
        if (parameters == null)
        {
            parameters = new MinimalParameters(arguments, getName());
        }
        return parameters;
    }

    //
    // ICommand
    //

    @Override
    public final int execute(final String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        if (isHelpRequest(args))
        {
            return 0;
        }
        this.arguments = args;
        final String sessionToken = tryCheckAndGetSessionToken();
        if (sessionToken == null)
        {
            return 1;
        }
        final ICIFEXComponent serviceOrNull = tryGetComponent();
        if (serviceOrNull == null)
        {
            return 2;
        }
        new Timer("keep-alive ping", true).schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                serviceOrNull.checkSession(sessionToken);
            }
        }, ONE_MINUTE, ONE_MINUTE);
        return execute(sessionToken, serviceOrNull, args);
    }

}
