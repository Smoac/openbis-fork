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
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;

/**
 * @author pkupczyk
 */
@Component
public class MaterialSqlTranslator extends AbstractCachingTranslator<Long, Material, MaterialFetchOptions> implements IMaterialSqlTranslator
{

    @Autowired
    private IMaterialBaseSqlTranslator baseTranslator;

    @Autowired
    private IMaterialTypeRelationSqlTranslator typeTranslator;

    @Autowired
    private IMaterialPropertySqlTranslator propertyTranslator;

    @Autowired
    private IMaterialMaterialPropertySqlTranslator materialPropertyTranslator;

    @Autowired
    private IMaterialRegistratorSqlTranslator registratorTranslator;

    @Autowired
    private IMaterialTagSqlTranslator tagsTranslator;

    @Autowired
    private IMaterialHistorySqlTranslator historyTranslator;

    @Override
    protected Material createObject(TranslationContext context, Long materialId, MaterialFetchOptions fetchOptions)
    {
        final Material material = new Material();
        material.setFetchOptions(new MaterialFetchOptions());
        return material;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> materialIds, MaterialFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IMaterialBaseSqlTranslator.class, baseTranslator.translate(context, materialIds, null));

        if (fetchOptions.hasType())
        {
            relations.put(IMaterialTypeRelationSqlTranslator.class, typeTranslator.translate(context, materialIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.put(IMaterialPropertySqlTranslator.class, propertyTranslator.translate(context, materialIds, fetchOptions.withProperties()));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.put(IMaterialMaterialPropertySqlTranslator.class,
                    materialPropertyTranslator.translate(context, materialIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IMaterialRegistratorSqlTranslator.class,
                    registratorTranslator.translate(context, materialIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasTags())
        {
            relations.put(IMaterialTagSqlTranslator.class, tagsTranslator.translate(context, materialIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(IMaterialHistorySqlTranslator.class, historyTranslator.translate(context, materialIds, fetchOptions.withHistory()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long materialId, Material result, Object objectRelations,
            MaterialFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        MaterialBaseRecord baseRecord = relations.get(IMaterialBaseSqlTranslator.class, materialId);

        result.setPermId(new MaterialPermId(baseRecord.code, baseRecord.typeCode));
        result.setCode(baseRecord.code);
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasType())
        {
            result.setType(relations.get(IMaterialTypeRelationSqlTranslator.class, materialId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(relations.get(IMaterialPropertySqlTranslator.class, materialId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(relations.get(IMaterialMaterialPropertySqlTranslator.class, materialId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IMaterialRegistratorSqlTranslator.class, materialId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasTags())
        {
            result.setTags((Set<Tag>) relations.get(IMaterialTagSqlTranslator.class, materialId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(IMaterialHistorySqlTranslator.class, materialId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

    }

}
