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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.GenericTableRowColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;

public class GenericTableRowColumnDefinitionUI extends GenericTableRowColumnDefinition implements
        IColumnDefinitionUI<GenericTableRow>
{
    public GenericTableRowColumnDefinitionUI(GenericTableColumnHeader header, String title)
    {
        super(header, title);
    }

    // GWT only
    @SuppressWarnings("unused")
    private GenericTableRowColumnDefinitionUI()
    {
        this(null, null);
    }

    public int getWidth()
    {
        return 100;
    }

    public boolean isHidden()
    {
        return false;
    }

    public boolean isNumeric()
    {
        return false;
    }
    
}