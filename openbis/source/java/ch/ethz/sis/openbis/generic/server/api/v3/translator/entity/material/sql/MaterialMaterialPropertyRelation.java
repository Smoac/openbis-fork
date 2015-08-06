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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relation;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;

/**
 * @author pkupczyk
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MaterialMaterialPropertyRelation implements Relation
{

    @Autowired
    private IMaterialSqlTranslator materialTranslator;

    private TranslationContext context;

    private Collection<Long> materialIds;

    private MaterialFetchOptions fetchOptions;

    private Map<Long, Map<String, Material>> materialProperties = new HashMap<Long, Map<String, Material>>();

    public MaterialMaterialPropertyRelation(TranslationContext context, Collection<Long> materialIds, MaterialFetchOptions fetchOptions)
    {
        this.context = context;
        this.materialIds = materialIds;
        this.fetchOptions = fetchOptions;
    }

    @Override
    public void load()
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        List<MaterialMaterialPropertyRecord> records = query.getMaterialProperties(new LongOpenHashSet(materialIds));
        Collection<Long> propertyValues = new HashSet<Long>();

        for (MaterialMaterialPropertyRecord record : records)
        {
            propertyValues.add(record.propertyValue);
        }

        Map<Long, Material> materials = materialTranslator.translate(context, propertyValues, fetchOptions);

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
    }

    public Map<String, Material> getMaterialProperties(Long materialId)
    {
        return materialProperties.get(materialId);
    }

}
