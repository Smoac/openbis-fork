/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.shared.util;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator.TimestampValidator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Franz-Josef Elmer
 */
public class SimplePropertyValidatorTest
{

    @DataProvider
    public Object[][] validTimestamps()
    {
        return new Object[][]{
                {"2020-05-16", "2020-05-16 00:00:00 +0200"},
                {"2020-5-16", "2020-05-16 00:00:00 +0200"},
                {"2019-01-16", "2019-01-16 00:00:00 +0100"},
                {"2019-01-16 3:4", "2019-01-16 03:04:00 +0100"},
                {"2019-01-16 18:23:56", "2019-01-16 18:23:56 +0100"},
                {"2019-01-16 18:23:56 +0700", "2019-01-16 12:23:56 +0100"},
                {"2019-01-16 18:23:56 GMT", "2019-01-16 19:23:56 +0100"},
                {"1/16/19", "2019-01-16 00:00:00 +0100"},
                {"1/16/19 8:9", "2019-01-16 08:09:00 +0100"},
                {"1/16/19 8:9 p", "2019-01-16 20:09:00 +0100"},
                {"1/16/19 18:19", "2019-01-16 18:19:00 +0100"},
                {"2019-01-20T10:58", "2019-01-20 10:58:00 +0100"},
                {"2019-01-20T10:58:24", "2019-01-20 10:58:24 +0100"},
                {"2019-05-20T10:58:24", "2019-05-20 10:58:24 +0200"},
                {"2019-05-20T10:58:24+07:00", "2019-05-20 05:58:24 +0200"},
                {"2019-05-20T10:58:24-07:30", "2019-05-20 20:28:24 +0200"},
                {"2019-01-20T10:58:24Z", "2019-01-20 11:58:24 +0100"},
                {"2019-05-20T10:58:24Z", "2019-05-20 12:58:24 +0200"},
        };
    }

    @DataProvider
    public Object[][] invalidTimestamps()
    {
        return new Object[][] {
                { "10-05-06" },
                { "10-05-06 7:23" },
                { "10-05-06 17:13:39" },
                { "2010-05-36 17:13:39" },
                { "2010-05-06 27:13:39" },
                { "13/12/11 7:39" },
                { "3/12/11 7:39:22" },
                { "10-05-06T7:23" },
                { "10-05-06T17:13:39" },
                { "2010-05-36T17:13:39" },
                { "2010-05-06T27:13:39" },
        };
    }

    @Test(dataProvider = "validTimestamps")
    public void testTimestampValidatorWithValidExamples(String stringToParse, String canonicalTimestamp)
    {
        TimestampValidator validator = new SimplePropertyValidator.TimestampValidator();

        assertEquals(validator.validate(stringToParse), canonicalTimestamp);
    }

    @Test(dataProvider = "invalidTimestamps")
    public void testTimestampValidatorWithInvalidExamples(String stringToParse)
    {
        TimestampValidator validator = new SimplePropertyValidator.TimestampValidator();

        try
        {
            validator.validate(stringToParse);
        } catch (UserFailureException e)
        {
            assertEquals(e.getMessage(), "Date value '" + stringToParse + "' has improper format. " +
                    "It must be one of '[yyyy-MM-dd\n" +
                    "yyyy-MM-dd HH:mm\n" +
                    "yyyy-MM-dd HH:mm:ss\n" +
                    "yyyy-MM-dd'T'HH:mm\n" +
                    "yyyy-MM-dd'T'HH:mm:ss\n" +
                    "M/d/yy\n" +
                    "M/d/yy h:mm a\n" +
                    "M/d/yy HH:mm\n" +
                    "yyyy-MM-dd HH:mm:ss Z\n" +
                    "yyyy-MM-dd'T'HH:mm:ssX\n" +
                    "yyyy-MM-dd HH:mm:ss ZZZ\n" +
                    "yyyy-MM-dd'T'HH:mm:ssXXX]'.");
        }
    }

}
