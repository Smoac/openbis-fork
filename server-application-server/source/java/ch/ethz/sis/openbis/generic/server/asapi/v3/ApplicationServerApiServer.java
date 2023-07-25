/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.openbis.common.api.server.AbstractApiServiceExporter;

/**
 * @author Franz-Josef Elmer
 */
@Controller
public class ApplicationServerApiServer extends AbstractApiServiceExporter
{

    @Resource(name = ApplicationServerApi.INTERNAL_SERVICE_NAME)
    private IApplicationServerApi service;

    @Autowired
    private ITransactionParticipant transactionParticipant;

    @Override
    public void afterPropertiesSet()
    {
        establishService(IApplicationServerApi.class, service, IApplicationServerApi.SERVICE_NAME,
                IApplicationServerApi.SERVICE_URL);
        super.afterPropertiesSet();
    }

    @RequestMapping(
            { IApplicationServerApi.SERVICE_URL, "/openbis" + IApplicationServerApi.SERVICE_URL,
                    "/openbis/openbis" + IApplicationServerApi.SERVICE_URL })
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        super.handleRequest(request, response);
    }

    @Override protected Object invoke(final RemoteInvocation invocation, final Object targetObject)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        try
        {
            String transactionId = (String) invocation.getAttribute(TransactionConst.TRANSACTION_ID_ATTRIBUTE);
            String transactionCoordinatorSecret = (String) invocation.getAttribute(TransactionConst.TRANSACTION_COORDINATOR_SECRET_ATTRIBUTE);

            if (TransactionConst.BEGIN_TRANSACTION_METHOD.equals(invocation.getMethodName()))
            {
                transactionParticipant.beginTransaction(transactionId, transactionCoordinatorSecret);
                return null;
            } else if (TransactionConst.PREPARE_TRANSACTION_METHOD.equals(invocation.getMethodName()))
            {
                transactionParticipant.prepareTransaction(transactionId, transactionCoordinatorSecret);
                return null;
            } else if (TransactionConst.COMMIT_TRANSACTION_METHOD.equals(invocation.getMethodName()))
            {
                transactionParticipant.commitTransaction(transactionId, transactionCoordinatorSecret);
                return null;
            } else if (TransactionConst.ROLLBACK_TRANSACTION_METHOD.equals(invocation.getMethodName()))
            {
                transactionParticipant.rollbackTransaction(transactionId, transactionCoordinatorSecret);
                return null;
            } else
            {
                return transactionParticipant.executeOperation(transactionId, transactionCoordinatorSecret, new ITransactionParticipantOperation()
                {
                    @Override public String getOperationName()
                    {
                        return invocation.getMethodName();
                    }

                    @Override public Object executeOperation() throws Throwable
                    {
                        return invocation.invoke(service);
                    }
                });
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | Error | RuntimeException e)
        {
            throw e;
        } catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

}
