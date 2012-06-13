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

package ch.systemsx.cisd.common.api.server.json.serializer;

import java.util.Collection;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeNameIdResolver;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.api.server.json.common.JsonConstants;

/**
 * @author pkupczyk
 */
public class JsonSerializerFactory extends BeanSerializerFactory
{

    public JsonSerializerFactory()
    {
        super(null);
    }

    @Override
    protected JsonSerializer<?> buildArraySerializer(SerializationConfig config, ArrayType type,
            BeanDescription beanDesc, boolean staticTyping, TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer) throws JsonMappingException
    {
        ArrayType newType =
                type.withContentTypeHandler(createContentTypeSerializer(config, type, null));
        return super.buildArraySerializer(config, newType, beanDesc, staticTyping,
                elementTypeSerializer, elementValueSerializer);
    }

    @Override
    protected JsonSerializer<?> buildCollectionSerializer(SerializationConfig config,
            CollectionType type, BeanDescription beanDesc, BeanProperty property,
            boolean staticTyping, TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer) throws JsonMappingException
    {
        CollectionType newType =
                type.withContentTypeHandler(createContentTypeSerializer(config, type, property));
        return super.buildCollectionSerializer(config, newType, beanDesc, property, staticTyping,
                elementTypeSerializer, elementValueSerializer);
    }

    @Override
    protected JsonSerializer<?> buildMapSerializer(SerializationConfig config, MapType type,
            BeanDescription beanDesc, boolean staticTyping, JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
            throws JsonMappingException
    {
        MapType newType =
                type.withContentTypeHandler(createContentTypeSerializer(config, type, null));
        return super.buildMapSerializer(config, newType, beanDesc, staticTyping, keySerializer,
                elementTypeSerializer, elementValueSerializer);
    }

    private TypeSerializer createContentTypeSerializer(SerializationConfig config,
            JavaType containerType, BeanProperty property)
    {
        JavaType contentType = containerType.getContentType();

        if (contentType == null || contentType.getRawClass() == null)
        {
            return null;
        }

        Class<?> contentClass = contentType.getRawClass();

        if (contentClass.equals(Object.class) || contentClass.isAnnotationPresent(JsonObject.class))
        {
            BeanDescription bean = config.introspectClassAnnotations(contentType.getRawClass());
            AnnotatedClass ac = bean.getClassInfo();
            AnnotationIntrospector ai = config.getAnnotationIntrospector();
            Collection<NamedType> subtypes =
                    config.getSubtypeResolver().collectAndResolveSubtypes(ac, config, ai);
            TypeIdResolver resolver =
                    TypeNameIdResolver.construct(config, contentType, subtypes, true, false);
            return new AsPropertyTypeSerializer(resolver, property, JsonConstants.getTypeField());
        } else
        {
            return null;
        }
    }

}
