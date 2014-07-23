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
import java.util.Date;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("DateFieldSearchCriterion")
public class DateFieldSearchCriterion extends AbstractFieldSearchCriterion<IDate>
{

    private static final long serialVersionUID = 1L;

    private ITimeZone timeZone = new ServerTimeZone();

    private IDateFormat dateFormat = new NormalDateFormat();

    DateFieldSearchCriterion(String fieldName, SearchFieldType fieldType)
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

    public DateFieldSearchCriterion withServerTimeZone()
    {
        this.timeZone = new ServerTimeZone();
        return this;
    }

    public DateFieldSearchCriterion withTimeZone(int hourOffset)
    {
        this.timeZone = new TimeZone(hourOffset);
        return this;
    }

    public DateFieldSearchCriterion withShortFormat()
    {
        this.dateFormat = new ShortDateFormat();
        return this;
    }

    public DateFieldSearchCriterion withNormalFormat()
    {
        this.dateFormat = new NormalDateFormat();
        return this;
    }

    public DateFieldSearchCriterion withLongFormat()
    {
        this.dateFormat = new LongDateFormat();
        return this;
    }

    public void setDateFormat(IDateFormat dateFormat)
    {
        checkValueFormat(getFieldValue(), dateFormat);
        this.dateFormat = dateFormat;
    }

    public IDateFormat getDateFormat()
    {
        return dateFormat;
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
        checkValueFormat(value, getDateFormat());
        super.setFieldValue(value);
    }

    private static void checkValueFormat(IDate value, IDateFormat format)
    {
        if (value instanceof AbstractDateValue)
        {
            try
            {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format.getFormat());
                simpleDateFormat.setLenient(false);
                simpleDateFormat.parse(((AbstractDateValue) value).getValue());
            } catch (ParseException e)
            {
                throw new IllegalArgumentException("Date value: " + value + " does not match the format: " + format);
            }
        }
    }

}