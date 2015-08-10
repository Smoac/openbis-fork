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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.sql;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.Select;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectQuery;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

/**
 * @author pkupczyk
 */
public interface ProjectQuery extends ObjectQuery
{

    @Select(sql = "select prh.main_proj_id as entityId, prh.pers_id_author as authorId, prh.relation_type as relationType, "
            + "prh.entity_perm_id as relatedObjectId, prh.valid_from_timestamp as validFrom, prh.valid_until_timestamp as validTo, "
            + "prh.space_id as spaceId, prh.expe_id as experimentId "
            + "from project_relationships_history prh where prh.valid_until_timestamp is not null and prh.main_proj_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ProjectRelationshipRecord> getRelationshipsHistory(LongSet projectIds);

}
