/*
 * Copyright 2013 ETH Zuerich, CISD
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

/**
 * @author pkupczyk
 */
public class JsTest
{

    public static void main(String[] args) throws Exception
    {
        boolean deamon = args.length > 0 && "true".equals(args[0]);

        JsTestApplicationServer as = new JsTestApplicationServer();
        as.setDeamon(deamon);

        JsTestDataStoreServer dss1 = new JsTestDataStoreServer();
        dss1.setName("DSS1");
        dss1.setRootPath("../datastore_server");
        dss1.setDumpsPath("../datastore_server/db");
        dss1.setDeamon(deamon);
        dss1.setDebugPort(20011);

        JsTestDataStoreServer dss2 = new JsTestDataStoreServer();
        dss2.setName("DSS2");
        dss2.setRootPath("../datastore_server2");
        dss2.setDumpsPath("../datastore_server2/db");
        dss2.setDeamon(deamon);
        dss2.setDebugPort(20012);

        as.start();
        dss1.start();
        dss2.start();
    }

}
