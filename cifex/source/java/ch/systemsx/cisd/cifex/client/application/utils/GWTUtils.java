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

package ch.systemsx.cisd.cifex.client.application.utils;

import java.util.HashMap;

/**
 * Some utility methods around <i>GWT</i>.
 * 
 * @author Christian Ribeaud
 */
public final class GWTUtils
{

    private static final String KEY_VALUE_SEPARATOR = "=";

    private static final String PARAMETER_SEPARATOR = "&";

    private GWTUtils()
    {
        // Can not be instantiated.
    }

    /** Returns the <i>search</i> of a <i>Javascript</i> window location. */
    public final static native String getParamString() /*-{
       return $wnd.location.search;
    }-*/;

    /**
     * Parses given URL <var>string</var> and returns the key-value pairs
     */
    public final static HashMap parseParamString(final String string)
    {
        assert string != null : "Given text can not be null.";
        final String text;
        if (string.startsWith("?"))
        {
            text = string.substring(1, string.length());
        } else
        {
            text = string;
        }
        final String[] ray = text.split(PARAMETER_SEPARATOR);
        final HashMap map = new HashMap();
        for (int i = 0; i < ray.length; i++)
        {
            final String[] substrRay = ray[i].split(KEY_VALUE_SEPARATOR);
            assert substrRay.length == 2 : "Only two items should be found here.";
            // map.put(substrRay[0], URL.decode(substrRay[1]));
            map.put(substrRay[0], substrRay[1]);
        }
        return map;
    }

}
