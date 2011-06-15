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

import java.util.Date;

import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.google.gwt.event.dom.client.KeyCodes;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DateFormField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * Utility methods for columns.
 * 
 * @author Franz-Josef Elmer
 */
public class ColumnUtils
{
    /**
     * Creates a {@link CellEditor} based on specified data type.
     */
    public static CellEditor createCellEditor(DataTypeCode dataTypeOrNull)
    {
        CellEditor editor;
        switch (dataTypeOrNull)
        {
            case MULTILINE_VARCHAR:
                editor = new CellEditor(new MultiLineCellEditorField())
                    {
                        // WORKAROUND to allow use enter key in table editing
                        @Override
                        protected void onSpecialKey(FieldEvent fe)
                        {
                            if (fe.getKeyCode() != KeyCodes.KEY_ENTER)
                            {
                                super.onSpecialKey(fe);
                            }
                        }
                    };
                break;
            case BOOLEAN:
                CheckBox checkBox = new CheckBox();
                editor = new StringBasedCellEditor(checkBox);
                break;
            case TIMESTAMP:
                editor = new StringBasedCellEditor(new DateFormField("", false));
                break;
            default:
                editor = new CellEditor(new DefaultCellEditorField());
        }
        return editor;
    }

    /**
     * Extension of GXT {@link CellEditor} with automatic conversion from String values that we hold
     * in tables to specific data types (like {@link Date} or {@link Boolean}) handled by editor
     * fields and vice versa.
     */
    private static class StringBasedCellEditor extends CellEditor
    {

        public StringBasedCellEditor(Field<? extends Object> field)
        {
            super(field);
        }

        @Override
        public Object preProcessValue(Object value)
        {
            if (value == null)
            {
                return value;
            }
            return getField().getPropertyEditor().convertStringValue(value.toString());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object postProcessValue(Object value)
        {
            if (value == null)
            {
                return value;
            }
            return getField().getPropertyEditor().getStringValue(value);
        }

    }
}
