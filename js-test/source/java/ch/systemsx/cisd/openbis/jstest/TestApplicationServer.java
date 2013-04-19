/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.jstest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author anttil
 */
public abstract class TestApplicationServer
{

    private boolean deamon;

    protected abstract int getPort();

    protected abstract String getWebXmlPath();

    protected abstract String getRootPath();

    protected abstract String getContextPath();

    protected abstract String getDatabaseDumpFolderPathOrNull();

    public void setDeamon(boolean deamon)
    {
        this.deamon = deamon;
    }

    public boolean isDeamon()
    {
        return deamon;
    }

    public void start() throws Exception
    {
        TestServerUtil.maybeRestoreDumps(getDatabaseDumpFolderPathOrNull());

        Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    Server server = new Server(getPort());

                    WebAppContext context = new WebAppContext();
                    context.setDescriptor(getWebXmlPath());
                    context.setResourceBase(getRootPath());
                    context.setContextPath(getContextPath());
                    context.setParentLoaderPriority(true);

                    server.setHandler(context);
                    try
                    {
                        server.start();
                        server.join();
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            };

        PrintStream originalOut = System.out;

        PipedOutputStream outpipe = new PipedOutputStream();
        PipedInputStream inpipe = new PipedInputStream(outpipe);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inpipe));
        PrintStream newOut = new PrintStream(outpipe);
        System.setOut(newOut);

        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();

        String line;
        while ((line = reader.readLine()) != null)
        {
            originalOut.println(line);

            if (line.contains("SERVER STARTED"))
            {
                originalOut.println("SERVER START DETECTED");
                break;
            }
        }
        outpipe.close();
        inpipe.close();
        reader.close();
        newOut.close();

        System.setOut(originalOut);
    }

}
