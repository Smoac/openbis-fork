/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * A static helper class to help with loading and saving {@link Properties}.
 *
 * @author Bernd Rinn
 */
public class PropertyIOUtils
{

    /**
     * Loads a text file line by line to a {@link List} of {@link String}s.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not <code>null</code>.
     * @return The content of the file line by line.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    private final static List<String> loadToStringList(final File file) throws IOExceptionUnchecked
    {
        assert file != null : "Unspecified file.";

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            return readStringList(new BufferedReader(fileReader));
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    private final static List<String> readStringList(final BufferedReader reader) throws IOExceptionUnchecked
    {
        assert reader != null : "Unspecified BufferedReader.";
        final List<String> list = new ArrayList<String>();
        try
        {
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
                list.add(line);
            }
            return list;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Loads properties from the specified file and adds them to the specified properties.
     * <ul>
     * <li>Empty lines and lines starting with a hash symbol '#' are ignored.
     * <li>All other lines should have a equals symbol '='. The trimmed part before/after '=' specify property key and value , respectively.
     * </ul>
     */
    public static void loadAndAppendProperties(Properties properties, File propertiesFile)
    {
        List<String> lines = loadToStringList(propertiesFile);
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i).trim();
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
    }

    /**
     * Loads properties from the specified file. This is a simpler version of {@link PropertyIOUtils#loadProperties(String)} which does not use
     * {@link Properties#load(InputStream)}:
     * <ul>
     * <li>Empty lines and lines starting with a hash symbol '#' are ignored.
     * <li>All other lines should have a equals symbol '='. The trimmed part before/after '=' specify property key and value , respectively.
     * </ul>
     * There is no character escaping as in {@link Properties#load(InputStream)} because this can lead to problems if this syntax isn't known by the
     * creator of a properties file.
     */
    public static Properties loadProperties(File propertiesFile)
    {
        Properties properties = new Properties();
        loadAndAppendProperties(properties, propertiesFile);
        return properties;
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
            PropertyUtils.trimProperties(properties);
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
     * Writes the specified string to the specified file.
     * 
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}.
     */
    public static void writeToFile(final File file, final String str) throws IOExceptionUnchecked
    {
        assert file != null : "Unspecified file.";
        assert str != null : "Unspecified string.";

        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(file);
            fileWriter.write(str);
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fileWriter);
        }
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
        writeToFile(propertiesFile, builder.toString());
    }

}
