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
import ch.systemsx.cisd.common.test.AssertionUtil;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class NumericValidatorFactoryTest extends AssertJUnit
{ 
    @Test
    public void testInvalidRange()
    {
        try
        {
            createValidator("[]");
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Invalid range: []", ex.getMessage());
        }
    }
    
    @Test
    public void testInvalidNumber()
    {
        NumericValidatorFactory validatorFactory = new NumericValidatorFactory(new Properties());
        IValidator validator = validatorFactory.createValidator();
        
        assertNotANumber(validator, "abc");
        assertNotANumber(validator, "-0-");
        assertNotANumber(validator, "");
    }
    
    @Test
    public void testNoRange()
    {
        NumericValidatorFactory validatorFactory = new NumericValidatorFactory(new Properties());
        IValidator validator = validatorFactory.createValidator();
        
        validator.assertValid("-Infinity");
        validator.assertValid("-1");
        validator.assertValid("-100");
        validator.assertValid("-1.78e-9");
        validator.assertValid("100.45");
        validator.assertValid("1.78e19");
        validator.assertValid("Infinity");
    }
    
    @Test
    public void testNegativeNumbers()
    {
        IValidator validator = createValidator("(-Infinity,0)");
        
        validator.assertValid("-1");
        validator.assertValid("-100");
        validator.assertValid("-1.78e-9");
        assertFailingToSmall("<= -Infinity", validator, "-Infinity");
        assertFailingToLarge(">= 0.0", validator, "0.0");
    }

    @Test
    public void testNonNegativeNumbers()
    {
        IValidator validator = createValidator("[ 0 , Infinity )");
        
        validator.assertValid("1");
        validator.assertValid("100");
        validator.assertValid("1.78e-9");
        validator.assertValid("0");
        assertFailingToSmall("< 0.0", validator, "-1.0E-9");
        assertFailingToLarge(">= Infinity", validator, "Infinity");
    }
    
    private IValidator createValidator(String range)
    {
        Properties properties = new Properties();
        properties.setProperty(NumericValidatorFactory.VALUE_RANGE_KEY, range);
        NumericValidatorFactory validatorFactory = new NumericValidatorFactory(properties);
        return validatorFactory.createValidator();
    }
    
    private void assertNotANumber(IValidator validator, String string)
    {
        try
        {
            validator.assertValid(string);
            fail("NumberFormatException expected");
        } catch (NumberFormatException ex)
        {
            AssertionUtil.assertContains(string, ex.getMessage());
        }
    }
    
    private void assertFailingToSmall(String expectedPostfix, IValidator validator, String number)
    {
        try
        {
            validator.assertValid(number);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Number to small: " + number + " " + expectedPostfix, ex.getMessage());
        }
    }
    
    private void assertFailingToLarge(String expectedPostfix, IValidator validator, String number)
    {
        try
        {
            validator.assertValid(number);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Number to large: " + number + " " + expectedPostfix, ex.getMessage());
        }
    }
}
