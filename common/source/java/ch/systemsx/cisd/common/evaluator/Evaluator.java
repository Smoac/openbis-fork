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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.python.core.PyCode;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyJavaInstance;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyNone;
import org.python.core.PyObject;
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
        this(expression, null, null);
    }

    /**
     * Creates a new {@link Evaluator}.
     * 
     * @param expression The expression to evaluate.
     * @param supportFunctionsOrNull If not <code>null</code>, all public static methods of the
     *            given class will be available to the evaluator as "supporting functions".
     * @param initialScriptOrNull If not <code>null</code>, this has to be a valid (Python) script
     *            which is evaluated initially, e.g. to define some new functions. Note: this script
     *            is trusted, so don't run any unvalidated code here!
     */
    public Evaluator(String expression, Class<?> supportFunctionsOrNull, String initialScriptOrNull)
            throws EvaluatorException
    {
        if (isMultiline(expression))
        {
            throw new EvaluatorException("Expression '" + expression + "' contains line breaks");
        }
        this.interpreter = new PythonInterpreter();
        // Security: do not allow file access.

        try
        {
            interpreter.exec("def open():\n   pass");
            if (supportFunctionsOrNull != null)
            {
                interpreter.exec("from " + supportFunctionsOrNull.getCanonicalName() + " import *");
            }
            if (initialScriptOrNull != null)
            {
                interpreter.exec(initialScriptOrNull);
            }
            this.expression = expression;
            this.compiledExpression = doCompile(expression);
        } catch (PyException ex)
        {
            throw toEvaluatorException(ex);
        }
    }

    /**
     * Returns <code>true</code> if specified function is defined in the script.
     */
    public boolean hasFunction(String functionName)
    {
        PyObject pyObject = interpreter.get(functionName);
        return pyObject instanceof PyFunction;
    }

    /**
     * Evaluates specified function with specified arguments. The arguments are turned into Python
     * Strings if they are Java {@link String} objects. The return value of the function is returned
     * as a Java object or <code>null</code>.
     * 
     * @throws EvaluatorException if evaluation fails.
     */
    public Object evalFunction(String functionName, Object... args)
    {
        try
        {
            PyObject pyObject = interpreter.get(functionName);
            if (pyObject == null)
            {
                throw new PyException(new PyString("Unknown function"), functionName);
            }
            if (pyObject instanceof PyFunction == false)
            {
                throw new PyException(new PyString("Not a function"), "'" + functionName
                        + "' is of type " + pyObject.getType().getFullName() + ".");
            }
            PyFunction func = (PyFunction) pyObject;
            PyObject[] pyArgs = new PyObject[args.length];
            for (int i = 0; i < args.length; i++)
            {
                pyArgs[i] = translateToPython(args[i]);
            }
            PyObject result = func.__call__(pyArgs);
            return translateToJava(result);
        } catch (PyException ex)
        {
            StringBuilder builder = new StringBuilder();
            for (Object argument : args)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(argument);
            }
            throw toEvaluatorException(ex, functionName + "(" + builder + ")");
        }
    }

    private PyObject translateToPython(Object javaObject)
    {
        if (javaObject instanceof String)
        {
            return new PyString((String) javaObject);
        }
        return new PyJavaInstance(javaObject);
    }

    /**
     * @return compiled <var>expression</var>
     * @throws EvaluatorException if compilation fails
     */
    private static PyCode doCompile(String expression) throws EvaluatorException
    {
        try
        {
            return __builtin__.compile("__result__=(" + expression + ")", "expression: "
                    + expression, "exec");
        } catch (PyException ex)
        {
            throw toEvaluatorException(ex, expression);
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
    public void delete(String name)
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
        final Object obj = getInterpreterResult();
        if (obj instanceof PyInteger)
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
     * Evaluates the expression of this evaluator and returns the result. Use this method if you do
     * not know what will be the result type.
     * 
     * @return evaluation result which can be of Long, Double or String type. All other types are
     *         converted to String representation except {@link PyNone} that represents null value
     *         and will be converted to <code>null</code>.
     */
    public Object eval()
    {
        doEval();
        final PyObject obj = getInterpreterResult();
        Object result = translateToJava(obj);
        if (result != null && result instanceof Long == false && result instanceof Double == false
                && result instanceof String == false)
        {
            return result.toString();
        }
        return result;
    }

    private Object translateToJava(final PyObject obj)
    {
        if (obj instanceof PyInteger)
        {
            return new Long(((PyInteger) obj).getValue());
        } else if (obj instanceof PyLong)
        {
            return new Long(((PyLong) obj).getValue().longValue());
        } else if (obj instanceof PyFloat)
        {
            return new Double(((PyFloat) obj).getValue());
        } else if (obj instanceof PyNone)
        {
            return null;
        } else if (obj instanceof PyList)
        {
            PyList pyList = (PyList) obj;
            PyObject[] array = pyList.getArray();
            List<Object> list = new ArrayList<Object>();
            for (int i = 0, n = pyList.size(); i < n; i++)
            {
                list.add(translateToJava(array[i]));
            }
            return list;
        } else if (obj instanceof PyJavaInstance)
        {
            return ((PyJavaInstance) obj).__tojava__(Object.class);
        } else
        {
            return obj == null ? null : obj.toString();
        }
    }

    private PyObject getInterpreterResult()
    {
        return interpreter.get("__result__");
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
            return ((PyInteger) getInterpreterResult()).getValue() > 0;
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
            return ((PyInteger) getInterpreterResult()).getValue();
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
            return ((PyLong) getInterpreterResult()).getValue();
        } catch (ClassCastException ex)
        {
            final ReturnType type = getType();
            throw new EvaluatorException("Expected a result of type " + ReturnType.BIGINT
                    + ", found " + type);
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
            return ((PyFloat) getInterpreterResult()).getValue();
        } catch (ClassCastException ex)
        {
            final ReturnType type = getType();
            throw new EvaluatorException("Expected a result of type " + ReturnType.DOUBLE
                    + ", found " + type);
        }
    }

    /**
     * Evaluates the expression of this evaluator and returns the result as a String. This method
     * can always be called.
     * <p>
     * NOTE: null will be returned if expression results in {@link PyNone}
     */
    public String evalAsString() throws EvaluatorException
    {
        Object result = eval();
        return result == null ? null : result.toString();
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
        return toEvaluatorException(ex, expression);
    }

    private static EvaluatorException toEvaluatorException(PyException ex, String expressionOrNull)
    {
        Exception exception = null;
        PyObject value = ex.value;
        if (value instanceof PyJavaInstance)
        {
            Object object = ((PyJavaInstance) value).__tojava__(Object.class);
            if (object instanceof Exception)
            {
                exception = (Exception) object;
            }
        }
        String msg = extractExceptionMessage(ex);
        if (expressionOrNull != null)
        {
            msg = "Error evaluating '" + expressionOrNull + "': " + msg;
        }
        return exception == null ? new EvaluatorException(msg) : new EvaluatorException(msg,
                exception);
    }

    private static String extractExceptionMessage(PyException ex)
    {
        final String[] description = StringUtils.split(ex.toString(), '\n');
        return description[description.length - 1];
    }

    public static boolean isMultiline(String expression)
    {
        return expression.indexOf('\n') >= 0;
    }
}
