/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.util.List;

/**
 * Some useful utlities methods for {@link String}s.
 * 
 * @author Bernd Rinn
 */
public class StringUtilities
{

    /**
     * Returns the capitalized form of <var>string</var>
     */
    public static String capitalize(String string)
    {
        assert string != null;

        if (string.length() == 0) // Guard: empty string.
        {
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    /**
     * @return The concatenated entries of the <var>list</var>, delimited by a space.
     */
    public static String concatenateWithSpace(List<String> list)
    {
        return concatenate(list, " ");
    }
    
    /**
     * @return The concatenated entries of the <var>list</var>, delimited by a new line.
     */
    public static String concatenateWithNewLine(List<String> list)
    {
        return concatenate(list, OSUtilities.LINE_SEPARATOR);
    }
    
   /**
     * @return The concatenated entries of the <var>list</var>, delimited by <var>delimiter</var>.
     */
    public static String concatenate(List<String> list, String delimiter)
    {
        final StringBuilder builder = new StringBuilder();
        for (String entry : list)
        {
            builder.append(entry).append(delimiter);
        }
        builder.setLength(Math.max(0, builder.length() - delimiter.length())); // Remove trailing delimiter.
        return builder.toString();
    }

}
