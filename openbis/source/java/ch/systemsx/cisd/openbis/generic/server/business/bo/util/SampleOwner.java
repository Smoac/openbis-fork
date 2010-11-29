package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Determines who is the <i>owner</i> of the sample: the space or to the database instance.
 * <p>
 * Stores the owner <i>PEs</i>.
 * </p>
 */
public final class SampleOwner
{
    // if filled, databaseInstanceOrNull must be null
    private SpacePE spaceOrNull;

    // if filled, spaceOrNull must be null
    private DatabaseInstancePE databaseInstanceOrNull;

    public SampleOwner(final SpacePE spaceOrNull, final DatabaseInstancePE databaseInstanceOrNull)
    {
        assert spaceOrNull == null || databaseInstanceOrNull == null;
        assert spaceOrNull != null || databaseInstanceOrNull != null;
        this.spaceOrNull = spaceOrNull;
        this.databaseInstanceOrNull = databaseInstanceOrNull;
    }

    public static SampleOwner createSpace(final SpacePE group)
    {
        return new SampleOwner(group, null);
    }

    public static SampleOwner createDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        return new SampleOwner(null, databaseInstance);
    }

    public boolean isSpaceLevel()
    {
        return spaceOrNull != null;
    }

    public boolean isDatabaseInstanceLevel()
    {
        return databaseInstanceOrNull != null;
    }

    public SpacePE tryGetSpace()
    {
        return spaceOrNull;
    }

    public DatabaseInstancePE tryGetDatabaseInstance()
    {
        return databaseInstanceOrNull;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        if (isSpaceLevel())
        {
            return "space: " + spaceOrNull;
        } else
        {
            return "db instance: " + databaseInstanceOrNull;
        }
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SampleOwner == false)
        {
            return false;
        }
        final SampleOwner that = (SampleOwner) obj;
        if (isSpaceLevel())
        {
            return this.spaceOrNull.equals(that.spaceOrNull);
        } else
        {
            return this.databaseInstanceOrNull.equals(that.databaseInstanceOrNull);
        }
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(spaceOrNull);
        builder.append(databaseInstanceOrNull);
        return builder.toHashCode();
    }
}
