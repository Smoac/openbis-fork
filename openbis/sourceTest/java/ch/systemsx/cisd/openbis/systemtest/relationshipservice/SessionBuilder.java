/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.relationshipservice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class SessionBuilder extends Builder<String>
{

    private String userName;

    private List<Pair<RoleCode, Space>> spaceRoles;

    private List<RoleCode> instanceRoles;

    public SessionBuilder(ICommonServerForInternalUse commonServer, IGenericServer genericServer)
    {
        super(commonServer, genericServer);
        this.userName = UUID.randomUUID().toString();
        this.spaceRoles = new ArrayList<Pair<RoleCode, Space>>();
        this.instanceRoles = new ArrayList<RoleCode>();
    }

    public SessionBuilder withSpaceRole(RoleCode role, Space space)
    {
        this.spaceRoles.add(new Pair<RoleCode, Space>(role, space));
        return this;
    }

    public SessionBuilder withInstanceRole(RoleCode role)
    {
        this.instanceRoles.add(role);
        return this;
    }

    @Override
    public String create()
    {
        commonServer.registerPerson(systemSession, userName);

        for (Pair<RoleCode, Space> role : spaceRoles)
        {
            commonServer.registerSpaceRole(systemSession, role.first,
                    new SpaceIdentifier(role.second.getInstance().getCode(), role.second
                            .getCode()), Grantee.createPerson(this.userName));
        }

        for (RoleCode role : instanceRoles)
        {
            commonServer.registerInstanceRole(systemSession, role, Grantee
                    .createPerson(this.userName));
        }

        return commonServer.tryToAuthenticate(userName, "pwd").getSessionToken();
    }

    private static class Pair<X, Y>
    {
        public final X first;

        public final Y second;

        public Pair(X first, Y second)
        {
            this.first = first;
            this.second = second;
        }
    }
}
