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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for showing a sample of given code.
 * 
 * @author Franz-Josef Elmer
 */
public final class ShowSample extends AbstractDefaultTestCommand
{
    private final String code;

    public ShowSample(final String code)
    {
        this.code = code;
        addCallbackClass(SampleBrowserGrid.ListEntitiesCallback.class);
    }

    @SuppressWarnings("unchecked")
    public void execute()
    {
        final Widget widget =
                GWTTestUtil.getWidgetWithID(SampleBrowserGrid.GRID_ID
                        + SampleBrowserGrid.SAMPLE_BROWSER_SUFFIX);
        assertTrue(widget instanceof Grid);
        final Grid<SampleModel> table = (Grid<SampleModel>) widget;
        table.fireEvent(Events.CellDoubleClick, createGridEvent(table));
    }

    private GridEvent createGridEvent(final Grid<SampleModel> table)
    {
        final ListStore<SampleModel> store = table.getStore();
        String codes = "";
        for (int i = 0; i < store.getCount(); i++)
        {
            final SampleModel row = store.getAt(i);
            String rowCode = String.valueOf(row.get(CommonSampleColDefKind.CODE.id())).trim();
            if (code.equalsIgnoreCase(rowCode))
            {
                final GridEvent gridEvent = new GridEvent(table);
                gridEvent.rowIndex = i;
                return gridEvent;
            }
            codes += rowCode;
            if (i < store.getCount() - 1)
            {
                codes += ", ";
            }
        }
        fail("Sample '" + code + "' not found in store with following codes: " + codes);
        return null; // just to make the compiler happy
    }

}
