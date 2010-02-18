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

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;

/**
 * Stores the {@link Role} and the "owner" to which this role is connected: database instance or a
 * group.
 * <p>
 * Note that {@link #equals(Object)} resp. {@link #hashCode()} are not overridden and so do not
 * consider the <code>owner</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class RoleWithIdentifier extends Role
{
    private final DatabaseInstancePE databaseInstanceOrNull;

    private final GroupPE groupOrNull;

    @Private
    RoleWithIdentifier(final RoleLevel roleGroup, final RoleCode roleName,
            final DatabaseInstancePE databaseInstanceOrNull, final GroupPE groupOrNull)
    {
        super(roleGroup, roleName);
        if (RoleLevel.GROUP.equals(roleGroup))
        {
            assert groupOrNull != null : "Unspecified identifier";
            assert databaseInstanceOrNull == null;
        } else
        {
            assert groupOrNull == null;
            assert databaseInstanceOrNull != null : "Unspecified identifier";
        }
        this.databaseInstanceOrNull = databaseInstanceOrNull;
        this.groupOrNull = groupOrNull;
    }

    /**
     * This method can be called only if role is defined on the database instance level.
     * 
     * @return database instance to which the role is assigned
     */
    public final DatabaseInstancePE getAssignedDatabaseInstance()
    {
        assert databaseInstanceOrNull != null;
        return databaseInstanceOrNull;
    }

    /**
     * This method can be called only if role is defined on the group level.
     * 
     * @return group to which the role is assigned
     */
    public final GroupPE getAssignedGroup()
    {
        assert groupOrNull != null;
        return groupOrNull;
    }

    /**
     * Create a <code>RoleWithCode</code> from given <var>roleAssignment</var>.
     */
    public final static RoleWithIdentifier createRole(final RoleAssignmentPE roleAssignment)
    {
        assert roleAssignment != null : "Unspecified role assignment";
        final RoleLevel roleGroup = figureRoleLevel(roleAssignment);
        final RoleCode roleName = roleAssignment.getRole();
        final DatabaseInstancePE databaseInstance = roleAssignment.getDatabaseInstance();
        final GroupPE group = roleAssignment.getGroup();
        return new RoleWithIdentifier(roleGroup, roleName, databaseInstance, group);
    }

    private static RoleLevel figureRoleLevel(final RoleAssignmentPE roleAssignment)
    {
        assert roleAssignment.getDatabaseInstance() == null || roleAssignment.getGroup() == null : "Either the space or the database instance must be null";
        final RoleLevel roleGroup =
                roleAssignment.getDatabaseInstance() != null ? RoleLevel.INSTANCE : roleAssignment
                        .getGroup() != null ? RoleLevel.GROUP : null;
        assert roleGroup != null : "Either the space or the database instance must not be null";
        return roleGroup;
    }

    //
    // Role
    //

    @Override
    public final String toString()
    {
        final StringBuilder builder = new StringBuilder(super.toString());
        builder.append("<").append(createOwnerDescription()).append(">");
        return builder.toString();
    }

    private String createOwnerDescription()
    {
        if (databaseInstanceOrNull != null)
        {
            return IdentifierHelper.createDatabaseInstanceIdentifier(databaseInstanceOrNull)
                    .toString();
        } else
        {
            return IdentifierHelper.createGroupIdentifier(groupOrNull).toString();
        }
    }
}
