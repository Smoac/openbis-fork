/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.config;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Tests for {@link Configuration}.
 *
 * @author Franz-Josef Elmer
 */
public class ConfigurationTest
{
    private static final class MockConfiguration implements IConfigurationDataProvider
    {
        private final Map<String, String> map;

        MockConfiguration(Map<String, String> map)
        {
            this.map = map;
        }
        
        public String getProperty(String key)
        {
            return map.get(key);
        }
    }
    
    @Test
    public void testGetStringProperty()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("greetings", "hello world");
        Configuration configuration = new Configuration(new MockConfiguration(map));
        
        assertEquals("hello world", configuration.getStringProperty("greetings"));
    }
    
    @Test
    public void testGetUndefinedStringProperty()
    {
        Map<String, String> map = new HashMap<String, String>();
        Configuration configuration = new Configuration(new MockConfiguration(map));

        try
        {
            configuration.getStringProperty("greetings");
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException e)
        {
            String message = e.getMessage();
            assertTrue(message, message.indexOf("greetings") >= 0);
        }
    }
    
    @Test
    public void testGetStringPropertyWithDefault()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("greetings", "hello world");
        Configuration configuration = new Configuration(new MockConfiguration(map));
        
        assertEquals("default", configuration.getStringProperty("blabla", "default"));
        assertEquals("hello world", configuration.getStringProperty("greetings", "default"));
    }
    
}
