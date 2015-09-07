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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset.sql;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.Select;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistoryPropertyRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

/**
 * @author pkupczyk
 */
public interface DataSetQuery extends ObjectQuery
{

    @Select(sql = "select dph.ds_id as entityId, dph.pers_id_author as authorId, pt.code as propertyCode, dph.value as propertyValue, dph.material as materialPropertyValue, dph.vocabulary_term as vocabularyPropertyValue, dph.valid_from_timestamp as validFrom, dph.valid_until_timestamp as validTo "
            + "from data_set_properties_history dph "
            + "left join data_set_type_property_types dtpt on dph.dstpt_id = dtpt.id "
            + "left join property_types pt on dtpt.prty_id = pt.id "
            + "where dph.ds_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<HistoryPropertyRecord> getPropertiesHistory(LongSet dataSetIds);

    @Select(sql = "select drh.main_data_id as entityId, drh.pers_id_author as authorId, drh.relation_type as relationType, "
            + "drh.entity_perm_id as relatedObjectId, drh.valid_from_timestamp as validFrom, drh.valid_until_timestamp as validTo, "
            + "drh.expe_id as experimentId, drh.samp_id as sampleId, drh.data_id as dataSetId "
            + "from data_set_relationships_history drh where drh.valid_until_timestamp is not null and drh.main_data_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataSetRelationshipRecord> getRelationshipsHistory(LongSet dataSetIds);
    
    @Select(sql = "select ds_id from post_registration_dataset_queue where ds_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getNotPostRegisteredDataSets(LongSet dataSetIds);

}
