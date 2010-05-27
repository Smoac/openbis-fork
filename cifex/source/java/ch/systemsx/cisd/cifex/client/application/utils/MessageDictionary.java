/*
 * Copyright 2009 ETH Zuerich, CISD
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

import com.google.gwt.i18n.client.Dictionary;

/**
 * Allows to access messages defined in message-dictionary.js. These messages can be changed at
 * runtime.
 * 
 * @author Tomasz Pylak
 */
public enum MessageDictionary
{
    WELCOME_NOTE_START_PAGE,

    HEADER_WEBPAGE_LINK,

    EDIT_USER_DIALOG_TITLE,

    UPLOADFILES_INFO_PERMANENT_USER,
    
    UPLOADFILES_INFO_TEMPORARY_USER;

    private static final Dictionary DICT = Dictionary.getDictionary("message_dict");

    private static final String[] PLACEHOLDERS =
        { "{0}", "{1}", "{2}", "{3}", "{4}", "{5}", "{6}", "{7}", "{8}", "{9}" };

    public static final String getInternationalizedLabel(MessageDictionary key, Object... args)
    {
        assert args.length <= PLACEHOLDERS.length;

        String value = DICT.get(key.name());
        for (int i = 0; i < args.length; ++i)
        {
            value = value.replace(PLACEHOLDERS[i], args[i].toString());
        }
        return value;
    }
}
