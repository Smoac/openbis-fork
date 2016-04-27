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

/**
 * The identity converter for <code>String</code>s.
 * 
 * @author Bernd Rinn
 */
public final class IdentityStringConverter implements Converter<String>
{

    private final String defaultValue;

    public IdentityStringConverter()
    {
        this(null);
    }

    public IdentityStringConverter(final String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    //
    // Converter
    //

    public String convert(String value)
    {
        return value;
    }

    /**
     * This converter does not have a default value.
     * 
     * @return <code>null</code>
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }
}
