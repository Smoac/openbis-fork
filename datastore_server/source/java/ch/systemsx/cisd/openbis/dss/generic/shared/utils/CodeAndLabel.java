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
 * Value object which has a label and a normalized code. Normalized means that the original code
 * arguments turn to upper case and any symbol which isn't from A-Z or 0-9 is replaced by an
 * underscore character.
 * 
 * @author Franz-Josef Elmer
 */
public class CodeAndLabel
{
    /**
     * Normalizes the specified code. That is lower-case characters are turned to upper case and any
     * symbol which isn't from A-Z or 0-9 is replaced by an underscore character.
     */
    public static String normalize(String code)
    {
        StringBuilder builder = new StringBuilder(code.toUpperCase());
        for (int i = 0, n = builder.length(); i < n; i++)
        {
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".indexOf(builder.charAt(i)) < 0)
            {
                builder.setCharAt(i, '_');
            }
        }
        return builder.toString();
    }
    
    private final String label;
    private final String code;
    
    /**
     * Creates an instance for specified code and label. The code will be normalized.
     */
    public CodeAndLabel(String code, String label)
    {
        this.code = normalize(code);
        this.label = label;
    }

    /**
     * Creates an instance from specified label with optional code prefix in form of
     * <code>&lt;code&gt;</code>. The code will be normalized.
     */
    public CodeAndLabel(String labelWithOptionalCode)
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
        code = normalize(c);
        label = t;
    }

    /**
     * Returns the label.
     */
    public final String getLabel()
    {
        return label;
    }

    /**
     * Returns the attribute.
     */
    public final String getCode()
    {
        return code;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof CodeAndLabel == false)
        {
            return false;
        }
        CodeAndLabel codeAndTitle = (CodeAndLabel) obj;
        return codeAndTitle.code.equals(code) && codeAndTitle.label.equals(label);
    }

    @Override
    public int hashCode()
    {
        return code.hashCode() * 37 + label.hashCode();
    }

    @Override
    public String toString()
    {
        return "<" + code + "> " + label;
    }
}
