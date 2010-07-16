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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Helper class to create URL's with parameters. Characters in path, parameter names and values are
 * URL encoded except '0'-'9', 'a'-'z', 'A'-'Z', ':', '/', '.', '*', '-', and '_'. Space character
 * is replaced by '+'.
 * 
 * @author Franz-Josef Elmer
 */
public class URLMethodWithParameters implements IsSerializable
{
    private final StringBuilder builder;

    private char delim = '?';

    /**
     * Create an instance with specified method URL without parameters.
     */
    public URLMethodWithParameters(String methodName)
    {
        builder = new StringBuilder();
        builder.append(BasicURLEncoder.encode(methodName, ":/"));
    }

    /**
     * Adds a parameter with specified name and value (without encoding).
     */
    public void addParameterWithoutEncoding(String parameterName, Object value)
    {
        addParameter(parameterName, value, false);
    }

    /**
     * Adds a parameter with specified name and value (with encoding).
     */
    public void addParameter(String parameterName, Object value)
    {
        addParameter(parameterName, value, true);
    }

    /**
     * Sets '#' as the next separator.
     */
    public void startHistoryToken()
    {
        delim = '#';
    }

    /**
     * Adds a parameter with specified name and value with optional encoding.
     */
    private void addParameter(String parameterName, Object value, boolean withEncoding)
    {
        String maybeEncodedName =
                withEncoding ? BasicURLEncoder.encode(parameterName) : parameterName;
        builder.append(delim).append(maybeEncodedName).append('=');
        if (value != null)
        {
            String maybeEncodedValue =
                    withEncoding ? BasicURLEncoder.encode(value.toString()) : value.toString();
            builder.append(maybeEncodedValue);
        }
        delim = '&';
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }

}
