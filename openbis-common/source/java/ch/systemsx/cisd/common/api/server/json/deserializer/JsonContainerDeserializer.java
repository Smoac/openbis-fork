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

package ch.systemsx.cisd.common.api.server.json.deserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.deser.BeanDeserializer;
import org.codehaus.jackson.map.deser.BeanDeserializerFactory;
import org.codehaus.jackson.map.deser.SettableBeanProperty;
import org.codehaus.jackson.type.JavaType;

import ch.systemsx.cisd.common.api.server.json.common.JsonConstants;
import ch.systemsx.cisd.common.api.server.json.mapping.IJsonClassValueToClassObjectsMapping;
import ch.systemsx.cisd.common.api.server.json.mapping.IJsonTypeValueToClassObjectMapping;

/**
 * @author pkupczyk
 */

@SuppressWarnings(
    { "rawtypes", "unchecked" })
public class JsonContainerDeserializer extends JsonDeserializer<Object>
{

    private JsonDeserializer<Object> originalDeserializer;

    private IJsonTypeValueToClassObjectMapping typeValueToClassObjectMapping;

    private IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping;

    public JsonContainerDeserializer(JsonDeserializer<Object> originalDeserializer,
            IJsonTypeValueToClassObjectMapping typeValueToClassObjectMapping,
            IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping)
    {
        this.originalDeserializer = originalDeserializer;
        this.typeValueToClassObjectMapping = typeValueToClassObjectMapping;
        this.classValueToClassObjectsMapping = classValueToClassObjectsMapping;
    }

    public static boolean canDeserialize(JavaType type)
    {
        Class<?> clazz = type.getRawClass();
        return Map.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz);
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        Object object = getOriginalDeserializer().deserialize(jp, ctxt);
        return tryConvertNestedMaps(object, ctxt);
    }

    private Object tryConvertNestedMaps(Object object, DeserializationContext ctxt)
    {
        if (object instanceof EnumMap || object instanceof EnumSet)
        {
            return object;
        }
        if (object instanceof Map)
        {
            return tryConvertNestedMapsInMap((Map) object, ctxt);
        } else if (object instanceof List)
        {
            return tryConvertNestedMapsInList((List) object, ctxt);
        } else if (object instanceof Set)
        {
            return tryConvertNestedMapsInCollection((Set) object, new HashSet(), ctxt);
        } else if (object instanceof Collection)
        {
            return tryConvertNestedMapsInCollection((Collection) object, new ArrayList(), ctxt);
        } else
        {
            return object;
        }
    }

    private Object tryConvertNestedMapsInMap(Map map, DeserializationContext ctxt)
    {
        if (map == null || map.isEmpty())
        {
            return map;
        }

        Iterator<Map.Entry> iterator = map.entrySet().iterator();

        String typeValue = null;
        String classValue = null;
        Map changedValues = null;

        while (iterator.hasNext())
        {
            Map.Entry entry = iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String)
            {
                if (JsonConstants.getTypeField().equals(key))
                {
                    typeValue = (String) entry.getValue();
                } else if (JsonConstants.getLegacyClassField().equals(key))
                {
                    classValue = (String) entry.getValue();
                }
            } else
            {
                Object newValue = tryConvertNestedMaps(value, ctxt);

                if (newValue != value)
                {
                    if (changedValues == null)
                    {
                        changedValues = new HashMap();
                    }
                    changedValues.put(key, newValue);
                }
            }
        }

        if (changedValues != null)
        {
            map.putAll(changedValues);
        }

        if (typeValue == null && classValue == null)
        {
            return map;
        } else
        {
            return tryConvertMap(map, typeValue, classValue, ctxt);
        }
    }

    private Object tryConvertNestedMapsInList(List list, DeserializationContext ctxt)
    {
        if (list == null || list.isEmpty())
        {
            return list;
        }

        ListIterator iterator = list.listIterator();

        while (iterator.hasNext())
        {
            Object item = iterator.next();
            Object newItem = tryConvertNestedMaps(item, ctxt);

            if (newItem != item)
            {
                iterator.set(newItem);
            }
        }
        return list;
    }

    private Object tryConvertNestedMapsInCollection(Collection collection,
            Collection newCollection, DeserializationContext ctxt)
    {
        if (collection == null || collection.isEmpty())
        {
            return collection;
        }

        Iterator iterator = collection.iterator();
        boolean changed = false;

        while (iterator.hasNext())
        {
            Object item = iterator.next();
            Object newItem = tryConvertNestedMaps(item, ctxt);

            if (newItem != item)
            {
                changed = true;
            }
            newCollection.add(newItem);
        }

        if (changed)
        {
            return newCollection;
        } else
        {
            return collection;
        }
    }

    private Object tryConvertMap(Map map, String typeValue, String classValue,
            DeserializationContext ctxt)
    {
        Class<?> clazz = tryGetClass(typeValue, classValue);

        if (clazz != null)
        {
            boolean hadTypeValue = map.containsKey(JsonConstants.getTypeField());
            boolean hadClassValue = map.containsKey(JsonConstants.getLegacyClassField());

            map.remove(JsonConstants.getTypeField());
            map.remove(JsonConstants.getLegacyClassField());

            try
            {
                Object instance = tryCreateInstance(clazz);

                Iterator<SettableBeanProperty> properties = tryGetProperties(clazz, ctxt);
                if (properties != null)
                {
                    trySetProperties(instance, properties, map);
                }

                return instance;

            } catch (IOException e)
            {
                if (hadTypeValue)
                {
                    map.put(JsonConstants.getTypeField(), typeValue);
                }
                if (hadClassValue)
                {
                    map.put(JsonConstants.getLegacyClassField(), classValue);
                }
                return map;
            }
        } else
        {
            return map;
        }
    }

    private Class tryGetClass(String typeValue, String classValue)
    {
        Class<?> clazz = null;

        if (typeValue != null)
        {
            clazz = getTypeValueToClassObjectMapping().getClass(typeValue);
        } else if (classValue != null)
        {
            List<Class<?>> clazzList = getClassValueToClassObjectsMapping().getClasses(classValue);
            if (clazzList != null && clazzList.size() == 1)
            {
                clazz = clazzList.get(0);
            }
        }

        return clazz;
    }

    private Object tryCreateInstance(Class<?> clazz) throws IOException
    {
        try
        {
            return clazz.newInstance();
        } catch (InstantiationException e)
        {
            return new JsonMappingException("Couldn't create an instance of class: " + clazz, e);
        } catch (IllegalAccessException e)
        {
            return new JsonMappingException("Couldn't create an instance of class: " + clazz, e);
        }
    }

    private Iterator<SettableBeanProperty> tryGetProperties(Class<?> clazz,
            DeserializationContext ctxt) throws IOException
    {
        JavaType type = ctxt.getConfig().getTypeFactory().constructType(clazz);
        JsonDeserializer deserializer =
                getBeanDeserializerFactory().createBeanDeserializer(ctxt.getConfig(),
                        ctxt.getDeserializerProvider(), type, null);

        if (deserializer instanceof BeanDeserializer)
        {
            return ((BeanDeserializer) deserializer).properties();
        } else
        {
            return null;
        }
    }

    private void trySetProperties(Object instance, Iterator<SettableBeanProperty> properties,
            Map values) throws IOException
    {
        while (properties.hasNext())
        {
            SettableBeanProperty property = properties.next();
            Object value = values.get(property.getName());
            property.set(instance, value);
        }
    }

    private BeanDeserializerFactory getBeanDeserializerFactory()
    {
        return BeanDeserializerFactory.instance;
    }

    private JsonDeserializer<Object> getOriginalDeserializer()
    {
        return originalDeserializer;
    }

    private IJsonTypeValueToClassObjectMapping getTypeValueToClassObjectMapping()
    {
        return typeValueToClassObjectMapping;
    }

    private IJsonClassValueToClassObjectsMapping getClassValueToClassObjectsMapping()
    {
        return classValueToClassObjectsMapping;
    }

}
