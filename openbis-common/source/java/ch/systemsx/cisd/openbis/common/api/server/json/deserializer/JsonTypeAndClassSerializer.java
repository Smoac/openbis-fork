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

package ch.systemsx.cisd.openbis.common.api.server.json.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
public class JsonTypeAndClassSerializer extends AsPropertyTypeSerializer
{

    public JsonTypeAndClassSerializer(TypeIdResolver idRes, BeanProperty property, String propName)
    {
        super(idRes, property, propName);
    }

    @Override
    public JsonTypeAndClassSerializer forProperty(BeanProperty prop)
    {
        if (_property == prop)
            return this;
        return new JsonTypeAndClassSerializer(this._idResolver, prop, this._typePropertyName);
    }

    @Override
    public void writeTypePrefixForObject(Object value, JsonGenerator jgen) throws IOException,
            JsonProcessingException
    {
        if (isValueWithType(value))
        {
            jgen.writeStartObject();
            jgen.writeStringField(_typePropertyName, idFromValue(value));
        } else
        {
            jgen.writeStartObject();
        }
    }

    @Override
    public void writeTypeSuffixForObject(Object value, JsonGenerator jgen) throws IOException,
            JsonProcessingException
    {
        jgen.writeEndObject();
    }

    @Override
    public void writeTypePrefixForScalar(Object value, JsonGenerator jgen) throws IOException,
            JsonProcessingException
    {
        if (isValueWithType(value))
        {
            super.writeTypePrefixForScalar(value, jgen);
        }
    }

    @Override
    public void writeTypeSuffixForScalar(Object value, JsonGenerator jgen) throws IOException,
            JsonProcessingException
    {
        if (isValueWithType(value))
        {
            super.writeTypeSuffixForScalar(value, jgen);
        }
    }

    @Override
    public void writeTypePrefixForArray(Object value, JsonGenerator jgen) throws IOException,
            JsonProcessingException
    {
        jgen.writeStartArray();
    }

    @Override
    public void writeTypeSuffixForArray(Object value, JsonGenerator jgen) throws IOException,
            JsonProcessingException
    {
        jgen.writeEndArray();
    }

    private boolean isValueWithType(Object value)
    {
        return value != null && value.getClass().isAnnotationPresent(JsonObject.class)
                && !value.getClass().isEnum();
    }

}
