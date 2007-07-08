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

package ch.systemsx.cisd.common.collections;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;

/**
 * <code>ValidatorUtils</code> provides reference implementations and utilities for the <code>Validator</code>
 * interface.
 * 
 * @author Christian Ribeaud
 */
public final class ValidatorUtils
{

    private ValidatorUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Creates a <code>Validator</code> based on the given pattern.
     * 
     * @return <code>null</code> if given <var>pattern</var> is also <code>null</code>.
     * @throws PatternSyntaxException if the expression's syntax is invalid.
     */
    public final static Validator<String> createPatternValidator(final String pattern)
    {
        if (pattern == null)
        {
            return null;
        }
        final Pattern regEx = Pattern.compile(convertToRegEx(pattern));
        return new Validator<String>()
            {

                //
                // Validator
                //

                public final boolean isValid(String text)
                {
                    return regEx.matcher(text).matches();
                }
            };
    }

    /**
     * Converts given pattern into a regular expression. This method does the following:
     * <ol>
     * <li>replaces any <code>?</code> with <code>.</code></li>
     * <li>replaces any <code>*</code> with <code>.*</code></li>
     * </ol>
     */
    final static String convertToRegEx(String pattern)
    {
        assert pattern != null;
        return StringUtils.replace(pattern.replace('?', '.'), "*", ".*");
    }
}
