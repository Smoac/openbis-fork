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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.evaluator.Evaluator.ReturnType;

/**
 * Tests of the {@link Evaluator}.
 * 
 * @author Bernd Rinn
 */
public class EvaluatorTest extends AssertJUnit
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

    @Test
    public void testEvalFunctionWithStringArgument()
    {
        Evaluator evaluator = new Evaluator("", null, "def hello(name):\n  return 'hello ' + name");
        assertEquals("hello world", evaluator.evalFunction("hello", "world").toString());
    }

    @Test
    public void testEvalFunctionWithTwoArguments()
    {
        Evaluator evaluator =
                new Evaluator("", null, "def get(map, key):\n  return map.get(key)\n");
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("physicists", Arrays.asList("Newton", "Einstein"));
        Object result = evaluator.evalFunction("get", map, "physicists");
        assertEquals("Result " + result.getClass(), true, result instanceof List);
        assertEquals("[Newton, Einstein]", result.toString());
    }

    @Test
    public void testEvalFunctionWhichReturnsAList()
    {
        Evaluator evaluator = new Evaluator("", null, "def get():\n  return ['a','b']");
        Object result = evaluator.evalFunction("get");
        assertEquals("Result " + result.getClass(), true, result instanceof List);
        assertEquals("[a, b]", result.toString());
    }

    @Test
    public void testEvalFunctionWithScriptError()
    {
        Evaluator evaluator = new Evaluator("", null, "def hello(name):\n  return unknown\n");
        try
        {
            evaluator.evalFunction("hello", "world");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error evaluating 'hello(world)': NameError: unknown", ex.getMessage());
        }
    }

    @Test
    public void testHasFunction()
    {
        Evaluator evaluator =
                new Evaluator("", null, "text = 'hi'\n"
                        + "def hello(name):\n  return 'hello ' + name");

        assertEquals(true, evaluator.hasFunction("hello"));
        assertEquals(false, evaluator.hasFunction("text"));
        assertEquals(false, evaluator.hasFunction("blabla"));
    }

    @Test
    public void testEvalUnkownFunction()
    {
        Evaluator evaluator = new Evaluator("", null, "def hello(name):\n  return 'hello ' + name");
        try
        {
            evaluator.evalFunction("func", "world");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error evaluating 'func(world)': Unknown function: func", ex.getMessage());
        }
    }

    @Test
    public void testEvalFunctionFunctionNameIsVariableName()
    {
        Evaluator evaluator =
                new Evaluator("", null, "text = 'universe'\n"
                        + "def hello(name):\n  return 'hello ' + name");
        try
        {
            evaluator.evalFunction("text", "world");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error evaluating 'text(world)': Not a function: 'text' is of type str.",
                    ex.getMessage());
        }
    }

    @Test
    public void testEvalFunctionWithNotEnoughArguments()
    {
        Evaluator evaluator = new Evaluator("", null, "def hello(name):\n  return 'hello ' + name");
        try
        {
            evaluator.evalFunction("hello");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error evaluating 'hello()': TypeError: "
                    + "hello() takes at least 1 argument (0 given)", ex.getMessage());
        }
    }

    @Test
    public void testEvalFunctionWithToManyArguments()
    {
        Evaluator evaluator = new Evaluator("", null, "def hello(name):\n  return 'hello ' + name");
        try
        {
            evaluator.evalFunction("hello", "world", "universe");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error evaluating 'hello(world, universe)': TypeError: "
                    + "hello() too many arguments; expected 1 got 2", ex.getMessage());
        }
    }

    @Test
    public void testEvalFunctionWithWrongArgument()
    {
        Evaluator evaluator =
                new Evaluator("", null, "def get(map, key):\n  return map.get(key)\n");
        try
        {
            evaluator.evalFunction("get", "world", "universe");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Error evaluating 'get(world, universe)': AttributeError: "
                    + "'string' object has no attribute 'get'", ex.getMessage());
        }
    }

    @Test
    public void testCheckScriptCompilation()
    {
        Evaluator.checkScriptCompilation("1+1");
        try
        {
            Evaluator.checkScriptCompilation("1+");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Script compilation failed with message:\n\n"
                    + "SyntaxError: ('invalid syntax', ('<string>', 1, 3, '1+'))", ex.getMessage());
        }
    }

}
