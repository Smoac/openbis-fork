/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.logging;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * A logger useful for testing.
 * 
 * @author Bernd Rinn
 */
public class AssertingLogger implements ISimpleLogger
{
    private final List<LogRecord> records = new ArrayList<LogRecord>();

    public void log(final LogLevel level, final String message)
    {
        records.add(new LogRecord(level, message));
    }

    public void assertNumberOfMessage(final int expectedNumberOfMessages)
    {
        assertEquals(expectedNumberOfMessages, records.size());
    }

    public void assertEq(final int i, final LogLevel expectedLevel, final String expectedMessage)
    {
        assertEquals(expectedLevel, records.get(i).level);
        assertEquals(expectedMessage, records.get(i).message);
    }

    private static class LogRecord
    {
        final LogLevel level;

        final String message;

        LogRecord(final LogLevel level, final String message)
        {
            this.level = level;
            this.message = message;
        }
    }
}