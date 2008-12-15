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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;

/**
 * Main view component of experiment browser.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentBrowser extends LayoutContainer implements Listener<TabPanelEvent>
{
    private static final String PREFIX = "experiment-browser";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final ExperimentBrowserGrid experimentBrowserGrid;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public ExperimentBrowser(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setId(ID);
        setLayout(new RowLayout());

        experimentBrowserGrid = new ExperimentBrowserGrid(viewContext);
        final ExperimentBrowserToolbar toolbar =
                new ExperimentBrowserToolbar(viewContext, experimentBrowserGrid);

        add(toolbar);
        add(experimentBrowserGrid, new RowData(1, 1));
    }

    public final void handleEvent(final TabPanelEvent be)
    {
        final String resultSetKey = experimentBrowserGrid.getResultSetKey();
        if (be.type == Events.Close && resultSetKey != null)
        {
            viewContext.getService().removeResultSet(resultSetKey,
                    new VoidAsyncCallback<Void>(viewContext));
        }
    }
}