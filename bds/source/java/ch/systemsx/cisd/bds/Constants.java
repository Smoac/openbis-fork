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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Some constants used inside the <i>BDS</i> library
 * 
 * @author Christian Ribeaud
 */
public final class Constants
{

    /** The date format pattern. */
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss Z";

    /** The only accepted path separator (system independent). */
    public final static char PATH_SEPARATOR = '/';

    /** The uniformly date format used. */
    // Note that DateFormats objects are not thread-safe.
    public static final ThreadLocal<DateFormat> DATE_FORMAT =
            new ThreadLocal<DateFormat>()
            {
                @Override
                protected DateFormat initialValue()
                {
                    return new SimpleDateFormat(DATE_FORMAT_PATTERN);
                }
            };

    private Constants()
    {
        // Can not be instantiated.
    }
}
