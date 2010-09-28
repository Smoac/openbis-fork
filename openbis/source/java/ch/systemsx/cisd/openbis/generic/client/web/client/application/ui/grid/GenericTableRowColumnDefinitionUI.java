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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;

/**
 * UI extension of {@link GenericTableRowColumnDefinition}.
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GenericTableRowColumnDefinitionUI extends GenericTableRowColumnDefinition implements
        IColumnDefinitionUI<GenericTableRow>
{
    private int width;

    public GenericTableRowColumnDefinitionUI(GenericTableColumnHeader header, String title)
    {
        this(header, title, 100);
    }

    public GenericTableRowColumnDefinitionUI(GenericTableColumnHeader header, String title, int width)
    {
        super(header, title);
        this.width = width;
    }
    
    // GWT only
    @SuppressWarnings("unused")
    private GenericTableRowColumnDefinitionUI()
    {
        this(null, null);
    }

    public int getWidth()
    {
        return width;
    }

    public boolean isHidden()
    {
        return false;
    }

    public boolean isNumeric()
    {
        DataTypeCode type = header.getType();
        return type == DataTypeCode.INTEGER || type == DataTypeCode.REAL;
    }

    public boolean isLink()
    {
        return header.isLinkable();
    }

    public String tryGetLink(GenericTableRow entity)
    {
        return null;
    }
    
}