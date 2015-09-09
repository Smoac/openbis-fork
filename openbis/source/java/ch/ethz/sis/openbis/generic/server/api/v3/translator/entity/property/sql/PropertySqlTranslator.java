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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.sql;

import java.util.Collection;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions;

/**
 * @author pkupczyk
 */
public abstract class PropertySqlTranslator extends AbstractCachingTranslator<Long, ObjectHolder<Map<String, String>>, PropertyFetchOptions>
        implements IPropertySqlTranslator
{

    @Override
    protected ObjectHolder<Map<String, String>> createObject(TranslationContext context, Long objectId, PropertyFetchOptions fetchOptions)
    {
        return new ObjectHolder<Map<String, String>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> objectIds, PropertyFetchOptions fetchOptions)
    {
        return loadProperties(objectIds);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateObject(TranslationContext context, Long objectId, ObjectHolder<Map<String, String>> result, Object relations,
            PropertyFetchOptions fetchOptions)
    {
        Map<Long, Map<String, String>> properties = (Map<Long, Map<String, String>>) relations;
        result.setObject(properties.get(objectId));
    }

    protected abstract Map<Long, Map<String, String>> loadProperties(Collection<Long> entityIds);

}