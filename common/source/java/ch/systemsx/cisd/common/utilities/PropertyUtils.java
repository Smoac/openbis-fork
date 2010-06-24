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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * A static helper class that should help to extract typed values from {@link Properties}.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyUtils
{

    static final String EMPTY_STRING_FORMAT = "Property '%s' is not specified.";

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

    private static final String LIST_SEPARATOR = ",";

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
     * Searches for the property with the specified key in this property list.
     * 
     * @return <code>null</code> or the value trimmed if found.
     */
    public final static String getProperty(final Properties properties, final String propertyKey)
    {
        assertParameters(properties, propertyKey);
        final String property = properties.getProperty(propertyKey);
        return property == null ? null : property.trim();
    }

    /**
     * Searches for the property with the specified key in this property list.
     * 
     * @return <code>null</code> or the value trimmed if found.
     */
    public final static String getProperty(final Properties properties, final String propertyKey,
            final String defaultValue)
    {
        final String property = getProperty(properties, propertyKey);
        return property == null ? defaultValue : property;
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
        String property = getProperty(properties, propertyKey);
        if (property == null)
        {
            throw createPropertyNotFoundException(properties, propertyKey);
        }
        if (property.length() == 0)
        {
            throw ConfigurationFailureException.fromTemplate(EMPTY_STRING_FORMAT, propertyKey);
        }
        return property;
    }

    private static ConfigurationFailureException createPropertyNotFoundException(
            final Properties properties, final String propertyKey)
    {
        return ConfigurationFailureException.fromTemplate(NOT_FOUND_PROPERTY_FORMAT, propertyKey,
                CollectionUtils.abbreviate(Collections.list(properties.propertyNames()), 10));
    }

    /**
     * @returns a list of comma separated values at the specified property key. Each item is trimmed
     *          and in upper case.
     * @throws ConfigurationFailureException when a property is not specified or is empty
     */
    public final static List<String> getMandatoryList(Properties properties, String propertyKey)
    {
        List<String> list = tryGetList(properties, propertyKey);
        if (list == null)
        {
            throw createPropertyNotFoundException(properties, propertyKey);
        }
        if (list.size() == 0)
        {
            throw ConfigurationFailureException.fromTemplate(EMPTY_STRING_FORMAT, propertyKey);
        }
        return list;
    }

    /**
     * @returns a list of comma separated values at the specific property key. Each item is trimmed
     *          and in upper cases.
     */
    public final static List<String> tryGetList(Properties properties, String propertyKey)
    {
        String itemsList = PropertyUtils.getProperty(properties, propertyKey);
        if (itemsList == null)
        {
            return null;
        }
        String[] items = itemsList.split(LIST_SEPARATOR);
        for (int i = 0; i < items.length; i++)
        {
            items[i] = items[i].trim().toUpperCase();
        }
        return Arrays.asList(items);
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
        final String longOrNull = getProperty(properties, propertyKey);
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
        final String longOrNull = getProperty(properties, propertyKey);
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
        final String intOrNull = getProperty(properties, propertyKey);
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
        final String intOrNull = getProperty(properties, propertyKey);
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
        final String booleanOrNull = getProperty(properties, propertyKey);
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
        final String charOrNull = getProperty(properties, propertyKey);
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

    public static enum Boolean
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

        public final boolean toBoolean()
        {
            return toBoolean(this);
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

    /**
     * Trims each value of given <var>properties</var> using {@link StringUtils#trim(String)}.
     */
    @SuppressWarnings("unchecked")
    public final static void trimProperties(final Properties properties)
    {
        assert properties != null : "Unspecified properties";
        for (final Enumeration<String> enumeration =
                (Enumeration<String>) properties.propertyNames(); enumeration.hasMoreElements(); /**/)
        {
            final String key = enumeration.nextElement();
            properties.setProperty(key, StringUtils.trim(properties.getProperty(key)));
        }
    }

    /**
     * Loads and returns {@link Properties} found in given <var>propertiesFilePath</var>.
     * 
     * @throws ConfigurationFailureException If an exception occurs when loading the properties.
     * @return never <code>null</code> but could return empty properties.
     */
    public final static Properties loadProperties(final String propertiesFilePath)
    {
        try
        {
            return loadProperties(new FileInputStream(propertiesFilePath), propertiesFilePath);
        } catch (FileNotFoundException ex)
        {
            final String msg = String.format("Properties file '%s' not found.", propertiesFilePath);
            throw new ConfigurationFailureException(msg, ex);
        }
    }

    /**
     * Loads and returns {@link Properties} found in given <var>propertiesFilePath</var>.
     * 
     * @throws ConfigurationFailureException If an exception occurs when loading the properties.
     * @return never <code>null</code> but could return empty properties.
     */
    public final static Properties loadProperties(final InputStream is, final String resourceName)
    {
        assert is != null : "No input stream specified";
        final Properties properties = new Properties();
        try
        {
            properties.load(is);
            trimProperties(properties);
            return properties;
        } catch (final Exception ex)
        {
            final String msg =
                    String.format("Could not load the properties from given resource '%s'.",
                            resourceName);
            throw new ConfigurationFailureException(msg, ex);
        } finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Loads properties from the specified file. This is a simpler version of
     * {@link #loadProperties(String)} which does not use {@link Properties#load(InputStream)}:
     * <ul>
     * <li>Empty lines and lines starting with a hash symbol '#' are ignored.
     * <li>All other lines should have a equals symbol '='. The trimmed part before/after '='
     * specify property key and value , respectively.
     * </ul>
     * There is no character escaping as in {@link Properties#load(InputStream)} because this can
     * lead to problems if this syntax isn't known by the creator of a properties file.
     */
    public static Properties loadProperties(File propertiesFile)
    {
        Properties properties = new Properties();
        List<String> lines = FileUtilities.loadToStringList(propertiesFile);
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            if (line.length() == 0 || line.startsWith("#"))
            {
                continue;
            }
            int indexOfEqualSymbol = line.indexOf('=');
            if (indexOfEqualSymbol < 0)
            {
                throw new UserFailureException("Missing '=' in line " + (i + 1)
                        + " of properties file '" + propertiesFile + "': " + line);
            }
            String key = line.substring(0, indexOfEqualSymbol).trim();
            String value = line.substring(indexOfEqualSymbol + 1).trim();
            properties.setProperty(key, value);
        }
        return properties;
    }

    /**
     * Saves the given <var>properties</var> to the given <var>propertiesFile</var>.
     */
    public static void saveProperties(File propertiesFile, Properties properties)
    {
        final StringBuilder builder = new StringBuilder();
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            final String key = entry.getKey().toString();
            final String value = entry.getValue().toString();
            builder.append(key);
            builder.append(" = ");
            builder.append(value);
            builder.append('\n');
        }
        FileUtilities.writeToFile(propertiesFile, builder.toString());
    }

}
