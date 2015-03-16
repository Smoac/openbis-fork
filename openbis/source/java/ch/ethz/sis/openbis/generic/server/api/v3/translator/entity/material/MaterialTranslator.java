/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material;

import java.util.HashSet;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.common.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.PersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.PropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.TagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * @author Jakub Straszewski
 */
public class MaterialTranslator extends AbstractCachingTranslator<MaterialPE, Material, MaterialFetchOptions>
{

    public MaterialTranslator(TranslationContext translationContext, MaterialFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
    }

    @Override
    protected Material createObject(MaterialPE materialPE)
    {
        final Material material = new Material();
        material.setPermId(new MaterialPermId(materialPE.getCode(), materialPE.getMaterialType().getCode()));
        material.setCode(materialPE.getCode());
        material.setModificationDate(materialPE.getModificationDate());
        material.setRegistrationDate(materialPE.getRegistrationDate());
        material.setFetchOptions(new MaterialFetchOptions());
        return material;
    }

    @Override
    protected void updateObject(MaterialPE materialPe, Material result, Relations relations)
    {
        if (getFetchOptions().hasProperties())
        {
            result.setProperties(new PropertyTranslator(getTranslationContext(), getFetchOptions().withProperties())
                    .translate(materialPe));
            result.getFetchOptions().withPropertiesUsing(getFetchOptions().withProperties());
        }

        if (getFetchOptions().hasMaterialProperties())
        {
            result.setMaterialProperties(new MaterialPropertyTranslator(getTranslationContext(), getFetchOptions().withMaterialProperties())
                    .translate(materialPe));
            result.getFetchOptions().withMaterialPropertiesUsing(getFetchOptions().withMaterialProperties());
        }

        if (getFetchOptions().hasTags())
        {
            List<Tag> tags =
                    new ListTranslator().translate(materialPe.getMetaprojects(), new TagTranslator(getTranslationContext(), getFetchOptions()
                            .withTags()));
            result.setTags(new HashSet<Tag>(tags));
            result.getFetchOptions().withTagsUsing(getFetchOptions().withTags());
        }

        if (getFetchOptions().hasRegistrator())
        {
            Person registrator =
                    new PersonTranslator(getTranslationContext(), getFetchOptions().withRegistrator()).translate(materialPe.getRegistrator());
            result.setRegistrator(registrator);
            result.getFetchOptions().withRegistratorUsing(getFetchOptions().withRegistrator());
        }
    }
}
