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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.JsonRpcInterceptor;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.ObjectMapperResource;
import ch.systemsx.cisd.common.spring.ServiceExceptionTranslator;
import ch.systemsx.cisd.openbis.common.api.server.AbstractApiJsonServiceExporter;

/**
 * @author Franz-Josef Elmer
 */
@Controller
public class ApplicationServerApiJsonServer extends AbstractApiJsonServiceExporter
{
    @Resource(name = ObjectMapperResource.NAME)
    private ObjectMapper objectMapper;

    @Resource(name = ApplicationServerApi.INTERNAL_SERVICE_NAME)
    private IApplicationServerApi service;

    @Autowired
    private ITransactionParticipant transactionParticipant;

    private final ThreadLocal<Map<String, String>> operationAttributes = new ThreadLocal<>();

    @Override
    public void afterPropertiesSet() throws Exception
    {
        setObjectMapper(objectMapper);
        establishService(IApplicationServerApi.class, service, IApplicationServerApi.SERVICE_NAME,
                IApplicationServerApi.JSON_SERVICE_URL);

        setInterceptorList(List.of(new JsonRpcInterceptor()
        {
            @Override public void preHandleJson(final JsonNode json)
            {
                JsonNode attributesNode = json.get(TransactionConst.ATTRIBUTES);
                Map<String, String> attributesMap = new HashMap<>();

                if (attributesNode instanceof ObjectNode)
                {
                    Iterator<String> attributeNames = attributesNode.fieldNames();
                    while (attributeNames.hasNext())
                    {
                        String attributeName = attributeNames.next();
                        JsonNode attributeValue = attributesNode.get(attributeName);
                        attributesMap.put(attributeName, attributeValue.asText());
                    }
                }

                operationAttributes.set(attributesMap);
            }

            @Override public void preHandle(final Object target, final Method method, final List<JsonNode> params)
            {
            }

            @Override public void postHandle(final Object target, final Method method, final List<JsonNode> params, final JsonNode result)
            {
            }

            @Override public void postHandleJson(final JsonNode json)
            {
                operationAttributes.remove();
            }
        }));

        setInterceptors(new Object[] {
                new ServiceExceptionTranslator(),
                new MethodInterceptor()
                {
                    @Override public Object invoke(final MethodInvocation invocation) throws Throwable
                    {
                        Map<String, String> attributesMap = operationAttributes.get();

                        String transactionId = attributesMap.get(TransactionConst.TRANSACTION_ID_ATTRIBUTE);
                        String transactionCoordinatorSecret = attributesMap.get(TransactionConst.TRANSACTION_COORDINATOR_SECRET_ATTRIBUTE);

                        if (TransactionConst.BEGIN_TRANSACTION_METHOD.equals(invocation.getMethod().getName()))
                        {
                            transactionParticipant.beginTransaction(transactionId, transactionCoordinatorSecret);
                            return null;
                        } else if (TransactionConst.PREPARE_TRANSACTION_METHOD.equals(invocation.getMethod().getName()))
                        {
                            transactionParticipant.prepareTransaction(transactionId, transactionCoordinatorSecret);
                            return null;
                        } else if (TransactionConst.COMMIT_TRANSACTION_METHOD.equals(invocation.getMethod().getName()))
                        {
                            transactionParticipant.commitTransaction(transactionId, transactionCoordinatorSecret);
                            return null;
                        } else if (TransactionConst.ROLLBACK_TRANSACTION_METHOD.equals(invocation.getMethod().getName()))
                        {
                            transactionParticipant.rollbackTransaction(transactionId, transactionCoordinatorSecret);
                            return null;
                        } else
                        {
                            return transactionParticipant.executeOperation(transactionId, transactionCoordinatorSecret,
                                    new ITransactionParticipantOperation()
                                    {
                                        @Override public String getOperationName()
                                        {
                                            return invocation.getMethod().getName();
                                        }

                                        @Override public Object executeOperation() throws Throwable
                                        {
                                            return invocation.proceed();
                                        }
                            });
                        }
                    }
                }
        });

        super.afterPropertiesSet();
    }

    @RequestMapping(
    { IApplicationServerApi.JSON_SERVICE_URL, "/openbis" + IApplicationServerApi.JSON_SERVICE_URL })
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException,
            IOException
    {
        super.handleRequest(request, response);
    }
}
