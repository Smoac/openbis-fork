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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Utilities for dealing with file encoding.
 * 
 * @author Piotr Buczek
 */
public final class UnicodeUtils
{
    /** default Unicode encoding used for strings - UTF-8 */
    public static final String DEFAULT_UNICODE_CHARSET = "UTF-8";

    private UnicodeUtils()
    {
        // Can not be instantiated.
    }

    /** @returns {@link Charset} for {@link #DEFAULT_UNICODE_CHARSET} */
    public final static Charset getDefaultUnicodeCharset()
    {
        return Charset.forName(UnicodeUtils.DEFAULT_UNICODE_CHARSET);
    }

    /**
     * @return {@link Reader} that uses the default Unicode encoding.
     * @throws EnvironmentFailureException if the encoding is not supported (shouldn't happen).
     * @see #DEFAULT_UNICODE_CHARSET
     */
    public final static Reader createReader(InputStream inputStream)
            throws EnvironmentFailureException
    {
        return createReader(inputStream, DEFAULT_UNICODE_CHARSET);
    }

    /**
     * @return {@link Reader} that uses the given encoding.
     * @throws EnvironmentFailureException if the encoding is not supported.
     */
    public final static Reader createReader(InputStream inputStream, String encoding)
            throws EnvironmentFailureException
    {
        try
        {
            return new InputStreamReader(inputStream, encoding);
        } catch (UnsupportedEncodingException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage());
        }
    }
}
