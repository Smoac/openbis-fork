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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.TypedTableGridColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ILinkGenerator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TypedTableGridColumnDefinitionUI<T extends IsSerializable> extends
        TypedTableGridColumnDefinition<T> implements IColumnDefinitionUI<TableModelRowWithObject<T>>
{
    private transient final ILinkGenerator<T> linkGeneratorOrNull;

    public TypedTableGridColumnDefinitionUI(TableModelColumnHeader header, String title, ILinkGenerator<T> linkGeneratorOrNull)
    {
        super(header, title);
        this.linkGeneratorOrNull = linkGeneratorOrNull;
    }

    // GWT only
    @SuppressWarnings("unused")
    private TypedTableGridColumnDefinitionUI()
    {
        this(null, null, null);
    }
    
    public int getWidth()
    {
        return header.getDefaultColumnWidth();
    }

    public boolean isHidden()
    {
        return false;
    }

    public boolean isLink()
    {
        return false;
    }

    public String tryGetLink(TableModelRowWithObject<T> entity)
    {
        if (linkGeneratorOrNull == null)
        {
            return null;
        }
        T objectOrNull = entity.getObjectOrNull();
        return objectOrNull == null ? null : linkGeneratorOrNull.tryGetLink(objectOrNull);
    }

    public boolean isNumeric()
    {
        DataTypeCode type = header.getDataType();
        return type == DataTypeCode.INTEGER || type == DataTypeCode.REAL;
    }

}
