/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.jython.evaluator;

import ch.systemsx.cisd.common.jython.IJythonInterpreterFactory;

/**
 * @author Jakub Straszewski
 */
public class Evaluator
{
    /**
     * The return type of this expression.
     */
    public enum ReturnType
    {
        BOOLEAN, INTEGER, BIGINT, DOUBLE, STRING, OTHER
    }

    private static IJythonEvaluatorFactory factory;
    private static IJythonInterpreterFactory interpreterFactory;

    static void setFactory(IJythonEvaluatorFactory factory)
    {
        Evaluator.factory = factory;
    }

    public static IJythonEvaluatorFactory getFactory()
    {
        if (factory == null)
        {
            // we should make sure that the initialization happens before first call to this method
            throw createException("evaluators");
        }
        return factory;
    }

    static void setInterpreterFactory(IJythonInterpreterFactory interpreterFactory)
    {
        Evaluator.interpreterFactory = interpreterFactory;
    }

    public static IJythonInterpreterFactory getInterpreterFactory()
    {
        if (interpreterFactory == null)
        {
            // we should make sure that the initialization happens before first call to this method
            throw createException("interpreters");
        }
        return interpreterFactory;
    }

    private static IllegalStateException createException(String type)
    {
        return new IllegalStateException(
                "Jython evaluator component not initialized. Application context is not initialized properly "
                        + "- JythonEvaluatorSpringComponent must be initialized before jython " + type + " are used.");
    }
    
    public static boolean isMultiline(String expression)
    {
        return expression.indexOf('\n') >= 0;
    }
}
