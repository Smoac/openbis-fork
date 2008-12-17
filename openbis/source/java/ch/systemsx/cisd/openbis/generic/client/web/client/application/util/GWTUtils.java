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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.ListBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;

/**
 * Some utility methods around <i>GWT</i>.
 * 
 * @author Christian Ribeaud
 */
public final class GWTUtils
{

    /**
     * The <code>null</code> value label for {@link ListBox} resp. {@link ComboBox} when no value
     * has been selected by the user.
     */
    public static final String NONE_LIST_ITEM = "(None)";

    private GWTUtils()
    {
        // Can not be instantiated.
    }

    private static boolean testing = false;

    /**
     * Whether we are in testing mode.
     */
    public static boolean isTesting()
    {
        return testing;
    }

    /**
     * Sets <code>testing</code> flag to <code>true</code>.
     */
    public static void testing()
    {
        testing = true;
    }

    /**
     * Selects given <var>value</var> of given <var>listBox</var>.
     */
    public final static void setSelectedItem(final ListBox listBox, final String value)
    {
        assert listBox != null : "Unspecified list box.";
        assert value != null : "Unspecified value.";
        for (int index = 0; index < listBox.getItemCount(); index++)
        {
            if (listBox.getItemText(index).equals(value))
            {
                listBox.setSelectedIndex(index);
                return;
            }
        }
        throw new IllegalArgumentException("Given value '" + value
                + "' not found in given list box.");
    }

    /**
     * Selects given <var>value</var> of given <var>comboBox</var>.
     */
    public final static <T extends ModelData> void setSelectedItem(final ComboBox<T> comboBox,
            final String property, final String value)
    {
        assert comboBox != null : "Unspecified combo box.";
        assert property != null : "Unspecified model property.";
        assert value != null : "Unspecified model property value.";
        final List<T> list = comboBox.getStore().findModels(property, value);
        if (list.size() == 0)
        {
            throw new IllegalArgumentException("Given value '" + value + "' for property '"
                    + property + "' not found in the combo box.");
        }
        final List<T> selection = new ArrayList<T>();
        selection.add(list.get(0));
        comboBox.setSelection(selection);
    }

    /**
     * Calls {@link Grid#setAutoExpandColumn(String)} on the last column visible.
     * <p>
     * Does nothing if all columns are hidden.
     * </p>
     */
    public final static <T extends ModelData> void setAutoExpandOnLastVisibleColumn(
            final Grid<T> grid)
    {
        final ColumnModel columnModel = grid.getColumnModel();
        for (int i = columnModel.getColumnCount(); --i >= 0;)
        {
            final ColumnConfig columnConfig = columnModel.getColumn(i);
            if (columnConfig.isHidden() == false)
            {
                // TODO 2008-12-28, Christian Ribeaud: This sometimes throws an exception.
                // grid.setAutoExpandColumn(columnConfig.getId());
                return;
            }
        }
    }

    /**
     * Tries to return the selected object (saved as {@link ModelDataPropertyNames#OBJECT} in the
     * model) from the given {@link ComboBox}.
     */
    public final static <T extends ModelData, O> O tryGetSingleSelected(final ComboBox<T> comboBox)
    {
        assert comboBox != null : "Unspecified combo box.";
        final List<T> selection = comboBox.getSelection();
        final int size = selection.size();
        if (size > 0)
        {
            assert size == 1 : "Only one item must be selected.";
            return selection.get(0).get(ModelDataPropertyNames.OBJECT);
        }
        return null;
    }
}
