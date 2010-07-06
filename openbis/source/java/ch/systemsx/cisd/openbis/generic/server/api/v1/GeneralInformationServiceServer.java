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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import javax.annotation.Resource;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.common.api.IRpcServiceNameServer;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.common.spring.ServiceExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Controller
@RequestMapping(
    { IGeneralInformationService.SERVICE_URL,
            "/openbis" + IGeneralInformationService.SERVICE_URL })
public class GeneralInformationServiceServer extends HttpInvokerServiceExporter
{
    @Resource(name = ResourceNames.GENERAL_INFORMATION_SERVICE_SERVER)
    private IGeneralInformationService service;
    
    @Resource(name = IRpcServiceNameServer.PREFFERED_BEAN_NAME)
    private RpcServiceNameServer nameServer;
    
    @Override
    public void afterPropertiesSet()
    {
        setServiceInterface(IGeneralInformationService.class);
        setService(service);
        setInterceptors(new Object[]
            { new ServiceExceptionTranslator() });

        int majorVersion = service.getMajorVersion();
        int minorVersion = service.getMinorVersion();
        RpcServiceInterfaceVersionDTO ifaceVersion =
                new RpcServiceInterfaceVersionDTO(IGeneralInformationService.SERVICE_NAME,
                        IGeneralInformationService.SERVICE_URL, majorVersion, minorVersion);
        nameServer.addSupportedInterfaceVersion(ifaceVersion);
        super.afterPropertiesSet();
    }
}
