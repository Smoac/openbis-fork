/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("dto.search.DateFieldSearchCriteria")
public class DateFieldSearchCriteria extends AbstractFieldSearchCriteria<IDate>
{

    private static final long serialVersionUID = 1L;

    private static final List<IDateFormat> DATE_FORMATS = new ArrayList<IDateFormat>();
    
    static {
        DATE_FORMATS.add(new ShortDateFormat());
        DATE_FORMATS.add(new NormalDateFormat());
        DATE_FORMATS.add(new LongDateFormat());
    }
    
    private ITimeZone timeZone = new ServerTimeZone();

    DateFieldSearchCriteria(String fieldName, SearchFieldType fieldType)
    {
        super(fieldName, fieldType);
    }

    public void thatEquals(Date date)
    {
        setFieldValue(new DateObjectEqualToValue(date));
    }

    public void thatEquals(String date)
    {
        setFieldValue(new DateEqualToValue(date));
    }

    public void thatIsLaterThanOrEqualTo(Date date)
    {
        setFieldValue(new DateObjectLaterThanOrEqualToValue(date));
    }

    public void thatIsLaterThanOrEqualTo(String date)
    {
        setFieldValue(new DateLaterThanOrEqualToValue(date));
    }

    public void thatIsEarlierThanOrEqualTo(Date date)
    {
        setFieldValue(new DateObjectEarlierThanOrEqualToValue(date));
    }

    public void thatIsEarlierThanOrEqualTo(String date)
    {
        setFieldValue(new DateEarlierThanOrEqualToValue(date));
    }

    public DateFieldSearchCriteria withServerTimeZone()
    {
        this.timeZone = new ServerTimeZone();
        return this;
    }

    public DateFieldSearchCriteria withTimeZone(int hourOffset)
    {
        this.timeZone = new TimeZone(hourOffset);
        return this;
    }

    public void setTimeZone(ITimeZone timeZone)
    {
        this.timeZone = timeZone;
    }

    public ITimeZone getTimeZone()
    {
        return timeZone;
    }

    @Override
    public void setFieldValue(IDate value)
    {
        checkValueFormat(value);
        super.setFieldValue(value);
    }

    private static void checkValueFormat(IDate value)
    {
        if (value instanceof AbstractDateValue)
        {
            for (IDateFormat dateFormat : DATE_FORMATS)
            {
                try
                {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat.getFormat());
                    simpleDateFormat.setLenient(false);
                    simpleDateFormat.parse(((AbstractDateValue) value).getValue());
                    return;
                } catch (ParseException e)
                {
                    // do nothing
                }
            }

            throw new IllegalArgumentException("Date value: " + value + " does not match any of the supported formats: "
                    + DATE_FORMATS);
        }
    }

}