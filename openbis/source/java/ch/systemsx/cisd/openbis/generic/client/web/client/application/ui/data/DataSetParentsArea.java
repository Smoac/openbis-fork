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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.TextArea;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * A text area to specify parents of a data set. Parents are specified by codes separated by commas,
 * spaces or new lines.
 * 
 * @author Piotr Buczek
 */
public final class DataSetParentsArea extends TextArea
{

    public static final String ID_SUFFIX_PARENTS = "_parents";

    public DataSetParentsArea(IMessageProvider messageProvider, String idPrefix)
    {
        super();

        setHeight("" + MultilineVarcharField.EM_TO_PIXEL * 10);
        this.setFieldLabel(messageProvider.getMessage(Dict.PARENTS));
        setEmptyText(messageProvider.getMessage(Dict.PARENTS_EMPTY));
        setId(createId(idPrefix));
    }

    public static String createId(String idPrefix)
    {
        return idPrefix + ID_SUFFIX_PARENTS;
    }

    // null if the area has not been modified, the list of all data set parent
    // codes otherwise
    public final String[] tryGetModifiedParentCodes()
    {
        if (isDirty() == false)
        {
            return null;
        }
        String text = getValue();
        if (StringUtils.isBlank(text) == false)
        {
            return text.split(GenericConstants.CODES_TEXTAREA_REGEX);
        } else
        {
            return new String[0];
        }
    }

    public final void setParents(List<ExternalData> parents)
    {
        setParentCodes(extractCodes(parents));
    }

    private static String[] extractCodes(List<ExternalData> parents)
    {
        String[] codes = new String[parents.size()];
        int i = 0;
        for (ExternalData parent : parents)
        {
            codes[i] = parent.getCode();
            i++;
        }
        return codes;
    }

    public final void setParentCodes(String[] parentCodes)
    {
        String textValue = createTextValue(parentCodes);
        setValue(textValue);
        setOriginalValue(textValue);
    }

    private static String createTextValue(String[] parentCodes)
    {
        StringBuffer sb = new StringBuffer();
        for (String parentCode : parentCodes)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            sb.append(parentCode);
        }
        return sb.toString();
    }
}
