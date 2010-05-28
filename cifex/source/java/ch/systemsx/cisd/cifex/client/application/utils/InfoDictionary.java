/*
 * Copyright 2010 ETH Zuerich, CISD
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
 * Allows to access info text defined in <code>info-dictionary.js</code>. Entries in this dictionary
 * can be changed at runtime.
 * 
 * @author Bernd Rinn
 */
public enum InfoDictionary
{

    START_PAGE_WELCOME_NOTE,

    SUPPORT_EMAIL,

    HEADER_WEBPAGE_LINK;

    private static final Dictionary INFO_DICT = Dictionary.getDictionary("info_dict");

    /**
     * Returns a customized info string from <code>info-dictionary.js</code>.
     */
    public static final String info(InfoDictionary key)
    {
        return INFO_DICT.get(key.name());
    }
}
