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
import org.apache.log4j.Logger;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.DataSetAccessGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;

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

    private static final Logger authorizationLog =
            LogFactory.getLogger(LogCategory.AUTH, DssServiceRpcAuthorizationAdvisor.class);

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
            final Object[] args = methodInvocation.getArguments();
            String sessionToken = getSessionToken(args);
            String dataSetCode = getDataSetCode(args, methodInvocation);
            AbstractDssServiceRpc recv = (AbstractDssServiceRpc) methodInvocation.getThis();
            if (false == recv.isDatasetAccessible(sessionToken, dataSetCode))
            {
                authorizationLog.info(String.format(
                        "[SESSION:'%s' DATA_SET:%s]: Authorization failure while "
                                + "invoking method '%s'", sessionToken, dataSetCode, MethodUtils
                                .describeMethod(methodInvocation.getMethod())));

                throw new AuthorizationFailureException("User does not have access to data set "
                        + dataSetCode);
            }

            return methodInvocation.proceed();
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

        private static String getDataSetCode(final Object[] args, MethodInvocation methodInvocation)
        {
            final int len = args.length;
            assert len > 1 : "The data set code should be contained in the second argument.";
            final Object secondObject = args[1];
            String dataSetCode;
            if (secondObject instanceof String)
            {
                dataSetCode = (String) secondObject;
            } else if (secondObject instanceof DataSetFileDTO)
            {
                DataSetFileDTO dsFile = (DataSetFileDTO) secondObject;
                dataSetCode = dsFile.getDataSetCode();
            } else
            {
                String methodDesc = MethodUtils.describeMethod(methodInvocation.getMethod());
                authorizationLog.info(String.format("Authorization failure while "
                        + "invoking method '%s': %s", methodDesc));

                throw new IllegalArgumentException("Method (" + methodDesc
                        + ") does not include the data set code as an argument");
            }

            return dataSetCode;
        }
    }

}
