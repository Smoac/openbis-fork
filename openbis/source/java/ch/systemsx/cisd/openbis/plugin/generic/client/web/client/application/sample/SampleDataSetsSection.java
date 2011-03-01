/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DatastoreServiceDescriptionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ReportingPluginSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.AbstractDataSetsSection;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.DataSetConnectionTypeProvider;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleDataSetsSection extends AbstractDataSetsSection
{
    private final CheckBox showOnlyDirectlyConnectedCheckBox;

    private final TechId sampleId;

    private final SampleType sampleType;

    public SampleDataSetsSection(final IViewContext<?> viewContext,
            final CheckBox showOnlyDirectlyConnectedCheckBox, TechId sampleId, SampleType sampleType)
    {
        super(viewContext.getMessage(Dict.EXTERNAL_DATA_HEADING), viewContext, sampleId);
        this.showOnlyDirectlyConnectedCheckBox = showOnlyDirectlyConnectedCheckBox;
        this.sampleId = sampleId;
        this.sampleType = sampleType;
    }

    @Override
    protected void initWidgets()
    {
        // first add check box
        getHeader().addTool(showOnlyDirectlyConnectedCheckBox);
        reportSelectionWidget
                .addSelectionChangedListener(new SelectionChangedListener<DatastoreServiceDescriptionModel>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<DatastoreServiceDescriptionModel> se)
                        {
                            final DatastoreServiceDescriptionModel selectedItem =
                                    se.getSelectedItem();
                            if (selectedItem != null)
                            {
                                DatastoreServiceDescription service = selectedItem.getBaseObject();

                                if (service.getLabel().equals(
                                        ReportingPluginSelectionWidget.METADATA))
                                {
                                    showOnlyDirectlyConnectedCheckBox.show();
                                } else
                                {
                                    showOnlyDirectlyConnectedCheckBox.hide();
                                }
                            }
                        }
                    });
        super.initWidgets();
    }

    @Override
    protected IDisposableComponent createDatasetBrowserComponent()
    {
        return SampleDataSetBrowser.create(viewContext, sampleId, sampleType,
                new DataSetConnectionTypeProvider(showOnlyDirectlyConnectedCheckBox));
    }

}
