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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.AnnotationUtils;
import ch.systemsx.cisd.common.utilities.AnnotationUtils.Parameter;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.AuthorizationGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.DataSetAccessGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.IAuthorizationGuardPredicate;

/**
 * The advisor for authorization in the DSS RPC interfaces.
 * <p>
 * This AOP advisor ensures that invocations of methods on DSS RPC services with the
 * {@link DataSetAccessGuard} annotation conform to the following the authorization requirements:
 * <ul>
 * <li>The session token (String) is the the first parameter</li>
 * <li>The second parameter is either a data set code (String) or a DataSetFileDTO object</li>
 * <li>The user who owns the session token (first argument) has access to the specified data set
 * code (second argument)</li>
 * </ul>
 * <p>
 * It does this check by invoking the method
 * {@link AbstractDssServiceRpc#isDatasetAccessible(String, String)} on the receiver of the method
 * containing the join point.
 * <p>
 * Though it is not necessary to subclass DefaultPointcutAdvisor for the implementation, we subclass
 * here because to make the configuration in spring a bit simpler.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcAuthorizationAdvisor extends DefaultPointcutAdvisor
{
    private static final long serialVersionUID = 1L;

    private static final Logger authorizationLog = LogFactory.getLogger(LogCategory.AUTH,
            DssServiceRpcAuthorizationAdvisor.class);

    /**
     * The public constructor.
     */
    public DssServiceRpcAuthorizationAdvisor()
    {
        this(new DssServiceRpcAuthorizationMethodInterceptor());
    }

    /**
     * Constructor for testing purposes.
     * 
     * @param methodInterceptor
     */
    public DssServiceRpcAuthorizationAdvisor(MethodInterceptor methodInterceptor)
    {
        super(new AnnotationMatchingPointcut(null, DataSetAccessGuard.class), methodInterceptor);
    }

    /**
     * Class for verifying authorization. Made public so it can be extended in tests.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class DssServiceRpcAuthorizationMethodInterceptor implements MethodInterceptor
    {
        /**
         * Get the session token and any guarded parameters and invoke the guards on those
         * parameters.
         */
        public Object invoke(MethodInvocation methodInvocation) throws Throwable
        {
            final Object[] args = methodInvocation.getArguments();
            String sessionToken = getSessionToken(args);
            List<Parameter<AuthorizationGuard>> annotatedParameters =
                    AnnotationUtils.getAnnotatedParameters(methodInvocation.getMethod(),
                            AuthorizationGuard.class);
            // At least one of the parameters must be annotated
            assert annotatedParameters.size() > 0 : "No guard defined";

            Object recv = methodInvocation.getThis();

            for (Parameter<AuthorizationGuard> param : annotatedParameters)
            {
                Object guarded = args[param.getIndex()];
                Status status = evaluateGuard(sessionToken, recv, param, guarded);
                if (status != Status.OK)
                {
                    authorizationLog.info(String.format(
                            "[SESSION:'%s' DATA_SET:%s]: Authorization failure while "
                                    + "invoking method '%s'", sessionToken, guarded,
                            MethodUtils.describeMethod(methodInvocation.getMethod())));
                    String errorMessage = "Data set does not exist.";
                    if (null != status.tryGetErrorMessage())
                    {
                        errorMessage = status.tryGetErrorMessage();
                    }

                    throw new IllegalArgumentException(errorMessage);
                }
            }

            return methodInvocation.proceed();
        }

        /**
         * Because the predicate is being invoked in a context in which its types are not known,
         * there is no way to do this in a statically type-safe way.
         */
        @SuppressWarnings(
            { "rawtypes", "unchecked" })
        private Status evaluateGuard(String sessionToken, Object recv,
                Parameter<AuthorizationGuard> param, Object guarded)
        {
            IAuthorizationGuardPredicate predicate = createPredicate(param);
            return predicate.evaluate(recv, sessionToken, guarded);
        }

        private IAuthorizationGuardPredicate<?, ?> createPredicate(
                Parameter<AuthorizationGuard> param)
        {
            Class<? extends IAuthorizationGuardPredicate<?, ?>> predicateClass =
                    param.getAnnotation().guardClass();
            return ClassUtils.createInstance(predicateClass);
        }

        /**
         * Get the session token from the MethodInvocation
         */
        private static String getSessionToken(final Object[] args)
        {
            final int len = args.length;
            assert len > 0 : "The session token should be the first argument.";
            final Object firstObject = args[0];
            assert firstObject instanceof String : "The session token should be the first argument.";

            return (String) firstObject;
        }
    }

}
