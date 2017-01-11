/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common;

import ch.systemsx.cisd.openbis.common.api.client.IServicePinger;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.EncapsulatedCommonServer;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class ServiceFinderUtils
{
    public static EncapsulatedCommonServer getEncapsulatedCommonServer(String sessionToken, String openBisServerUrl)
    {
        ServiceFinder finder = new ServiceFinder("openbis", "/rmi-common");
        ICommonServer commonServer =
                finder.createService(ICommonServer.class, openBisServerUrl,
                        new IServicePinger<ICommonServer>()
                            {
                                @Override
                                public void ping(ICommonServer service)
                                {
                                    service.getVersion();
                                }
                            });
        return EncapsulatedCommonServer.create(commonServer, sessionToken);
    }
}