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

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A test that focuses on the creation of ColumnDefinitions to make sure that useful error messages are provided.
 * More comprehensive tests of the functionality in the context of validation are found in the DataSetValidatorForTSVTest class.
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

    @Test
    public void testInvalidOrderKeyColumnDefinition()
    {
        // Combine tests of 0 and negative order keys into one test method

        // Test 0 case
        Properties props = new Properties();
        props.setProperty(ColumnDefinition.ORDER_KEY, "0");
        try
        {
            ColumnDefinition.create("col", props);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Order value has to be positive: " + "0", ex.getMessage());
        }

        // Test negative case
        props = new Properties();
        props.setProperty(ColumnDefinition.ORDER_KEY, "-1");
        try
        {
            ColumnDefinition.create("col", props);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Order value has to be positive: " + "-1", ex.getMessage());
        }
    }
    
    @Test
    public void testValidOrderKeyColumnDefinition()
    {
        Properties props = new Properties();
        props.setProperty(ColumnDefinition.ORDER_KEY, "1");
        ColumnDefinition definition = ColumnDefinition.create("col", props);
        
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
        assertEquals(new Integer(1), definition.getOrderOrNull());
    }
    
    // TODO 2009-11-04, Chandrasekhar: assertValidHeader throws a NullPointerException
    @Test(groups="broken")
    public void testValidSimpleHeaderPatternColumnDefinition()
    {
        Properties props = new Properties();
        props.setProperty(ColumnDefinition.HEADER_PATTERN_KEY, "ID");
        ColumnDefinition definition = ColumnDefinition.create("col", props);
        
        // We did not specify anything about the value -- these should all pass
        IValidator valueValidator = definition.createValidator();
        valueValidator.assertValid(null);
        valueValidator.assertValid("");
        valueValidator.assertValid("abc");
        
        try
        {
            definition.assertValidHeader(null);
            fail("null header should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("According to column definition 'col' the following header is invalid: null", ex.getMessage());
        }
        
        try
        {
            definition.assertValidHeader("");
            fail("Empty string header should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("According to column definition 'col' the following header is invalid: ", ex.getMessage());
        }
        assertEquals(false, definition.isValidHeader(""));
        
        try
        {
            definition.assertValidHeader("abc");
            fail("A header of 'abc' should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("According to column definition 'col' the following header is invalid: abc", ex.getMessage());
        }
        assertEquals(false, definition.isValidHeader("abc"));
        
        definition.assertValidHeader("ID");
        assertEquals(true, definition.isValidHeader("ID"));
    }
    
    // TODO 2009-11-04, Chandrasekhar: assertValidHeader throws a NullPointerException
    @Test(groups="broken")
    public void testValidSimpleMandatoryHeaderPatternColumnDefinition()
    {
        Properties props = new Properties();
        props.setProperty(ColumnDefinition.HEADER_PATTERN_KEY, "ID");
        props.setProperty(ColumnDefinition.MANDATORY_KEY, "true");
        ColumnDefinition definition = ColumnDefinition.create("col", props);
        
        // We did not specify anything about the value -- these should all pass
        IValidator valueValidator = definition.createValidator();
        valueValidator.assertValid(null);
        valueValidator.assertValid("");
        valueValidator.assertValid("abc");
        
        try
        {
            definition.assertValidHeader(null);
            fail("null header should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("According to column definition 'col' the following header is invalid: null", ex.getMessage());
        }
        
        try
        {
            definition.assertValidHeader("");
            fail("Empty string header should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("According to column definition 'col' the following header is invalid: ", ex.getMessage());
        }
        
        try
        {
            definition.assertValidHeader("abc");
            fail("A header of 'abc' should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("According to column definition 'col' the following header is invalid: abc", ex.getMessage());
        }

        definition.assertValidHeader("ID");
        assertEquals(true, definition.isValidHeader("ID"));
    }
}
