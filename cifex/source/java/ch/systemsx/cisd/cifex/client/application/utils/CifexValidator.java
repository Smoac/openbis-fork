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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_RECIPIENT_FIELD_INVALID_MSG;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.VALIDATION_EMAIL_MSG;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.VALIDATION_REQUIRED_BLANK_MSG;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.VALIDATION_WRONG_USERCODE_MSG;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.msg;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;

import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.IValidator;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * @author Basil Neff
 */
public class CifexValidator
{

    /**
     * Validator for user inputs like 'id:<user code>' or e-mail addresses.
     */
    public static IValidator USER_VALIDATOR = new IValidator()
        {
            @Override
            public String validate(String value)
            {
                final String[] result = value.split(",\\s*");
                if (result.length == 0)
                {
                    return msg(VALIDATION_REQUIRED_BLANK_MSG);
                }
                for (int i = 0; i < result.length; i++)
                {
                    assert result[i] != null : "Must not be null.";
                    final String item = result[i].trim();
                    if (item.length() > 0
                            && StringUtils.matches(Constants.EMAIL_REGEX, item) == false
                            && StringUtils.matches(Constants.USER_CODE_WITH_ID_PREFIX_REGEX,
                                    item, Constants.CASE_INSENSITIVE_MATCHING) == false)
                    {
                        return msg(UPLOAD_FILES_RECIPIENT_FIELD_INVALID_MSG);
                    }
                }
                return null;
            }
        };

    /**
     * Returns a validator for a user field. The validator allows to specify email addresses and
     * user codes (with the prefix 'id:'). The Validator allows that the field has one or more
     * entries, which are separated by comma or tabs.
     */
    public static Validator getUserFieldValidator()
    {
        return new Validator()
            {

                @Override
                public String validate(Field<?> field, String value)
                {
                    return USER_VALIDATOR.validate(value);
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

                @Override
                public String validate(Field<?> field, String value)
                {
                    if (value == null)
                    {
                        return msg(VALIDATION_REQUIRED_BLANK_MSG);
                    }
                    final String item = value.trim();
                    if (item.length() == 0)
                    {
                        return msg(VALIDATION_REQUIRED_BLANK_MSG);
                    }
                    if (StringUtils.matches(Constants.EMAIL_REGEX, item) == false)
                    {
                        return msg(VALIDATION_EMAIL_MSG);
                    }
                    return null;
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

                @Override
                public String validate(Field<?> field, String value)
                {
                    if (value == null || value.length() == 0)
                    {
                        return msg(VALIDATION_REQUIRED_BLANK_MSG);
                    }
                    if (StringUtils.matches(Constants.USER_CODE_REGEX, value) == false)
                    {
                        return msg(VALIDATION_WRONG_USERCODE_MSG);
                    }
                    return null;
                }
            };
    }
}
