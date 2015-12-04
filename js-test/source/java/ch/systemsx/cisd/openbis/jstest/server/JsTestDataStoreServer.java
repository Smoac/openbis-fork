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

package ch.systemsx.cisd.openbis.jstest.server;

import java.io.File;
import java.io.IOException;

import ch.systemsx.cisd.openbis.test.server.TestDataStoreServer;

/**
 * @author pkupczyk
 */
public abstract class JsTestDataStoreServer extends TestDataStoreServer
{

    @Override
    protected String getCommand()
    {
        String classpath = System.getProperty("selenium.dss-runtime-classpath");
        if (classpath == null || classpath.length() == 0) {
            classpath = System.getProperty("java.class.path");
        }
        
        try
        {
            File extraClassPath = new File("../../../targets/classes");
            classpath += ":" + (extraClassPath).getCanonicalPath();
        } catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("Extra classpath missing, check JsTestDataStoreServer for details.");
            System.exit(-1);
        }
        
        return "java -ea -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address="
                    + getDebugPort()
                    + " -Dfile.encoding=UTF-8 -classpath "+ classpath +" ch.systemsx.cisd.openbis.dss.generic.DataStoreServer";
    }
}
