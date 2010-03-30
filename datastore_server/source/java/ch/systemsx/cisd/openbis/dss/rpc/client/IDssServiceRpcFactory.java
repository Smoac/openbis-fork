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

package ch.systemsx.cisd.openbis.dss.rpc.client;

import ch.systemsx.cisd.openbis.dss.rpc.shared.IDssServiceRpcV1;

/**
 * The interface for creating proxies to the data store server.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDssServiceRpcFactory
{

    public abstract IDssServiceRpcV1 getServiceV1(String serviceURL,
            boolean getServerCertificateFromServer) throws IncompatibleAPIVersionsException;

}