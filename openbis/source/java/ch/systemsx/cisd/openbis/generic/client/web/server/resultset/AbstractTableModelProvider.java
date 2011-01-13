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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 * Superclass of {@link ITableModelProvider} implementations which creates the actual table model
 * lazily.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractTableModelProvider<T extends ISerializable> implements
        ITableModelProvider<T>
{
    private TypedTableModel<T> model;

    public final TypedTableModel<T> getTableModel()
    {
        if (model == null)
        {
            model = createTableModel();
        }
        return model;
    }

    public TypedTableModel<T> getTableModel(int maxSize)
    {
        TypedTableModel<T> tableModel = getTableModel();
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        List<TableModelRowWithObject<T>> rows = tableModel.getRows();
        List<TableModelRowWithObject<T>> limitedRows = new ArrayList<TableModelRowWithObject<T>>();
        for (TableModelRowWithObject<T> row : rows)
        {
            if (limitedRows.size() == maxSize)
            {
                break;
            }
            limitedRows.add(row);
        }
        return new TypedTableModel<T>(headers, limitedRows);
    }

    /**
     * Creates the table model. The returned instance is always returned by {@link #getTableModel()}
     * .
     */
    protected abstract TypedTableModel<T> createTableModel();

}
