package ch.systemsx.cisd.openbis.generic.shared;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/** Interface defining methods for database instance access. */
public interface IDatabaseInstanceFinder
{
    /**
     * Tries to find the database instance with specified code.
     * 
     * @return <code>null</code> if not found.
     */
    public DatabaseInstancePE tryFindDatabaseInstanceByCode(final String databaseInstanceCode);

    /**
     * Tries to find the database instance with specified UUID.
     * 
     * @return <code>null</code> if not found.
     */
    public DatabaseInstancePE tryFindDatabaseInstanceByUUID(final String databaseInstanceUUID);

    /**
     * Returns the home database instance.
     */
    public DatabaseInstancePE getHomeDatabaseInstance();
}
