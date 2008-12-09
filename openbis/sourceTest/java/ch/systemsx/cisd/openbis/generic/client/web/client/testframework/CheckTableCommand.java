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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * Command for checking the content (i.e. <code>ListStore</code>) of a table (i.e.
 * <code>Grid</code>). It uses a fluent API approach for its methods to prepare expectations.
 * 
 * @author Franz-Josef Elmer
 */
public class CheckTableCommand extends AbstractDefaultTestCommand
{
    private final String tableID;

    private int expectedNumberOfRows = -1;

    private final List<Row> expectedRows = new ArrayList<Row>();

    private final List<Row> unexpectedRows = new ArrayList<Row>();

    /**
     * Creates an instance for the specified table or grid ID.
     */
    public CheckTableCommand(final String tableID)
    {
        super();
        this.tableID = tableID;
    }

    /**
     * Creates an instance for the specified table or grid ID and the specified class of the
     * triggering call-back object.
     */
    public CheckTableCommand(final String tableID,
            final Class<? extends AsyncCallback<?>> callbackClass)
    {
        super(callbackClass);
        this.tableID = tableID;
    }

    /**
     * Prepares this with the expectation upon the number of table rows.
     */
    public CheckTableCommand expectedSize(final int numberOfRows)
    {
        this.expectedNumberOfRows = numberOfRows;
        return this;
    }

    public CheckTableCommand unexpectedRow(final Row row)
    {
        unexpectedRows.add(row);
        return this;
    }

    /**
     * Prepares this with the expectation upon a certain row to be appear in the table.
     */
    public CheckTableCommand expectedRow(final Row row)
    {
        expectedRows.add(row);
        return this;
    }

    public void execute()
    {
        final ListStore<ModelData> store = getTableStore();
        final List<Row> matchedRows = new ArrayList<Row>();
        for (final Row expectedRow : expectedRows)
        {
            for (int i = 0; i < store.getCount(); i++)
            {
                final ModelData row = store.getAt(i);
                if (match(expectedRow, row))
                {
                    matchedRows.add(expectedRow);
                    break;
                }
            }
        }
        expectedRows.removeAll(matchedRows);
        if (expectedRows.isEmpty() == false)
        {
            final StringBuffer buffer = new StringBuffer("Unmatched expected rows:");
            for (final Row expectedRow : expectedRows)
            {
                buffer.append('\n').append(expectedRow);
            }
            fail(buffer.toString());
        }
        if (expectedNumberOfRows >= 0)
        {
            assertEquals(expectedNumberOfRows, store.getCount());
        }

        for (final Row unexpectedRow : unexpectedRows)
        {
            for (int i = 0; i < store.getCount(); i++)
            {
                final ModelData row = store.getAt(i);
                assertFalse(match(unexpectedRow, row));
            }
        }
    }

    private boolean match(final Row expectedRow, final ModelData row)
    {
        for (final Map.Entry<String, Object> entry : expectedRow.getColumnIDValuesMap().entrySet())
        {
            if (TestUtil.isEqual(row.get(entry.getKey()), wrapNull(entry.getValue())) == false)
            {
                return false;
            }
        }
        return true;
    }

    private Object wrapNull(Object value)
    {
        if (value == null)
        {
            return "";
        } else
        {
            return value;
        }
    }

    @SuppressWarnings("unchecked")
    private ListStore<ModelData> getTableStore()
    {
        final Widget widget = GWTTestUtil.getWidgetWithID(tableID);
        assertTrue("Not a Widget of type Grid: " + widget.getClass(), widget instanceof Grid);
        return ((Grid<ModelData>) widget).getStore();
    }

}
