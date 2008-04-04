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

/**
 * Operations on {@link java.lang.String} that are <code>null</code> safe.
 * 
 * @author Christian Ribeaud
 */
public final class StringUtils
{
    public static final String EMPTY_STRING = "";

    private StringUtils()
    {
        // Can not be instantiated
    }

    /**
     * Whether given <var>value</var> is blank or not.
     */
    public final static boolean isBlank(final String value)
    {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Whether given <var>value</var> is empty or not.
     */
    public final static boolean isEmpty(final String value)
    {
        return value == null || value.length() == 0;
    }

    /** Returns an empty if given <var>stringOrNull</var> is <code>null</code>. */
    public final static String emptyIfNull(final String stringOrNull)
    {
        return stringOrNull == null ? EMPTY_STRING : stringOrNull;
    }
}
