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
 * @author     Franz-Josef Elmer
 */
public class URLMethodWithParameters implements IsSerializable
{
    private final StringBuilder builder;

    private char delim = '?';

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

    public void addParameter(String parameterName, Object value)
    {
        builder.append(delim).append(encode(parameterName)).append('=');
        if (value != null)
        {
            builder.append(encode(value.toString()));
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
    
    // When encoding a String, the following rules apply:
    //
    // The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the
    // same.
    // The special characters ".", "-", "*", and "_" remain the same.
    // The space character " " is converted into a plus sign "+".
    // All other characters are unsafe and are first converted into one or more bytes using some
    // encoding scheme. Then each byte is represented by the 3-character string "%xy", where xy is
    // the two-digit hexadecimal representation of the byte. The recommended encoding scheme to use
    // is UTF-8. However, for compatibility reasons, if an encoding is not specified, then the
    // default encoding of the platform is used.
    // For example using UTF-8 as the encoding scheme the string "The string Ÿ@foo-bar" would get
    // converted to "The+string+%C3%BC%40foo-bar" because in UTF-8 the character Ÿ is encoded as two
    // bytes C3 (hex) and BC (hex), and the character @ is encoded as one byte 40 (hex).
    private String encode(char c)
    {
        if (".-*_:".indexOf(c) >= 0 || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9'))
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
