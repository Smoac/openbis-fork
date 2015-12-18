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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.tag;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.Select;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.common.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

/**
 * @author pkupczyk
 */
public interface TagQuery extends ObjectQuery
{

    @Select(sql = "select m.id, m.name, m.description, p.user_id as owner, m.private as isPrivate, m.creation_date as registrationDate from "
            + "metaprojects m, persons p where m.owner = p.id and m.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<TagBaseRecord> getTags(LongSet tagIds);

    @Select(sql = "select m.id, p.user_id as owner, not m.private as isPublic from metaprojects m, persons p where "
            + "m.owner = p.id and m.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<TagAuthorizationRecord> getAuthorizations(LongOpenHashSet tagIds);

    @Select(sql = "select m.id as objectId, m.owner as relatedId from metaprojects m where m.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getOwnerIds(LongOpenHashSet tagIds);

}
