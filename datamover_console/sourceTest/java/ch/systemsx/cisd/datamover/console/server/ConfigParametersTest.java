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

package ch.systemsx.cisd.datamover.console.server;

import static ch.systemsx.cisd.datamover.console.server.ConfigParameters.DATAMOVERS;
import static ch.systemsx.cisd.datamover.console.server.ConfigParameters.LOCATION;
import static ch.systemsx.cisd.datamover.console.server.ConfigParameters.REFRESH_TIME_INTERVAL;
import static ch.systemsx.cisd.datamover.console.server.ConfigParameters.TARGETS;
import static ch.systemsx.cisd.datamover.console.server.ConfigParameters.WORKING_DIRECTORY;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for corresponding {@link ConfigParameters} class.
 * 
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ConfigParameters.class)
public class ConfigParametersTest
{
    @Test
    public void testEmptyProperties()
    {
        try
        {
            new ConfigParameters(new Properties());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException e)
        {
            assertEquals("Given key '" + TARGETS + "' not found in properties '[]'", e.getMessage());
        }
    }

    @Test
    public void testMissingDatamovers()
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty(TARGETS, "1");
            properties.setProperty("1." + LOCATION, "/target1");
            new ConfigParameters(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException e)
        {
            assertEquals("Given key '" + DATAMOVERS + "' not found in properties '[" + TARGETS
                    + ", 1." + LOCATION + "]'", e.getMessage());
        }
    }

    @Test
    public void testEmptyDatamovers()
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty(TARGETS, "1");
            properties.setProperty("1." + LOCATION, "/target1");
            properties.setProperty(DATAMOVERS, "");
            new ConfigParameters(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException e)
        {
            assertEquals("Property '" + DATAMOVERS + "' is an empty string.", e.getMessage());
        }
    }

    @Test
    public void testEmptyTargets()
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty(TARGETS, "");
            properties.setProperty(DATAMOVERS, "");
            new ConfigParameters(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException e)
        {
            assertEquals("Property '" + TARGETS + "' is an empty string.", e.getMessage());
        }
    }

    @Test
    public void testProperConfigParameters()
    {
        Properties properties = new Properties();
        properties.setProperty(TARGETS, "t1 t2");
        properties.setProperty("t1." + LOCATION, "/target1  ");
        properties.setProperty("t2." + LOCATION, "/target2");
        properties.setProperty(DATAMOVERS, "dm1 dm2");
        properties.setProperty("dm1." + WORKING_DIRECTORY, "wd1");
        properties.setProperty("dm2." + WORKING_DIRECTORY, "wd2");
        ConfigParameters configParameters = new ConfigParameters(properties);

        assertEquals(60000, configParameters.getRefreshTimeInterval());
        assertEquals("{t1=/target1, t2=/target2}", configParameters.getTargets().toString());
        assertEquals("{dm1=" + FileUtilities.getCanonicalPath(new File("wd1")) + ", dm2="
                + FileUtilities.getCanonicalPath(new File("wd2")) + "}", configParameters
                .getDatamoversWorkingDirectories().toString());
    }

    @Test
    public void testProperConfigParametersWithRefreshTimeInterval()
    {
        Properties properties = new Properties();
        properties.setProperty(REFRESH_TIME_INTERVAL, "10");
        properties.setProperty(TARGETS, "t");
        properties.setProperty("t." + LOCATION, "/target");
        properties.setProperty(DATAMOVERS, "dm");
        properties.setProperty("dm." + WORKING_DIRECTORY, "wd");
        ConfigParameters configParameters = new ConfigParameters(properties);

        assertEquals(10000, configParameters.getRefreshTimeInterval());
    }
}
