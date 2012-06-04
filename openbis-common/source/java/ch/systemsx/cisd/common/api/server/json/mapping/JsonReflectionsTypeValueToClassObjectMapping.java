/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.api.server.json.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.api.server.json.common.JsonConstants;

/**
 * @author pkupczyk
 */
public class JsonReflectionsTypeValueToClassObjectMapping implements
        IJsonTypeValueToClassObjectMapping
{

    private static JsonReflectionsTypeValueToClassObjectMapping instance;

    private Map<String, Class<?>> typeToClassMap;

    public JsonReflectionsTypeValueToClassObjectMapping(String prefix)
    {
        typeToClassMap = createTypeToClassMap(prefix);
    }

    @Override
    public Class<?> getClass(String type)
    {
        return typeToClassMap.get(type);
    }

    private static Map<String, Class<?>> createTypeToClassMap(String prefix)
    {
        Reflections reflections = new Reflections(prefix);

        Set<Class<?>> types = reflections.getTypesAnnotatedWith(JsonObject.class);
        Map<String, Class<?>> typesMap = new HashMap<String, Class<?>>();

        if (types != null)
        {
            for (Class<?> type : types)
            {
                JsonObject typeAnnotation = type.getAnnotation(JsonObject.class);
                typesMap.put(typeAnnotation.value(), type);
            }
        }

        return typesMap;
    }

    public static final JsonReflectionsTypeValueToClassObjectMapping getInstance()
    {
        synchronized (JsonReflectionsTypeValueToClassObjectMapping.class)
        {
            if (instance == null)
            {
                instance =
                        new JsonReflectionsTypeValueToClassObjectMapping(
                                JsonConstants.getClassesPrefix());
            }
            return instance;
        }
    }
}
