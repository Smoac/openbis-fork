/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetComputeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DatastoreServiceDescriptionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ReportingPluginSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.DataSetConnectionTypeProvider;

/**
 * @author Franz-Josef Elmer
 */
class SampleDataSetBrowser extends AbstractExternalDataGrid
{
    private static final String PREFIX = "sample-data-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final DataSetConnectionTypeProvider connectionTypeProvider;

    public static IDisposableComponent create(
            IViewContext<?> viewContext,
            TechId sampleId,
            final SampleType sampleType,
            final DataSetConnectionTypeProvider connectionTypeProvider,
            DropDownList<DatastoreServiceDescriptionModel, DatastoreServiceDescription> reportSelectionWidget,
            DropDownList<DatastoreServiceDescriptionModel, DatastoreServiceDescription> processingSelectionWidget,
            IOnReportComponentGeneratedAction reportGeneratedAction)
    {
        IViewContext<ICommonClientServiceAsync> commonViewContext =
                viewContext.getCommonViewContext();

        SampleDataSetBrowser browser =
                new SampleDataSetBrowser(commonViewContext, sampleId, connectionTypeProvider)
                    {
                        @Override
                        public String getGridDisplayTypeID()
                        {
                            return super.getGridDisplayTypeID() + "-" + sampleType.getCode();
                        }

                    };
        SelectionChangedListener<DatastoreServiceDescriptionModel> serviceChangedListener =
                createServiceSelectionChangedListener(viewContext, browser, reportGeneratedAction);
        reportSelectionWidget.addSelectionChangedListener(serviceChangedListener);
        processingSelectionWidget.addSelectionChangedListener(serviceChangedListener);
        return browser.asDisposableWithoutToolbar();
    }

    private final TechId sampleId;

    private SampleDataSetBrowser(IViewContext<ICommonClientServiceAsync> viewContext,
            TechId sampleId, DataSetConnectionTypeProvider connectionTypeProvider)
    {
        super(viewContext, createBrowserId(sampleId), createGridId(sampleId),
                DisplayTypeIDGenerator.SAMPLE_DETAILS_GRID);
        this.sampleId = sampleId;
        this.connectionTypeProvider = connectionTypeProvider;
        // refresh data when connection type provider value changes
        connectionTypeProvider.setOnChangeAction(new IDelegatedAction()
            {
                public void execute()
                {
                    refresh();
                }
            });
    }

    private static SelectionChangedListener<DatastoreServiceDescriptionModel> createServiceSelectionChangedListener(
            final IViewContext<?> viewContext, final SampleDataSetBrowser browser,
            final IOnReportComponentGeneratedAction reportGeneratedAction)
    {
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
                    // viewContext.getCommonService().processDatasets(
                    // service,
                    // items.createCriteria(false),
                    // AsyncCallbackWithProgressBar.decorate(new ProcessingDisplayCallback(
                    // viewContext), "Scheduling processing..."));
                }

                private void showMetadataView()
                {
                    reportGeneratedAction.execute(browser.asDisposableWithoutToolbar());
                }

                private void showGeneratedReportComponentView(DatastoreServiceDescription service)
                {
                    SelectedAndDisplayedItems items =
                            browser.getSelectedAndDisplayedItemsAction().execute();
                    DataSetComputeUtils.createComputeAction(viewContext.getCommonViewContext(),
                            items, service, service.getServiceKind(), reportGeneratedAction)
                            .execute();
                    // DisplayedOrSelectedDatasetCriteria criteria = items.createCriteria(false);
                    // DataSetReportGenerator.generateAndInvoke(viewContext.getCommonViewContext(),
                    // service, items.createCriteria(false), reportGeneratedAction);
                }
            };

    }

    public static final String createGridId(TechId sampleId)
    {
        return createBrowserId(sampleId) + "-grid";
    }

    public static final String createBrowserId(TechId sampleId)
    {
        return ID_PREFIX + sampleId;
    }

    @Override
    protected void listDatasets(DefaultResultSetConfig<String, ExternalData> resultSetConfig,
            final AbstractAsyncCallback<ResultSetWithEntityTypes<ExternalData>> callback)
    {
        boolean onlyDirectlyConnected = connectionTypeProvider.getShowOnlyDirectlyConnected();
        viewContext.getService().listSampleDataSets(sampleId, resultSetConfig,
                onlyDirectlyConnected, callback);
    }
}
