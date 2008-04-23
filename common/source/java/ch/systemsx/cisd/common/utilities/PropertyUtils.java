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

import java.util.Collections;
import java.util.Properties;

import org.apache.commons.lang.math.NumberUtils;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * A static helper class that should help to extract typed values from {@link Properties}.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyUtils
{

    static final String EMPTY_STRING_FORMAT = "Property '%s' is an empty string.";

    static final String NON_BOOLEAN_VALUE_FORMAT =
            "Invalid boolean '%s'. Default value '%s' will be used.";

    static final String NON_CHAR_VALUE_FORMAT =
            "Invalid character '%s' (incorrect length). Default value '%s' will be used.";

    static final String NON_INT_VALUE_FORMAT =
            "Invalid integer '%s'. Default value '%s' will be used.";

    static final String NOT_POSITIVE_INT_VALUE_FORMAT =
            "Invalid positive integer '%s'. Default value '%s' will be used.";

    static final String NON_LONG_VALUE_FORMAT =
            "Invalid long '%s'. Default value '%s' will be used.";

    static final String NOT_POSITIVE_LONG_VALUE_FORMAT =
            "Invalid positive long '%s'. Default value '%s' will be used.";

    static final String NOT_FOUND_PROPERTY_FORMAT = "Given key '%s' not found in properties '%s'";

    private PropertyUtils()
    {
        // This class can not be instantiated.
    }

    private static void assertParameters(final Properties properties, final String propertyKey)
    {
        assert properties != null : "Given properties can not be null.";
        assert propertyKey != null : "Given property key can not be null.";
    }

    /**
     * Looks up given mandatory <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @throws ConfigurationFailureException if given <var>propertyKey</var> could not be found or
     *             if it is empty.
     */
    public final static String getMandatoryProperty(final Properties properties,
            final String propertyKey) throws ConfigurationFailureException
    {
        assertParameters(properties, propertyKey);
        String property = properties.getProperty(propertyKey);
        if (property == null)
        {
            throw ConfigurationFailureException.fromTemplate(NOT_FOUND_PROPERTY_FORMAT,
                    propertyKey, CollectionUtils.abbreviate(Collections.list(properties
                            .propertyNames()), 10));
        }
        property = property.trim();
        if (property.length() == 0)
        {
            throw ConfigurationFailureException.fromTemplate(EMPTY_STRING_FORMAT, propertyKey);
        }
        return property;
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static long getLong(final Properties properties, final String propertyKey,
            final long defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        final String longOrNull = properties.getProperty(propertyKey);
        if (longOrNull == null)
        {
            return defaultValue;
        }
        if (NumberUtils.isNumber(longOrNull) == false)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_LONG_VALUE_FORMAT, longOrNull,
                        defaultValue));
            }
            return defaultValue;
        }
        return NumberUtils.createNumber(longOrNull).longValue();
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static long getLong(final Properties properties, final String propertyKey,
            final long defaultValue)
    {
        return getLong(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static long getPosLong(final Properties properties, final String propertyKey,
            final long defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        assert defaultValue > -1 : "Negative default value (< 0).";
        final String longOrNull = properties.getProperty(propertyKey);
        if (longOrNull == null)
        {
            return defaultValue;
        }
        if (NumberUtils.isDigits(longOrNull) == false)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_LONG_VALUE_FORMAT, longOrNull,
                        defaultValue));
            }
            return defaultValue;
        }
        return Long.parseLong(longOrNull);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static long getPosLong(final Properties properties, final String propertyKey,
            final long defaultValue)
    {
        return getPosLong(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static int getInt(final Properties properties, final String propertyKey,
            final int defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        final String intOrNull = properties.getProperty(propertyKey);
        if (intOrNull == null)
        {
            return defaultValue;
        }
        if (NumberUtils.isNumber(intOrNull) == false)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_INT_VALUE_FORMAT, intOrNull,
                        defaultValue));
            }
            return defaultValue;
        }
        return NumberUtils.createNumber(intOrNull).intValue();
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static int getInt(final Properties properties, final String propertyKey,
            final int defaultValue)
    {
        return getInt(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static int getPosInt(final Properties properties, final String propertyKey,
            final int defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        assert defaultValue > -1 : "Negative default value (< 0).";
        final String intOrNull = properties.getProperty(propertyKey);
        if (intOrNull == null)
        {
            return defaultValue;
        }
        if (NumberUtils.isDigits(intOrNull) == false)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_INT_VALUE_FORMAT, intOrNull,
                        defaultValue));
            }
            return defaultValue;
        }
        return Integer.parseInt(intOrNull);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static int getPosInt(final Properties properties, final String propertyKey,
            final int defaultValue)
    {
        return getPosInt(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static boolean getBoolean(final Properties properties, final String propertyKey,
            final boolean defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        final String booleanOrNull = properties.getProperty(propertyKey);
        if (booleanOrNull == null)
        {
            return defaultValue;
        }
        final Boolean bool = Boolean.getBoolean(booleanOrNull);
        if (bool == null)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_BOOLEAN_VALUE_FORMAT,
                        booleanOrNull, defaultValue));
            }
            return defaultValue;
        }
        return Boolean.toBoolean(bool);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static char getChar(final Properties properties, final String propertyKey,
            final char defaultValue)
    {
        return getChar(properties, propertyKey, defaultValue, null);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static char getChar(final Properties properties, final String propertyKey,
            final char defaultValue, final ISimpleLogger loggerOrNull)
    {
        assertParameters(properties, propertyKey);
        final String charOrNull = properties.getProperty(propertyKey);
        if (charOrNull == null)
        {
            return defaultValue;
        }
        if (charOrNull.length() != 1)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.INFO, String.format(NON_CHAR_VALUE_FORMAT, charOrNull,
                        defaultValue));
                return defaultValue;
            }
        }
        return charOrNull.charAt(0);
    }

    /**
     * Looks up given <var>propertyKey</var> in given <var>properties</var>.
     * 
     * @return <code>defaultValue</code> if given <var>propertyKey</var> could not be found.
     */
    public final static boolean getBoolean(final Properties properties, final String propertyKey,
            final boolean defaultValue)
    {
        return getBoolean(properties, propertyKey, defaultValue, null);
    }

    //
    // Helper classes
    //

    private static enum Boolean
    {
        TRUE("1", "yes", "true"), FALSE("0", "no", "false");

        private final String[] values;

        Boolean(final String... values)
        {
            this.values = values;
        }

        public final static Boolean getBoolean(final String value)
        {
            if (contains(TRUE.values, value))
            {
                return TRUE;
            }
            if (contains(FALSE.values, value))
            {
                return FALSE;
            }
            return null;
        }

        public final static boolean toBoolean(final Boolean bool)
        {
            if (bool == TRUE)
            {
                return true;
            }
            return false;
        }

        private final static boolean contains(final String[] values, final String value)
        {
            for (final String string : values)
            {
                if (string.equalsIgnoreCase(value))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
