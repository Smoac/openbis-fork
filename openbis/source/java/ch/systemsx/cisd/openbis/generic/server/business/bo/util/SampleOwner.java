package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import org.apache.commons.lang.builder.HashCodeBuilder;

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

    public SampleOwner(final SpacePE spaceOrNull)
    {
        this.spaceOrNull = spaceOrNull;
    }

    public static SampleOwner createSpace(final SpacePE group)
    {
        return new SampleOwner(group);
    }

    public static SampleOwner createDatabaseInstance()
    {
        return new SampleOwner(null);
    }

    public boolean isSpaceLevel()
    {
        return spaceOrNull != null;
    }

    public boolean isDatabaseInstanceLevel()
    {
        return spaceOrNull == null;
    }

    public SpacePE tryGetSpace()
    {
        return spaceOrNull;
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
            return "db instance";
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
            return that.isDatabaseInstanceLevel();
        }
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(spaceOrNull);
        return builder.toHashCode();
    }
}
