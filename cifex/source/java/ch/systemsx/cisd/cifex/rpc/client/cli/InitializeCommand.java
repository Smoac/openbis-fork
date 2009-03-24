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

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A command that initializes this client for a given server.
 * 
 * @author Bernd Rinn
 */
public class InitializeCommand extends AbstractCommand
{

    private static final String NAME = "init";

    private static InitializeCommand instance;

    /** Not to be instantiated outside of this class. */
    private InitializeCommand()
    {
        super(NAME);
    }

    /** Returns the unique instance of this class. */
    public final static synchronized InitializeCommand getInstance()
    {
        if (instance == null)
        {
            instance = new InitializeCommand();
        }
        return instance;
    }

    public int execute(String[] arguments) throws UserFailureException, EnvironmentFailureException
    {
        try
        {
            System.out.println("This command initializes the client for a given server.");
            String baseURL;
            baseURL =
                    getConsoleReader().readLine("Server Base URL (e.g. 'https://myserver:8443'): ");
            if (StringUtils.isBlank(baseURL))
            {
                System.err.println("No server base URL has been specified.");
                System.exit(1);
            }
            // Remove trailing /, if any.
            if (baseURL.endsWith("/"))
            {
                baseURL = baseURL.substring(0, baseURL.length() - 1);
            }
            // Fix port 
            final URL url = new URL(baseURL);
            if (url.getPort() < 0)
            {
                if ("https".equals(url.getProtocol()))
                {
                    baseURL += ":443";
                } else if ("http".equals(url.getProtocol()))
                {
                    baseURL += ":80";
                } else
                {
                    System.err.println("URL does not contain given.");
                    System.exit(1);
                }
            }
            // Write Base URL to file.
            FileUtils.writeStringToFile(getBaseURLFile(), baseURL);

            // Initialize Trust Store.
            if (tryGetService(baseURL, true) == null)
            {
                return 2;
            }
            return 0;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
