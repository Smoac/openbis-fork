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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import net.lemnik.eodsql.QueryTool;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.PropertyRelation;

/**
 * @author pkupczyk
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MaterialPropertyRelation extends PropertyRelation
{

    public MaterialPropertyRelation(Collection<Long> entityIds)
    {
        super(entityIds);
    }

    @Override
    protected Map<Long, Map<String, String>> loadProperties(Collection<Long> entityIds)
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        List<MaterialPropertyRecord> properties = query.getProperties(new LongOpenHashSet(entityIds));
        Map<Long, Map<String, String>> result = new HashMap<Long, Map<String, String>>();

        for (MaterialPropertyRecord property : properties)
        {
            Map<String, String> materialProperties = result.get(property.materialId);

            if (materialProperties == null)
            {
                materialProperties = new HashMap<String, String>();
                result.put(property.materialId, materialProperties);
            }

            if (property.propertyValue != null)
            {
                materialProperties.put(property.propertyCode, property.propertyValue);
            } else if (property.materialPropertyValueCode != null)
            {
                materialProperties.put(property.propertyCode, property.materialPropertyValueCode + " (" + property.materialPropertyValueTypeCode
                        + ")");
            } else if (property.vocabularyPropertyValue != null)
            {
                materialProperties.put(property.propertyCode, property.vocabularyPropertyValue);
            } else
            {
                throw new IllegalArgumentException("Unsupported property kind");
            }
        }

        return result;
    }

}
