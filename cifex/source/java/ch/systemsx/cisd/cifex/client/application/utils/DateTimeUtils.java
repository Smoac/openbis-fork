/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.utils;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * General date/time/duration manipulation utilities.
 * 
 * @author Christian Ribeaud
 */
public final class DateTimeUtils
{
    /**
     * Number of milliseconds in a standard second.
     */
    public static final long MILLIS_PER_SECOND = 1000;

    /**
     * Number of milliseconds in a standard minute.
     */
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;

    /**
     * Number of milliseconds in a standard hour.
     */
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;

    /**
     * Default date/time format.
     * <p>
     * <code>zzz</code> produces <code>GMT+01:00</code>.
     * </p>
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss zzz";

    /** Default <code>DateTimeFormat</code> used here. */
    private static final DateTimeFormat defaultDateTimeFormat =
            DateTimeFormat.getFormat(DateTimeUtils.DEFAULT_DATE_TIME_FORMAT);

    private DateTimeUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Formats a date object with default format {@link #DEFAULT_DATE_TIME_FORMAT}.
     */
    public final static String formatDate(final Date date)
    {
        return formatDate(date, null);
    }

    /**
     * Formats a date object with given pattern.
     */
    public final static String formatDate(final Date date, final String pattern)
    {
        assert date != null : "Undefined date.";
        final DateTimeFormat dateTimeFormat;
        if (pattern == null || pattern.equals(DEFAULT_DATE_TIME_FORMAT))
        {
            dateTimeFormat = defaultDateTimeFormat;
        } else
        {
            dateTimeFormat = DateTimeFormat.getFormat(pattern);
        }
        return dateTimeFormat.format(date);
    }

    private final static String padWithZerosIfNeeded(final String value)
    {
        assert value != null : "Missing value";
        return value.length() == 1 ? "0" + value : value;
    }

    /**
     * Formats the time gap as a string, using the format.
     * <p>
     * The format used is: <i>H</i>:<i>m</i>:<i>s</i>. Examples are: <code>123:09:23</code>,
     * <code>00:12:04</code>, etc.
     * </p>
     */
    public final static String formatDuration(final long millis)
    {
        long durationMillis = millis;

        final int hours = (int) (durationMillis / MILLIS_PER_HOUR);
        durationMillis = durationMillis - (hours * MILLIS_PER_HOUR);

        final int minutes = (int) (durationMillis / MILLIS_PER_MINUTE);
        durationMillis = durationMillis - (minutes * MILLIS_PER_MINUTE);

        final int seconds = (int) (durationMillis / MILLIS_PER_SECOND);
        durationMillis = durationMillis - (seconds * MILLIS_PER_SECOND);

        final StringBuffer buffer = new StringBuffer();
        buffer.append(padWithZerosIfNeeded(Integer.toString(hours))).append(":");
        buffer.append(padWithZerosIfNeeded(Integer.toString(minutes))).append(":");
        buffer.append(padWithZerosIfNeeded(Integer.toString(seconds)));
        return buffer.toString();
    }
    
    /**
     * Parses specified duration and return it in minutes. In case of a ':' everything left of ':'
     * will be interpreted as hours. Examples:
     * <code>42</code> and <code>0:42</code> will return 42. 
     * <code>6002</code> and <code>100:2</code> will return 6002.
     * 
     * @throw NumberFormatException if left or right of ':' not a number appears.
     */
    public static int parseDurationInMinutes(String duration)
    {
        int indexOfColon = duration.indexOf(':');
        if (indexOfColon < 0)
        {
            return Integer.parseInt(duration);
        }
        return Integer.parseInt(duration.substring(0, indexOfColon)) * 60
                + Integer.parseInt(duration.substring(indexOfColon + 1));
    }
    
    /**
     * Formats the specified duration into the form <code>&lt;hours&gt;:&lt;minutes&gt;</code>.
     */
    public static String formatDurationInMinutes(int duration)
    {
        if (duration < 60)
        {
            return Integer.toString(duration);
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(duration / 60).append(':');
        buffer.append(padWithZerosIfNeeded(Integer.toString(duration % 60)));
        return buffer.toString();
    }
}
