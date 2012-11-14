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
import java.util.List;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.TypeMapper;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaProjectWithEntityId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongSetMapper;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.StringArrayMapper;

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
public interface IDatasetListingQuery extends BaseQuery, IPropertyListingQuery
{
    public static final int FETCH_SIZE = 1000;

    public final static String SELECT_ALL =
            "select * from data left outer join external_data on data.id = external_data.data_id left outer join link_data on data.id = link_data.data_id ";

    public final static String SELECT_ALL_EXTERNAL_DATAS =
            "select * from data join external_data on data.id = external_data.data_id ";

    /**
     * Returns the datasets for the given experiment id.
     */
    @Select(sql = SELECT_ALL + " WHERE data.expe_id = ?{1}")
    public DataIterator<DatasetRecord> getDatasetsForExperiment(long experimentId);

    @Select(sql = "with recursive connected_data as ( "
            + "select * from data as d left outer join external_data as ed on d.id = ed.data_id left outer join link_data as ld on d.id = ld.data_id "
            + "where expe_id = ?{1} "
            + "   or samp_id in (with recursive connected_samples as "
            + "                    (select id from samples where expe_id = ?{1} "
            + "                     union select s.id from connected_samples as cs "
            + "                                       inner join sample_relationships as sr on sr.sample_id_parent = cs.id "
            + "                                       left join samples as s on s.id = sr.sample_id_child) "
            + "                  select * from connected_samples) "
            + "union select d.*, ed.*, ld.* from connected_data as cd "
            + "                       inner join data_set_relationships as dr on dr.data_id_parent = cd.id "
            + "                       left join data as d on d.id = dr.data_id_child "
            + "                       left outer join external_data as ed on d.id = ed.data_id"
            + "                       left outer join link_data as ld on d.id = ld.data_id) "
            + "select * from connected_data")
    public DataIterator<DatasetRecord> getDataSetsForExperimentAndDescendents(long experimentId);

    /**
     * Returns the directly connected datasets for the given sample id.
     */
    @Select(sql = SELECT_ALL + " WHERE data.samp_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsForSample(long sampleId);

    /**
     * Returns the directly connected datasets for the given sample ids.
     */
    @Select(sql = SELECT_ALL + " WHERE data.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsForSamples(LongSet sampleIds);

    /**
     * Returns data sets that are newer than the data set of specified id.
     */
    @Select(sql = SELECT_ALL + " WHERE data.id > ?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getNewDataSets(long lastSeenDatasetId);

    /**
     * Returns datasets that are newer than dataset with given id (<var>lastSeenDatasetId</var>) and
     * are directly connected with samples of sample type with given <var>sampleTypeId</var>.
     */
    @Select(sql = SELECT_ALL
            + " WHERE data.id > ?{2} AND data.samp_id IN (SELECT id FROM samples s WHERE s.saty_id=?{1})", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getNewDataSetsForSampleType(long sampleTypeId,
            long lastSeenDatasetId);

    /**
     * Returns datasets from store with given id that have status equal 'AVAILABLE' and were
     * modified before given date.
     */
    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + "    WHERE data.dast_id = ?{1} AND external_data.status = 'AVAILABLE' "
            + "    AND data.registration_timestamp < ?{2} AND external_data.present_in_archive=?{3}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getAvailableExtDatasRegisteredBefore(long dataStoreId,
            Date lastModificationDate, boolean presentInArchive);

    /**
     * Like {@link #getAvailableExtDatasRegisteredBefore(long, Date, boolean)} with additional
     * condition for data set type id.
     */
    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + "    WHERE data.dast_id = ?{1} AND external_data.status = 'AVAILABLE' "
            + "    AND data.registration_timestamp < ?{2} AND external_data.present_in_archive=?{3} "
            + "    AND data.dsty_id = ?{4}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getAvailableExtDatasRegisteredBeforeWithDataSetType(
            long dataStoreId, Date lastModificationDate, boolean presentInArchive,
            long dataSetTypeId);

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
    @Select(sql = SELECT_ALL
            + "    WHERE data.id IN (SELECT data_id_child FROM data_set_relationships r WHERE r.data_id_parent = any(?{1}))", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getChildDatasetsForParents(LongSet parentDatasetIds);

    /**
     * Returns the datasets that are parents of a dataset with given id.
     */
    @Select(sql = SELECT_ALL
            + " WHERE data.id IN (SELECT data_id_parent FROM data_set_relationships r WHERE r.data_id_child=?{1})", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getParentDatasetsForChild(long childDatasetId);

    /**
     * Returns the datasets that are contained in a dataset with given id.
     */
    @Select(sql = SELECT_ALL + " WHERE data.ctnr_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getContainedDatasetsForContainer(long containerDatasetId);

    /**
     * Returns the datasets for the given <var>datasetId</var>.
     */
    @Select(SELECT_ALL + " where data.id=?{1}")
    public DatasetRecord getDataset(long datasetId);

    /**
     * Returns all datasets in the database.
     */
    @Select(sql = SELECT_ALL
            + " where (select dbin_id from data_set_types t where t.id = data.dsty_id) = ?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasets(long dbInstanceId);

    @Select(sql = "select id, code, data_set_kind from data_set_types where dbin_id=?{1}")
    public DataSetTypeRecord[] getDatasetTypes(long databaseInstanceId);

    @Select(sql = "select id, code, download_url from data_stores where dbin_id=?{1}")
    public DataStoreRecord[] getDataStores(long databaseInstanceId);

    @Select(sql = "select id, code from file_format_types where dbin_id=?{1}")
    public CodeRecord[] getFileFormatTypes(long databaseInstanceId);

    @Select(sql = "select id, code from locator_types")
    public CodeRecord[] getLocatorTypes();

    @Select(sql = "select id, code, label, url_template, is_openbis from external_data_management_systems where dbin_id=?{1}")
    public ExternalDataManagementSystemRecord[] getExternalDataManagementSystems(
            long databaseInstanceId);

    //
    // Datasets
    //

    /**
     * Returns the datasets for the given <var>entityIds</var>.
     */
    @Select(sql = SELECT_ALL + " where data.id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasets(LongSet entityIds);

    @Select(sql = SELECT_ALL + " where data.code = any(?{1})", parameterBindings =
        { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasets(String[] datasetCodes);

    @Select(sql = SELECT_ALL + " where data.dast_id = ?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsByDataStoreId(long dataStoreId);

    // NOTE: we list ALL data sets (even those in trash) using data_all table here
    @Select(sql = "SELECT code, share_id FROM data_all LEFT OUTER JOIN external_data "
            + "ON data_all.id = external_data.data_id WHERE data_all.dast_id = ?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetCodeWithShareIdRecord> getAllDatasetsWithShareIdsByDataStoreId(
            long dataStoreId);

    /**
     * Returns the children dataset ids of the specified datasets.
     */
    @Select(sql = "select data_id_child from data_set_relationships where data_id_parent = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getDatasetChildrenIds(LongSet sampleId);

    @Select(sql = "select id from data where ctnr_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getContainedDataSetIds(LongSet containerIDs);

    //
    // Entity Properties
    //

    /**
     * Returns all generic property values of all datasets specified by <var>entityIds</var>.
     * 
     * @param entityIds The set of sample ids to get the property values for.
     */
    @Select(sql = "SELECT pr.ds_id as entity_id, etpt.prty_id, etpt.script_id, pr.value, sc.script_type "
            + "      FROM data_set_properties pr"
            + "      JOIN data_set_type_property_types etpt ON pr.dstpt_id=etpt.id"
            + "      LEFT OUTER JOIN scripts sc ON etpt.script_id = sc.id"
            + "     WHERE pr.value is not null AND pr.ds_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet entityIds);

    /**
     * Returns property values for specified type code of all datasets specified by
     * <var>entityIds</var>.
     * 
     * @param entityIds The set of sample ids to get the property values for.
     * @param propertyTypeCode type code f properties we want to fetch
     */
    @Select(sql = "SELECT pr.ds_id as entity_id, etpt.prty_id, etpt.script_id, pr.value "
            + "      FROM data_set_properties pr"
            + "      JOIN data_set_type_property_types etpt ON pr.dstpt_id=etpt.id"
            + "      JOIN property_types pt ON etpt.prty_id=pt.id"
            + "     WHERE pr.value is not null AND pr.ds_id = any(?{1}) AND pt.code = ?{2}", parameterBindings =
        { LongSetMapper.class, TypeMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet entityIds, String propertyTypeCode);

    /**
     * Returns all controlled vocabulary property values of all datasets specified by
     * <var>entityIds</var>.
     * 
     * @param entityIds The set of sample ids to get the property values for.
     */
    @Select(sql = "SELECT pr.ds_id as entity_id, etpt.prty_id, etpt.script_id, cvte.id, cvte.covo_id, cvte.code, cvte.label, cvte.ordinal, cvte.is_official, cvte.description"
            + "      FROM data_set_properties pr"
            + "      JOIN data_set_type_property_types etpt ON pr.dstpt_id=etpt.id"
            + "      JOIN controlled_vocabulary_terms cvte ON pr.cvte_id=cvte.id"
            + "     WHERE pr.cvte_id is not null AND pr.ds_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
            LongSet entityIds);

    /**
     * Returns all material-type property values of all datasets specified by <var>entityIds</var>.
     * 
     * @param entityIds The set of sample ids to get the property values for.
     */
    @Select(sql = "SELECT pr.ds_id as entity_id, etpt.prty_id, etpt.script_id, m.id, m.code, m.maty_id"
            + "      FROM data_set_properties pr"
            + "      JOIN data_set_type_property_types etpt ON pr.dstpt_id=etpt.id"
            + "      JOIN materials m ON pr.mate_prop_id=m.id "
            + "     WHERE pr.mate_prop_id is not null AND pr.ds_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
            LongSet entityIds);

    @Select(sql = "WITH RECURSIVE connected_data(id, code, ctnr_id, dast_id, location) AS ("
            + "    SELECT d.id, d.code, d.ctnr_id, d.dast_id, ed.location FROM data AS d LEFT OUTER JOIN external_data AS ed ON d.id = ed.data_id WHERE d.code = ?{1}"
            + "  UNION ALL"
            + "    SELECT d.id, d.code, d.ctnr_id, d.dast_id, ed.location"
            + "    FROM connected_data AS cd INNER JOIN data AS d ON cd.id = d.ctnr_id LEFT OUTER JOIN external_data AS ed ON d.id = ed.data_id"
            + ")"
            + "SELECT c.id, c.code, c.ctnr_id, c.location, d.code as data_store_code, d.remote_url as data_store_url FROM connected_data c JOIN data_stores d ON c.dast_id = d.id")
    public DataIterator<DatasetLocationNodeRecord> listLocationsByDatasetCode(String datasetCode);

    @Select(sql = "select m.id as id, m.name as name, m.description as description, p.user_id as owner_name, "
            + " m.private as is_private, m.creation_date as creation_date, ma.data_id as entity_id "
            + " from metaprojects m, metaproject_assignments ma, persons p "
            + " where ma.data_id = any(?{1}) and m.owner = ?{2} and m.id = ma.mepr_id and m.owner = p.id", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MetaProjectWithEntityId> getMetaprojects(LongSet entityIds, Long userId);

}
