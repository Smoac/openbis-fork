/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.detailed;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.time.DateUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;

/**
 * @author pkupczyk
 */
public class DateRangeCalculator
{

    private static final String FORMAT_WITHOUT_TIME = "y-M-d";

    private static final String FORMAT_WITH_MINUTES = "y-M-d HH:mm";

    private static final String FORMAT_WITH_SECONDS = "y-M-d HH:mm:ss";

    private static final String[] DATE_FORMATS = { FORMAT_WITH_SECONDS, FORMAT_WITH_MINUTES, FORMAT_WITHOUT_TIME };

    private Date upper;

    private Date lower;

    public DateRangeCalculator(String date, String timeZone, CompareType compareType)
    {
        lower = parseDate(date);
        makeDateGMT(lower, timeZone);

        if (CompareType.EQUALS.equals(compareType))
        {
            if (isDateWithTime(date))
            {
                lower = new Date(lower.getTime() - (lower.getTime() % DateUtils.MILLIS_PER_DAY));
            }
            upper = new Date(lower.getTime() + DateUtils.MILLIS_PER_DAY);
        } else if (CompareType.LESS_THAN_OR_EQUAL.equals(compareType))
        {
            if (isDateWithoutTime(date))
            {
                upper = new Date(lower.getTime() + DateUtils.MILLIS_PER_DAY);
            } else
            {
                upper = new Date(lower.getTime());
            }
            lower = new Date(0);
        } else if (CompareType.MORE_THAN_OR_EQUAL.equals(compareType))
        {
            upper = new Date(Long.MAX_VALUE);
        }
    }

    public Date getLowerDate()
    {
        return lower;
    }

    public Date getUpperDate()
    {
        return upper;
    }

    private Date parseDate(String dateAsString)
    {
        for (String format : DATE_FORMATS)
        {
            Date date = parseDate(dateAsString, format);
            if (date != null)
            {
                return date;
            }
        }
        throw new UserFailureException("Couldn't parse date '" + dateAsString
                + "'. It has to match one of the following formats: " + Arrays.asList(DATE_FORMATS));
    }

    private Date parseDate(String dateAsString, String formatAsString)
    {
        SimpleDateFormat format = new SimpleDateFormat(formatAsString);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
            return format.parse(dateAsString);
        } catch (ParseException ex)
        {
            return null;
        }
    }

    private void makeDateGMT(Date date, String timeZone)
    {
        int offset = getTimeZoneOffset(timeZone, date);
        date.setTime(date.getTime() + offset);
    }

    private int getTimeZoneOffset(String timeZone, Date date)
    {
        if (timeZone.equals(DetailedSearchCriterion.SERVER_TIMEZONE))
        {
            return -TimeZone.getDefault().getOffset(date.getTime());
        }

        String offset = timeZone;
        if (timeZone.startsWith("+"))
        {
            offset = timeZone.substring(1);
        } else if (timeZone.equals("Z"))
        {
            offset = "0";
        }

        try
        {
            return (int) (-Double.parseDouble(offset) * DateUtils.MILLIS_PER_HOUR);
        } catch (NumberFormatException e)
        {
            return 0;
        }
    }

    private boolean isDateWithTime(String dateAsString)
    {
        return parseDate(dateAsString, FORMAT_WITH_MINUTES) != null || parseDate(dateAsString, FORMAT_WITH_SECONDS) != null;
    }

    private boolean isDateWithoutTime(String dateAsString)
    {
        return false == isDateWithTime(dateAsString);
    }

}
