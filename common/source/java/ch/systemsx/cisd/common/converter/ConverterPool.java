/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * A pool of {@link Converter}.
 * <p>
 * You can have your own instance of this class or you can use the static accessor to get a 'public' instance.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ConverterPool
{
    
    /** A 'public' instance of this class. */
    private final static ConverterPool instance = new ConverterPool();

    private final Map<Class, Converter> converters;

    public ConverterPool()
    {
        converters = createConverters();
    }

    private final static Map<Class, Converter> createConverters()
    {
        return new HashMap<Class, Converter>();
    }

    /** Returns <code>instance</code>. */
    public final static ConverterPool getInstance()
    {
        return instance;
    }

    /**
     * Registers given <code>Converter</code> for given <code>Class</code>.
     */
    public final <T> void registerConverter(Class<T> type, Converter<T> converter)
    {
        converters.put(type, converter);
    }

    /**
     * Unegisters corresponding <code>Converter</code> for given <code>Class</code>.
     */
    public final <T> void unregisterConverter(Class<T> type)
    {
        converters.remove(type);
    }

    /** Does the conversion. */
    public final Object convert(String value, String format, Class type)
    {
        Converter converter = converters.get(type);
        if (format != null)
        {
            converter.setFormat(format);
        }
        if (converter == null)
        {
            return value;
        }
        if (value == null)
        {
            converter.getDefaultValue();
        }
        return converter.convert(value);
    }
}