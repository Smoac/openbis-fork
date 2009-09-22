package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;

/**
 * Determines who is the <i>owner</i> of the sample: the group or to the database instance.
 * <p>
 * Stores the owner <i>PEs</i>.
 * </p>
 */
public final class SampleOwner
{
    // if filled, databaseInstanceOrNull must be null
    private GroupPE groupOrNull;

    // if filled, groupOrNull must be null
    private DatabaseInstancePE databaseInstanceOrNull;

    public SampleOwner(final GroupPE groupOrNull, final DatabaseInstancePE databaseInstanceOrNull)
    {
        assert groupOrNull == null || databaseInstanceOrNull == null;
        assert groupOrNull != null || databaseInstanceOrNull != null;
        this.groupOrNull = groupOrNull;
        this.databaseInstanceOrNull = databaseInstanceOrNull;
    }

    public static SampleOwner createGroup(final GroupPE group)
    {
        return new SampleOwner(group, null);
    }

    public static SampleOwner createDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        return new SampleOwner(null, databaseInstance);
    }

    public boolean isGroupLevel()
    {
        return groupOrNull != null;
    }

    public boolean isDatabaseInstanceLevel()
    {
        return databaseInstanceOrNull != null;
    }

    public GroupPE tryGetGroup()
    {
        return groupOrNull;
    }

    public DatabaseInstancePE tryGetDatabaseInstance()
    {
        return databaseInstanceOrNull;
    }
}
