/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.vocabulary.sql;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.Select;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

/**
 * @author pkupczyk
 */
public interface VocabularyQuery extends ObjectQuery
{

    @Select(sql = "select v.id, v.code, v.description, v.is_internal_namespace as isInternalNamespace, v.modification_timestamp as modificationDate, v.registration_timestamp as registrationDate "
            + "from controlled_vocabularies v where v.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<VocabularyBaseRecord> getVocabularies(LongSet vocabularyIds);

    @Select(sql = "select v.id as objectId, v.pers_id_registerer as relatedId "
            + "from controlled_vocabularies v where v.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet vocabularyIds);

    @Select(sql = "select t.id, t.code, t.label, t.description, t.ordinal, t.is_official as isOfficial, t.registration_timestamp as registrationDate "
            + "from controlled_vocabulary_terms t where t.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<VocabularyTermBaseRecord> getTerms(LongSet termIds);

    @Select(sql = "select t.id as objectId, t.pers_id_registerer as relatedId "
            + "from controlled_vocabulary_terms t where t.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTermRegistratorIds(LongSet termIds);

    @Select(sql = "select t.id as objectId, t.covo_id as relatedId "
            + "from controlled_vocabulary_terms t where t.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTermVocabularyIds(LongSet termIds);

}
