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

import com.gwtext.client.widgets.form.ValidationException;
import com.gwtext.client.widgets.form.Validator;

import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * @author Basil Neff
 */
public class CifexValidator
{

    /**
     * Returns a validator for a user field. The validator allows to specify email addresses and
     * user codes (with the prefix 'id:'). The Validator allows that the field has one or more
     * entries, which are separated by comma or tabs.
     */
    public static Validator getUserFieldValidator()
    {
        return new Validator()
            {
                public final boolean validate(final String value) throws ValidationException
                {
                    final String[] result = value.split("[,\\s]+");
                    if (result.length == 0)
                    {
                        return false;
                    }
                    for (int i = 0; i < result.length; i++)
                    {
                        assert result[i] != null : "Must not be null.";
                        final String item = result[i].trim();
                        if (item.length() > 0
                                && StringUtils.matches(Constants.EMAIL_REGEX, item) == false
                                && StringUtils.matches(Constants.USER_CODE_WITH_ID_PREFIX_REGEX, item) == false)
                        {
                            return false;
                        }
                    }
                    return true;
                }
            };
    }

    /**
     * Returns a validator which only allows email addresses in a user field. There is only one
     * email address allowed per field.
     */
    public static Validator getEmailFieldValidator()
    {
        return new Validator()
            {
                public final boolean validate(final String value) throws ValidationException
                {
                    assert value != null : "Must not be null";
                    final String item = value.trim();
                    if (item.length() == 0)
                    {
                        return false;
                    }
                    if (StringUtils.matches(Constants.EMAIL_REGEX, item) == false)
                    {
                        return false;
                    }
                    return true;
                }
            };
    }

    /**
     * Returns a validator for a user code field. Available user codes are defined by
     * <code>StringUtils.USER_CODE_REGEX</code>.
     */
    public static Validator getUserCodeFieldValidator()
    {
        return new Validator()
            {
                public final boolean validate(final String value) throws ValidationException
                {
                    assert value != null : "Must not be null";
                    if (value.length() == 0)
                    {
                        return false;
                    }
                    if (StringUtils.matches(Constants.USER_CODE_REGEX, value) == false)
                    {
                        return false;
                    }
                    return true;
                }
            };
    }
}
