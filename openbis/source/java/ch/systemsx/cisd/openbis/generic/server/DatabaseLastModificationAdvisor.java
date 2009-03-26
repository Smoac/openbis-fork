/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * @author Tomasz Pylak
 */
public final class DatabaseLastModificationAdvisor extends DefaultPointcutAdvisor
{
    private static final long serialVersionUID = 1L;

    private static final Logger modificationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatabaseLastModificationAdvisor.class);

    public DatabaseLastModificationAdvisor(LastModificationState state)
    {
        super(createPointcut(), createAdvice(state));
    }

    private final static Advice createAdvice(LastModificationState state)
    {
        return new DatabaseLastModificationMethodInterceptor(state);
    }

    private final static Pointcut createPointcut()
    {
        Pointcut p1 = new AnnotationMatchingPointcut(null, DatabaseUpdateModification.class);
        Pointcut p2 =
                new AnnotationMatchingPointcut(null, DatabaseCreateOrDeleteModification.class);
        return new ComposablePointcut(p1).union(p2);
    }

    //
    // Helper classes
    //

    private static final class DatabaseLastModificationMethodInterceptor implements
            MethodInterceptor
    {
        private final LastModificationState state;

        private final Map<Method, Set<DatabaseModificationKind>> annotationsCache;

        public DatabaseLastModificationMethodInterceptor(LastModificationState state)
        {
            this.annotationsCache = new HashMap<Method, Set<DatabaseModificationKind>>();
            this.state = state;
        }

        public final Object invoke(final MethodInvocation methodInvocation) throws Throwable
        {
            long currentTimestamp = new Date().getTime();
            final Method method = methodInvocation.getMethod();
            Object result = methodInvocation.proceed();
            registerModification(method, currentTimestamp);
            return result;
        }

        private void registerModification(Method method, long currentTimestamp)
        {
            Set<DatabaseModificationKind> modificationKinds = getCachedModificationKinds(method);
            for (DatabaseModificationKind modification : modificationKinds)
            {
                modificationLog.debug(String.format("Method '%s' registered at %s: %s", MethodUtils
                        .describeMethod(method), new Date(currentTimestamp), modification));
                state.registerModification(modification, currentTimestamp);
            }
        }

        private Set<DatabaseModificationKind> getCachedModificationKinds(Method method)
        {
            Set<DatabaseModificationKind> result = annotationsCache.get(method);
            if (result == null)
            {
                result = getModificationKinds(method);
                annotationsCache.put(method, result);
            }
            return result;
        }
    }

    private static Set<DatabaseModificationKind> getModificationKinds(Method method)
    {
        Set<DatabaseModificationKind> categories = new HashSet<DatabaseModificationKind>();
        processCreateOrDeleteAnnotation(method, categories);
        processUpdateAnnotation(method, categories);
        return categories;
    }

    private static void processUpdateAnnotation(Method method,
            Set<DatabaseModificationKind> categories)
    {
        DatabaseUpdateModification annotation =
                method.getAnnotation(DatabaseUpdateModification.class);
        if (annotation == null)
        {
            return;
        }
        for (ObjectKind objectKind : annotation.value())
        {
            categories.add(DatabaseModificationKind.createUpdate(objectKind));
        }
    }

    private static void processCreateOrDeleteAnnotation(Method method,
            Set<DatabaseModificationKind> categories)
    {
        DatabaseCreateOrDeleteModification annotation =
                method.getAnnotation(DatabaseCreateOrDeleteModification.class);
        if (annotation == null)
        {
            return;
        }
        for (ObjectKind objectKind : annotation.value())
        {
            categories.add(DatabaseModificationKind.createNew(objectKind));
        }
    }
}
