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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.List;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;

/**
 * A {@link MultilineVarcharField} extension with support of handling list of items (Strings).
 * 
 * @author Piotr Buczek
 */
public class MultilineItemsField extends MultilineVarcharField
{
    /** Constructor for default sized field (5 lines). */
    public MultilineItemsField(final String label, final boolean mandatory)
    {
        super(label, mandatory);
    }

    /** Constructor for multiline field with given number of lines. */
    public MultilineItemsField(final String label, final boolean mandatory, int lines)
    {
        super(label, mandatory, lines);
    }

    /**
     * null if the area has not been modified, the list of all items (separated by comma or a new
     * line) otherwise
     */
    public final String[] tryGetModifiedItemList()
    {
        if (isDirty() == false)
        {
            return null;
        }
        String text = getValue();
        if (StringUtils.isBlank(text) == false)
        {
            return text.split(GenericConstants.ITEMS_TEXTAREA_REGEX);
        } else
        {
            return new String[0];
        }
    }

    public final void setItems(List<String> items)
    {
        String textValue = createTextValue(items);
        setValue(textValue);
        setOriginalValue(textValue);
    }

    public final void appendItem(String item)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getValue() == null ? "" : getValue());
        appendItem(sb, item);
        setValue(sb.toString());
    }

    private static String createTextValue(List<String> items)
    {
        StringBuilder sb = new StringBuilder();
        for (String item : items)
        {
            appendItem(sb, item);
        }
        return sb.toString();
    }

    private static final void appendItem(StringBuilder sb, String item)
    {
        if (sb.length() > 0)
        {
            sb.append(GenericConstants.ITEMS_TEXTAREA_DEFAULT_SEPARATOR);
        }
        sb.append(item);
    }

}
