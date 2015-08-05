/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.sql;

import java.util.Collection;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;

/**
 * @author pkupczyk
 */
@Component
public class TagSqlTranslator extends AbstractCachingTranslator<Long, Tag, TagFetchOptions> implements ITagSqlTranslator
{

    @Override
    protected Collection<Long> shouldTranslate(TranslationContext context, Collection<Long> inputs, TagFetchOptions fetchOptions)
    {
        // TODO authorization
        return inputs;
    }

    @Override
    protected Tag createObject(TranslationContext context, Long tagId, TagFetchOptions fetchOptions)
    {
        Tag result = new Tag();
        result.setFetchOptions(new TagFetchOptions());
        return result;
    }

    @Override
    protected Relations getObjectsRelations(TranslationContext context, Collection<Long> tagIds, TagFetchOptions fetchOptions)
    {
        Relations relations = new Relations();

        relations.add(createRelation(TagBaseRelation.class, tagIds));

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long tagId, Tag result, Relations relations, TagFetchOptions fetchOptions)
    {
        TagBaseRelation baseRelation = relations.get(TagBaseRelation.class);
        TagBaseRecord baseRecord = baseRelation.getRecord(tagId);

        result.setPermId(new TagPermId(baseRecord.owner, baseRecord.name));
        result.setCode(baseRecord.name);
        result.setDescription(baseRecord.description);
        result.setPrivate(baseRecord.isPrivate);
        result.setRegistrationDate(baseRecord.registrationDate);
    }

}
