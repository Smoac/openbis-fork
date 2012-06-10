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

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.impl.AsPropertyTypeSerializer;
import org.codehaus.jackson.map.jsontype.impl.TypeNameIdResolver;
import org.codehaus.jackson.map.ser.BeanSerializerFactory;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.type.JavaType;

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
            BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
    {
        ArrayType newType =
                type.withContentTypeHandler(createContentTypeSerializer(config, type, property));
        return super.buildArraySerializer(config, newType, beanDesc, property, staticTyping,
                elementTypeSerializer, elementValueSerializer);
    }

    @Override
    protected JsonSerializer<?> buildCollectionSerializer(SerializationConfig config,
            CollectionType type, BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping, TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer)
    {
        CollectionType newType =
                type.withContentTypeHandler(createContentTypeSerializer(config, type, property));
        return super.buildCollectionSerializer(config, newType, beanDesc, property, staticTyping,
                elementTypeSerializer, elementValueSerializer);
    }

    @Override
    protected JsonSerializer<?> buildCollectionLikeSerializer(SerializationConfig config,
            CollectionLikeType type, BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping, TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer)
    {
        CollectionLikeType newType =
                type.withContentTypeHandler(createContentTypeSerializer(config, type, property));
        return super.buildCollectionLikeSerializer(config, newType, beanDesc, property,
                staticTyping, elementTypeSerializer, elementValueSerializer);
    }

    @Override
    protected JsonSerializer<?> buildMapSerializer(SerializationConfig config, MapType type,
            BasicBeanDescription beanDesc, BeanProperty property, boolean staticTyping,
            JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer)
    {
        MapType newType =
                type.withContentTypeHandler(createContentTypeSerializer(config, type, property));
        return super.buildMapSerializer(config, newType, beanDesc, property, staticTyping,
                keySerializer, elementTypeSerializer, elementValueSerializer);
    }

    @Override
    protected JsonSerializer<?> buildMapLikeSerializer(SerializationConfig config,
            MapLikeType type, BasicBeanDescription beanDesc, BeanProperty property,
            boolean staticTyping, JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
    {
        MapLikeType newType =
                type.withContentTypeHandler(createContentTypeSerializer(config, type, property));
        return super.buildMapLikeSerializer(config, newType, beanDesc, property, staticTyping,
                keySerializer, elementTypeSerializer, elementValueSerializer);
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
            BasicBeanDescription bean =
                    config.introspectClassAnnotations(contentType.getRawClass());
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
