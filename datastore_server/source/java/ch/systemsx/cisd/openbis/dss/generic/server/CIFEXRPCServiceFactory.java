/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.RPCServiceFactory;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

final class CIFEXRPCServiceFactory implements ICIFEXRPCServiceFactory
{
    private static final long serialVersionUID = 1L;
    private final String cifexURL;
    
    private transient ICIFEXRPCService service;
    
    CIFEXRPCServiceFactory(String cifexURL)
    {
        this.cifexURL = cifexURL;
    }

    public ICIFEXRPCService createService()
    {
        if (service == null)
        {
            final String serviceURL = cifexURL + Constants.CIFEX_RPC_PATH;
            service = RPCServiceFactory.createServiceProxy(serviceURL, true);
            final int serverVersion = service.getVersion();
            if (ICIFEXRPCService.VERSION != serverVersion)
            {
                throw new EnvironmentFailureException("The version of the CIFEX server is not "
                        + ICIFEXRPCService.VERSION + " but " + serverVersion + ".");
            }
        }
        return service;
    }
}