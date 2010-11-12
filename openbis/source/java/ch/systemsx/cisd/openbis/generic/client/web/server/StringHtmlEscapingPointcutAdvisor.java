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

import java.lang.reflect.Method;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;

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

            if (method.getReturnType() == Void.TYPE)
            {
                return false;
            }

            // This is handled in the cache manager
            if (method.getReturnType() == TypedTableResultSet.class)
            {
                return false;
            }

            // This is handled in the cache manager
            if (method.getReturnType() == ResultSet.class)
            {
                return false;
            }

            // Don't need to escape this method
            if ("getLastModificationState".equals(method.getName()))
            {
                return false;
            }

            // This is handled in the cache manager
            if (method.getReturnType() == ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes.class)
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
        /**
         * Get the session token and any guarded parameters and invoke the guards on those
         * parameters.
         */
        public Object invoke(MethodInvocation methodInvocation) throws Throwable
        {
            Object result = methodInvocation.proceed();

            result = escapeObject(methodInvocation, result);

            return result;
        }

        private Object escapeObject(MethodInvocation methodInvocation, Object unescapedResult)
        {
            Object result = unescapedResult;
            // Need to log unescaped result here, since it might be modified below
            escapeLog.debug(methodInvocation.getMethod().getName() + " converting   "
                    + unescapedResult);
            if (unescapedResult instanceof String)
            {
                if ("getExportTable".equals(methodInvocation.getMethod().getName()))
                {
                    result = StringEscapeUtils.unescapeHtml((String) unescapedResult);
                } else
                {
                    // Do we need to escape strings in general?
                    // StringEscapeUtils.escapeHtml((String) unescapedResult);
                }
            } else
            {
                // Escape the result objects
                ReflectingStringEscaper.escapeDeep(unescapedResult);
            }
            escapeLog.debug(methodInvocation.getMethod().getName() + " converted to " + result);
            return result;
        }
    }
}
