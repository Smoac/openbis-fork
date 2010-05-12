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

package ch.systemsx.cisd.openbis.dss.client.api.cli;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.args4j.Argument;
import ch.systemsx.cisd.args4j.Option;

/**
 * Command line arguments for the dss command. The format is:
 * <p>
 * <code>
 * [options] DATA_SET_CODE
 * </code>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class GlobalArguments
{
    @Option(name = "u", longName = "username", usage = "User login name (required)")
    protected String username = "";

    @Option(name = "p", longName = "password", usage = "User login password (required)")
    protected String password = "";

    @Option(name = "s", longName = "server-base-url", usage = "URL for openBIS Server (required)")
    protected String serverBaseUrl = "";

    @Option(name = "h", longName = "help", skipForExample = true)
    protected boolean isHelp = false;

    @Argument
    protected List<String> arguments = new ArrayList<String>();

    public boolean isHelp()
    {
        return isHelp;
    }

    public void setHelp(boolean isHelp)
    {
        this.isHelp = isHelp;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getServerBaseUrl()
    {
        return serverBaseUrl;
    }

    public void setServerBaseUrl(String serverBaseUrl)
    {
        this.serverBaseUrl = serverBaseUrl;
    }

    public String getDataSetCode()
    {
        List<String> args = getArguments();
        if (args.size() < 1)
        {
            return "";
        }
        return args.get(0);
    }

    public String getRequestedPath()
    {
        List<String> args = getArguments();
        String path;
        if (args.size() < 2)
        {
            path = "/";
        } else
        {
            path = args.get(1);
        }
        return path;
    }

    public List<String> getArguments()
    {
        return arguments;
    }

    /**
     * Check that the arguments make sense.
     */
    public boolean isComplete()
    {
        if (isHelp)
        {
            return true;
        }

        // At the moment, username, passowrd, and server base url should all be non-empty
        if (username.length() < 1)
        {
            return false;
        }

        if (password.length() < 1)
        {
            return false;
        }
        if (serverBaseUrl.length() < 1)
        {
            return false;
        }
        if (getDataSetCode().length() < 1)
        {
            return false;
        }

        return true;

    }
}
