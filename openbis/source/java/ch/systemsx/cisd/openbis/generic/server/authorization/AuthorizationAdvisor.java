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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.AnnotationUtils;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.common.utilities.AnnotationUtils.Parameter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;

/**
 * The <i>openBIS</i> authorization {@link Advisor}.
 * 
 * @author Christian Ribeaud
 */
public final class AuthorizationAdvisor extends DefaultPointcutAdvisor
{
    private static final long serialVersionUID = 1L;

    private static final Logger authorizationLog =
            LogFactory.getLogger(LogCategory.AUTH, AuthorizationAdvisor.class);

    public AuthorizationAdvisor(final IAuthorizationComponentFactory authorizationComponentFactory)
    {
        this(authorizationComponentFactory.createAccessController(), authorizationComponentFactory
                .createReturnValueFilter());
    }

    private AuthorizationAdvisor(final IAccessController accessController,
            final IReturnValueFilter returnValueFilter)
    {
        super(createPointcut(), createAdvice(accessController, returnValueFilter));
    }

    private final static Advice createAdvice(final IAccessController accessController,
            final IReturnValueFilter returnValueFilter)
    {
        return new AuthorizationMethodInterceptor(accessController, returnValueFilter);
    }

    private final static Pointcut createPointcut()
    {
        return new AnnotationMatchingPointcut(null, RolesAllowed.class);
    }

    //
    // Helper classes
    //

    private static final class AuthorizationMethodInterceptor implements MethodInterceptor
    {
        private final IAccessController accessController;

        private final IReturnValueFilter returnValueFilter;

        /**
         * A cache for the method parameters which contains {@link AuthorizationGuard} annotation.
         */
        private final Map<Method, List<Parameter<AuthorizationGuard>>> methodAuthorizationGuards =
                new HashMap<Method, List<Parameter<AuthorizationGuard>>>();

        AuthorizationMethodInterceptor(final IAccessController accessController,
                final IReturnValueFilter returnValueFilter)
        {
            this.accessController = accessController;
            this.returnValueFilter = returnValueFilter;
        }

        private final List<Parameter<AuthorizationGuard>> getMethodAuthorizationGuards(
                final Method method)
        {
            synchronized (methodAuthorizationGuards)
            {
                List<Parameter<AuthorizationGuard>> annotatedParameters =
                        methodAuthorizationGuards.get(method);
                if (annotatedParameters == null)
                {
                    annotatedParameters =
                            AnnotationUtils
                                    .getAnnotatedParameters(method, AuthorizationGuard.class);
                    methodAuthorizationGuards.put(method, annotatedParameters);
                }
                return annotatedParameters;
            }
        }

        @SuppressWarnings("unchecked")
        private final static Argument<?> toArgument(final Parameter<AuthorizationGuard> parameter,
                final Object[] args)
        {
            return new Argument(parameter.getType(), args[parameter.getIndex()], parameter
                    .getAnnotation());
        }

        private final Argument<?>[] createArguments(final MethodInvocation methodInvocation)
        {
            final List<Argument<?>> arguments = new ArrayList<Argument<?>>();
            final Object[] args = methodInvocation.getArguments();
            final List<Parameter<AuthorizationGuard>> authorizationGuards =
                    getMethodAuthorizationGuards(methodInvocation.getMethod());
            for (Parameter<AuthorizationGuard> parameter : authorizationGuards)
            {
                arguments.add(toArgument(parameter, args));
            }
            return arguments.toArray(Argument.EMPTY_ARRAY);
        }

        //
        // MethodInterceptor
        //

        public final Object invoke(final MethodInvocation methodInvocation) throws Throwable
        {
            final IAuthSession session = obtainSession(methodInvocation);
            final Method method = methodInvocation.getMethod();
            final Status status =
                    accessController.isAuthorized(session, method,
                            createArguments(methodInvocation));
            if (StatusFlag.OK.equals(status.getFlag()) == false)
            {
                final String groupCode = session.tryGetHomeGroupCode();
                final String errorMessage = status.tryGetErrorMessage();
                authorizationLog.info(String.format(
                        "[USER:'%s' GROUP:%s]: Authorization failure while "
                                + "invoking method '%s': %s", session.getUserName(),
                        groupCode == null ? "<UNDEFINED>" : "'" + groupCode + "'",
                        MethodUtils.describeMethod(method), errorMessage));
                throw new AuthorizationFailureException(errorMessage);
            }
            return returnValueFilter.applyFilter(session, method, methodInvocation.proceed());
        }

        private IAuthSession obtainSession(final MethodInvocation methodInvocation)
        {
            final Object[] args = methodInvocation.getArguments();
            final int len = args.length;
            assert len > 0 : "At least one argument is expected";
            final Object firstObject = args[0];
            if (firstObject instanceof IAuthSession)
            {
                IAuthSession session = (IAuthSession) firstObject;
                return session;
            }
            if (firstObject instanceof String)
            {
                String sessionToken = (String) firstObject;
                
                Object wrappedObject = methodInvocation.getThis();
                if (wrappedObject instanceof ISessionProvider)
                {
                    ISessionProvider sessionProvider = (ISessionProvider) wrappedObject;
                    return sessionProvider.getAuthSession(sessionToken);
                }
                throw new AssertionError("Wrapped object doesn't implement "
                        + ISessionProvider.class.getSimpleName() + ": " + wrappedObject);   
            }
            throw new AssertionError("First argument is neither an "
                    + IAuthSession.class.getSimpleName() + " object nor a String.");
        }
    }
}
