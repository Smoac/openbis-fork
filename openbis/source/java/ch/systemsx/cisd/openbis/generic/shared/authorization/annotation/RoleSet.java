/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.authorization.annotation;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.openbis.generic.shared.authorization.Role;
import ch.systemsx.cisd.openbis.generic.shared.authorization.Role.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;

/**
 * An enumeration which defines some role sets.
 * 
 * @author Christian Ribeaud
 */
public enum RoleSet
{
    NONE(),

    INSTANCE_ADMIN(instanceRole(RoleCode.ADMIN)),
    
    SPACE_ADMIN(INSTANCE_ADMIN, spaceRole(RoleCode.ADMIN)),

    POWER_USER(SPACE_ADMIN, spaceRole(RoleCode.POWER_USER)),

    USER(POWER_USER, spaceRole(RoleCode.USER)),

    INSTANCE_ADMIN_OBSERVER(INSTANCE_ADMIN, instanceRole(RoleCode.OBSERVER)),
    
    OBSERVER(USER, spaceRole(RoleCode.OBSERVER)),

    ETL_SERVER(INSTANCE_ADMIN, spaceRole(RoleCode.ETL_SERVER), instanceRole(RoleCode.ETL_SERVER));

    private final Set<Role> roles;

    private RoleSet(final RoleSet roleSet, final Role... roles)
    {
        this(roles);
        this.roles.addAll(roleSet.roles);
    }

    private RoleSet(final Role... roles)
    {
        this.roles = new LinkedHashSet<Role>();
        this.roles.addAll(Arrays.asList(roles));
    }

    private static Role spaceRole(final RoleCode roleCode)
    {
        return createRole(RoleLevel.SPACE, roleCode);
    }

    private static Role instanceRole(final RoleCode roleCode)
    {
        return createRole(RoleLevel.INSTANCE, roleCode);
    }

    private static Role createRole(final RoleLevel level, final RoleCode roleCode)
    {
        return new Role(level, roleCode);
    }

    public final Set<Role> getRoles()
    {
        return roles;
    }

    @Override
    public String toString()
    {
        return CollectionUtils.abbreviate(roles, -1);
    }
}
