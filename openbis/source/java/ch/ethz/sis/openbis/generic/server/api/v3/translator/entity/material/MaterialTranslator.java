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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.IPropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.ITagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author Jakub Straszewski
 */
@Component
public class MaterialTranslator extends AbstractCachingTranslator<MaterialPE, Material, MaterialFetchOptions> implements IMaterialTranslator
{

    @Autowired
    private IMaterialTypeTranslator typeTranslator;

    @Autowired
    private IPropertyTranslator propertyTranslator;

    @Autowired
    private IMaterialPropertyTranslator materialPropertyTranslator;

    @Autowired
    private IPersonTranslator personTranslator;

    @Autowired
    private ITagTranslator tagTranslator;

    @Override
    protected Material createObject(TranslationContext context, MaterialPE materialPE, MaterialFetchOptions fetchOptions)
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
    protected void updateObject(TranslationContext context, MaterialPE materialPe, Material result, Relations relations,
            MaterialFetchOptions fetchOptions)
    {
        if (fetchOptions.hasProperties())
        {
            result.setProperties(propertyTranslator.translate(context, materialPe, fetchOptions.withProperties()));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(materialPropertyTranslator.translate(context, materialPe, fetchOptions.withMaterialProperties()));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasTags())
        {
            Map<MetaprojectPE, Tag> tags = tagTranslator.translate(context, materialPe.getMetaprojects(), fetchOptions.withTags());
            result.setTags(new HashSet<Tag>(tags.values()));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(personTranslator.translate(context, materialPe.getRegistrator(), fetchOptions.withRegistrator()));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasType())
        {
            result.setType(typeTranslator.translate(context, materialPe.getMaterialType(), fetchOptions.withType()));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

    }
}
