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

import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.codehaus.jackson.type.JavaType;

import ch.systemsx.cisd.common.api.server.json.mapping.IJsonClassValueToClassObjectsMapping;
import ch.systemsx.cisd.common.api.server.json.mapping.IJsonTypeValueToClassObjectMapping;

/**
 * @author pkupczyk
 */
public class JsonDeserializerProvider extends StdDeserializerProvider
{

    private IJsonTypeValueToClassObjectMapping typeValueToClassObjectMapping;

    private IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping;

    public JsonDeserializerProvider(
            IJsonTypeValueToClassObjectMapping typeValueToClassObjectMapping,
            IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping)
    {
        this.typeValueToClassObjectMapping = typeValueToClassObjectMapping;
        this.classValueToClassObjectsMapping = classValueToClassObjectsMapping;
    }

    @Override
    protected JsonDeserializer<Object> _createDeserializer(DeserializationConfig config,
            JavaType type, BeanProperty property) throws JsonMappingException
    {
        JsonDeserializer<Object> deserializer = super._createDeserializer(config, type, property);

        if (JsonContainerDeserializer.canDeserialize(type))
        {
            return new JsonContainerDeserializer(deserializer, typeValueToClassObjectMapping,
                    classValueToClassObjectsMapping);
        } else
        {
            return deserializer;
        }
    }

}
