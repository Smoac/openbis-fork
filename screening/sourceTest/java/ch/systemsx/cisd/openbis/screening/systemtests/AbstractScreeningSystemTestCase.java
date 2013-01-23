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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.io.File;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.SystemTestCase;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;

/**
 * System test case for screening. Starts both AS and DSS.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractScreeningSystemTestCase extends SystemTestCase
{

    /**
     * Return the location of the openBIS application context config.
     */
    @Override
    protected String getApplicationContextLocation()
    {
        return "classpath:screening-applicationContext.xml";
    }

    /**
     * sets up the openbis database to be used by the tests.
     */
    @Override
    protected void setUpDatabaseProperties()
    {
        TestInitializer.initEmptyDbWithIndex();
    }

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-" + getClass().getSimpleName());
    }

}
