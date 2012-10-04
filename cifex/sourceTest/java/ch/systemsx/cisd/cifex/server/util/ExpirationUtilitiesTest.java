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

package ch.systemsx.cisd.cifex.server.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.time.DateTimeUtils;

/**
 * Test cases for {@link ExpirationUtilities}.
 * 
 * @author Bernd Rinn
 */
public class ExpirationUtilitiesTest
{

    @Test
    public void testFixExpiration() throws ParseException
    {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final Date rd = df.parse("2009-12-09 11:23:15");
        Date d = df.parse("2009-12-12 14:39:55");
        assertEquals("2009-12-12 23:59:59",
                df.format(ExpirationUtilities.fixExpiration(new Date(), d, null, null, 0)));

        d = df.parse("2009-12-12 14:39:55");
        assertEquals("2009-12-12 23:59:59",
                df.format(ExpirationUtilities.fixExpiration(new Date(), d, rd, 10, 0)));
        assertEquals("2009-12-12 23:59:59",
                df.format(ExpirationUtilities.fixExpiration(new Date(), d, rd, 3, 0)));
        assertEquals("2009-12-11 23:59:59",
                df.format(ExpirationUtilities.fixExpiration(new Date(), d, rd, 2, 0)));
        assertEquals("2009-12-10 23:59:59",
                df.format(ExpirationUtilities.fixExpiration(new Date(), d, rd, 1, 0)));
    }

    @Test
    public void testFixExpirationDefault() throws ParseException
    {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final Date rd = df.parse("2009-12-09 11:23:15");
        assertEquals("2009-12-10 23:59:59",
                df.format(ExpirationUtilities.fixExpiration(new Date(), null, rd, null, 1)));
    }

    @Test
    public void testTryExtendExpirationForPermanentUser()
    {
        final Date fileRetentionDate = DateUtils.addDays(new Date(), 7);
        assertNull(ExpirationUtilities.tryExtendExpiration(new Date(), null, new Date(0L), fileRetentionDate,
                10));
    }

    @Test
    public void testTryExtendExpirationForTemporaryUserExtensionNecessaryAndGranted()
    {
        final Date now = new Date();
        final Date registrationDate = DateUtils.addDays(now, -10);
        final Date currentExpirationDate =
                DateTimeUtils.extendUntilEndOfDay(DateUtils.addDays(now, 2));
        final int maxNumberOfDaysUserRetention = 20;
        final Date fileRetentionDate = DateUtils.addDays(now, 7);
        final Date expectedNewExpirationDate = DateTimeUtils.extendUntilEndOfDay(fileRetentionDate);
        assertEquals(expectedNewExpirationDate, ExpirationUtilities.tryExtendExpiration(now,
                currentExpirationDate, registrationDate, fileRetentionDate,
                maxNumberOfDaysUserRetention));
    }

    @Test
    public void testTryExtendExpirationForTemporaryUserExtensionNecessaryButDenied()
    {
        final Date now = new Date();
        final Date registrationDate = DateUtils.addDays(now, -10);
        final Date currentExpirationDate =
                DateTimeUtils.extendUntilEndOfDay(DateUtils.addDays(now, 2));
        final int maxNumberOfDaysUserRetention = 12;
        final Date fileRetentionDate = DateUtils.addDays(now, 7);
        assertNull(ExpirationUtilities.tryExtendExpiration(now, currentExpirationDate,
                registrationDate, fileRetentionDate, maxNumberOfDaysUserRetention));
    }

    @Test
    public void testTryExtendExpirationForTemporaryUserExtensionUnnecessary()
    {
        final Date now = new Date();
        final Date registrationDate = DateUtils.addDays(now, -10);
        final Date currentExpirationDate =
                DateTimeUtils.extendUntilEndOfDay(DateUtils.addDays(now, 10));
        final int maxNumberOfDaysUserRetention = 20;
        final Date fileRetentionDate = DateUtils.addDays(now, 7);
        assertNull(ExpirationUtilities.tryExtendExpiration(now, currentExpirationDate,
                registrationDate, fileRetentionDate, maxNumberOfDaysUserRetention));
    }

}
