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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class TagTranslator extends AbstractCachingTranslator<MetaprojectPE, Tag, TagFetchOptions> implements ITagTranslator
{
    @Autowired
    private IPersonTranslator personTranslator;

    @Override
    public boolean shouldTranslate(TranslationContext context, MetaprojectPE object, TagFetchOptions fetchOptions)
    {
        boolean isPublic = false == object.isPrivate();
        return isPublic || object.getOwner().getUserId().equals(context.getSession().tryGetPerson().getUserId());
    }

    @Override
    protected Tag createObject(TranslationContext context, MetaprojectPE tag, TagFetchOptions fetchOptions)
    {
        Tag result = new Tag();

        result.setPermId(new TagPermId(tag.getIdentifier()));
        result.setCode(tag.getName());
        result.setDescription(tag.getDescription());
        result.setPrivate(tag.isPrivate());
        result.setRegistrationDate(tag.getCreationDate());
        result.setFetchOptions(new TagFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(TranslationContext context, MetaprojectPE tag, Tag result, Relations relations, TagFetchOptions fetchOptions)
    {
        if (fetchOptions.hasOwner())
        {
            result.setOwner(personTranslator.translate(context, tag.getOwner(), fetchOptions.withOwner()));
            result.getFetchOptions().withOwnerUsing(fetchOptions.withOwner());
        }
    }

}
