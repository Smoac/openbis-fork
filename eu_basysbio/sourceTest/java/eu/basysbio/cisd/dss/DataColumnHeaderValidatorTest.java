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

import static eu.basysbio.cisd.dss.DataColumnHeaderValidator.ELEMENTS_KEY;
import static eu.basysbio.cisd.dss.DataColumnHeaderValidator.PATTERN_KEY;
import static eu.basysbio.cisd.dss.DataColumnHeaderValidator.TERMS_KEY;
import static eu.basysbio.cisd.dss.DataColumnHeaderValidator.TYPE_INTEGER;
import static eu.basysbio.cisd.dss.DataColumnHeaderValidator.TYPE_KEY;
import static eu.basysbio.cisd.dss.DataColumnHeaderValidator.TYPE_STRING;
import static eu.basysbio.cisd.dss.DataColumnHeaderValidator.TYPE_VOCABULARY;

import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.etlserver.validation.Result;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataColumnHeaderValidatorTest extends AssertJUnit
{
    private DataColumnHeaderValidator validator;

    @BeforeMethod
    public void beforeMethod()
    {
        Properties properties = new Properties();
        properties.setProperty(ELEMENTS_KEY, "a, b, c");
        properties.setProperty("a." + TYPE_KEY, TYPE_STRING);
        properties.setProperty("a." + PATTERN_KEY, "a[0-9]+");
        properties.setProperty("b." + TYPE_KEY, TYPE_VOCABULARY);
        properties.setProperty("b." + TERMS_KEY, "alpha, beta, gamma");
        properties.setProperty("c." + TYPE_KEY, TYPE_INTEGER);
        validator = new DataColumnHeaderValidator(properties);
    }
    
    @Test
    public void testUnrestricted()
    {
        Result result = new DataColumnHeaderValidator(new Properties()).validateHeader("blabla");
        
        assertEquals(true, result.isValid());
    }
    
    @Test
    public void testMissingElementValidatorDefinition()
    {
        Properties properties = new Properties();
        properties.setProperty(ELEMENTS_KEY, "a, b");
        
        try
        {
            new DataColumnHeaderValidator(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Missing property '" + TYPE_KEY
                    + "' for element 'a' of data column header validator.", ex.getMessage());
        }
    }
    
    @Test
    public void testUnknownValidatorType()
    {
        Properties properties = new Properties();
        properties.setProperty(ELEMENTS_KEY, "a");
        properties.setProperty("a." + TYPE_KEY, "blabla");
        
        try
        {
            new DataColumnHeaderValidator(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Unknown validator type 'blabla' "
                    + "for element 'a' of data column header validator.", ex.getMessage());
        }
    }
    
    @Test
    public void testMissingTermsOfVocabularyValidatorDefinition()
    {
        Properties properties = new Properties();
        properties.setProperty(ELEMENTS_KEY, "a");
        properties.setProperty("a." + TYPE_KEY, TYPE_VOCABULARY);
        
        try
        {
            new DataColumnHeaderValidator(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in validator definition for element 'a' "
                    + "of data column header validator: Given key '" + TERMS_KEY
                    + "' not found in properties '[" + TYPE_KEY + "]'", ex.getMessage());
        }
    }
    
    @Test
    public void testDuplicatedTermsOfVocabularyValidatorDefinition()
    {
        Properties properties = new Properties();
        properties.setProperty(ELEMENTS_KEY, "a");
        properties.setProperty("a." + TYPE_KEY, TYPE_VOCABULARY);
        properties.setProperty("a." + TERMS_KEY, "alpha, alpha");
        
        try
        {
            new DataColumnHeaderValidator(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in validator definition for element 'a' "
                    + "of data column header validator: Duplicated name 'alpha' in '" + TERMS_KEY
                    + "' property.", ex.getMessage());
        }
    }
    
    @Test
    public void testMissingPatternOfStringValidatorDefinition()
    {
        Properties properties = new Properties();
        properties.setProperty(ELEMENTS_KEY, "a");
        properties.setProperty("a." + TYPE_KEY, TYPE_STRING);
        
        try
        {
            new DataColumnHeaderValidator(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in validator definition for element 'a' "
                    + "of data column header validator: Given key '" + PATTERN_KEY
                    + "' not found in properties '[" + TYPE_KEY + "]'", ex.getMessage());
        }
    }
    
    @Test
    public void testInvalidPatternOfStringValidatorDefinition()
    {
        Properties properties = new Properties();
        properties.setProperty(ELEMENTS_KEY, "a");
        properties.setProperty("a." + TYPE_KEY, TYPE_STRING);
        properties.setProperty("a." + PATTERN_KEY, "[?");
        
        try
        {
            new DataColumnHeaderValidator(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in validator definition for element 'a' "
                    + "of data column header validator: Invalid regular expression: [?", ex
                    .getMessage());
        }
    }
    
    @Test
    public void testValidHeaders()
    {
        assertEquals(Result.OK, validator.validateHeader("a42::beta::-34"));
        assertEquals(Result.OK, validator.validateHeader("a4711::gamma::0"));
        assertEquals(Result.OK, validator.validateHeader("a0::alpha::+42::additional element"));
    }
    
    @Test
    public void testNotEnoughHeaders()
    {
        assertInvalid("3 elements separated by '::' expected instead of only 2.", "a42::beta");
    }
    
    @Test
    public void testInvalidVocabularyTypeHeaderElement()
    {
        assertInvalid("Element 'Beta' is invalid: It is not a term from the following vocabulary: "
                + "[gamma, beta, alpha]", "a42::Beta::12");
    }
    
    @Test
    public void testInvalidStringTypeHeaderElement()
    {
        assertInvalid("Element 'A42' is invalid: It does not match the following regular expression: "
                + "a[0-9]+", "A42::beta::12");
    }
    
    @Test
    public void testInvalidIntegerTypeHeaderElement()
    {
        assertInvalid("Element '1.2' is invalid: It is not an integer number.", "a42::beta::1.2");
    }
    
    private void assertInvalid(String expectedFailure, String header)
    {
        assertEquals(expectedFailure, validator.validateHeader(header).toString());
    }
    
}
