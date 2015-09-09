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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.QueryTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;

/**
 * @author pkupczyk
 */
@Component
public class MaterialMaterialPropertySqlTranslator extends AbstractCachingTranslator<Long, ObjectHolder<Map<String, Material>>, MaterialFetchOptions>
        implements IMaterialMaterialPropertySqlTranslator
{

    @Autowired
    private IMaterialSqlTranslator materialTranslator;

    @Override
    protected ObjectHolder<Map<String, Material>> createObject(TranslationContext context, Long objectId, MaterialFetchOptions fetchOptions)
    {
        return new ObjectHolder<Map<String, Material>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> objectIds, MaterialFetchOptions fetchOptions)
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        List<MaterialMaterialPropertyRecord> records = query.getMaterialProperties(new LongOpenHashSet(objectIds));
        Collection<Long> propertyValues = new HashSet<Long>();

        for (MaterialMaterialPropertyRecord record : records)
        {
            propertyValues.add(record.propertyValue);
        }

        Map<Long, Material> materials = materialTranslator.translate(context, propertyValues, fetchOptions);
        Map<Long, Map<String, Material>> materialProperties = new HashMap<Long, Map<String, Material>>();

        for (MaterialMaterialPropertyRecord record : records)
        {
            Map<String, Material> properties = materialProperties.get(record.materialId);
            if (properties == null)
            {
                properties = new HashMap<String, Material>();
                materialProperties.put(record.materialId, properties);
            }
            properties.put(record.propertyCode, materials.get(record.propertyValue));
        }

        return materialProperties;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateObject(TranslationContext context, Long objectId, ObjectHolder<Map<String, Material>> result, Object relations,
            MaterialFetchOptions fetchOptions)
    {
        Map<Long, Map<String, Material>> materialProperties = (Map<Long, Map<String, Material>>) relations;
        result.setObject(materialProperties.get(objectId));
    }

}
