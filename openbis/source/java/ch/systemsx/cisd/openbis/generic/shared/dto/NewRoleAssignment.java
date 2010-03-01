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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.common.annotation.BeanProperty;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * <i>Bean</i> for registering new role assignments.
 * <p>
 * It is used by the parser. This explains the {@link BeanProperty} annotations.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class NewRoleAssignment extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final NewRoleAssignment[] EMPTY_ARRAY = new NewRoleAssignment[0];

    private DatabaseInstanceIdentifier databaseInstanceIdentifier;

    private GroupIdentifier groupIdentifier;

    private Grantee grantee;

    private RoleCode role;

    public final RoleCode getRole()
    {
        return role;
    }

    @BeanProperty(label = "role")
    public final void setRole(final RoleCode role)
    {
        this.role = role;
    }

    public final DatabaseInstanceIdentifier getDatabaseInstanceIdentifier()
    {
        return databaseInstanceIdentifier;
    }

    @BeanProperty(label = "instance")
    public final void setDatabaseInstanceIdentifier(
            final DatabaseInstanceIdentifier databaseInstanceIdentifier)
    {
        this.databaseInstanceIdentifier = databaseInstanceIdentifier;
    }

    public final GroupIdentifier getGroupIdentifier()
    {
        return groupIdentifier;
    }

    @BeanProperty(label = "space")
    public final void setGroupIdentifier(final GroupIdentifier groupIdentifier)
    {
        this.groupIdentifier = groupIdentifier;
    }

    public final Grantee getGrantee()
    {
        return grantee;
    }

    @BeanProperty(label = "grantee")
    public final void setGrantee(final Grantee grantee)
    {
        this.grantee = grantee;
    }

    //
    // AbstractHashable
    //

    @Override
    public final String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(grantee).append("=").append(role).append("@");
        if (getGroupIdentifier() == null)
        {
            builder.append(getDatabaseInstanceIdentifier());
        } else
        {
            builder.append(getGroupIdentifier());
        }
        return builder.toString();
    }
}
