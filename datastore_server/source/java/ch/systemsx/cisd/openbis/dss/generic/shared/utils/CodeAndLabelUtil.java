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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import ch.systemsx.cisd.openbis.generic.shared.dto.CodeAndLabel;

/**
 * Utility class to operate on codes and labels.
 * 
 * @author Tomasz Pylak
 */
public class CodeAndLabelUtil
{
    public static CodeAndLabel create(String code, String label)
    {
        return new CodeAndLabel(normalize(code), label);
    }

    /**
     * Creates an instance from specified label with optional code prefix in form of
     * <code>&lt;code&gt;</code>. The code will be normalized.
     */
    public static CodeAndLabel create(String labelWithOptionalCode)
    {
        String t = labelWithOptionalCode;
        String c = labelWithOptionalCode;
        if (labelWithOptionalCode.startsWith("<"))
        {
            int indexOfClosing = labelWithOptionalCode.indexOf('>');
            if (indexOfClosing > 0)
            {
                c = labelWithOptionalCode.substring(1, indexOfClosing).trim();
                t = labelWithOptionalCode.substring(indexOfClosing + 1).trim();
            }
        }
        String code = normalize(c);
        String rest = t.trim();
        String label = rest.length() == 0 ? code : rest;
        return new CodeAndLabel(code, label);
    }

    /**
     * Normalizes the specified code. That is lower-case characters are turned to upper case and any
     * symbol which isn't from A-Z or 0-9 is replaced by an underscore character.
     */
    public static String normalize(String code)
    {
        StringBuilder builder = new StringBuilder(code.toUpperCase().trim());
        for (int i = 0, n = builder.length(); i < n; i++)
        {
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".indexOf(builder.charAt(i)) < 0)
            {
                builder.setCharAt(i, '_');
            }
        }
        return builder.toString();
    }
}
