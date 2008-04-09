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
import java.util.Map;

import com.google.gwt.http.client.URL;

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

    /**
     * Returns the <i>search</i> of a <i>Javascript</i> window location (without the starting
     * <code>?</code> if any).
     * 
     * @return something like <code>key1=value1&key2=value2</code>.
     */
    public final static native String getParamString() /*-{
             var search = $wnd.location.search;
             return search.indexOf("?") == 0 ? search.substring(1) : search;
          }-*/;

    /**
     * Parses given URL <var>string</var> and returns the key-value pairs
     */
    public final static Map parseParamString(final String string)
    {
        assert string != null : "Given text can not be null.";
        final String[] ray = string.split(PARAMETER_SEPARATOR);
        final Map map = new HashMap();
        for (int i = 0; i < ray.length; i++)
        {
            final String[] substrRay = ray[i].split(KEY_VALUE_SEPARATOR);
            assert substrRay.length == 2 : "Only two items should be found here.";
            map.put(substrRay[0], URL.decodeComponent(substrRay[1]));
        }
        return map;
    }

}
