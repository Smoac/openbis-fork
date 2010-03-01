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

package ch.systemsx.cisd.openbis.generic.shared.authorization;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;

/**
 * A well defined role, composed of a {@link RoleLevel} and a {@link RoleCode}.
 * 
 * @author Christian Ribeaud
 */
public class Role
{
    private final RoleLevel roleLevel;

    private final RoleCode roleName;

    public Role(final RoleLevel roleLevel, final RoleCode roleName)
    {
        assert roleLevel != null : "Unspecified role level";
        assert roleName != null : "Unspecified role name";
        this.roleLevel = roleLevel;
        this.roleName = roleName;
    }

    public final RoleLevel getRoleLevel()
    {
        return roleLevel;
    }

    public final RoleCode getRoleName()
    {
        return roleName;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Role == false)
        {
            return false;
        }
        final Role that = (Role) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(roleLevel, that.roleLevel);
        builder.append(roleName, that.roleName);
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(roleLevel);
        builder.append(roleName);
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return roleLevel + "." + roleName;
    }

    //
    // Helper classes
    //

    public static enum RoleLevel
    {
        INSTANCE, SPACE;
    }

}
