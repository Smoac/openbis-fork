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

package ch.systemsx.cisd.common.api.server.json;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;

import ch.systemsx.cisd.common.api.server.json.deserializer.JsonDeserializerFactory;
import ch.systemsx.cisd.common.api.server.json.introspector.JsonTypeAndClassAnnotationIntrospector;
import ch.systemsx.cisd.common.api.server.json.mapping.IJsonBaseTypeToSubTypesMapping;
import ch.systemsx.cisd.common.api.server.json.mapping.IJsonClassValueToClassObjectsMapping;
import ch.systemsx.cisd.common.api.server.json.mapping.JsonReflectionsBaseTypeToSubTypesMapping;
import ch.systemsx.cisd.common.api.server.json.mapping.JsonStaticClassValueToClassObjectsMapping;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithContainerTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithDateTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithEnumTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithIgnoredProperties;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNested;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNestedChild;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithPrimitiveTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithPrivateAccess;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithRenamedProperties;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithType;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeA;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeAA;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeALegalDuplicate;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeB;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeBIllegalDuplicate;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeButNoSubtypes;
import ch.systemsx.cisd.common.api.server.json.resolver.JsonReflectionsSubTypeResolver;
import ch.systemsx.cisd.common.api.server.json.serializer.JsonSerializerFactory;

/**
 * @author pkupczyk
 */
public class JsonTestObjectMapper extends ObjectMapper
{

    public JsonTestObjectMapper()
    {
        super(null, null, new DefaultDeserializationContext.Impl(new JsonDeserializerFactory(
                getClassMapping())));
        setAnnotationIntrospector(new JsonTypeAndClassAnnotationIntrospector(getClassMapping()));
        setSubtypeResolver(new JsonReflectionsSubTypeResolver(getTypeMapping()));
        setSerializerFactory(new JsonSerializerFactory());
        configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    private static IJsonClassValueToClassObjectsMapping getClassMapping()
    {
        JsonStaticClassValueToClassObjectsMapping classMapping =
                new JsonStaticClassValueToClassObjectsMapping();
        classMapping.addClass(ObjectWithType.CLASS, ObjectWithType.class);
        classMapping.addClass(ObjectWithTypeA.CLASS, ObjectWithTypeA.class);
        classMapping.addClass(ObjectWithTypeA.CLASS, ObjectWithTypeALegalDuplicate.class);
        classMapping.addClass(ObjectWithTypeAA.CLASS, ObjectWithTypeAA.class);
        classMapping.addClass(ObjectWithTypeB.CLASS, ObjectWithTypeB.class);
        classMapping.addClass(ObjectWithTypeB.CLASS, ObjectWithTypeBIllegalDuplicate.class);
        classMapping.addClass(ObjectWithTypeButNoSubtypes.CLASS, ObjectWithTypeButNoSubtypes.class);
        classMapping.addClass(ObjectNested.CLASS, ObjectNested.class);
        classMapping.addClass(ObjectNestedChild.CLASS, ObjectNestedChild.class);
        classMapping.addClass(ObjectWithPrimitiveTypes.CLASS, ObjectWithPrimitiveTypes.class);
        classMapping.addClass(ObjectWithNestedTypes.CLASS, ObjectWithNestedTypes.class);
        classMapping.addClass(ObjectWithEnumTypes.CLASS, ObjectWithEnumTypes.class);
        classMapping.addClass(ObjectWithContainerTypes.CLASS, ObjectWithContainerTypes.class);
        classMapping.addClass(ObjectWithDateTypes.CLASS, ObjectWithDateTypes.class);
        classMapping.addClass(ObjectWithIgnoredProperties.CLASS, ObjectWithIgnoredProperties.class);
        classMapping.addClass(ObjectWithRenamedProperties.CLASS, ObjectWithRenamedProperties.class);
        classMapping.addClass(ObjectWithPrivateAccess.CLASS, ObjectWithPrivateAccess.class);
        return classMapping;
    }

    private static IJsonBaseTypeToSubTypesMapping getTypeMapping()
    {
        return new JsonReflectionsBaseTypeToSubTypesMapping(JsonTestObjectMapper.class.getPackage()
                .getName());
    }

}
