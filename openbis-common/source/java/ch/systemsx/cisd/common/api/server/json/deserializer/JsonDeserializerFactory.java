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
import java.io.StringWriter;
import java.util.Collection;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.deser.BeanDeserializerFactory;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.impl.TypeNameIdResolver;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.type.JavaType;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.api.server.json.common.JsonConstants;
import ch.systemsx.cisd.common.api.server.json.mapping.IJsonClassValueToClassObjectsMapping;
import ch.systemsx.cisd.common.api.server.json.mapping.JsonStaticClassValueToClassObjectsMapping;

/**
 * @author pkupczyk
 */
public class JsonDeserializerFactory extends BeanDeserializerFactory
{

    private IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping =
            new JsonStaticClassValueToClassObjectsMapping();

    public JsonDeserializerFactory(
            IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping)
    {
        super(null);
        this.classValueToClassObjectsMapping = classValueToClassObjectsMapping;
    }

    @Override
    public JsonDeserializer<?> createArrayDeserializer(DeserializationConfig config,
            DeserializerProvider p, ArrayType type, BeanProperty property)
            throws JsonMappingException
    {
        ArrayType newType =
                type.withContentTypeHandler(createContentTypeDeserializer(config, type, property));
        return super.createArrayDeserializer(config, p, newType, property);
    }

    @Override
    public JsonDeserializer<?> createCollectionDeserializer(DeserializationConfig config,
            DeserializerProvider p, CollectionType type, BeanProperty property)
            throws JsonMappingException
    {
        CollectionType newType =
                type.withContentTypeHandler(createContentTypeDeserializer(config, type, property));
        return super.createCollectionDeserializer(config, p, newType, property);
    }

    @Override
    public JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationConfig config,
            DeserializerProvider p, CollectionLikeType type, BeanProperty property)
            throws JsonMappingException
    {
        CollectionLikeType newType =
                type.withContentTypeHandler(createContentTypeDeserializer(config, type, property));
        return super.createCollectionLikeDeserializer(config, p, newType, property);
    }

    @Override
    public JsonDeserializer<?> createMapDeserializer(DeserializationConfig config,
            DeserializerProvider p, MapType type, BeanProperty property)
            throws JsonMappingException
    {
        MapType newType =
                type.withContentTypeHandler(createContentTypeDeserializer(config, type, property));
        return super.createMapDeserializer(config, p, newType, property);
    }

    @Override
    public JsonDeserializer<?> createMapLikeDeserializer(DeserializationConfig config,
            DeserializerProvider p, MapLikeType type, BeanProperty property)
            throws JsonMappingException
    {
        MapLikeType newType =
                type.withContentTypeHandler(createContentTypeDeserializer(config, type, property));
        return super.createMapLikeDeserializer(config, p, newType, property);
    }

    private TypeDeserializer createContentTypeDeserializer(DeserializationConfig config,
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
                    TypeNameIdResolver.construct(config, contentType, subtypes, false, true);
            JsonTypeAndClassWithFallbackDeserializer deserializer =
                    new JsonTypeAndClassWithFallbackDeserializer(contentType, subtypes, resolver,
                            property, JsonConstants.getTypeField());
            deserializer.setClassValueToClassObjectsMapping(this.classValueToClassObjectsMapping);
            return deserializer;
        } else
        {
            return null;
        }
    }

    private class JsonTypeAndClassWithFallbackDeserializer extends JsonTypeAndClassDeserializer
    {

        public JsonTypeAndClassWithFallbackDeserializer(JavaType type,
                Collection<NamedType> subtypes, TypeIdResolver idRes, BeanProperty property,
                String typePropName)
        {
            super(type, subtypes, idRes, property, typePropName);
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser parser, DeserializationContext ctxt)
                throws IOException
        {
            String json = parserToString(parser);
            JsonFactory factory = new JsonFactory(parser.getCodec());

            try
            {
                JsonParser jp1 = factory.createJsonParser(json);
                jp1.nextToken();
                return super.deserializeTypedFromObject(jp1, ctxt);
            } catch (IOException e)
            {
                JsonParser jp2 = factory.createJsonParser(json);
                jp2.nextToken();
                return super.deserializeWithoutType(jp2, ctxt, null);
            }
        }

        private String parserToString(JsonParser parser)
        {
            try
            {
                JsonFactory factory = new JsonFactory(parser.getCodec());
                StringWriter writer = new StringWriter();
                JsonGenerator generator = factory.createJsonGenerator(writer);
                generator.setCodec(parser.getCodec());
                JsonNode node = parser.readValueAs(JsonNode.class);
                generator.writeTree(node);
                return writer.toString();
            } catch (IOException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
    }

}
