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

package ch.systemsx.cisd.openbis.dss.rpc.shared;

import java.io.Serializable;

/**
 * Describes an RPC interface supported by the data store server.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcInterface implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String interfaceName;

    private String interfaceUrlSuffix;

    /**
     * The name of this interface used for identification.
     */
    public String getInterfaceName()
    {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName)
    {
        this.interfaceName = interfaceName;
    }

    /**
     * The suffix added to the DSS URL to produce the URL for this interface. Used by a service
     * factory to create a proxy to the service.
     */
    public String getInterfaceUrlSuffix()
    {
        return interfaceUrlSuffix;
    }

    public void setInterfaceUrlSuffix(String interfaceUrlSuffix)
    {
        this.interfaceUrlSuffix = interfaceUrlSuffix;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("DssServiceRpcInterface[");
        sb.append(getInterfaceName());
        sb.append(",");
        sb.append(getInterfaceUrlSuffix());
        sb.append("]");
        return sb.toString();
    }
}
