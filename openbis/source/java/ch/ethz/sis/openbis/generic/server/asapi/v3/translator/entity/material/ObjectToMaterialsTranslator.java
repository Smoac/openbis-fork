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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.common.ObjectToManyRelationTranslator;

/**
 * @author pkupczyk
 */
public abstract class ObjectToMaterialsTranslator extends ObjectToManyRelationTranslator<Material, MaterialFetchOptions> implements
        IObjectToMaterialsTranslator
{

    @Autowired
    private IMaterialTranslator materialTranslator;

    @Override
    protected Map<Long, Material> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            MaterialFetchOptions relatedFetchOptions)
    {
        return materialTranslator.translate(context, relatedIds, relatedFetchOptions);
    }

    @Override
    protected Collection<Material> createCollection()
    {
        return new ArrayList<Material>();
    }

}
