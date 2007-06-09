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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * A <code>Converter</code> implementation for {@link Date}.
 * <p>
 * {@link #getDefaultValue()} always returns the current system time.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class DateConverter implements Converter<Date>
{
    private SimpleDateFormat dateFormat;

    public DateConverter()
    {
    }

    public DateConverter(String datePattern)
    {
        setFormat(datePattern);
    }

    /**
     * Create a date format for the specified pattern.
     * 
     * @param pattern The date pattern
     * @return The DateFormat
     */
    private final static SimpleDateFormat createDateFormat(String pattern)
    {
        return new SimpleDateFormat(pattern);
    }

    //
    // Converter
    //

    /**
     * Converts given <code>value</code> to a <code>Date</code>.
     * <p>
     * Return {@link #getDefaultValue()} if no date pattern has been defined or if a <code>ParseException</code>
     * occurs.
     * </p>
     */
    public final Date convert(String value)
    {
        if (dateFormat == null)
        {
            // FIXME 2007-06-09, Bernd Rinn: This shoulnd't return a default value, because that is not a conversion of
            // the value at all. It is, tought, unlclear, what it should do. Why is it possible to construct a
            // DateConverer without a dateFormat in the first place?
            return getDefaultValue();
        }
        try
        {
            return dateFormat.parse(value);
        } catch (ParseException ex)
        {
            return getDefaultValue();
        }
    }

    public final Date getDefaultValue()
    {
        return new Date();
    }

    public final void setFormat(String datePattern)
    {
        if (StringUtils.isBlank(datePattern))
        {
            return;
        }
        if (dateFormat == null || dateFormat.toPattern().equals(datePattern) == false)
        {
            dateFormat = createDateFormat(datePattern);
        }
    }

}