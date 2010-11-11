/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ReflectingStringEscaper;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;

/**
 * The advisor for automatically escaping HTML strings in the values returned by the
 * CommonClientService.
 * <p>
 * Though it is not necessary to subclass DefaultPointcutAdvisor for the implementation, we subclass
 * here to make the configuration in spring a bit simpler.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class StringHtmlEscapingPointcutAdvisor extends DefaultPointcutAdvisor
{
    private static final long serialVersionUID = 1L;

    private static final Logger escapeLog = LogFactory.getLogger(LogCategory.OPERATION,
            StringHtmlEscapingPointcutAdvisor.class);

    /**
     * The public constructor.
     */
    public StringHtmlEscapingPointcutAdvisor()
    {
        this(new StringHtmlEscapingPointcutAdvisorMethodInterceptor());
    }

    /**
     * Constructor for testing purposes.
     * 
     * @param methodInterceptor
     */
    public StringHtmlEscapingPointcutAdvisor(MethodInterceptor methodInterceptor)
    {
        super(new CommonClientServiceMatchingPointcut(), methodInterceptor);
    }

    private static class CommonClientServiceMatchingPointcut extends StaticMethodMatcherPointcut
    {
        @SuppressWarnings("rawtypes")
        public boolean matches(Method method, Class targetClass)
        {
            if (targetClass != CommonClientService.class)
            {
                return false;
            }
            if (method.getDeclaringClass() != ICommonClientService.class)
            {
                return false;
            }

            if (method.getName().equals("getApplicationInfo"))
            {
                escapeLog.info("Asked to handle getApplicationInfo");
                return false;
            }

            if (method.getReturnType() == Void.TYPE)
            {
                return false;
            }

            return true;
        }
    }

    /**
     * Class for verifying authorization. Made public so it can be extended in tests.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class StringHtmlEscapingPointcutAdvisorMethodInterceptor implements
            MethodInterceptor
    {
        private final WeakHashMap<Object, WeakReference<Object>> alreadyEscapedObjects =
                new WeakHashMap<Object, WeakReference<Object>>();

        /**
         * Get the session token and any guarded parameters and invoke the guards on those
         * parameters.
         */
        public Object invoke(MethodInvocation methodInvocation) throws Throwable
        {
            Object result = methodInvocation.proceed();

            // check if the object has already been escaped
            synchronized (this)
            {
                WeakReference<Object> alreadyEscaped = alreadyEscapedObjects.get(result);
                if (alreadyEscaped != null)
                {
                    escapeObject(methodInvocation, result);
                    alreadyEscapedObjects.put(result, new WeakReference<Object>(result));
                }
            }

            return result;
        }

        private void escapeObject(MethodInvocation methodInvocation, Object result)
        {
            escapeLog.info(methodInvocation.getMethod().getName() + " converting   " + result);
            if (result instanceof String)
            {
                StringEscapeUtils.escapeHtml((String) result);
            } else
            {
                // Escape the result objects
                ReflectingStringEscaper.escapeDeep(result);
            }
            escapeLog.info(methodInvocation.getMethod().getName() + " converted to " + result);
        }
    }
}
