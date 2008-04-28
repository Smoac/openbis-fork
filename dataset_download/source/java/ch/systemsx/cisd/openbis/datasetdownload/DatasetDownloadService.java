/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datasetdownload;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;

/**
 * Main class of the service. Starts up jetty with {@link DatasetDownloadServlet}.
 *
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadService
{
    public static void main(String[] args) throws Exception 
    {
        Server server = new Server(8081);
        Context context = new Context(server, "/", Context.SESSIONS);
        context.addServlet(DatasetDownloadServlet.class, "/dataset-download");
        server.start();
    }
}
