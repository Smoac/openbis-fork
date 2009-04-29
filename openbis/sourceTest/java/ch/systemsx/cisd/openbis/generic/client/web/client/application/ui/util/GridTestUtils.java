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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util;

import junit.framework.Assert;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.EntityPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.BrowserGridPagingToolBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.TestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Utility methods to test subclasses of {@link AbstractBrowserGrid}
 * 
 * @author Tomasz Pylak
 */
public class GridTestUtils
{
    /** simulates pressing the refresh button for the currently opened grid */
    public static final void pressRefreshGridButton()
    {
        final Button refresh =
                (Button) GWTTestUtil.getWidgetWithID(BrowserGridPagingToolBar.REFRESH_BUTTON_ID);
        Assert.assertTrue(refresh.isEnabled());
        GWTTestUtil.clickButtonWithID(BrowserGridPagingToolBar.REFRESH_BUTTON_ID);
    }

    /**
     * Fires a double click event on a first row which contains given value in a column with a
     * specified id.
     */
    public static <T extends ModelData> void fireDoubleClick(final Grid<T> table, String columnId,
            String columnValue)
    {
        GridEvent event = createGridEvent(table, columnId, columnValue);
        table.fireEvent(Events.CellDoubleClick, event);
    }

    /**
     * Fires a single click event on a first row which contains given value in a column with a
     * specified id.
     */
    public static <T extends ModelData> void fireSingleClick(final Grid<T> table, String columnId,
            String columnValue)
    {
        GridEvent event = createGridEvent(table, columnId, columnValue);
        table.fireEvent(Events.CellClick, event);
    }

    private static <T extends ModelData> GridEvent createGridEvent(final Grid<T> table,
            String columnId, String columnValue)
    {
        final ListStore<T> store = table.getStore();
        String codes = "";
        for (int i = 0; i < store.getCount(); i++)
        {
            final T row = store.getAt(i);
            String rowCode = TestUtil.normalize(row.get(columnId));
            if (columnValue.equalsIgnoreCase(rowCode))
            {
                final GridEvent gridEvent = new GridEvent(table);
                gridEvent.rowIndex = i;
                gridEvent.colIndex = table.getColumnModel().findColumnIndex(columnId);
                return gridEvent;
            }
            codes += rowCode;
            if (i < store.getCount() - 1)
            {
                codes += ", ";
            }
        }
        Assert.fail("The column with id '" + columnId + "' has never the value '" + columnValue
                + "'. Following values were found: " + codes);
        return null; // just to make the compiler happy
    }

    public static String getPropertyColumnIdentifier(final String propertyCode,
            boolean internalNamespace)
    {
        final PropertyType propertyType = createPropertyType(propertyCode, internalNamespace);
        final String identifier =
                new EntityPropertyColDef<Sample>(propertyType, true).getIdentifier();
        return identifier;
    }

    public final static PropertyType createPropertyType(final String propertyCode,
            final boolean internalNamespace)
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setInternalNamespace(internalNamespace);
        propertyType.setSimpleCode(propertyCode.toUpperCase());
        return propertyType;
    }

    public static String getDataSetPropertyColumnIdentifier(final String propertyCode,
            boolean internalNamespace)
    {
        final PropertyType propertyType = createPropertyType(propertyCode, internalNamespace);
        final String identifier =
                new DataSetPropertyColDef(propertyType, false, 10, propertyType.getLabel())
                        .getIdentifier();
        return identifier;
    }
}
