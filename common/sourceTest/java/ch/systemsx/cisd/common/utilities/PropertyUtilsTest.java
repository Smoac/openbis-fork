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

import java.io.File;
import java.util.Properties;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;

/**
 * Test cases for corresponding {@link PropertyUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyUtilsTest extends AbstractFileSystemTestCase
{

    private BufferedAppender appender;

    @BeforeMethod
    public final void beforeMethod()
    {
        appender = new BufferedAppender();
        appender.resetLogContent();
    }
    
    @Test
    public void testLoadProperties()
    {
        File propertiesFile = new File(workingDirectory, "p.properties");
        FileUtilities.writeToFile(propertiesFile, "answer = 42\n\n# comment\n  key=4711  ");
        Properties properties = PropertyUtils.loadProperties(propertiesFile);
        
        assertEquals("42", properties.getProperty("answer"));
        assertEquals("4711", properties.getProperty("key"));
    }
    
    @Test
    public void testLoadInvalidProperties()
    {
        File propertiesFile = new File(workingDirectory, "p.properties");
        FileUtilities.writeToFile(propertiesFile, "answer=42\nquestion");
        
        try
        {
            PropertyUtils.loadProperties(propertiesFile);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Missing '=' in line 2 of properties file '" + propertiesFile
                    + "': question", ex.getMessage());
        }
    }

    @Test
    public final void testTrim()
    {
        final Properties properties = new Properties();
        final String propertyKey = "key";
        properties.setProperty(propertyKey, " value  ");
        assertEquals("value", PropertyUtils.getProperty(properties, propertyKey));
    }

    @Test
    public final void testGetMandatoryProperty()
    {
        boolean fail = true;
        try
        {
            PropertyUtils.getMandatoryProperty(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
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
        properties.setProperty(propertyKey, "  ");
        try
        {
            PropertyUtils.getMandatoryProperty(properties, propertyKey);
        } catch (final ConfigurationFailureException ex)
        {
            assertEquals(String.format(PropertyUtils.EMPTY_STRING_FORMAT, propertyKey), ex
                    .getMessage());
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

    @Test
    public final void testGetPosInt()
    {
        final String key = "posInt";
        final Properties properties = new Properties();
        // -7
        properties.setProperty(key, "-7");
        assertFalse(NumberUtils.isDigits("-7"));
        assertTrue(NumberUtils.isNumber("-7"));
        assertEquals(4, PropertyUtils.getPosInt(properties, key, 4));
        assertEquals(-7, PropertyUtils.getInt(properties, key, 4));
        // 0
        properties.setProperty(key, "0");
        assertEquals(0, PropertyUtils.getInt(properties, key, 4));
        assertEquals(0, PropertyUtils.getPosInt(properties, key, 4));
        // 34L
        properties.setProperty(key, "34L");
        assertEquals(34, PropertyUtils.getInt(properties, key, 4));
    }

    @Test
    public final void testGetPosLong()
    {
        final String key = "posInt";
        final Properties properties = new Properties();
        properties.setProperty(key, "-7");
        assertFalse(NumberUtils.isDigits("-7"));
        assertTrue(NumberUtils.isNumber("-7"));
        assertEquals(4, PropertyUtils.getPosLong(properties, key, 4));
        assertEquals(-7, PropertyUtils.getLong(properties, key, 4));
    }
}