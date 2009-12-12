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

package ch.systemsx.cisd.common.utilities;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

/**
 * A suite of utilities surrounding the use of the {@link java.util.Calendar} and
 * {@link java.util.Date} object.
 * 
 * @author Christian Ribeaud
 */
public final class DateTimeUtils
{
    /**
     * Returns the time zone in the following form: <code>GMT+01:00</code> (could not be easily
     * performed using {@link DateFormat}).
     */
    public final static String getTimeZone(final Date date)
    {
        assert date != null : "Unspecified date.";
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        final StringBuffer zoneString = new StringBuffer();
        int value = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
        if (value < 0)
        {
            zoneString.append('-');
            // Suppress the '-' sign for text display.
            value = -value;
        } else
        {
            zoneString.append('+');
        }
        int num = value / (int) DateUtils.MILLIS_PER_HOUR;
        zoneString.append(prependZeroIfNeeded(num));
        zoneString.append(":");
        num = (value % (int) DateUtils.MILLIS_PER_HOUR) / (int) DateUtils.MILLIS_PER_MINUTE;
        zoneString.append(prependZeroIfNeeded(num));
        return "GMT" + zoneString.toString();
    }
    
    private final static String prependZeroIfNeeded(final int num)
    {
        return num < 10 ? "0" + num : "" + num;
    }
    
    /**
     * Renders the specified duration.
     */
    public static String renderDuration(long durationInMilliseconds)
    {
        if (durationInMilliseconds < 1000)
        {
            return render(durationInMilliseconds, "millisecond");
        }
        long durationInSeconds = (durationInMilliseconds + 500) / 1000;
        if (durationInSeconds < 100)
        {
            return render(durationInSeconds, "second");
        }
        long durationInMinutes = (durationInSeconds + 30) / 60;
        if (durationInMinutes < 60)
        {
            return render(durationInMinutes, "minute");
        }
        long minutes = durationInMinutes % 60;
        long hours = durationInMinutes / 60;
        return render(hours, "hour") + " and " + render(minutes, "minute");
    }
    
    private static String render(long value, String unit)
    {
        return value == 1 ? value + " " + unit : value + " " + unit + "s"; 
    }
    
    /**
     * Extends the given <var>date</var> until the end of the day.
     */
    public static Date extendUntilEndOfDay(Date date)
    {
        return DateUtils.addMilliseconds(DateUtils.addDays(DateUtils.truncate(date,
                Calendar.DAY_OF_MONTH), 1), -1);
    }


}
