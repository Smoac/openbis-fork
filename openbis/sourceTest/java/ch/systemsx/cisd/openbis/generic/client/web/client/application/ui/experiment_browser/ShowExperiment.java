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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment_browser;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for showing a experiment of given code.
 * 
 * @author Franz-Josef Elmer
 */
public final class ShowExperiment extends AbstractDefaultTestCommand
{
    private final String code;

    public ShowExperiment(final String code)
    {
        this.code = code;
        addCallbackClass(ExperimentBrowserGrid.ListExperimentsCallback.class);
    }

    @SuppressWarnings("unchecked")
    public void execute()
    {
        final Widget widget = GWTTestUtil.getWidgetWithID(ExperimentBrowserGrid.GRID_ID);
        assertTrue(widget instanceof Grid);
        final Grid<ExperimentModel> table = (Grid<ExperimentModel>) widget;
        table.fireEvent(Events.CellDoubleClick, createGridEvent(table));
    }

    private GridEvent createGridEvent(final Grid<ExperimentModel> table)
    {
        final ListStore<ExperimentModel> store = table.getStore();
        for (int i = 0; i < store.getCount(); i++)
        {
            final ExperimentModel row = store.getAt(i);
            if (code.equals(row.get(ModelDataPropertyNames.CODE)))
            {
                final GridEvent gridEvent = new GridEvent(table);
                gridEvent.rowIndex = i;
                return gridEvent;
            }
        }
        fail("Experiment '" + code + "' not found in store with " + store.getCount() + " rows.");
        return null;
    }
}
