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

package ch.systemsx.cisd.common.api.server.json.resolver;

import java.util.Collection;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.SubtypeResolver;

import ch.systemsx.cisd.common.api.server.json.mapping.IJsonBaseTypeToSubTypesMapping;

/**
 * A custom resolver that detects sub types automatically using Reflections library instead of
 * reading @JsonSubTypes annotations that had to be maintained manually.
 * 
 * @author pkupczyk
 */
public class JsonReflectionsSubTypeResolver extends SubtypeResolver
{

    private IJsonBaseTypeToSubTypesMapping typeToSubTypesMapping;

    public JsonReflectionsSubTypeResolver(IJsonBaseTypeToSubTypesMapping typeToSubTypesMapping)
    {
        this.typeToSubTypesMapping = typeToSubTypesMapping;
    }

    @Override
    public void registerSubtypes(NamedType... types)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerSubtypes(Class<?>... classes)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypes(AnnotatedMember property,
            MapperConfig<?> config, AnnotationIntrospector ai)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypes(AnnotatedClass basetype,
            MapperConfig<?> config, AnnotationIntrospector ai)
    {
        return typeToSubTypesMapping.getSubTypes(basetype.getRawType());
    }

}
