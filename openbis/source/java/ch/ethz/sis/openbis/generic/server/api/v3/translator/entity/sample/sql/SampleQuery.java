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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.sql;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.Select;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistoryPropertyRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

/**
 * @author pkupczyk
 */
public interface SampleQuery extends ObjectQuery
{

    @Select(sql = "select sph.samp_id as entityId, sph.pers_id_author as authorId, pt.code as propertyCode, sph.value as propertyValue, sph.material as materialPropertyValue, sph.vocabulary_term as vocabularyPropertyValue, sph.valid_from_timestamp as validFrom, sph.valid_until_timestamp as validTo "
            + "from sample_properties_history sph "
            + "left join sample_type_property_types stpt on sph.stpt_id = stpt.id "
            + "left join property_types pt on stpt.prty_id = pt.id "
            + "where sph.samp_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<HistoryPropertyRecord> getPropertiesHistory(LongSet sampleIds);

    @Select(sql = "select srh.main_samp_id as entityId, srh.pers_id_author as authorId, srh.relation_type as relationType, "
            + "srh.entity_perm_id as relatedObjectId, srh.valid_from_timestamp as validFrom, srh.valid_until_timestamp as validTo, "
            + "srh.space_id as spaceId, srh.expe_id as experimentId, srh.samp_id as sampleId, srh.data_id as dataSetId "
            + "from sample_relationships_history srh where srh.valid_until_timestamp is not null and srh.main_samp_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SampleRelationshipRecord> getRelationshipsHistory(LongSet sampleIds);

    @Select(sql = "select s.id as objectId, s.expe_id as relatedId from samples s where s.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getExperiments(LongSet sampleIds);

    @Select(sql = "select sr.sample_id_child as objectId, sr.sample_id_parent as relatedId from "
            + "sample_relationships sr, relationship_types rt "
            + "where sr.relationship_id = rt.id and rt.code = 'PARENT_CHILD' and sr.sample_id_child = any(?{1}) order by sr.id", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getParents(LongSet sampleIds);

}
