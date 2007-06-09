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
    /**
     * Converts the <var>value</var> <code>String</code> into an object of type <code>T</code>.
     * 
     * @param value The <code>String</code> to convert. <code>null</code> is acceptable here.
     * @return The converted value, or <code>null</code>, if the <var>value</var> was <code>null</code>.
     */
    public T convert(String value);

    /**
     * @return The default in case the value we try to convert is <code>null</code>. If the converter does not
     *         support a default value, the return value of this method may be <code>null</code> as well.
     */
    public T getDefaultValue();

    /**
     * Sets a <code>String</code> format for this <code>Converter</code>. What this means exactly or whether this
     * makes sense at all, depends on the type <code>T</code> of course.
     * 
     *  FIXME 2007-06-09, Bernd Rinn: Is this method reasonable? How will it be used?
     */
    public void setFormat(String format);
}
