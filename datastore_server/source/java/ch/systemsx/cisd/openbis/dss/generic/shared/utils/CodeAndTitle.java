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

/**
 * Extracts from a string two other strings called 'title' and 'code'. The string has the form
 * <tt>&lt;code&gt; title</tt>. The code part is optional. In this case the code is given by the
 * title. Note: In any case the code will be normalized. That is, it is turn to upper case and any
 * symbol which isn't from A-Z or 0-9 is replaced by an underscore character.
 * 
 * @author Franz-Josef Elmer
 */
public class CodeAndTitle
{
    private String title;
    private String code;

    public CodeAndTitle(String titleWithOptionalCode)
    {
        title = titleWithOptionalCode;
        code = titleWithOptionalCode;
        if (titleWithOptionalCode.startsWith("<"))
        {
            int indexOfClosing = titleWithOptionalCode.indexOf('>');
            if (indexOfClosing > 0)
            {
                code = titleWithOptionalCode.substring(1, indexOfClosing).trim();
                title = titleWithOptionalCode.substring(indexOfClosing + 1).trim();
            }
        }
        StringBuilder builder = new StringBuilder(code.toUpperCase());
        for (int i = 0, n = builder.length(); i < n; i++)
        {
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".indexOf(builder.charAt(i)) < 0)
            {
                builder.setCharAt(i, '_');
            }
        }
        code = builder.toString();

    }

    public final String getTitle()
    {
        return title;
    }

    public final String getCode()
    {
        return code;
    }
}
