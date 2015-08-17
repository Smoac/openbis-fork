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
import java.util.Comparator;
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
    private MaterialBaseTranslator baseTranslator;

    @Autowired
    private MaterialTypeRelationTranslator typeTranslator;

    @Autowired
    private MaterialPropertySqlTranslator propertyTranslator;

    @Autowired
    private MaterialMaterialPropertyTranslator materialPropertyTranslator;

    @Autowired
    private MaterialRegistratorTranslator registratorTranslator;

    @Autowired
    private MaterialTagTranslator tagsTranslator;

    @Autowired
    private MaterialHistoryTranslator historyTranslator;

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

        relations.put(MaterialBaseTranslator.class, baseTranslator.translate(context, materialIds, null));

        if (fetchOptions.hasType())
        {
            relations.put(MaterialTypeRelationTranslator.class, typeTranslator.translate(context, materialIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.put(MaterialPropertySqlTranslator.class, propertyTranslator.translate(context, materialIds, fetchOptions.withProperties()));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.put(MaterialMaterialPropertyTranslator.class,
                    materialPropertyTranslator.translate(context, materialIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(MaterialRegistratorTranslator.class,
                    registratorTranslator.translate(context, materialIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasTags())
        {
            relations.put(MaterialTagTranslator.class, tagsTranslator.translate(context, materialIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(MaterialHistoryTranslator.class, historyTranslator.translate(context, materialIds, fetchOptions.withHistory()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long materialId, Material result, Object objectRelations,
            MaterialFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        MaterialBaseRecord baseRecord = relations.get(MaterialBaseTranslator.class, materialId);

        result.setPermId(new MaterialPermId(baseRecord.code, baseRecord.typeCode));
        result.setCode(baseRecord.code);
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasType())
        {
            result.setType(relations.get(MaterialTypeRelationTranslator.class, materialId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(relations.get(MaterialPropertySqlTranslator.class, materialId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(relations.get(MaterialMaterialPropertyTranslator.class, materialId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(MaterialRegistratorTranslator.class, materialId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasTags())
        {
            result.setTags((Set<Tag>) relations.get(MaterialTagTranslator.class, materialId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(MaterialHistoryTranslator.class, materialId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

    }

    @Override
    protected Comparator<Material> getObjectComparator(TranslationContext context, final MaterialFetchOptions fetchOptions)
    {
        if (fetchOptions.getSortBy() == null)
        {
            return null;
        }

        if (fetchOptions.getSortBy().isCode())
        {
            return new Comparator<Material>()
                {
                    @Override
                    public int compare(Material o1, Material o2)
                    {
                        if (fetchOptions.getSortBy().code().isAsc())
                        {
                            return o1.getCode().compareTo(o2.getCode());
                        } else
                        {
                            return -o1.getCode().compareTo(o2.getCode());
                        }
                    }
                };
        }

        if (fetchOptions.getSortBy().isRegistrationDate())
        {
            return new Comparator<Material>()
                {
                    @Override
                    public int compare(Material o1, Material o2)
                    {
                        return -o1.getRegistrationDate().compareTo(o2.getRegistrationDate());
                    }
                };
        }

        return null;
    }

}
