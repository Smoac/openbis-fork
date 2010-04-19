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
        for (int i = 0, n = methodName.length(); i < n; i++)
        {
            char c = methodName.charAt(i);
            if (c == '/')
            {
                builder.append(c);
            } else
            {
                builder.append(encode(c));
            }

        }
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
        String maybeEncodedName = withEncoding ? encode(parameterName) : parameterName;
        builder.append(delim).append(maybeEncodedName).append('=');
        if (value != null)
        {
            String maybeEncodedValue = withEncoding ? encode(value.toString()) : value.toString();
            builder.append(maybeEncodedValue);
        }
        delim = '&';
    }

    private String encode(String string)
    {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0, n = string.length(); i < n; i++)
        {
            buffer.append(encode(string.charAt(i)));
        }
        return buffer.toString();
    }

    private String encode(char c)
    {
        if (".-*_:".indexOf(c) >= 0 || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')
                || ('0' <= c && c <= '9'))
        {
            return Character.toString(c);
        }
        if (c == ' ')
        {
            return "+";
        }
        int b1 = (c >> 8) & 0xff;
        int b2 = c & 0xff;
        return (b1 == 0 ? "" : encode(b1)) + encode(b2);
    }

    private String encode(int number)
    {
        return "%" + Integer.toHexString((number >> 4) & 0xf) + Integer.toHexString(number & 0xf);

    }

    @Override
    public String toString()
    {
        return builder.toString();
    }

}
