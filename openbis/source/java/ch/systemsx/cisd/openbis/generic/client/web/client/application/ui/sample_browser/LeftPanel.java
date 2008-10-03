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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

class LeftPanel extends LayoutContainer
{

    final RightPanel rightPanel;

    private final GenericViewContext viewContext;

    public LeftPanel(GenericViewContext viewContext, RightPanel rightPanel)
    {
        this.viewContext = viewContext;
        this.rightPanel = rightPanel;
        setLayout(new FlowLayout());
        setBorders(true);
        refresh();

    }

    private void display(List<SampleType> sampleTypes)
    {
        removeAll();
        ListStore<SampleTypeModel> sampleTypeStore = new ListStore<SampleTypeModel>();
        for (SampleType st : sampleTypes)
        {
            sampleTypeStore.add(new SampleTypeModel(st));
        }
        final SampleTypeSelectionWidget sampleTypeSelectionWidget =
                new SampleTypeSelectionWidget(sampleTypeStore);
        add(sampleTypeSelectionWidget);
        add(new Button("View >>", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {

                    final SampleType selectedType = sampleTypeSelectionWidget.tryGetSelected();
                    if (selectedType != null)
                    {
                        rightPanel.refresh(selectedType);
                    } else
                    {
                        MessageBox.alert("Sample type not selected",
                                "Please choose a sample type.", null);
                    }
                }
            }));
        layout();
    }

    public void refresh()
    {
        viewContext.getService().listSampleTypes(
                new AbstractAsyncCallback<List<SampleType>>(viewContext)
                    {
                        @Override
                        public void process(List<SampleType> sampleTypes)
                        {
                            display(sampleTypes);
                        }
                    });
    }

}