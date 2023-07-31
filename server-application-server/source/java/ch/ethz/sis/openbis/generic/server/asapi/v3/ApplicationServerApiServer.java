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
import java.lang.reflect.Method;
import java.util.Arrays;

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
    private IApplicationServerApi applicationServerApi;

    @Autowired
    private ITransactionParticipant transactionParticipant;

    @Override
    public void afterPropertiesSet()
    {
        establishService(IApplicationServerApi.class, applicationServerApi, IApplicationServerApi.SERVICE_NAME,
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
        Method method = null;

        try
        {
            method = applicationServerApi.getClass().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
        } catch (Exception ignore)
        {
        }

        if (method != null)
        {
            return method.invoke(applicationServerApi, invocation.getArguments());
        }

        try
        {
            method = transactionParticipant.getClass().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
        } catch (Exception ignore)
        {
        }

        if (method != null)
        {
            return method.invoke(transactionParticipant, invocation.getArguments());
        }

        throw new NoSuchMethodException("No method found with name: " + invocation.getMethodName() + " and argument types: " + Arrays.toString(
                invocation.getParameterTypes()));
    }

}
