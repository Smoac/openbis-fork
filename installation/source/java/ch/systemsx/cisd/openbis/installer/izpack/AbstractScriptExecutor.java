/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.izforge.izpack.api.data.AutomatedInstallData;

/**
 * Abstract class that can execute admin scripts as part of the installation.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractScriptExecutor
{
    protected static final String INSTALL_BIN_PATH_VARNAME = "INSTALL_BIN_PATH";

    protected String getAdminScript(AutomatedInstallData data, String scriptFileName)
    {
        File adminScriptFile = getAdminScriptFile(data, scriptFileName);
        return adminScriptFile.getAbsolutePath();
    }

    protected File getAdminScriptFile(AutomatedInstallData data, String scriptFileName)
    {
        return new File(data.getVariable(INSTALL_BIN_PATH_VARNAME), scriptFileName);
    }

    protected void executeAdminScript(Map<String, String> customEnv, String... command)
    {
        executeAdminScript(customEnv, System.out, System.err, command);
    }

    protected void executeAdminScript(Map<String, String> customEnv, OutputStream out,
            OutputStream err, String... command)
    {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().putAll(System.getenv());
        if (customEnv != null)
        {
            pb.environment().putAll(customEnv);
        }
        try
        {
            Process process = pb.start();
            pipe(process.getErrorStream(), err);
            pipe(process.getInputStream(), out);
            int returnValue = process.waitFor();
            if (returnValue != 0)
            {
                System.err.println("Executing of command " + pb.command()
                        + " has failed. Aborting ...");
                System.exit(returnValue);
            }
        } catch (Exception e)
        {
            System.out.println("Error executing " + command[0] + ": " + e.getMessage());
        }
    }
    

    private void pipe(final InputStream src, final OutputStream dest)
    {
        new Thread(new Runnable() 
        {
            @Override
            public void run() 
            {
                try 
                {
                    byte[] buffer = new byte[1024];
                    for (int n = 0; n != -1; n = src.read(buffer)) 
                    {
                        dest.write(buffer, 0, n);
                    }
                } catch (IOException e) 
                { 
                }
            }
            }).start();
    }

}
