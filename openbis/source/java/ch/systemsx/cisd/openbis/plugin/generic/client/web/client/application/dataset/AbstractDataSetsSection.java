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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetComputeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetReportGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DatastoreServiceDescriptionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ProcessingPluginSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ReportingPluginSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * @author Piotr Buczek
 */
public abstract class AbstractDataSetsSection extends DisposableTabContent
{
    protected final DropDownList<DatastoreServiceDescriptionModel, DatastoreServiceDescription> reportSelectionWidget;

    protected final DropDownList<DatastoreServiceDescriptionModel, DatastoreServiceDescription> processingSelectionWidget;

    protected final IOnReportComponentGeneratedAction reportGeneratedAction;

    private IDisposableComponent metadataComponent;

    public AbstractDataSetsSection(final String header, final IViewContext<?> viewContext,
            IIdHolder ownerIdOrNull)
    {
        super(header, viewContext, ownerIdOrNull);
        this.reportSelectionWidget = new ReportingPluginSelectionWidget(viewContext, ownerIdOrNull);
        this.processingSelectionWidget =
                new ProcessingPluginSelectionWidget(viewContext, ownerIdOrNull);
        this.reportGeneratedAction = new IOnReportComponentGeneratedAction()
            {
                public void execute(IDisposableComponent gridComponent)
                {
                    replaceContent(gridComponent);
                }
            };
        setIds(DisplayTypeIDGenerator.DATA_SETS_SECTION);
    }

    protected abstract IDisposableComponent createDatasetBrowserComponent();

    protected void initWidgets()
    {
        getHeader().addTool(reportSelectionWidget);
        getHeader().addTool(processingSelectionWidget);
    }

    @Override
    protected final IDisposableComponent createDisposableContent()
    {
        initWidgets();
        metadataComponent = createDatasetBrowserComponent();

        SelectionChangedListener<DatastoreServiceDescriptionModel> serviceChangedListener =
                createServiceSelectionChangedListener(viewContext, metadataComponent,
                        reportGeneratedAction);
        reportSelectionWidget.addSelectionChangedListener(serviceChangedListener);
        processingSelectionWidget.addSelectionChangedListener(serviceChangedListener);
        return metadataComponent;
    }

    @Override
    protected final void replaceContent(IDisposableComponent content)
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
    public final void disposeComponents()
    {
        // when tab is closed dispose also the metadata component
        super.disposeComponents();
        metadataComponent.dispose(); // NOTE: second dispose on a grid does nothing
    }

    private static SelectionChangedListener<DatastoreServiceDescriptionModel> createServiceSelectionChangedListener(
            final IViewContext<?> viewContext, final IDisposableComponent metadataComponent,
            final IOnReportComponentGeneratedAction reportGeneratedAction)
    {
        final AbstractExternalDataGrid browser =
                (AbstractExternalDataGrid) metadataComponent.getComponent();
        return new SelectionChangedListener<DatastoreServiceDescriptionModel>()
            {

                @Override
                public void selectionChanged(
                        SelectionChangedEvent<DatastoreServiceDescriptionModel> se)
                {
                    final DatastoreServiceDescriptionModel selectedItem = se.getSelectedItem();
                    if (selectedItem != null)
                    {
                        DatastoreServiceDescription service = selectedItem.getBaseObject();

                        if (service.getLabel().equals(ReportingPluginSelectionWidget.METADATA))
                        {
                            showMetadataView();
                        } else
                        {
                            switch (service.getServiceKind())
                            {
                                case PROCESSING:
                                    process(service);
                                    break;
                                case QUERIES:
                                    showGeneratedReportComponentView(service);
                                    break;
                            }
                        }
                    }

                }

                private void process(DatastoreServiceDescription service)
                {
                    SelectedAndDisplayedItems items =
                            browser.getSelectedAndDisplayedItemsAction().execute();
                    DataSetComputeUtils.createComputeAction(viewContext.getCommonViewContext(),
                            items, service, service.getServiceKind(), reportGeneratedAction)
                            .execute();
                }

                private void showMetadataView()
                {
                    reportGeneratedAction.execute(metadataComponent);
                }

                private void showGeneratedReportComponentView(DatastoreServiceDescription service)
                {
                    SelectedAndDisplayedItems items =
                            browser.getSelectedAndDisplayedItemsAction().execute();

                    if (browser.getSelectedItems().isEmpty())
                    {
                        // when no data sets were selected perform query without asking
                        DisplayedOrSelectedDatasetCriteria criteria = items.createCriteria(false);
                        DataSetReportGenerator.generateAndInvoke(
                                viewContext.getCommonViewContext(), service, criteria,
                                reportGeneratedAction);
                    } else
                    {
                        DataSetComputeUtils.createComputeAction(viewContext.getCommonViewContext(),
                                items, service, service.getServiceKind(), reportGeneratedAction)
                                .execute();
                    }
                }
            };

    }
}
