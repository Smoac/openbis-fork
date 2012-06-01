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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.json;

import org.codehaus.jackson.map.ObjectMapper;

import ch.systemsx.cisd.common.api.server.json.deserializer.JsonDeserializerProvider;
import ch.systemsx.cisd.common.api.server.json.introspector.JsonTypeAndClassAnnotationIntrospector;
import ch.systemsx.cisd.common.api.server.json.mapping.JsonReflectionsBaseTypeToSubTypesMapping;
import ch.systemsx.cisd.common.api.server.json.mapping.JsonReflectionsTypeValueToClassObjectMapping;
import ch.systemsx.cisd.common.api.server.json.resolver.JsonReflectionsSubTypeResolver;

/**
 * Jackson library object mapper used in screening OpenBIS.
 * 
 * @author pkupczyk
 */
public class ScreeningObjectMapper extends ObjectMapper
{

    public ScreeningObjectMapper()
    {
        setAnnotationIntrospector(new JsonTypeAndClassAnnotationIntrospector(
                ScreeningJsonClassValueToClassObjectsMapping.getInstance()));
        setSubtypeResolver(new JsonReflectionsSubTypeResolver(
                JsonReflectionsBaseTypeToSubTypesMapping.getInstance()));
        setDeserializerProvider(new JsonDeserializerProvider(
                JsonReflectionsTypeValueToClassObjectMapping.getInstance(),
                ScreeningJsonClassValueToClassObjectsMapping.getInstance()));
    }

}
