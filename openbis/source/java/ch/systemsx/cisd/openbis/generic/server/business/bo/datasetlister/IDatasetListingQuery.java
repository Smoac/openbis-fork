/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Date;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongSetMapper;

/**
 * A {@link TransactionQuery} interface for obtaining large sets of dataset-related entities from
 * the database.
 * <p>
 * This interface is intended to be used only in this package. The <code>public</code> modifier is
 * needed for creating a dynamic proxy by the EOD SQL library.
 * 
 * @author Tomasz Pylak
 */
@Private
@Friend(toClasses =
    { DataStoreRecord.class })
public interface IDatasetListingQuery extends TransactionQuery, IPropertyListingQuery
{
    public static final int FETCH_SIZE = 1000;

    /**
     * Returns the datasets for the given experiment id.
     */
    @Select(sql = "select * from data join external_data on data.id = external_data.data_id"
            + " where data.expe_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsForExperiment(LongSet experimentIds);

    /**
     * Returns the directly connected datasets for the given sample id.
     */
    @Select(sql = "select * from data join external_data on data.id = external_data.data_id where data.samp_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsForSample(long sampleId);

    /**
     * Returns the directly connected datasets for the given sample ids.
     */
    @Select(sql = "select * from data join external_data on data.id = external_data.data_id where data.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsForSamples(LongSet sampleIds);

    /**
     * Returns datasets that are newer than dataset with given id (<var>lastSeenDatasetId</var>) and
     * are directly connected with samples of sample type with given <var>sampleTypeId</var>.
     */
    @Select(sql = "SELECT * FROM data JOIN external_data ON data.id = external_data.data_id"
            + "    WHERE data.id > ?{2} AND data.samp_id IN (SELECT id FROM samples s WHERE s.saty_id=?{1})", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getNewDataSetsForSampleType(long sampleTypeId,
            long lastSeenDatasetId);

    /**
     * Returns datasets from store with given id that have status equal 'AVAILABLE' and were
     * modified before given date.
     */
    @Select(sql = "SELECT * FROM data JOIN external_data ON data.id = external_data.data_id"
            + "    WHERE data.dast_id = ?{1} AND external_data.status = 'AVAILABLE' "
            + "    AND data.registration_timestamp < ?{2}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getAvailableDataSetsRegisteredBefore(long dataStoreId,
            Date lastModificationDate);

    /**
     * Like {@link #getAvailableDataSetsRegisteredBefore(long, Date)} with additional condition for
     * data set type id.
     */
    @Select(sql = "SELECT * FROM data JOIN external_data ON data.id = external_data.data_id"
            + "    WHERE data.dast_id = ?{1} AND external_data.status = 'AVAILABLE' "
            + "    AND data.registration_timestamp < ?{2} AND data.dsty_id = ?{3}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getAvailableDataSetsRegisteredBeforeWithDataSetType(
            long dataStoreId, Date lastModificationDate, long dataSetTypeId);

    /**
     * Returns the directly connected dataset ids for the given sample id.
     */
    @Select(sql = "select id from data where data.samp_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<Long> getDatasetIdsForSample(long sampleId);

    /**
     * Returns ids of datasets directly connected to samples with given ids.
     */
    @Select(sql = "select id from data where data.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getDatasetIdsForSamples(LongSet sampleIds);

    @Select(sql = "select * from data_set_relationships where data_id_child = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRelationRecord> listParentDataSetIds(LongSet ids);

    @Select(sql = "select * from data_set_relationships where data_id_parent = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRelationRecord> listChildrenDataSetIds(LongSet ids);

    /**
     * Returns all datasets that are children of any specified dataset id.
     */
    @Select(sql = "SELECT * FROM data JOIN external_data ON data.id = external_data.data_id"
            + "    WHERE data.id IN (SELECT data_id_child FROM data_set_relationships r WHERE r.data_id_parent = any(?{1}))", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getChildDatasetsForParents(LongSet parentDatasetIds);

    /**
     * Returns the datasets that are parents of a dataset with given id.
     */
    @Select(sql = "SELECT * FROM data JOIN external_data ON data.id = external_data.data_id"
            + "    WHERE data.id IN (SELECT data_id_parent FROM data_set_relationships r WHERE r.data_id_child=?{1})", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getParentDatasetsForChild(long childDatasetId);

    /**
     * Returns the datasets for the given <var>datasetId</var>.
     */
    @Select("select * from data d join external_data e on d.id = e.data_id" + " where d.id=?{1}")
    public DatasetRecord getDataset(long datasetId);

    /**
     * Returns all datasets in the database.
     */
    @Select(sql = "select * from data d join external_data e on d.id = e.data_id"
            + "     where (select dbin_id from data_set_types t where t.id = d.dsty_id) = ?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasets(long dbInstanceId);

    @Select(sql = "select id, code from data_set_types where dbin_id=?{1}")
    public CodeRecord[] getDatasetTypes(long databaseInstanceId);

    @Select(sql = "select id, code, download_url from data_stores where dbin_id=?{1}")
    public DataStoreRecord[] getDataStores(long databaseInstanceId);

    @Select(sql = "select id, code from file_format_types where dbin_id=?{1}")
    public CodeRecord[] getFileFormatTypes(long databaseInstanceId);

    @Select(sql = "select id, code from locator_types")
    public CodeRecord[] getLocatorTypes();

    //
    // Datasets
    //

    /**
     * Returns the datasets for the given <var>entityIds</var>.
     */
    @Select(sql = "select * from data join external_data on data.id = external_data.data_id where data.id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasets(LongSet entityIds);

    /**
     * Returns the children dataset ids of the specified datasets.
     */
    @Select(sql = "select data_id_child from data_set_relationships where data_id_parent = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getDatasetChildrenIds(LongSet sampleId);

    //
    // Entity Properties
    //

    /**
     * Returns all generic property values of all datasets specified by <var>entityIds</var>.
     * 
     * @param entityIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select pr.ds_id as entity_id, etpt.prty_id, pr.value from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "   where pr.value is not null and pr.ds_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet entityIds);

    /**
     * Returns all controlled vocabulary property values of all datasets specified by
     * <var>entityIds</var>.
     * 
     * @param entityIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select pr.ds_id as entity_id, etpt.prty_id, cvte.id, cvte.covo_id, cvte.code, cvte.label, cvte.ordinal"
            + "      from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "      join controlled_vocabulary_terms cvte on pr.cvte_id=cvte.id"
            + "   where pr.ds_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
            LongSet entityIds);

    /**
     * Returns all material-type property values of all datasets specified by <var>entityIds</var>.
     * 
     * @param entityIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select pr.ds_id as entity_id, etpt.prty_id, m.id, m.code, m.maty_id"
            + "      from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "      join materials m on pr.mate_prop_id=m.id where pr.ds_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
            LongSet entityIds);

}
