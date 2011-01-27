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

package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedProperty;

/**
 * 
 *
 * @author felmer
 */
public class ManagedPropertyEvaluatorTest extends AssertJUnit
{
    @Test
    public void testAssertBatchColumnNames()
    {
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("", "");
        ManagedPropertyEvaluator.assertBatchColumnNames("p", Arrays.<String>asList(), bindings);
        bindings.clear();
        bindings.put("A", "alpha");
        try
        {
            ManagedPropertyEvaluator.assertBatchColumnNames("p", Arrays.<String>asList(), bindings);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No subcolumns expected for property 'p': [A]", ex.getMessage());
        }
        try
        {
            ManagedPropertyEvaluator.assertBatchColumnNames("p", Arrays.<String>asList("A", "B"), bindings);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Following columns are missed: [p:B]", ex.getMessage());
        }
        bindings.put("B", "beta");
        ManagedPropertyEvaluator.assertBatchColumnNames("p", Arrays.<String>asList("A", "B"), bindings);
    }
    
    @Test
    public void testEmptyScript()
    {
        assertEquals(0, new ManagedPropertyEvaluator("").getBatchColumnNames().size());
    }
    
    @Test
    public void testScriptWithSyntaxError()
    {
        try
        {
            new ManagedPropertyEvaluator("a =");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("SyntaxError: ('invalid syntax', ('<string>', 1, 4, 'a ='))",
                    ex.getMessage());
        }
    }

    @Test
    public void testScriptWithBatchColumnNamesFunctionButMissingUpdateFromBatchFunction()
    {
        try
        {
            new ManagedPropertyEvaluator("def batchColumnNames():\n return ['A']");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Function 'batchColumnNames' defined but not 'updateFromBatchInput'.",
                    ex.getMessage());
        }
    }
    
    @Test
    public void testScriptWithBatchColumnNamesFunctionWhichDoesNotReturnAList()
    {
        try
        {
            new ManagedPropertyEvaluator("def batchColumnNames():\n return 42\n" +
            		"def updateFromBatchInput():\n  None");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Function 'batchColumnNames' doesn't return a List "
                    + "but an object of type 'java.lang.Long': 42", ex.getMessage());
        }
    }

    @Test
    public void testScriptWithBatchColumnNamesFunctionWhichReturnLowerCaseNames()
    {
        try
        {
            new ManagedPropertyEvaluator(
                    "def batchColumnNames():\n return ['abc', 'A', 'e42', 42]\n"
                            + "def updateFromBatchInput():\n  None");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("The following batch column names "
                    + "as returned by function 'batchColumnNames' "
                    + "are not in upper case: [abc, e42]", ex.getMessage());
        }
    }

    @Test
    public void testGetBatchColumnNamesScript()
    {
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator("def batchColumnNames():\n return ['A', 42]\n"
                        + "def updateFromBatchInput():\n  None");
        assertEquals("[A, 42]", evaluator.getBatchColumnNames().toString());
    }
    
    @Test
    public void testUpdateFromBatchInputCallsAssertBatchColumnNames()
    {
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator("");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("A", "42");
        
        try
        {
            evaluator.updateFromBatchInput(property, bindings);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No subcolumns expected for property 'p': [A]", ex.getMessage());
        }
    }
    
    @Test
    public void testUpdateFromBatchInputWithNoScript()
    {
        ManagedPropertyEvaluator evaluator =
            new ManagedPropertyEvaluator("");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("", "42");
        
        evaluator.updateFromBatchInput(property, bindings);
        
        assertEquals("42", property.getValue());
    }
    
    @Test
    public void testUpdateFromBatchInputWithNoColumns()
    {
        ManagedPropertyEvaluator evaluator =
            new ManagedPropertyEvaluator("def updateFromBatchInput(bindings):\n"
                    + "  property.setValue(bindings.get(''))");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("", "42");
        
        evaluator.updateFromBatchInput(property, bindings);
        
        assertEquals("42", property.getValue());
    }
    
    @Test
    public void testUpdateFromBatchInputWithColumns()
    {
        ManagedPropertyEvaluator evaluator =
            new ManagedPropertyEvaluator("def batchColumnNames():\n return ['A', 'B']\n"
                    + "def updateFromBatchInput(bindings):\n"
                    + "  property.setValue(bindings.get('A') + bindings.get('B'))");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("A", "4");
        bindings.put("B", "2");
        
        evaluator.updateFromBatchInput(property, bindings);
        
        assertEquals("42", property.getValue());
    }
    
}
