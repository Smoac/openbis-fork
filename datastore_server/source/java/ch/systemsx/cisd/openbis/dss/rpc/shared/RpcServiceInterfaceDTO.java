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
public class RpcServiceInterfaceDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String interfaceName;

    private String interfaceUrlSuffix;

    private int interfaceMajorVersion;

    private int interfaceMinorVersion;

    public RpcServiceInterfaceDTO()
    {
        interfaceName = "Unknown";
        interfaceUrlSuffix = "unknown";
        interfaceMajorVersion = 0;
        interfaceMinorVersion = 0;
    }

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

    /**
     * The major version of the interface. E.g., an interface with version 2.11 has major version 2.
     */
    public int getInterfaceMajorVersion()
    {
        return interfaceMajorVersion;
    }

    public void setInterfaceMajorVersion(int interfaceMajorVersion)
    {
        this.interfaceMajorVersion = interfaceMajorVersion;
    }

    /**
     * The major version of the interface. E.g., an interface with version 2.11 has minor version
     * 11.
     */
    public int getInterfaceMinorVersion()
    {
        return interfaceMinorVersion;
    }

    public void setInterfaceMinorVersion(int interfaceMinorVersion)
    {
        this.interfaceMinorVersion = interfaceMinorVersion;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (false == obj instanceof RpcServiceInterfaceDTO)
            return false;

        RpcServiceInterfaceDTO other = (RpcServiceInterfaceDTO) obj;
        return getInterfaceName().equals(other.getInterfaceName())
                && getInterfaceUrlSuffix().equals(other.getInterfaceUrlSuffix())
                && getInterfaceMajorVersion() == other.getInterfaceMajorVersion()
                && getInterfaceMinorVersion() == other.getInterfaceMinorVersion();
    }

    @Override
    public int hashCode()
    {
        int hash = getInterfaceName().hashCode();
        hash = hash * 31 + getInterfaceUrlSuffix().hashCode();
        hash = hash * 31 + getInterfaceMajorVersion();
        hash = hash * 31 + getInterfaceMinorVersion();
        return hash;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("RpcServiceInterface[");
        sb.append(getInterfaceName());
        sb.append(",");
        sb.append(getInterfaceUrlSuffix());
        sb.append(",v.");
        sb.append(getInterfaceMajorVersion());
        sb.append(".");
        sb.append(getInterfaceMinorVersion());
        sb.append("]");
        return sb.toString();
    }
}
