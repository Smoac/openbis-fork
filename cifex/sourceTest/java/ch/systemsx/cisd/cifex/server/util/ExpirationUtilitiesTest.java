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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.Test;

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
        assertEquals("2009-12-12 23:59:59", df.format(ExpirationUtilities.fixExpiration(d, null, null)));

        d = df.parse("2009-12-12 14:39:55");
        assertEquals("2009-12-12 23:59:59", df.format(ExpirationUtilities.fixExpiration(d, rd, 10)));
        assertEquals("2009-12-12 23:59:59", df.format(ExpirationUtilities.fixExpiration(d, rd, 3)));
        assertEquals("2009-12-11 23:59:59", df.format(ExpirationUtilities.fixExpiration(d, rd, 2)));
        assertEquals("2009-12-10 23:59:59", df.format(ExpirationUtilities.fixExpiration(d, rd, 1)));
    }
    
}
