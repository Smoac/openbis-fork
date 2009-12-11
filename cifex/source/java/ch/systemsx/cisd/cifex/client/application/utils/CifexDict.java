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
 * Allows to access messages defined in runtime-dictionary.js. Use this class to have dictionary
 * messages which can be changed on runtime.
 * 
 * @author Tomasz Pylak
 */
public enum CifexDict
{
    WELCOME_NOTE;

    private static final Dictionary DICT = Dictionary.getDictionary("dict");

    public static final String get(CifexDict key)
    {
        return DICT.get(key.name());
    }
}
