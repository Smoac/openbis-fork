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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.sql;

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
public interface ExperimentQuery extends ObjectQuery
{

    @Select(sql = "select e.id, e.code, e.perm_id as permId, p.code as projectCode, sp.code as spaceCode, e.registration_timestamp as registrationDate, e.modification_timestamp as modificationDate "
            + "from experiments e join projects p on e.proj_id = p.id "
            + "join spaces sp on p.space_id = sp.id "
            + "where e.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ExperimentBaseRecord> getExperiments(LongSet experimentIds);

    @Select(sql = "select eph.expe_id as entityId, eph.pers_id_author as authorId, pt.code as propertyCode, eph.value as propertyValue, eph.material as materialPropertyValue, eph.vocabulary_term as vocabularyPropertyValue, eph.valid_from_timestamp as validFrom, eph.valid_until_timestamp as validTo "
            + "from experiment_properties_history eph "
            + "left join experiment_type_property_types etpt on eph.etpt_id = etpt.id "
            + "left join property_types pt on etpt.prty_id = pt.id "
            + "where eph.expe_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<HistoryPropertyRecord> getPropertiesHistory(LongSet experimentIds);

    @Select(sql = "select erh.main_expe_id as entityId, erh.pers_id_author as authorId, erh.relation_type as relationType, "
            + "erh.entity_perm_id as relatedObjectId, erh.valid_from_timestamp as validFrom, erh.valid_until_timestamp as validTo, "
            + "erh.proj_id as projectId, erh.samp_id as sampleId, erh.data_id as dataSetId "
            + "from experiment_relationships_history erh where erh.valid_until_timestamp is not null and erh.main_expe_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ExperimentRelationshipRecord> getRelationshipsHistory(LongSet experimentIds);

    @Select(sql = "select s.expe_id as objectId, s.id as relatedId from samples s where s.expe_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSampleIds(LongSet experimentIds);

}
