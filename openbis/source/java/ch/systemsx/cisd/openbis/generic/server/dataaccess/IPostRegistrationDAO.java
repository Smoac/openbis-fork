package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

public interface IPostRegistrationDAO
{
    /**
     * Adds dataset to post-registration queue.
     */
    public void addDataSet(DataPE dataSet);

    /**
     * Removes dataset from the post-registration queue.
     */
    public void removeDataSet(DataPE dataSet);

    /**
     * Find all datasets wich are in the post-registration queue.
     */
    public Collection<Long> listDataSetsForPostRegistration();

}
