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

package ch.systemsx.cisd.openbis.generic.shared;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceFactory.ILimsServiceStubFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DefaultLimsServiceStubFactory implements ILimsServiceStubFactory
{
    public static final int SERVER_TIMEOUT_MIN = 5;

    public IETLLIMSService createServiceStub(String serverUrl)
    {
        return HttpInvokerUtils.createServiceStub(IETLLIMSService.class, serverUrl,
                SERVER_TIMEOUT_MIN);
    }

}
