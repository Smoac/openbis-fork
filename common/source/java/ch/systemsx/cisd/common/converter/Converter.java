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
 * General purpose data type converter that can be registered and used in {@link ConverterPool} to manage the conversion
 * of objects from one type to another.
 * 
 * @author Christian Ribeaud
 */
public interface Converter<T>
{
    /** Convert the specified input object into an output object of the specified type. */
    public T convert(String value);

    /** Returns the default in case the value we try to convert is <code>null</code>. */
    public T getDefaultValue();
    
    /** Sets a <code>String</code> format for this <code>Converter</code>. */
    public void setFormat(String format);
}
