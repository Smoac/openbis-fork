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

package ch.systemsx.cisd.cifex.client.application.utils;


/**
 * Some utilities around <code>String</code>.
 * 
 * @author Christian Ribeaud
 */
public final class StringUtils
{

    public static final String EMPTY_STRING = "";

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

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

    public final static String abbreviate(final String value, final int maxLength)
    {
        assert maxLength > 4;
        if (value.length() > maxLength)
        {
            return value.substring(0, maxLength - 3) + "...";
        } else
        {
            return value;
        }
    }

    /**
     * Returns <code>null</code> if given <var>value</var> is blank.
     */
    public final static String nullIfBlank(final String value)
    {
        return isBlank(value) ? null : value;
    }

    /** Returns an empty if given <var>stringOrNull</var> is <code>null</code>. */
    public final static String emptyIfNull(final String stringOrNull)
    {
        return stringOrNull == null ? EMPTY_STRING : stringOrNull;
    }

    /**
     * Returns <code>true</code> if given <var>regExp</var> could be found in given <var>value</var>.
     */
    public final native static boolean matches(final String regExp, final String value) /*-{
                   var re = new RegExp(regExp);
                   return value.search(re) > -1;
                }-*/;
}
