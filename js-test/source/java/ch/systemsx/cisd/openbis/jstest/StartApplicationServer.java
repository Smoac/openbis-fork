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

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author anttil
 */
public class StartApplicationServer
{

    public static void go() throws Exception
    {
        Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    Server server = new Server(20000);

                    WebAppContext context = new WebAppContext();
                    context.setDescriptor("targets/www/ch.systemsx.cisd.openbis.plugin.screening.OpenBIS/WEB-INF/web.xml");
                    context.setResourceBase("targets/www/ch.systemsx.cisd.openbis.plugin.screening.OpenBIS");
                    context.setContextPath("/openbis");
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

        Thread t = new Thread(r);
        //t.setDaemon(true);
        t.start();
    }

}
