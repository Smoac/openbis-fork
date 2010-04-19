package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

/**
 * Contains data which uniquely define a dataset
 * 
 * @author Tomasz Pylak
 */
public interface IDatasetIdentifier
{
    /** a code of the dataset */
    String getDatasetCode();

    /** a code which points to the datastore server on which the dataset is accessible */
    String getDatastoreCode();
}