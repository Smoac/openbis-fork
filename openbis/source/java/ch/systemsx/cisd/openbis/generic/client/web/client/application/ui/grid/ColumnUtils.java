/*
 * Copyright 2011 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * Utility methods for columns.
 *
 * @author Franz-Josef Elmer
 */
public class ColumnUtils
{
    /**
     * Creates a field for {@link CellEditor} based on specified data type.
     */
    public static Field<? extends Object> createCellEditorField(DataTypeCode dataTypeOrNull)
    {
        switch (dataTypeOrNull)
        {
            case MULTILINE_VARCHAR: return new MultiLineCellEditorField();
            default: return new DefaultCellEditorField();
        }
    }

}
