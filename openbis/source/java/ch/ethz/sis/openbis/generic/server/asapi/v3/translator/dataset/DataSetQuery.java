/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.MaterialPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyAssignmentRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * @author pkupczyk
 */
public interface DataSetQuery extends ObjectQuery
{

    @Select(sql = "select d.id, ed.storage_confirmation as isStorageConfirmed, s.code as sampleCode, sc.code as sampleContainerCode, ss.code as sampleSpaceCode, "
            + "e.code as experimentCode, ep.code as experimentProjectCode, es.code as experimentSpaceCode from data d "
            + "left join external_data ed on d.id = ed.data_id "
            + "left join samples s on d.samp_id = s.id "
            + "left join samples sc on s.samp_id_part_of = sc.id "
            + "left join spaces ss on s.space_id = ss.id "
            + "left join experiments e on d.expe_id = e.id "
            + "left join projects ep on e.proj_id = ep.id "
            + "left join spaces es on ep.space_id = es.id "
            + "where d.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataSetAuthorizationRecord> getAuthorizations(LongSet dataSetIds);

    @Select(sql = "select d.id, d.code, d.is_derived as isDerived, d.data_producer_code as dataProducer, d.production_timestamp as dataProductionDate, d.access_timestamp as accessDate, d.modification_timestamp as modificationDate, d.registration_timestamp as registrationDate "
            + "from data d where d.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataSetBaseRecord> getDataSets(LongSet dataSetIds);

    @Select(sql = "select ed.data_id as objectId, ed.data_id as relatedId from external_data ed where ed.data_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getPhysicalDataIds(LongSet dataSetIds);

    @Select(sql = "select ed.data_id as id, ed.share_id as shareId, ed.location, ed.size, ed.status, ed.is_complete as isComplete, ed.present_in_archive as isPresentInArchive, ed.storage_confirmation as isStorageConfirmed, ed.speed_hint as speedHint "
            + "from external_data ed where ed.data_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PhysicalDataBaseRecord> getPhysicalDatas(LongSet dataSetIds);

    @Select(sql = "select ld.data_id as objectId, ld.data_id as relatedId from link_data ld where ld.data_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getLinkedDataIds(LongSet dataSetIds);

    @Select(sql = "select ld.data_id as id, ld.external_code as externalCode from link_data ld where ld.data_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<LinkedDataBaseRecord> getLinkedDatas(LongSet dataSetIds);

    @Select(sql = "select ld.data_id as objectId, ld.edms_id as relatedId from link_data ld where ld.data_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getExternalDmsIds(LongSet dataSetIds);

    @Select(sql = "select ed.data_id as objectId, ed.ffty_id as relatedId from external_data ed where ed.data_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getFileFormatTypeIds(LongSet dataSetIds);

    @Select(sql = "select fft.id, fft.code, fft.description from file_format_types fft where fft.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<FileFormatTypeBaseRecord> getFileFormatTypes(LongSet fileFormatTypeIds);

    @Select(sql = "select ed.data_id as objectId, ed.loty_id as relatedId from external_data ed where ed.data_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getLocatorTypeIds(LongSet dataSetIds);

    @Select(sql = "select lt.id, lt.code, lt.description from locator_types lt where lt.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<LocatorTypeBaseRecord> getLocatorTypes(LongSet locatorTypeIds);

    @Select(sql = "select ed.data_id as objectId, ed.cvte_id_stor_fmt as relatedId from external_data ed where ed.data_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getStorageFormatIds(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.dsty_id as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTypeIds(LongSet dataSetIds);

    @Select(sql = "select dt.id, dt.code, dt.data_set_kind as kind, dt.description, dt.main_ds_pattern as mainDataSetPattern, dt.main_ds_path as mainDataSetPath, "
            + "dt.deletion_disallow as disallowDeletion, dt.modification_timestamp as modificationDate from data_set_types dt where dt.id = any(?{1})", parameterBindings = {
                    LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataSetTypeBaseRecord> getTypes(LongSet dataSetTypeIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql = "select p.ds_id as objectId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, p.value as propertyValue, m.code as materialPropertyValueCode, mt.code as materialPropertyValueTypeCode, cvt.code as vocabularyPropertyValue "
            + "from data_set_properties p "
            + "left join materials m on p.mate_prop_id = m.id "
            + "left join controlled_vocabulary_terms cvt on p.cvte_id = cvt.id "
            + "left join material_types mt on m.maty_id = mt.id "
            + "join data_set_type_property_types etpt on p.dstpt_id = etpt.id "
            + "join property_types pt on etpt.prty_id = pt.id "
            + "where p.ds_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PropertyRecord> getProperties(LongSet dataSetIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql = "select p.ds_id as objectId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, p.mate_prop_id as propertyValue "
            + "from data_set_properties p "
            + "join data_set_type_property_types etpt on p.dstpt_id = etpt.id "
            + "join property_types pt on etpt.prty_id = pt.id "
            + "where p.mate_prop_id is not null and p.ds_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialPropertyRecord> getMaterialProperties(LongSet dataSetIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql = "select ph.ds_id as objectId, ph.pers_id_author as authorId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, ph.value as propertyValue, ph.material as materialPropertyValue, ph.vocabulary_term as vocabularyPropertyValue, ph.valid_from_timestamp as validFrom, ph.valid_until_timestamp as validTo "
            + "from data_set_properties_history ph "
            + "join data_set_type_property_types etpt on ph.dstpt_id = etpt.id "
            + "join property_types pt on etpt.prty_id = pt.id "
            + "where ph.ds_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<HistoryPropertyRecord> getPropertiesHistory(LongSet dataSetIds);

    @Select(sql = "select drh.main_data_id as objectId, drh.pers_id_author as authorId, drh.relation_type as relationType, "
            + "drh.entity_perm_id as relatedObjectId, drh.valid_from_timestamp as validFrom, drh.valid_until_timestamp as validTo, "
            + "drh.expe_id as experimentId, drh.samp_id as sampleId, drh.data_id as dataSetId "
            + "from data_set_relationships_history drh where drh.valid_until_timestamp is not null and drh.main_data_id = any(?{1})", parameterBindings = {
                    LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataSetRelationshipRecord> getRelationshipsHistory(LongSet dataSetIds);

    @Select(sql = "select ds_id from post_registration_dataset_queue where ds_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getNotPostRegisteredDataSets(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.samp_id as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSampleIds(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.expe_id as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getExperimentIds(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.dast_id as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getDataStoreIds(LongSet dataSetIds);

    @Select(sql = "select dr.data_id_child as objectId, dr.data_id_parent as relatedId from "
            + "data_set_relationships dr, relationship_types rt "
            + "where dr.relationship_id = rt.id and rt.code = 'PARENT_CHILD' and dr.data_id_child = any(?{1}) order by dr.ordinal", parameterBindings = {
                    LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getParentIds(LongSet dataSetIds);

    @Select(sql = "select dr.data_id_parent as objectId, dr.data_id_child as relatedId from "
            + "data_set_relationships dr, relationship_types rt "
            + "where dr.relationship_id = rt.id and rt.code = 'PARENT_CHILD' and dr.data_id_parent = any(?{1}) order by dr.ordinal", parameterBindings = {
                    LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getChildIds(LongSet dataSetIds);

    @Select(sql = "select dr.data_id_child as objectId, dr.data_id_parent as relatedId from "
            + "data_set_relationships dr, relationship_types rt "
            + "where dr.relationship_id = rt.id and rt.code = 'CONTAINER_COMPONENT' and dr.data_id_child = any(?{1}) order by dr.ordinal", parameterBindings = {
                    LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getContainerIds(LongSet dataSetIds);

    @Select(sql = "select dr.data_id_parent as objectId, dr.data_id_child as relatedId from "
            + "data_set_relationships dr, relationship_types rt "
            + "where dr.relationship_id = rt.id and rt.code = 'CONTAINER_COMPONENT' and dr.data_id_parent = any(?{1}) order by dr.ordinal", parameterBindings = {
                    LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getComponentIds(LongSet dataSetIds);

    @Select(sql = "select ma.data_id as objectId, ma.mepr_id as relatedId from metaproject_assignments ma where ma.data_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTagIds(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.pers_id_registerer as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.pers_id_modifier as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getModifierIds(LongSet dataSetIds);

    @Select(sql = "select dsty_id as objectId, id as relatedId from data_set_type_property_types where dsty_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getPropertyAssignmentIds(LongSet dataSetTypeIds);

    @Select(sql = "select * from data_set_type_property_types where id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PropertyAssignmentRecord> getPropertyAssignments(LongSet dataSetTypePropertyTypeIds);

}
