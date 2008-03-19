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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * Another class of Properties that allows recursive references for property keys and values. For example,
 * 
 * <pre>
 * A=12345678
 * B=${A}90
 * C=${B} plus more
 * 
 * </code></pre>
 * 
 * will result in <code>getProperty("C")</code> returning the value "1234567890 plus more". The keys will be rewritten
 * when queried (dynamically), thus the order of adding properties is unimportant. Cyclic references are handled by
 * removing the current key before resolving it, i.e. when setting A=${B} and B=${A} and then asking for A, you will get
 * ${A}.
 * 
 * @author Christian Ribeaud
 */
public final class ExtendedProperties extends Properties
{

    private static final long serialVersionUID = 1L;

    /** Usual (or default) property separator in the super class. */
    private static final String PROPERTY_SEPARATOR = ", ";

    /** Default placeholder suffix: "}" */
    private static final String SUFFIX = "}";

    /** Default placeholder prefix: "${" */
    private static final String PREFIX = "${";

    /** The minimum length a string should have to be considered. */
    private static final int MIN_LENGTH = PREFIX.length() + SUFFIX.length() + 1;

    public static ExtendedProperties createWith(Properties properties)
    {
        ExtendedProperties result = new ExtendedProperties();
        result.putAll(properties);
        return result;
    }

    /**
     * @see Properties#Properties()
     */
    public ExtendedProperties()
    {
        super();
    }

    /**
     * Returns a subset of given <code>Properties</code> based on given property key prefix.
     * 
     * @param prefix string, each property key should start with.
     * @param dropPrefix If <code>true</code> the prefix will be removed from the key.
     */
    public static ExtendedProperties getSubset(Properties properties, String prefix,
            boolean dropPrefix)
    {
        return ExtendedProperties.createWith(properties).getSubset(prefix, dropPrefix);
    }

    public final ExtendedProperties getSubset(final String prefix, boolean dropPrefix)
    {
        assert prefix != null : "Missing prefix";

        ExtendedProperties result = new ExtendedProperties();
        int prefixLength = prefix.length();
        for (Enumeration<?> enumeration = propertyNames(); enumeration.hasMoreElements(); )
        {
            String key = enumeration.nextElement().toString();
            if (key.startsWith(prefix))
            {
                result.put(dropPrefix ? key.substring(prefixLength) : key, getProperty(key));
            }
        }
        return result;
    }

    /**
     * Removes all properties with names starting with given prefix
     */
    public void removeSubset(final String prefix)
    {
        for (Enumeration<?> enumeration = propertyNames(); enumeration.hasMoreElements(); )
        {
            String key = enumeration.nextElement().toString();
            if (key.startsWith(prefix))
            {
                remove(key);
            }
        }
    }

    private final String expandValue(final String key, final String value)
    {
        if (value == null || value.length() < MIN_LENGTH)
        {
            return value;
        }
        final StringBuilder result = new StringBuilder(value.length());
        result.append(value);
        int startName = result.indexOf(PREFIX);
        final int prefixLen = PREFIX.length();
        int endName = result.indexOf(SUFFIX, startName + prefixLen);
        final int suffixLen = SUFFIX.length();
        while (startName >= 0 && endName > startName)
        {
            final String paramName = result.substring(startName + prefixLen, endName);
            // recurse into this variable, prevent cyclic references by removing the current key
            // before asking for the property and the setting it again afterwards.
            remove(key);
            String paramValue = getProperty(paramName);
            super.setProperty(key, value);
            if (paramValue != null)
            {
                result.replace(startName, endName + suffixLen, paramValue);
                startName += paramValue.length();
            } else
            {
                startName = endName + suffixLen;
            }
            startName = result.indexOf(PREFIX, startName);
            endName = result.indexOf(SUFFIX, startName + prefixLen);
        }
        return result.toString();
    }

    /**
     * Returns the value of property <code>key</code> without resolving.
     * <p>
     * So the {@link java.util.Map#get(java.lang.Object)} works as usual and returns raw (not expanded with substituted
     * parameters) property value.
     * </p>
     */
    public final String getUnalteredProperty(final String key)
    {
        return (String) get(key);
    }

    //
    // Properties
    //

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
     * </p>
     * 
     * @see java.util.Properties#getProperty(java.lang.String)
     */
    @Override
    public final String getProperty(final String key)
    {
        final String result = super.getProperty(key);
        return result == null ? null : expandValue(key, result);
    }

    /**
     * @see java.util.Properties#getProperty(java.lang.String, java.lang.String)
     */
    @Override
    public final String getProperty(final String key, final String defaultValue)
    {
        final String result = getProperty(key);
        return result == null ? expandValue(key, defaultValue) : result;
    }

    @Override
    public final String toString()
    {
        return StringUtils
                .replace(super.toString(), PROPERTY_SEPARATOR, SystemUtils.LINE_SEPARATOR);
    }
}
