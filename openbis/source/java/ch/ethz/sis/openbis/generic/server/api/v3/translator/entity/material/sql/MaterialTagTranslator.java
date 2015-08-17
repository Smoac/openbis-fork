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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.QueryTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectToManyRelationTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.sql.ITagSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;

/**
 * @author pkupczyk
 */
@Component
public class MaterialTagTranslator extends ObjectToManyRelationTranslator<Tag, TagFetchOptions>
{

    @Autowired
    private ITagSqlTranslator tagTranslator;

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet objectIds)
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        return query.getTagIds(objectIds);
    }

    @Override
    protected Map<Long, Tag> translateRelated(TranslationContext context, Collection<Long> relatedIds, TagFetchOptions relatedFetchOptions)
    {
        return tagTranslator.translate(context, relatedIds, relatedFetchOptions);
    }

    @Override
    protected Collection<Tag> createCollection()
    {
        return new LinkedHashSet<Tag>();
    }

}
