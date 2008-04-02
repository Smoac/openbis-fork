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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * Test cases for corresponding {@link PropertyUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyUtilsTest
{

    private final BufferedAppender appender = new BufferedAppender();

    @Test
    public final void testGetMandatoryProperty()
    {
        try
        {
            PropertyUtils.getMandatoryProperty(null, null);
            fail("Null values not allowed.");
        } catch (final AssertionError e)
        {
            // Nothing to do here.
        }
        final Properties properties = new Properties();
        final String propertyKey = "key";
        try
        {
            PropertyUtils.getMandatoryProperty(properties, propertyKey);
            fail("Given property key not found.");
        } catch (final ConfigurationFailureException ex)
        {
            assertEquals(String.format(PropertyUtils.NOT_FOUND_PROPERTY_FORMAT, propertyKey, "[]"),
                    ex.getMessage());
        }
        final String value = "value";
        properties.setProperty(propertyKey, value);
        assertEquals(value, PropertyUtils.getMandatoryProperty(properties, propertyKey));
    }

    private final ISimpleLogger createSimpleLogger()
    {
        return new Log4jSimpleLogger(Logger.getRootLogger());
    }

    @Test
    public final void testGetLong()
    {
        appender.resetLogContent();
        final Properties properties = new Properties();
        final String propertyKey = "key";
        final long defaultValue = 123L;
        // Property not defined
        final ISimpleLogger simpleLogger = createSimpleLogger();
        assertEquals(defaultValue, PropertyUtils.getLong(properties, propertyKey, defaultValue,
                simpleLogger));
        assertEquals("", appender.getLogContent());
        // A non-long value
        appender.resetLogContent();
        String value = "choubidou";
        properties.setProperty(propertyKey, value);
        assertEquals(defaultValue, PropertyUtils.getLong(properties, propertyKey, defaultValue,
                simpleLogger));
        assertEquals(String.format(PropertyUtils.NON_LONG_VALUE_FORMAT, value, defaultValue),
                appender.getLogContent());
        // A long value
        appender.resetLogContent();
        value = "678";
        properties.setProperty(propertyKey, value);
        assertEquals(Long.parseLong(value), PropertyUtils.getLong(properties, propertyKey,
                defaultValue, simpleLogger));
        assertEquals("", appender.getLogContent());
    }

    @Test
    public final void testGetBoolean()
    {
        appender.resetLogContent();
        final Properties properties = new Properties();
        final String propertyKey = "key";
        final boolean defaultValue = false;
        // Property not defined
        final ISimpleLogger simpleLogger = createSimpleLogger();
        assertEquals(defaultValue, PropertyUtils.getBoolean(properties, propertyKey, defaultValue,
                simpleLogger));
        assertEquals("", appender.getLogContent());
        // A non-boolean value
        appender.resetLogContent();
        String value = "choubidou";
        properties.setProperty(propertyKey, value);
        assertEquals(defaultValue, PropertyUtils.getBoolean(properties, propertyKey, defaultValue,
                simpleLogger));
        assertEquals(String.format(PropertyUtils.NON_BOOLEAN_VALUE_FORMAT, value, defaultValue),
                appender.getLogContent());
        // TRUE
        appender.resetLogContent();
        value = "TRUE";
        properties.setProperty(propertyKey, value);
        assertEquals(true, PropertyUtils.getBoolean(properties, propertyKey, defaultValue,
                simpleLogger));
        assertEquals("", appender.getLogContent());
        // 1
        appender.resetLogContent();
        value = "1";
        properties.setProperty(propertyKey, value);
        assertEquals(true, PropertyUtils.getBoolean(properties, propertyKey, defaultValue,
                simpleLogger));
        assertEquals("", appender.getLogContent());
        // YeS
        appender.resetLogContent();
        value = "YeS";
        properties.setProperty(propertyKey, value);
        assertEquals(true, PropertyUtils.getBoolean(properties, propertyKey, defaultValue,
                simpleLogger));
        assertEquals("", appender.getLogContent());
        // on
        appender.resetLogContent();
        value = "on";
        properties.setProperty(propertyKey, value);
        assertEquals(defaultValue, PropertyUtils.getBoolean(properties, propertyKey, defaultValue,
                simpleLogger));
        assertEquals(String.format(PropertyUtils.NON_BOOLEAN_VALUE_FORMAT, value, defaultValue),
                appender.getLogContent());
    }
}