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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid;
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
    public static final String SHOW_ONLY_DIRECTLY_CONNECTED_CHECKBOX_ID_POSTFIX =
            "-show_only_directly_connected_checkbox";

    private final CheckBox showOnlyDirectlyConnectedCheckBox;

    private final TechId sampleId;

    private final SampleType sampleType;

    public SampleDataSetsSection(final IViewContext<?> viewContext, TechId sampleId,
            SampleType sampleType)
    {
        super(viewContext.getMessage(Dict.EXTERNAL_DATA_HEADING), viewContext, sampleId);
        this.showOnlyDirectlyConnectedCheckBox = createShowOnlyDirectlyConnectedCheckBox();
        this.sampleId = sampleId;
        this.sampleType = sampleType;
    }

    private CheckBox createShowOnlyDirectlyConnectedCheckBox()
    {
        CheckBox result = new CheckBox();
        result.setId(getId() + SHOW_ONLY_DIRECTLY_CONNECTED_CHECKBOX_ID_POSTFIX);
        result.setBoxLabel(viewContext.getMessage(Dict.SHOW_ONLY_DIRECTLY_CONNECTED));
        result.setValue(true);
        return result;
    }

    @Override
    protected void initWidgets(AbstractExternalDataGrid browser)
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
        super.initWidgets(browser);
    }

    @Override
    protected IDisposableComponent createDatasetBrowserComponent()
    {
        return SampleDataSetBrowser.create(viewContext, sampleId, sampleType,
                new DataSetConnectionTypeProvider(showOnlyDirectlyConnectedCheckBox));
    }

}
