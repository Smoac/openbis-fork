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

import java.io.Serializable;

import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier.Constants;

/**
 * Identifies an owner of a sample: group or database instance.
 * 
 * @author Tomasz Pylak
 */
public class SampleOwnerIdentifier extends AbstractHashable implements Serializable,
        Comparable<SampleOwnerIdentifier>
{
    private static final long serialVersionUID = IServer.VERSION;

    // if not null, sample is defined on the group level
    private GroupIdentifier groupIdentOrNull;

    // if not null, sample is defined on the database instance level
    private DatabaseInstanceIdentifier databaseInstanceIdentOrNull;

    protected SampleOwnerIdentifier(final DatabaseInstanceIdentifier databaseInstanceIdentOrNull,
            final GroupIdentifier groupIdentOrNull)
    {
        this.databaseInstanceIdentOrNull = databaseInstanceIdentOrNull;
        this.groupIdentOrNull = groupIdentOrNull;
    }

    /** Database-instance level {@link SampleOwnerIdentifier}. */
    public SampleOwnerIdentifier(final DatabaseInstanceIdentifier instanceIdentifier)
    {
        this(checkNotNull(instanceIdentifier), null);
    }

    private static DatabaseInstanceIdentifier checkNotNull(
            final DatabaseInstanceIdentifier identifier)
    {
        assert identifier != null : "database identifier cannot be null";
        return identifier;
    }

    /** Group level {@link SampleOwnerIdentifier}. */
    public SampleOwnerIdentifier(final GroupIdentifier groupIdentifier)
    {
        this(null, checkNotNull(groupIdentifier));
    }

    private static GroupIdentifier checkNotNull(final GroupIdentifier identifier)
    {
        assert identifier != null : "space identifier cannot be null";
        return identifier;
    }

    /** @return if sample is connected to its home group */
    public boolean isInsideHomeGroup()
    {
        return isGroupLevel() && groupIdentOrNull.isHomeSpace();
    }

    /**
     * true if sample belongs to the group. {@link #isDatabaseInstanceLevel()} will return false in
     * such a case.
     */
    public boolean isGroupLevel()
    {
        return groupIdentOrNull != null;
    }

    /**
     * true if sample belongs to the database instance. {@link #isGroupLevel()} will return false in
     * such a case.
     */
    public boolean isDatabaseInstanceLevel()
    {
        return databaseInstanceIdentOrNull != null;
    }

    @Override
    public String toString()
    {
        if (isGroupLevel())
        {
            if (isInsideHomeGroup())
            {
                return "";
            } else
            {
                return groupIdentOrNull.toString() + Constants.IDENTIFIER_SEPARATOR;
            }
        } else if (isDatabaseInstanceLevel())
        {
            if (databaseInstanceIdentOrNull.isHomeDatabase())
            {
                return "" + Constants.IDENTIFIER_SEPARATOR;
            } else
            {
                return databaseInstanceIdentOrNull.getDatabaseInstanceCode()
                        + Constants.DATABASE_INSTANCE_SEPARATOR + Constants.IDENTIFIER_SEPARATOR;
            }
        } else
        {
            throw new IllegalStateException("sample owner is unknown");
        }
    }

    /**
     * It is a good pattern to use {@link #isDatabaseInstanceLevel()} before calling this method.
     * 
     * @return The database instance which is the owner or null if the owner is not a database
     *         instance but a group.
     */
    public DatabaseInstanceIdentifier getDatabaseInstanceLevel()
    {
        return databaseInstanceIdentOrNull;
    }

    /**
     * It is a good pattern to use {@link #isGroupLevel()} before calling this method.
     * 
     * @return The group which is the owner or null if the owner is not a group, but database
     *         instance.
     */
    public GroupIdentifier getGroupLevel()
    {
        return groupIdentOrNull;
    }

    // for bean conversion only!
    @Deprecated
    public void setGroupLevel(final GroupIdentifier groupIdentOrNull)
    {
        this.groupIdentOrNull = groupIdentOrNull;
    }

    // for bean conversion only!
    @Deprecated
    public void setDatabaseInstanceLevel(
            final DatabaseInstanceIdentifier databaseInstanceIdentOrNull)
    {
        this.databaseInstanceIdentOrNull = databaseInstanceIdentOrNull;
    }

    //
    // Comparable
    //

    public int compareTo(final SampleOwnerIdentifier other)
    {
        if (isGroupLevel())
        {
            if (other.isGroupLevel())
            {
                return getGroupLevel().compareTo(other.getGroupLevel());
            } else
            {
                return 1;
            }
        } else if (isDatabaseInstanceLevel())
        {
            if (other.isGroupLevel())
            {
                return -1;
            } else
            {
                return getDatabaseInstanceLevel().compareTo(other.getDatabaseInstanceLevel());
            }
        } else
            throw new IllegalStateException("sample owner is unknown");
    }
}
