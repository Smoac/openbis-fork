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

package ch.systemsx.cisd.bds;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.bds.hcs.HCSImageFormat1_0;

/**
 * A store of all active formats.
 * 
 * @author Christian Ribeaud
 */
public final class FormatStore
{

    private final static Map<String, Format> formats = createFormats();

    private FormatStore()
    {
        // Can not be instantiated.
    }

    private final static Map<String, Format> createFormats()
    {
        final Map<String, Format> map = new HashMap<String, Format>();
        map.put(getKey(UnknownFormat1_0.UNKNOWN_1_0), UnknownFormat1_0.UNKNOWN_1_0);
        map.put(getKey(HCSImageFormat1_0.HCS_IMAGE_1_0), HCSImageFormat1_0.HCS_IMAGE_1_0);
        return map;
    }

    /** Constructs an unique key for given <var>format</var>. */
    private final static String getKey(final Format format)
    {
        return getKey(format.getCode(), format.getVersion(), format.getVariant());
    }

    /** Constructs an unique key for given <var>formatCode</var>, <var>version</var> and <var>variant</var>. */
    private final static String getKey(final String formatCode, final Version version, final String variant)
    {
        String key = formatCode + version.toString();
        if (variant != null)
        {
            key += variant;
        }
        return key;
    }

    /**
     * Returns corresponding <code>Format</code> for given format code, version and format variant.
     */
    public final static Format getFormat(final String formatCode, final Version version, final String formatVariant)
            throws DataStructureException
    {
        return formats.get(getKey(formatCode, version, formatVariant));
    }
}
