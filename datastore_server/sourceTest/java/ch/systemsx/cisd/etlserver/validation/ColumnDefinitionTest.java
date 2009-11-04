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

package ch.systemsx.cisd.etlserver.validation;

import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ColumnDefinitionTest extends AssertJUnit
{
    @Test
    public void testDefaultColumnDefinition()
    {
        ColumnDefinition definition = ColumnDefinition.create("col", new Properties());
        
        IValidator validator = definition.createValidator();
        validator.assertValid(null);
        validator.assertValid("");
        validator.assertValid("abc");
        
        definition.assertValidHeader("blabla");
        assertEquals(true, definition.isValidHeader("blabla"));
        definition.assertValidHeader("");
        assertEquals(true, definition.isValidHeader(""));
        
        assertEquals("col", definition.getName());
        assertEquals(false, definition.isMandatory());
        assertEquals(null, definition.getOrderOrNull());
    }
    
}
