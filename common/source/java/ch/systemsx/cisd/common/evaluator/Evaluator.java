package ch.systemsx.cisd.common.evaluator;
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

import java.math.BigInteger;

import org.apache.commons.lang.StringUtils;
import org.python.core.PyCode;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyLong;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.core.__builtin__;
import org.python.util.PythonInterpreter;

/**
 * A class for evaluating expressions, based on Jython.
 * <p>
 * This class is optimized for evaluating the same expression with a different set of variables
 * repeatedly and efficient. The mode of usage of this class is:
 * <ol>
 * <li>Construct an {@link Evaluator} with an appropriate expression.</li>
 * <li>Set all variables needed for evaluation via {@link #set(String, Object)}</li>
 * <li>Call one of {@link #getType()}, {@link #evalAsString()}, {@link #evalToBoolean()},
 * {@link #evalToInt()}, {@link #evalToBigInt()}, {@link #evalToDouble()}.</li>
 * <li>Repeat from step 2</li>
 * </ol>
 * 
 * @author Bernd Rinn
 */
public final class Evaluator
{
    private final PythonInterpreter interpreter;

    private final String expression;

    private final PyCode compiledExpression;

    /**
     * Call once before using the object to initialize.
     */
    public static void initialize()
    {
        PySystemState.initialize();
    }

    /**
     * The return type of this expression.
     */
    public enum ReturnType
    {
        INTEGER_OR_BOOLEAN, BIGINT, DOUBLE, STRING, OTHER
    }

    /**
     * Creates a new {@link Evaluator}.
     * 
     * @param expression The expression to evaluate.
     */
    public Evaluator(String expression) throws EvaluatorException
    {
        this(expression, null);
    }
    
    /**
     * Creates a new {@link Evaluator}.
     * 
     * @param expression The expression to evaluate.
     * @param supportFunctionsOrNull If not <code>null</code>, all public static methods of the
     *            given class will be available to the evaluator as "supporting functions".
     */
    public Evaluator(String expression, Class<?> supportFunctionsOrNull) throws EvaluatorException
    {
        if (expression.indexOf('\n') >= 0)
        {
            throw new EvaluatorException("Expression '" + expression + "' contains line breaks");
        }
        this.interpreter = new PythonInterpreter();
        if (supportFunctionsOrNull != null)
        {
            interpreter.exec("from " + supportFunctionsOrNull.getCanonicalName() + " import *");
        }
        this.expression = expression;
        try
        {
            this.compiledExpression =
                    __builtin__.compile("__result__=(" + expression + ")", "expression: " + expression,
                            "exec");
        } catch (PyException ex)
        {
            throw toEvaluatorException(ex);
        }
    }

    /**
     * Sets the variable <var>name</var> to <var>value</var> in the evaluator's name space.
     */
    public void set(String name, Object value)
    {
        interpreter.set(name, value);
    }

    /**
     * Deletes the variable <var>name</var> from the evaluator's name space.
     */
    public void del(String name)
    {
        interpreter.getLocals().__delitem__(name);
    }

    /**
     * Returns <code>true</code> if and only if the variable <var>name</var> exists in the
     * evaluator's name space.
     */
    public boolean has(String name)
    {
        return ((PyStringMap) interpreter.getLocals()).has_key(new PyString(name));
    }

    /**
     * Returns the {@link ReturnType} of the expression of this evaluator.
     */
    public ReturnType getType()
    {
        doEval();
        final Object obj = interpreter.get("__result__");
        if (obj instanceof PyInteger || obj instanceof PyInteger)
        {
            return ReturnType.INTEGER_OR_BOOLEAN;
        } else if (obj instanceof PyLong)
        {
            return ReturnType.BIGINT;
        } else if (obj instanceof PyFloat)
        {
            return ReturnType.DOUBLE;
        } else if (obj instanceof PyString)
        {
            return ReturnType.STRING;
        } else
        {
            return ReturnType.OTHER;
        }
    }

    /**
     * Evaluates the expression of this evaluator and returns the result, assuming that the
     * expression has a boolean return type.
     */
    public boolean evalToBoolean() throws EvaluatorException
    {
        doEval();
        try
        {
            return ((PyInteger) interpreter.get("__result__")).getValue() > 0;
        } catch (ClassCastException ex)
        {
            final ReturnType type = getType();
            throw new EvaluatorException("Expected a result of type "
                    + ReturnType.INTEGER_OR_BOOLEAN + ", found " + type);
        }
    }

    /**
     * Evaluates the expression of this evaluator and returns the result, assuming that the
     * expression has an integer return type.
     */
    public int evalToInt() throws EvaluatorException
    {
        doEval();
        try
        {
            return ((PyInteger) interpreter.get("__result__")).getValue();
        } catch (ClassCastException ex)
        {
            final ReturnType type = getType();
            throw new EvaluatorException("Expected a result of type "
                    + ReturnType.INTEGER_OR_BOOLEAN + ", found " + type);
        }
    }

    /**
     * Evaluates the expression of this evaluator and returns the result, assuming that the
     * expression has a big integer return type.
     */
    public BigInteger evalToBigInt() throws EvaluatorException
    {
        doEval();
        try
        {
            return ((PyLong) interpreter.get("__result__")).getValue();
        } catch (ClassCastException ex)
        {
            final ReturnType type = getType();
            throw new EvaluatorException("Expected a result of type "
                    + ReturnType.BIGINT + ", found " + type);
        }
    }

    /**
     * Evaluates the expression of this evaluator and returns the result, assuming that the
     * expression has a floating point (double) return type.
     */
    public double evalToDouble() throws EvaluatorException
    {
        doEval();
        try
        {
            return ((PyFloat) interpreter.get("__result__")).getValue();
        } catch (ClassCastException ex)
        {
            final ReturnType type = getType();
            throw new EvaluatorException("Expected a result of type "
                    + ReturnType.DOUBLE + ", found " + type);
        }
    }

    /**
     * Evaluates the expression of this evaluator and returns the result as a String. This method
     * can always be called.
     */
    public String evalAsString() throws EvaluatorException
    {
        doEval();
        return interpreter.get("__result__").toString();
    }

    private void doEval() throws EvaluatorException
    {
        try
        {
            interpreter.exec(compiledExpression);
        } catch (PyException ex)
        {
            throw toEvaluatorException(ex);
        }
    }

    private EvaluatorException toEvaluatorException(PyException ex)
    {
        final String[] description = StringUtils.split(ex.toString(), '\n');
        final String msg =
                "Error evaluating '" + expression + "': " + description[description.length - 1];
        return new EvaluatorException(msg);
    }
}
