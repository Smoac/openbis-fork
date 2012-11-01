/*
 * Copyright 2009 ETH Zuerich, CISD
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

package eu.basysbio.cisd.dss;

import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetTypeTranslatorTest extends AssertJUnit
{
    @Test
    public void testMissingTypes()
    {
        try
        {
            new DataSetTypeTranslator(new Properties());
            fail("ConfigurationFailureException");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + DataSetTypeTranslator.DATA_SET_TYPES_KEY
                    + "' not found in properties '[]'", ex.getMessage());
        }
    }
    
    @Test
    public void test()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetTypeTranslator.DATA_SET_TYPES_KEY, "a, b");
        properties.setProperty("a", "alpha");
        DataSetTypeTranslator translator = new DataSetTypeTranslator(properties);
        
        assertEquals("alpha", translator.translate("a"));
        assertEquals("B", translator.translate("b"));
        try
        {
            translator.translate("B");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown data set type: B", ex.getMessage());
        }
    }
}
