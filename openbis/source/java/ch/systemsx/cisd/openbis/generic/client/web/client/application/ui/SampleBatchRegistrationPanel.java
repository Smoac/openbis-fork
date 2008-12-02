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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * The {@link LayoutContainer} extension for batch registering a sample.
 * 
 * @author Christian Ribeaud
 */
public final class SampleBatchRegistrationPanel extends LayoutContainer
{
    private static final String ID = "sample-batch-registration";

    private final SampleTypeSelectionWidget sampleTypeSelection;

    public SampleBatchRegistrationPanel(final CommonViewContext viewContext)
    {
        setId(GenericConstants.ID_PREFIX + ID);
        setScrollMode(Scroll.AUTO);
        sampleTypeSelection = new SampleTypeSelectionWidget(viewContext, true, ID);
        final ToolBar toolBar = createToolBar();
        add(toolBar);
        sampleTypeSelection.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {

                //
                // SelectionChangedListener
                //

                @Override
                public final void selectionChanged(final SelectionChangedEvent<ModelData> se)
                {
                    final SampleType sampleType = sampleTypeSelection.tryGetSelected();
                    if (sampleType != null)
                    {
                        removeAll();
                        add(toolBar);
                        add(viewContext.getClientPluginFactoryProvider().getClientPluginFactory(
                                sampleType.getCode()).createViewClientForSampleType(
                                sampleType.getCode()).createBatchRegistrationForSampleType(
                                sampleType));
                        layout();
                    }
                }
            });
    }

    private final ToolBar createToolBar()
    {
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Sample type:"));
        toolBar.add(new AdapterToolItem(sampleTypeSelection));
        return toolBar;
    }
}
