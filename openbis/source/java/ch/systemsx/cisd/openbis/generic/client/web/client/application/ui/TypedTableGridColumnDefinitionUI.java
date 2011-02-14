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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.TypedTableGridColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ILinkGenerator;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
public class TypedTableGridColumnDefinitionUI<T extends ISerializable> extends
        TypedTableGridColumnDefinition<T> implements
        IColumnDefinitionUI<TableModelRowWithObject<T>>
{
    private transient final ILinkGenerator<T> linkGeneratorOrNull;

    private boolean hidden;

    public TypedTableGridColumnDefinitionUI(TableModelColumnHeader header, String title,
            String downloadURL, String sessionID, ILinkGenerator<T> linkGeneratorOrNull)
    {
        super(header, title, downloadURL, sessionID);
        this.linkGeneratorOrNull = linkGeneratorOrNull;
        if (header != null)
        {
            hidden = header.isHidden();
        }
    }

    // GWT only
    @SuppressWarnings("unused")
    private TypedTableGridColumnDefinitionUI()
    {
        this(null, null, null, null, null);
    }

    public int getWidth()
    {
        return header.getDefaultColumnWidth();
    }

    public final void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    public boolean isHidden()
    {
        return hidden;
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
        // TODO
        final ISerializableComparable value = entity.getValues().get(header.getIndex());
        final T objectOrNull = entity.getObjectOrNull();
        return objectOrNull == null ? null : linkGeneratorOrNull.tryGetLink(objectOrNull, value);
    }

    public boolean isNumeric()
    {
        DataTypeCode type = header.getDataType();
        return type == DataTypeCode.INTEGER || type == DataTypeCode.REAL;
    }

}
