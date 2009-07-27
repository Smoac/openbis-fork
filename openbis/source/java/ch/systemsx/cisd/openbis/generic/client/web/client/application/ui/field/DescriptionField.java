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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;

/**
 * Text field allowing to specify description.
 * 
 * @author Izabela Adamczyk
 */
public class DescriptionField extends MultilineVarcharField
{
    public static final String ID_SUFFIX = "_description";

    public DescriptionField(IMessageProvider messageProvider, boolean mandatory)
    {
        super(messageProvider.getMessage(Dict.DESCRIPTION), mandatory);
        setMaxLength(GenericConstants.DESCRIPTION_1000);
    }

    public DescriptionField(IMessageProvider messageProvider, boolean mandatory, String parentId)
    {
        this(messageProvider, mandatory);
        setId(parentId + ID_SUFFIX);
    }

    public void setValueAndUnescape(String unescapedValue)
    {
        setValue(StringEscapeUtils.unescapeHtml(unescapedValue));
    }

}
