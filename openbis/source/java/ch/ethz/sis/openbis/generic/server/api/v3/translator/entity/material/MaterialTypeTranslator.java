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

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.MaterialType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;

/**
 * @author Jakub Straszewski
 */
@Component
public class MaterialTypeTranslator extends AbstractCachingTranslator<MaterialTypePE, MaterialType, MaterialTypeFetchOptions> implements
        IMaterialTypeTranslator
{

    @Override
    protected MaterialType createObject(TranslationContext context, MaterialTypePE input, MaterialTypeFetchOptions fetchOptions)
    {
        final MaterialType materialType = new MaterialType();
        materialType.setPermId(new EntityTypePermId(input.getCode()));
        materialType.setCode(input.getCode());
        materialType.setDescription(input.getDescription());
        materialType.setModificationDate(input.getModificationDate());
        return materialType;
    }

    @Override
    protected void updateObject(TranslationContext context, MaterialTypePE input, MaterialType output, Relations relations,
            MaterialTypeFetchOptions fetchOptions)
    {
    }

}
