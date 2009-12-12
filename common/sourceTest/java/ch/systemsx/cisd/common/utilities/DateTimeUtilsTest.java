/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link DateTimeUtils}.
 *
 * @author Franz-Josef Elmer
 */
public class DateTimeUtilsTest extends AssertJUnit
{
    @Test
    public void testRenderDuration()
    {
        assertEquals("42 milliseconds", DateTimeUtils.renderDuration(42));
        assertEquals("1 second", DateTimeUtils.renderDuration(1000));
        assertEquals("42 seconds", DateTimeUtils.renderDuration(42 * 1000 + 499));
        assertEquals("42 seconds", DateTimeUtils.renderDuration(42 * 1000 - 499));
        assertEquals("99 seconds", DateTimeUtils.renderDuration(99 * 1000));
        assertEquals("2 minutes", DateTimeUtils.renderDuration(100 * 1000));
        assertEquals("42 minutes", DateTimeUtils.renderDuration(42 * 1000 * 60));
        assertEquals("59 minutes", DateTimeUtils.renderDuration(59 * 1000 * 60));
        assertEquals("1 hour and 0 minutes", DateTimeUtils.renderDuration(60 * 1000 * 60));
        assertEquals("1 hour and 1 minute", DateTimeUtils.renderDuration(61 * 1000 * 60));
        assertEquals("2 hours and 3 minutes", DateTimeUtils.renderDuration(123 * 1000 * 60));
    }

    @Test
    public void testExtendUntilEndOfDay() throws ParseException
    {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = df.parse("2009-12-12 14:39:55");
        assertEquals("2009-12-12 23:59:59", df.format(DateTimeUtils.extendUntilEndOfDay(d)));
        
        d = df.parse("2009-12-31 00:00:00");
        assertEquals("2009-12-31 23:59:59", df.format(DateTimeUtils.extendUntilEndOfDay(d)));

        d = df.parse("2010-01-01 23:59:59");
        assertEquals("2010-01-01 23:59:59", df.format(DateTimeUtils.extendUntilEndOfDay(d)));
    }
    
}
