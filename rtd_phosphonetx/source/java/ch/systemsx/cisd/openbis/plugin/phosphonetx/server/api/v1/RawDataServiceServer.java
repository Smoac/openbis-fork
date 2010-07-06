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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.api.v1;

import javax.annotation.Resource;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.common.api.IRpcServiceNameServer;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.common.spring.ServiceExceptionTranslator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IRawDataService;

/**
 * Server wrapping {@link IRawDataService}.
 * 
 * @author Franz-Josef Elmer
 */
@Controller
@RequestMapping(
    { IRawDataService.SERVER_URL, "/openbis" + IRawDataService.SERVER_URL })
public class RawDataServiceServer extends HttpInvokerServiceExporter
{
    @Resource(name = Constants.PHOSPHONETX_RAW_DATA_SERVICE)
    private IRawDataService service;

    @Resource(name = IRpcServiceNameServer.PREFFERED_BEAN_NAME)
    private RpcServiceNameServer nameServer;

    @Override
    public void afterPropertiesSet()
    {
        setServiceInterface(IRawDataService.class);
        setService(service);
        setInterceptors(new Object[]
            { new ServiceExceptionTranslator() });
        int majorVersion = service.getMajorVersion();
        int minorVersion = service.getMinorVersion();
        RpcServiceInterfaceVersionDTO ifaceVersion =
                new RpcServiceInterfaceVersionDTO(IRawDataService.SERVICE_NAME,
                        IRawDataService.SERVER_URL, majorVersion, minorVersion);
        nameServer.addSupportedInterfaceVersion(ifaceVersion);
        super.afterPropertiesSet();
    }
}
