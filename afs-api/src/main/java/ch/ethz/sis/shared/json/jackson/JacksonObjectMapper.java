/*
 * Copyright 2022 ETH Zürich, SIS
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

package ch.ethz.sis.shared.json.jackson;

import ch.ethz.sis.shared.json.JSONObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.InputStream;

public class JacksonObjectMapper implements JSONObjectMapper
{
    //
    // Singleton
    //
    private static final JacksonObjectMapper jacksonObjectMapper;

    static
    {
        jacksonObjectMapper = new JacksonObjectMapper();
    }

    public static JSONObjectMapper getInstance()
    {
        return jacksonObjectMapper;
    }

    //
    // Class implementation
    //

    private final ObjectMapper objectMapper;

    public JacksonObjectMapper()
    {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.enableDefaultTyping();
    }

    @Override
    public <T> T readValue(final InputStream src, final Class<T> valueType) throws Exception
    {
        return objectMapper.readValue(src, valueType);
    }

    @Override
    public byte[] writeValue(final Object value) throws Exception
    {
        return objectMapper.writeValueAsBytes(value);
    }
}
