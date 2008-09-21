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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.common.utilities.StringUtilities;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Identifies a group.
 * 
 * @author Izabela Adamczyk
 */
public class GroupIdentifier extends DatabaseInstanceIdentifier implements
        Comparable<GroupIdentifier>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private String groupCodeOrNull;

    public static final String HOME_GROUP = null;

    public static GroupIdentifier createHome()
    {
        return new GroupIdentifier(HOME_GROUP, DatabaseInstanceIdentifier.HOME);
    }

    public GroupIdentifier(final DatabaseInstanceIdentifier databaseInstanceIdentifier,
            final String groupCode)
    {
        this(databaseInstanceIdentifier.getDatabaseInstanceCode(), groupCode);
    }

    public GroupIdentifier(final String databaseInstanceCode, final String groupCode)
    {
        super(databaseInstanceCode);
        setGroupCode(groupCode);
    }

    public final String getGroupCode()
    {
        return StringUtils.upperCase(groupCodeOrNull);
    }

    public final void setGroupCode(final String groupCode)
    {
        this.groupCodeOrNull = groupCode;
    }

    //
    // DatabaseInstanceIdentifier
    //

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof GroupIdentifier == false)
        {
            return false;
        }
        final GroupIdentifier that = (GroupIdentifier) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getDatabaseInstanceCode(), that.getDatabaseInstanceCode());
        builder.append(getGroupCode(), that.getGroupCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getDatabaseInstanceCode());
        builder.append(getGroupCode());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        if (getDatabaseInstanceCode() == null)
        {
            return Constants.IDENTIFIER_SEPARATOR + groupCodeOrNull;
        } else
        {
            return super.toString() + Constants.DATABASE_INSTANCE_SEPARATOR
                    + Constants.IDENTIFIER_SEPARATOR + groupCodeOrNull;
        }
    }

    public boolean isHomeGroup()
    {
        return groupCodeOrNull == null;
    }

    /** Do not use this constructor. It exists just to make automatic bean conversion possible */
    @Deprecated
    public GroupIdentifier()
    {
        super();
    }

    /** this method exists just to make automatic bean conversion possible */
    @Deprecated
    public void setHomeGroup(final boolean isHomeGroup)
    {
        // empty
    }

    //
    // Comparable
    //

    public final int compareTo(final GroupIdentifier other)
    {
        final int dbCompare = super.compareTo(other);
        if (dbCompare == 0)
        {
            return StringUtilities.compareNullable(getGroupCode(), other.getGroupCode());
        } else
        {
            return dbCompare;
        }
    }
}
