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

package ch.systemsx.cisd.openbis.generic.server;

import javax.annotation.Resource;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.common.api.IRpcServiceNameServer;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.common.spring.ServiceExceptionTranslator;

/**
 * A servlet that exports the name server via the HttpInvoker interface.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Controller
@RequestMapping(
    { NameServerServlet.NAME_SERVER_URL, "/openbis" + NameServerServlet.NAME_SERVER_URL })
public class NameServerServlet extends HttpInvokerServiceExporter
{
    private final static String NAME_SERVER_URL = IRpcServiceNameServer.PREFFERED_URL_SUFFIX;

    private final static String NAME_SERVER_SERVICE_NAME =
            IRpcServiceNameServer.PREFFERED_SERVICE_NAME;

    @Resource(name = IRpcServiceNameServer.PREFFERED_BEAN_NAME)
    private RpcServiceNameServer nameServer;

    @Override
    public void afterPropertiesSet()
    {
        setServiceInterface(IRpcServiceNameServer.class);
        setService(nameServer);
        setInterceptors(new Object[]
            { new ServiceExceptionTranslator() });
        RpcServiceInterfaceVersionDTO ifaceVersion =
                new RpcServiceInterfaceVersionDTO(NAME_SERVER_SERVICE_NAME, NAME_SERVER_URL, 1, 0);
        nameServer.addSupportedInterfaceVersion(ifaceVersion);
        super.afterPropertiesSet();
    }
}
