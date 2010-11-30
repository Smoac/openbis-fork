/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Loads global properties.
 * <p>
 * Global properties are defined as a comment and start with {@link #GLOBAL_PROPERTIES} followed by
 * a new line character. Each property is defined in a separate line and has the following format:
 * <p>
 * # key = value
 * </p>
 * First line in a different format marks the end of global properties. (Empty line can be used).
 * <p>
 * Example:
 * </p>
 * </p> --- FILE ---<br>
 * # GLOBAL_PROPERTIES: <br>
 * # key1 = value1 <br>
 * # key2 = value2 <br>
 * <br>
 * code parent experiment <br>
 * --- EOF ---
 * 
 * @author Izabela Adamczyk
 */
public class GlobalPropertiesLoader
{
    private static final String COMMENT_PREFIX = "#";

    static String GLOBAL_PROPERTIES = "#GLOBAL_PROPERTIES:";

    public static GlobalProperties load(File file) throws FileNotFoundException
    {
        GlobalProperties properties = new GlobalProperties();
        InputStreamReader reader = new FileReader(file);
        try
        {
            LineIterator it = IOUtils.lineIterator(reader);
            boolean propertiesStarted = false;
            while (it.hasNext())
            {
                String line = it.nextLine();
                if (isGlobalPropertiesStarter(line))
                {
                    propertiesStarted = true;
                    continue;
                }
                if (propertiesStarted)
                {
                    String[] definitionOrNull = tryGetPropertyDefinition(line);
                    if (definitionOrNull != null)
                    {
                        properties.add(definitionOrNull[0], definitionOrNull[1]);
                        continue;
                    } else
                    {
                        return properties;
                    }
                }
            }
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
        return properties;
    }

    private static String[] tryGetPropertyDefinition(String line)
    {
        String commentPrefix = COMMENT_PREFIX;
        if (line.startsWith(commentPrefix))
        {
            String[] splitted = StringUtils.split(line.substring(commentPrefix.length()), "=", 2);
            if (splitted.length == 2)
            {
                String[] result = new String[2];
                result[0] = StringUtils.trim(splitted[0]);
                result[1] = StringUtils.trim(splitted[1]);
                return result;
            }
        }
        return null;
    }

    private static boolean isGlobalPropertiesStarter(String line)
    {
        return line.equals(GLOBAL_PROPERTIES);
    }

}
