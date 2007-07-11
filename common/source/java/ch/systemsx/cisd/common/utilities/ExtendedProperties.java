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

import java.util.Enumeration;
import java.util.Properties;

/**
 * This implementation supports parameters substitution in property value.
 * 
 * @see #getProperty(String)
 * @author Christian Ribeaud
 */
public final class ExtendedProperties extends Properties
{
    private static final long serialVersionUID = 1L;

    /** Default placeholder prefix: "${" */
    private static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    /** Default placeholder suffix: "}" */
    private static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

    /**
     * @see java.util.Properties#Properties()
     */
    public ExtendedProperties()
    {
        super();
    }

    /**
     * @see java.util.Properties#Properties(java.util.Properties)
     */
    public ExtendedProperties(Properties defs)
    {
        super(defs);
    }

    /**
     * Any parameter like <code>${propertyName}</code> in property value will be replaced with the value of property
     * with name <code>propertyName</code>.
     * <p>
     * For example, for the following set of properties:
     * 
     * <pre>
     * param1 = abcd
     * param2 = efgh
     * param3 = Alphabet starts with: ${param1}${param2}
     * </pre>
     * 
     * The call <code>props.getProperty("param3")</code> returns:
     * 
     * <pre>
     * Alphabet starts with: abcdefgh
     * </pre>
     * 
     * Note also that call <code>props.get("param3")</code> returns:
     * 
     * <pre>
     * Alphabet starts with: ${param1}${param2}
     * </pre>
     * 
     * So the {@link java.util.Map#get(java.lang.Object)} works as usual and returns raw (not expanded with substituted
     * parameters) property value.
     * </p>
     * 
     * @see java.util.Properties#getProperty(java.lang.String)
     */
    @Override
    public String getProperty(String key)
    {
        String result = super.getProperty(key);
        return result == null ? null : expandValue(result);
    }

    /**
     * @see java.util.Properties#getProperty(java.lang.String, java.lang.String)
     */
    @Override
    public String getProperty(String key, String defaultValue)
    {
        String result = getProperty(key);
        return result == null ? expandValue(defaultValue) : result;
    }

    /**
     * Returns a subset of given <code>Properties</code> based on given property key prefix.
     * 
     * @param prefix string, each property key should start with.
     */
    public final ExtendedProperties getSubset(final String prefix)
    {
        ExtendedProperties result = new ExtendedProperties();
        for (Enumeration enumeration = propertyNames(); enumeration.hasMoreElements();)
        {
            String key = enumeration.nextElement().toString();
            if (key.startsWith(prefix))
            {
                result.put(key, getProperty(key));
            }
        }
        return result;
    }

    private final String expandValue(final String value)
    {
        if (value == null || value.length() < 4)
        {
            return value;
        }
        StringBuilder result = new StringBuilder(value.length());
        result.append(value);
        int p1 = result.indexOf(DEFAULT_PLACEHOLDER_PREFIX);
        int p2 = result.indexOf(DEFAULT_PLACEHOLDER_SUFFIX, p1 + 2);
        while (p1 >= 0 && p2 > p1)
        {
            String paramName = result.substring(p1 + 2, p2);
            // TODO 2007-07-11, Franz-Josef Elmer: This recursive call has to be checked against cyclic dependencies
            // in a properties file like a = ${b}, b = ${a}
            String paramValue = getProperty(paramName);
            if (paramValue != null)
            {
                result.replace(p1, p2 + 1, paramValue);
                p1 += paramValue.length();
            } else
            {
                p1 = p2 + 1;
            }
            p1 = result.indexOf(DEFAULT_PLACEHOLDER_PREFIX, p1);
            p2 = result.indexOf(DEFAULT_PLACEHOLDER_SUFFIX, p1 + 2);
        }
        return result.toString();
    }
}
