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

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.PropertyRelation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;

/**
 * @author pkupczyk
 */
@Component
public class MaterialSqlTranslator extends AbstractCachingTranslator<Long, Material, MaterialFetchOptions> implements IMaterialSqlTranslator
{

    @Override
    protected Material createObject(TranslationContext context, Long materialId, MaterialFetchOptions fetchOptions)
    {
        final Material material = new Material();
        material.setFetchOptions(new MaterialFetchOptions());
        return material;
    }

    @Override
    protected Relations getObjectsRelations(TranslationContext context, Collection<Long> materialIds, MaterialFetchOptions fetchOptions)
    {
        Relations relations = new Relations();

        relations.add(createRelation(MaterialBaseRelation.class, materialIds));

        if (fetchOptions.hasType())
        {
            relations.add(createRelation(MaterialTypeRelation.class, context, materialIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.add(createRelation(MaterialPropertyRelation.class, materialIds));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.add(createRelation(MaterialMaterialPropertyRelation.class, context, materialIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.add(createRelation(MaterialRegistratorRelation.class, context, materialIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasTags())
        {
            relations.add(createRelation(MaterialTagsRelation.class, context, materialIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.add(createRelation(MaterialHistoryRelation.class, context, materialIds, fetchOptions.withHistory()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long materialId, Material result, Relations relations,
            MaterialFetchOptions fetchOptions)
    {
        MaterialBaseRelation baseRelation = relations.get(MaterialBaseRelation.class);
        MaterialBaseRecord baseRecord = baseRelation.getRecord(materialId);

        result.setPermId(new MaterialPermId(baseRecord.code, baseRecord.typeCode));
        result.setCode(baseRecord.code);
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasType())
        {
            MaterialTypeRelation relation = relations.get(MaterialTypeRelation.class);
            result.setType(relation.getRelated(materialId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasProperties())
        {
            PropertyRelation relation = relations.get(MaterialPropertyRelation.class);
            result.setProperties(relation.getProperties(materialId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            MaterialMaterialPropertyRelation relation = relations.get(MaterialMaterialPropertyRelation.class);
            result.setMaterialProperties(relation.getMaterialProperties(materialId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasRegistrator())
        {
            MaterialRegistratorRelation relation = relations.get(MaterialRegistratorRelation.class);
            result.setRegistrator(relation.getRelated(materialId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasTags())
        {
            MaterialTagsRelation relation = relations.get(MaterialTagsRelation.class);
            result.setTags(relation.getRelatedSet(materialId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasHistory())
        {
            MaterialHistoryRelation relation = relations.get(MaterialHistoryRelation.class);
            result.setHistory(relation.getRelated(materialId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

    }
}
