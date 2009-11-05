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
import java.util.regex.PatternSyntaxException;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A test that focuses on the creation of ColumnDefinitions to make sure that useful error messages
 * are provided. More comprehensive tests of the functionality in the context of validation are
 * found in the DataSetValidatorForTSVTest class.
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
    public void testZeroOrderKey()
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

    }

    @Test
    public void testNegativeOrderKey()
    {
        // Test negative case
        Properties props = new Properties();
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
    public void testValidOrderKey()
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
    @Test(groups = "broken")
    public void testValidSimpleHeaderPattern()
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
            assertEquals(
                    "According to column definition 'col' the following header is invalid: null",
                    ex.getMessage());
        }

        try
        {
            definition.assertValidHeader("");
            fail("Empty string header should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("According to column definition 'col' the following header is invalid: ",
                    ex.getMessage());
        }
        assertEquals(false, definition.isValidHeader(""));

        try
        {
            definition.assertValidHeader("abc");
            fail("A header of 'abc' should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "According to column definition 'col' the following header is invalid: abc", ex
                            .getMessage());
        }
        assertEquals(false, definition.isValidHeader("abc"));

        definition.assertValidHeader("ID");
        assertEquals(true, definition.isValidHeader("ID"));
    }

    // TODO 2009-11-04, Chandrasekhar: assertValidHeader throws a NullPointerException
    @Test(groups = "broken")
    public void testValidSimpleMandatoryHeaderPattern()
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
            assertEquals(
                    "According to column definition 'col' the following header is invalid: null",
                    ex.getMessage());
        }

        try
        {
            definition.assertValidHeader("");
            fail("Empty string header should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("According to column definition 'col' the following header is invalid: ",
                    ex.getMessage());
        }

        try
        {
            definition.assertValidHeader("abc");
            fail("A header of 'abc' should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "According to column definition 'col' the following header is invalid: abc", ex
                            .getMessage());
        }

        definition.assertValidHeader("ID");
        assertEquals(true, definition.isValidHeader("ID"));
    }

    @Test
    public void testInvalidRegexHeaderPattern()
    {
        Properties props = new Properties();
        props.setProperty(ColumnDefinition.HEADER_PATTERN_KEY, "ID{ab");
        try
        {
            ColumnDefinition.create("col", props);
            fail("The header pattern 'ID{ab' is not valid regex -- should raise PatternSyntaxException");
        } catch (PatternSyntaxException ex)
        {
            assertEquals("Illegal repetition near index 1\n" + "ID{ab\n" + " ^", ex.getMessage());
        }
    }

    @Test
    public void testValidRegexHeaderPattern()
    {
        Properties props = new Properties();
        props.setProperty(ColumnDefinition.HEADER_PATTERN_KEY, "ID[ab]+");
        ColumnDefinition definition = ColumnDefinition.create("col", props);

        try
        {
            definition.assertValidHeader("ID");
            fail("A header of 'ID' should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "According to column definition 'col' the following header is invalid: ID", ex
                            .getMessage());
        }

        try
        {
            definition.assertValidHeader("IDabc");
            fail("A header of 'IDabc' should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "According to column definition 'col' the following header is invalid: IDabc",
                    ex.getMessage());
        }

        definition.assertValidHeader("IDa");
        definition.assertValidHeader("IDab");
        definition.assertValidHeader("IDaba");
        definition.assertValidHeader("IDabbb");
    }

    @Test
    public void testInvalidValueValidatorClass()
    {
        Properties props = new Properties();
        props.setProperty(ColumnDefinition.VALUE_VALIDATOR_KEY, "NotARealClass");
        try
        {
            ColumnDefinition.create("col", props);
            fail("The value validator key 'NotARealClass' does not reference a valid class name -- should raise a ClassNotFoundException");
        } catch (CheckedExceptionTunnel ex)
        {
            assertEquals("java.lang.ClassNotFoundException: NotARealClass", ex.getMessage());
        }
    }

    @Test
    public void testInvalidValueValidatorType()
    {
        Properties props = new Properties();
        props.setProperty(DefaultValueValidatorFactory.VALUE_TYPE_KEY, "notarealtype");
        try
        {
            ColumnDefinition.create("col", props);
            fail("The value validator value type key 'notarealtype' does not reference a valid validator type -- should raise a ConfigurationFailureException");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Invalid value-type: notarealtype", ex.getMessage());
        }
    }

    @Test
    public void testNumericValueValidatorType()
    {
        Properties props = new Properties();
        props.setProperty(DefaultValueValidatorFactory.VALUE_TYPE_KEY, "numeric");
        ColumnDefinition definition = ColumnDefinition.create("col", props);

        IValidator valueValidator = definition.createValidator();
        try
        {
            valueValidator.assertValid(null);
            fail("A value of null should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("Empty value is not allowed.", ex.getMessage());
        }

        try
        {
            valueValidator.assertValid("");
            fail("An empty value should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("Empty value is not allowed.", ex.getMessage());
        }

        try
        {
            valueValidator.assertValid("abc");
            fail("A value of 'abc' should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("Not a number: abc", ex.getMessage());
        }

        valueValidator.assertValid("1");
        valueValidator.assertValid("-20");
        valueValidator.assertValid("6.28");
        valueValidator.assertValid("107.51f");
        valueValidator.assertValid("-9864.21d");
    }

    @Test
    public void testDefaultStringValueValidatorType()
    {

        ColumnDefinition definition;
        Properties props;
        IValidator valueValidator;

        props = new Properties();
        props.setProperty(DefaultValueValidatorFactory.VALUE_TYPE_KEY, "string");
        try
        {
            ColumnDefinition.create("col", props);
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key 'value-pattern' not found in properties '[value-type]'", ex
                    .getMessage());
        }

        props = new Properties();
        props.setProperty(DefaultValueValidatorFactory.VALUE_TYPE_KEY, "string");
        props.setProperty(StringValidatorFactory.VALUE_PATTERN_KEY, "[0-9]+");
        definition = ColumnDefinition.create("col", props);
        valueValidator = definition.createValidator();
        try
        {
            valueValidator.assertValid(null);
            fail("A value of null should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("Empty value is not allowed.", ex.getMessage());
        }

        try
        {
            valueValidator.assertValid("");
            fail("An empty value should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("Empty value is not allowed.", ex.getMessage());
        }

        try
        {
            valueValidator.assertValid("abc");
            fail("A value of 'abc' should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("'abc' doesn't match the following regular expression: [0-9]+", ex
                    .getMessage());
        }

        valueValidator.assertValid("1");
        valueValidator.assertValid("12837");

    }

    @Test
    public void testStringWithEmptyValuesValueValidatorType()
    {

        ColumnDefinition definition;
        Properties props;
        IValidator valueValidator;

        props = new Properties();
        props.setProperty(DefaultValueValidatorFactory.VALUE_TYPE_KEY, "string");
        props.setProperty(StringValidatorFactory.VALUE_PATTERN_KEY, ".+ :.*");
        props.setProperty(AbstractValidatorFactory.ALLOW_EMPTY_VALUES_KEY, "true");
        definition = ColumnDefinition.create("col", props);
        valueValidator = definition.createValidator();

        try
        {
            valueValidator.assertValid("abc");
            fail("A value of 'abc' should raise UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("'abc' doesn't match the following regular expression: .+ :.*", ex
                    .getMessage());
        }

        valueValidator.assertValid(null);
        valueValidator.assertValid("");

        valueValidator.assertValid("A : bc");
        valueValidator.assertValid("Hello : World");
        valueValidator.assertValid("Hello :");
    }
}
