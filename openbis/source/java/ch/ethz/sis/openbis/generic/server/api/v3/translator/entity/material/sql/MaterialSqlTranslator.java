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
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.IHistoryTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.IPropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.PropertyRelation;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.ITagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;

/**
 * @author Jakub Straszewski
 */
@Component
public class MaterialSqlTranslator extends AbstractCachingTranslator<Long, Material, MaterialFetchOptions> implements IMaterialSqlTranslator
{

    @Autowired
    private IMaterialTypeSqlTranslator typeTranslator;

    @Autowired
    private IPropertyTranslator propertyTranslator;

    @Autowired
    private IMaterialPropertySqlTranslator materialPropertyTranslator;

    @Autowired
    private IPersonTranslator personTranslator;

    @Autowired
    private ITagTranslator tagTranslator;

    @Autowired
    private IHistoryTranslator historyTranslator;

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

        relations.add(new MaterialBaseRelation(materialIds));

        if (fetchOptions.hasProperties())
        {
            relations.add(new MaterialPropertyRelation(materialIds));
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

        if (fetchOptions.hasProperties())
        {
            PropertyRelation relation = relations.get(MaterialPropertyRelation.class);
            result.setProperties(relation.getProperties(materialId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }
    }

}
