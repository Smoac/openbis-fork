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

package ch.systemsx.cisd.openbis.generic.client.console;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jline.ConsoleReader;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AdminConsole
{
    private static final String SERVICE_PATH = "/rmi-common";


    public static void main(String[] args) throws Exception
    {
        if (args.length < 3)
        {
            System.err.println("Usage: java -jar openbis-admin-console.jar "
                    + " <server url> <script path> <user ID> [<password>]");
            System.exit(1);
        }
        String serverURL = args[0];
        String script = args[1];
        String userID = args[2];
        
        String password;
        if (args.length == 4)
        {
            password =args[3];
        } else
        {
            password = getConsoleReader().readLine("Password: ", Character.valueOf('*'));
        }
        ICommonServer service = HttpInvokerUtils.createServiceStub(ICommonServer.class, serverURL + SERVICE_PATH, 5);

        SessionContextDTO session = service.tryToAuthenticate(userID, password);
        if (session == null)
        {
            System.err.println("Authentication failed");
        } else
        {
            System.out.println("Run script " + script);
            Map<String, ICommand> commands = createCommands();
            String sessionToken = session.getSessionToken();
            List<String> lines = FileUtilities.loadToStringList(new File(script));
            ScriptContext context = new ScriptContext();
            for (int i = 0; i < lines.size(); i++)
            {
                String line = lines.get(i).trim();
                if (line.length() > 0 && line.startsWith("--") == false)
                {
                    int indexOfSpace = line.indexOf(' ');
                    String command = indexOfSpace < 0 ? line : line.substring(0, indexOfSpace);
                    String argument = indexOfSpace < 0 ? "" : line.substring(indexOfSpace).trim();
                    argument = context.resolveVariables(argument);
                    ICommand cmd = commands.get(command);
                    if (cmd == null)
                    {
                        throw createException(i, line, "Unknown command: " + command);
                    }
                    try
                    {
                        cmd.execute(service, sessionToken, context, argument);
                    } catch (RuntimeException ex)
                    {
                        throw createException(i, line, ex);
                    }
                }
            }
            service.logout(sessionToken);
        }
    }
    
    private static UserFailureException createException(int lineIndex, String line, Object reason)
    {
        String message = "Error in line " + (lineIndex + 1) + " [" + line
                + "]\nReason: " + reason;
        if (reason instanceof Throwable)
        {
            Throwable throwable = (Throwable) reason;
            return new UserFailureException(message, throwable);
        }
        return new UserFailureException(message);
    }

    private static ConsoleReader getConsoleReader()
    {
        try
        {
            return new ConsoleReader();
        } catch (final IOException ex)
        {
            throw new EnvironmentFailureException("ConsoleReader could not be instantiated.",
                    ex);
        }
    }
    
    private static Map<String, ICommand> createCommands()
    {
        HashMap<String, ICommand> map = new HashMap<String, ICommand>();
        map.put("register-property-type", new RegisterPropertyType());
        map.put("register-type", new RegisterType());
        map.put("assign-to", new Assignment());
        map.put("set", new Set());
        return map;
    }
}
