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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DatastoreServiceDescriptionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ProcessingPluginSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ReportingPluginSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.DataSetConnectionTypeProvider;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleDataSetsSection extends DisposableTabContent
{
    private final CheckBox showOnlyDirectlyConnectedCheckBox;

    private final DropDownList<DatastoreServiceDescriptionModel, DatastoreServiceDescription> reportSelectionWidget;

    private final DropDownList<DatastoreServiceDescriptionModel, DatastoreServiceDescription> processingSelectionWidget;

    private final TechId sampleId;

    private final SampleType sampleType;

    private IDisposableComponent metadataComponent;

    public SampleDataSetsSection(final IViewContext<?> viewContext,
            CheckBox showOnlyDirectlyConnectedCheckBox, TechId sampleId, SampleType sampleType)
    {
        super(viewContext.getMessage(Dict.EXTERNAL_DATA_HEADING), viewContext, sampleId);
        this.showOnlyDirectlyConnectedCheckBox = showOnlyDirectlyConnectedCheckBox;
        this.reportSelectionWidget = new ReportingPluginSelectionWidget(viewContext, sampleId);
        this.processingSelectionWidget = new ProcessingPluginSelectionWidget(viewContext, sampleId);
        this.sampleId = sampleId;
        this.sampleType = sampleType;
        setIds(DisplayTypeIDGenerator.DATA_SETS_SECTION);
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        getHeader().addTool(showOnlyDirectlyConnectedCheckBox);
        getHeader().addTool(reportSelectionWidget);
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
        getHeader().addTool(processingSelectionWidget);
        final IOnReportComponentGeneratedAction gridGeneratedAction =
                new IOnReportComponentGeneratedAction()
                    {
                        public void execute(IDisposableComponent gridComponent)
                        {
                            replaceContent(gridComponent);
                        }
                    };
        metadataComponent =
                SampleDataSetBrowser.create(viewContext, sampleId, sampleType,
                        new DataSetConnectionTypeProvider(showOnlyDirectlyConnectedCheckBox),
                        reportSelectionWidget, processingSelectionWidget, gridGeneratedAction);
        return metadataComponent;
    }

    @Override
    protected void replaceContent(IDisposableComponent content)
    {
        if (content != null)
        {
            removeAll();
            if (disposableComponentOrNull != null
                    && disposableComponentOrNull.getComponent().equals(
                            metadataComponent.getComponent()) == false)
            {
                super.disposeComponents(); // don't dispose metadata component
            }
            updateContent(content, true);
        }
    }

    @Override
    public void disposeComponents()
    {
        // when tab is closed dispose also the metadata component
        super.disposeComponents();
        metadataComponent.dispose(); // NOTE: second dispose on a grid does nothing
    }
}
