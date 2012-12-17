/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache for Evaluator instances. Enables considerable speedup of python expressions, as the python
 * code needs to be loaded into an interpreter only once (per thread; every thread has a cache of
 * its own).
 * 
 * @author anttil
 */
public class EvaluatorCache
{

    /**
     * ThreadLocal cache of Evaluator instances and their initial states. The Key consists of the
     * script and the expression of the Evaluator.
     */
    private static ThreadLocal<Map<Key, EvaluatorState>> caches =
            new ThreadLocal<Map<Key, EvaluatorState>>()
                {
                    @Override
                    protected Map<Key, EvaluatorState> initialValue()
                    {
                        return new HashMap<Key, EvaluatorState>();
                    }
                };

    /**
     * Returns an Evaluator instance for given script and expression from cache. If the cache
     * contains no such Evaluator, then it is created and added to the cache. All global variables
     * that were set after the Evaluator instance was cached are cleared.
     */
    public static Evaluator getEvaluator(String expression, Class<?> supportFunctionsOrNull,
            String scriptOrNull)
    {
        Key key = new Key(scriptOrNull, expression);
        EvaluatorState state = caches.get().get(key);
        if (state == null)
        {
            Evaluator evaluator = new Evaluator(expression, supportFunctionsOrNull, scriptOrNull);
            Collection<String> globals = evaluator.getGlobalVariables();
            state = new EvaluatorState(evaluator, globals);
            caches.get().put(key, state);
        }
        return state.getCleanInstance();
    }

    /**
     * A class containing an Evaluator instance and its initial state.
     * 
     * @author anttil
     */
    private static class EvaluatorState
    {
        private final Evaluator evaluator;

        private final Collection<String> globals;

        public EvaluatorState(Evaluator evaluator, Collection<String> globals)
        {
            this.evaluator = evaluator;
            this.globals = globals;
        }

        /**
         * Returns an evaluator instance in its initial state.
         */
        public Evaluator getCleanInstance()
        {
            for (String value : evaluator.getGlobalVariables())
            {
                if (globals.contains(value) == false)
                {
                    evaluator.delete(value);
                }
            }
            return this.evaluator;
        }
    }

    /**
     * Caching key.
     * 
     * @author anttil
     */
    private static class Key
    {
        private final String script;

        private final String expression;

        public Key(String script, String expression)
        {
            this.script = (script != null ? script : "");
            this.expression = (expression != null ? expression : "");
        }

        public String getScript()
        {
            return script;
        }

        public String getExpression()
        {
            return expression;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof Key)
            {
                Key key = (Key) o;
                return key.getScript().equals(getScript())
                        && key.getExpression().equals(getExpression());
            } else
            {
                return false;
            }
        }

        @Override
        public int hashCode()
        {
            return script.hashCode() + 17 * expression.hashCode();
        }
    }

}
