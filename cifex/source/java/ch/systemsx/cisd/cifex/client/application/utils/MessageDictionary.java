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
 * Allows to access messages defined in <code>message-dictionary.js</code>. Entries in this
 * dictionary can be changed at runtime.
 * 
 * @author Tomasz Pylak
 */
public enum MessageDictionary
{
    UNKNOWN_LABEL,

    LAUNCH_JWS_APPLICATION_TITLE,

    GRID_FILTERS_LABEL,

    GRID_COLUMNS_LABEL,

    CONTACT_SUPPORT_LABEL,

    HELP_DISCLAIMER_LABEL,

    HELP_DISCLAIMER_TITLE,

    HELP_FAQ_LABEL,
    
    HELP_FAQ_TITLE,
    
    HELP_MANUAL_LABEL,
    
    HELP_TOOLS_LABEL,

    EDIT_USER_DIALOG_TITLE,

    UPLOADFILES_INFO_PERMANENT_USER,

    UPLOADFILES_INFO_TEMPORARY_USER;

    private static final Dictionary MSG_DICT = Dictionary.getDictionary("message_dict");

    private static final String[] PLACEHOLDERS =
        { "{0}", "{1}", "{2}", "{3}", "{4}", "{5}", "{6}", "{7}", "{8}", "{9}" };

    /**
     * Returns a localized message string from <code>message-dictionary.js</code>.
     */
    public static final String msg(MessageDictionary key)
    {
        return MSG_DICT.get(key.name());
    }

    /**
     * Returns a localized message string from <code>message-dictionary.js</code>.
     */
    public static final String msg(MessageDictionary key, Object... args)
    {
        assert args.length <= PLACEHOLDERS.length;

        String value = MSG_DICT.get(key.name());
        for (int i = 0; i < args.length; ++i)
        {
            value = value.replace(PLACEHOLDERS[i], args[i].toString());
        }
        return value;
    }

}
