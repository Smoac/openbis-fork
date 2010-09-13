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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.DataSetAccessGuard;

/**
 * The advisor for authorization in the DSS RPC interfaces.
 * <p>
 * This AOP advisor ensures that invocations to the DSS RPC services pass the authorization
 * requirements.
 * <p>
 * Though it is not necessary to subclass DefaultPointcutAdvisor for the implementation, we subclass
 * here because to make the configuration in spring a bit simpler.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcAuthorizationAdvisor extends DefaultPointcutAdvisor
{
    private static final long serialVersionUID = 1L;

    /**
     * The public constructor.
     */
    public DssServiceRpcAuthorizationAdvisor()
    {
        this(new TestMethodInterceptor());
    }

    /**
     * Constructor for testing purposes.
     * 
     * @param methodInterceptor
     */
    DssServiceRpcAuthorizationAdvisor(MethodInterceptor methodInterceptor)
    {
        super(new AnnotationMatchingPointcut(null, DataSetAccessGuard.class), methodInterceptor);
    }

    private static class TestMethodInterceptor implements MethodInterceptor
    {

        public Object invoke(MethodInvocation methodInvocation) throws Throwable
        {
            return methodInvocation.proceed();
        }

    }

}
