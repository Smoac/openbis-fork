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

package ch.systemsx.cisd.common.evaluator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.evaluator.Evaluator.ReturnType;

/**
 * Tests of the {@link Evaluator}.
 * 
 * @author Bernd Rinn
 */
public class EvaluatorTest
{
    @BeforeTest
    public void init()
    {
        Evaluator.initialize();
    }

    @Test
    public void testEvalWithNull()
    {
        final Evaluator eval = new Evaluator("None");
        assertEquals(null, eval.eval());
        assertEquals(null, eval.evalAsString());
    }

    @Test
    public void testSimpleExpression()
    {
        final Evaluator eval = new Evaluator("1+1");
        assertEquals(2, eval.evalToInt());
    }

    @Test
    public void testExpressionWithVariables()
    {
        final Evaluator eval = new Evaluator("a*b");
        eval.set("a", 2);
        eval.set("b", 3.1);
        assertEquals(6.2, eval.evalToDouble(), 1e-15);
    }

    @Test
    public void testExpressionWithVariablesMultipleEvaluations()
    {
        final Evaluator eval = new Evaluator("a*b");
        eval.set("a", 2);
        eval.set("b", 3.1);
        assertEquals(6.2, eval.evalToDouble(), 1e-15);
        eval.set("a", 0.5);
        eval.set("b", 2.0);
        assertEquals(1.0, eval.evalToDouble(), 1e-15);
    }

    @Test
    public void testVariables()
    {
        final Evaluator eval = new Evaluator("a");
        eval.set("a", 2);
        assertTrue(eval.has("a"));
        assertFalse(eval.has("b"));
        eval.delete("a");
        assertFalse(eval.has("a"));
    }

    @Test
    public void testBuiltInFunctionEval()
    {
        final Evaluator eval = new Evaluator("min(1,2)");
        assertEquals(1, eval.evalToInt());
        final Evaluator eval2 = new Evaluator("max(1,2)");
        assertEquals(2, eval2.evalToInt());
    }

    public static class Functions
    {
        public static double Min(double a, double b)
        {
            return (a < b) ? a : b;
        }

        public static double Min(double... vals)
        {
            double result = Double.MAX_VALUE;
            for (double v : vals)
            {
                if (v < result)
                {
                    result = v;
                }
            }
            return result;
        }

        public static int Min(int a, int b)
        {
            return (a < b) ? a : b;
        }
    }

    @Test
    public void testFunctionEval()
    {
        Evaluator eval = new Evaluator("Min(1,2)", Functions.class, null);
        assertEquals(1, eval.evalToInt());
        eval = new Evaluator("Min([1,2,0.1])", Functions.class, null);
        assertEquals(0.1, eval.evalToDouble(), 1e-15);
        eval = new Evaluator("Min(v)", Functions.class, null);
        eval.set("v", new double[]
            { 1, 2, -99.9, 3 });
        assertEquals(-99.9, eval.evalToDouble(), 1e-15);
    }

    @Test
    public void testInitialScript()
    {
        final Evaluator eval =
                new Evaluator("hello()", null, "def hello():\n   return 'Hello World'");
        assertEquals("Hello World", eval.evalAsString());
    }

    @Test
    public void testGetTypeChanging()
    {
        final Evaluator eval = new Evaluator("a");
        eval.set("a", 2);
        assertEquals(ReturnType.INTEGER_OR_BOOLEAN, eval.getType());
        eval.set("a", "2");
        assertEquals(ReturnType.STRING, eval.getType());
    }

    @Test(expectedExceptions = EvaluatorException.class)
    public void testInvalidExpression()
    {
        new Evaluator("pass");
    }

    @Test(expectedExceptions = EvaluatorException.class)
    public void testInvalidExpressionWithLineBreaks()
    {
        new Evaluator("1\nimport os\nos.remove('/etc/passwd')");
    }

    @Test(expectedExceptions = EvaluatorException.class)
    public void testMissingVariable()
    {
        final Evaluator eval = new Evaluator("a");
        eval.evalAsString();
    }

    @Test
    public void testInvalidReturnValue()
    {
        final Evaluator eval = new Evaluator("a");
        eval.set("a", true);
        assertEquals(ReturnType.INTEGER_OR_BOOLEAN, eval.getType());
        assertEquals("1", eval.evalAsString());
        try
        {
            eval.evalToDouble();
        } catch (EvaluatorException ex)
        {
            assertEquals(ex.getMessage(),
                    "Expected a result of type DOUBLE, found INTEGER_OR_BOOLEAN", ex.getMessage());
        }
    }

    @Test
    public void testWriteFails()
    {
        final File tagFile = new File("targets/newfile");
        tagFile.delete();
        final Evaluator eval =
                new Evaluator("open('targets/newfile', 'w').write('Should not work')");
        try
        {
            eval.evalAsString();
        } catch (EvaluatorException ex)
        {
            // expected
        }
        assertFalse(tagFile.exists());
    }
}
