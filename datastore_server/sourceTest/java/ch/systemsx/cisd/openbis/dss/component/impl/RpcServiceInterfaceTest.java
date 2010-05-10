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

package ch.systemsx.cisd.openbis.dss.component.impl;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.rpc.shared.RpcServiceInterfaceDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class RpcServiceInterfaceTest extends AssertJUnit
{
    @Test
    public void testToString()
    {
        final RpcServiceInterfaceDTO iface = new RpcServiceInterfaceDTO();
        iface.setInterfaceName("DSS Generic");
        iface.setInterfaceUrlSuffix("/rpc/dss-generic/v1");
        iface.setInterfaceMajorVersion(1);
        iface.setInterfaceMinorVersion(7);
        assertEquals("RpcServiceInterface[DSS Generic,/rpc/dss-generic/v1,v.1.7]", iface.toString());
    }

    @Test
    public void testEquality()
    {
        final int majorVersion = (int) (Math.random() * 10);
        final int minorVersion = (int) (Math.random() * 100);
        final RpcServiceInterfaceDTO iface1 = new RpcServiceInterfaceDTO();
        iface1.setInterfaceName("DSS Generic");
        iface1.setInterfaceUrlSuffix("/rpc/dss-generic/v1");
        iface1.setInterfaceMajorVersion(majorVersion);
        iface1.setInterfaceMinorVersion(minorVersion);

        final RpcServiceInterfaceDTO iface2 = new RpcServiceInterfaceDTO();
        iface2.setInterfaceName("DSS Generic");
        iface2.setInterfaceUrlSuffix("/rpc/dss-generic/v1");
        iface2.setInterfaceMajorVersion(majorVersion);
        iface2.setInterfaceMinorVersion(minorVersion);

        assertTrue(iface1 != iface2);
        assertEquals(iface1, iface2);
        assertEquals(iface1.hashCode(), iface2.hashCode());
    }
}
