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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Jakub Straszewski
 */
public class MaterialPropertyTranslator extends
        AbstractCachingTranslator<IEntityPropertiesHolder, Map<String, Material>, MaterialFetchOptions>
{

    public MaterialPropertyTranslator(TranslationContext translationContext, MaterialFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
    }

    @Override
    protected Map<String, Material> createObject(IEntityPropertiesHolder entity)
    {
        if (false == entity.isPropertiesInitialized())
        {
            HibernateUtils.initialize(entity.getProperties());
        }

        HashMap<String, Material> properties = new HashMap<String, Material>();

        for (EntityPropertyPE property : entity.getProperties())
        {
            if (property.getMaterialValue() != null)
            {
                String code = property.getEntityTypePropertyType().getPropertyType().getCode();
                MaterialPE materialPe = property.getMaterialValue();

                Material material =
                        new MaterialTranslator(getTranslationContext(), getFetchOptions()).translate(materialPe);
                properties.put(code, material);
            }
        }
        return properties;
    }

    @Override
    protected void updateObject(IEntityPropertiesHolder input, Map<String, Material> output, Relations relations)
    {

    }

}
