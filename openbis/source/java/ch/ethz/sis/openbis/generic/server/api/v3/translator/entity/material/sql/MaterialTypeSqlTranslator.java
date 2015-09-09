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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.MaterialType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;

/**
 * @author pkupczyk
 */
@Component
public class MaterialTypeSqlTranslator extends AbstractCachingTranslator<Long, MaterialType, MaterialTypeFetchOptions> implements
        IMaterialTypeSqlTranslator
{

    @Autowired
    private IMaterialTypeBaseSqlTranslator baseTranslator;

    @Override
    protected MaterialType createObject(TranslationContext context, Long typeId, MaterialTypeFetchOptions fetchOptions)
    {
        final MaterialType materialType = new MaterialType();
        materialType.setFetchOptions(new MaterialTypeFetchOptions());
        return materialType;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> typeIds, MaterialTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IMaterialTypeBaseSqlTranslator.class, baseTranslator.translate(context, typeIds, null));

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long typeId, MaterialType result, Object objectRelations,
            MaterialTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        MaterialTypeBaseRecord baseRecord = relations.get(IMaterialTypeBaseSqlTranslator.class, typeId);

        result.setPermId(new EntityTypePermId(baseRecord.code));
        result.setCode(baseRecord.code);
        result.setDescription(baseRecord.description);
        result.setModificationDate(baseRecord.modificationDate);
    }

}
