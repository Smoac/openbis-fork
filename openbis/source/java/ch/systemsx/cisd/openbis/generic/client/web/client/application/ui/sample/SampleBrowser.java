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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.CommonColumnsConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ParentColumnsConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.PropertyColumnsConfig;

/**
 * Main view component of Sample Browser.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleBrowser extends LayoutContainer implements Listener<TabPanelEvent>
{
    private static final String PREFIX = "sample-browser";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private SampleBrowserGrid sampleBrowserGrid;

    public SampleBrowser(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        setId(ID);
        setLayout(new RowLayout());

        final CommonColumnsConfig commonColumns = new CommonColumnsConfig(viewContext);
        final ParentColumnsConfig parentColumns = new ParentColumnsConfig(viewContext);
        final PropertyColumnsConfig propertyColumns = new PropertyColumnsConfig();

        sampleBrowserGrid =
                new SampleBrowserGrid(viewContext, commonColumns, parentColumns, propertyColumns);
        final SampleBrowserToolbar toolbar =
                new SampleBrowserToolbar(viewContext, sampleBrowserGrid, commonColumns,
                        parentColumns, propertyColumns);

        add(toolbar);
        add(sampleBrowserGrid, new RowData(1, 1));
    }

    //
    // Listener
    //

    public final void handleEvent(final TabPanelEvent be)
    {
        if (be.type == Events.Close)
        {
            sampleBrowserGrid.disposeCache();
        }
    }
}