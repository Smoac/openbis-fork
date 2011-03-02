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

import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetComputeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetProcessingMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetReportGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DatastoreServiceDescriptionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ReportingPluginSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * @author Piotr Buczek
 */
public abstract class AbstractDataSetsSection extends DisposableTabContent
{
    protected final ReportingPluginSelectionWidget reportSelectionWidget;

    protected final IOnReportComponentGeneratedAction reportGeneratedAction;

    private IDisposableComponent metadataComponent;

    public AbstractDataSetsSection(final String header, final IViewContext<?> viewContext,
            IIdHolder ownerIdOrNull)
    {
        super(header, viewContext, ownerIdOrNull);
        this.reportSelectionWidget = new ReportingPluginSelectionWidget(viewContext, ownerIdOrNull);
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

    protected void initWidgets(AbstractExternalDataGrid browser)
    {
        getHeader().addTool(reportSelectionWidget);
        if (viewContext.isSimpleMode() == false)
        {
            // processing plugins should be hidden in simple view mode
            viewContext.getCommonService().listDataStoreServices(DataStoreServiceKind.PROCESSING,
                    new LoadProcessingPluginsCallback(viewContext, browser));
        }
    }

    @Override
    protected final IDisposableComponent createDisposableContent()
    {
        metadataComponent = createDatasetBrowserComponent();
        initWidgets(extractBrowser(metadataComponent));

        SelectionChangedListener<DatastoreServiceDescriptionModel> reportChangedListener =
                createReportSelectionChangedListener(viewContext, metadataComponent,
                        reportGeneratedAction);
        reportSelectionWidget.addSelectionChangedListener(reportChangedListener);
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
        if (metadataComponent != null)
        {
            metadataComponent.dispose(); // NOTE: second dispose on a grid does nothing
        }
    }

    private static AbstractExternalDataGrid extractBrowser(IDisposableComponent metadataComponent)
    {
        return (AbstractExternalDataGrid) metadataComponent.getComponent();
    }

    private static SelectionChangedListener<DatastoreServiceDescriptionModel> createReportSelectionChangedListener(
            final IViewContext<?> viewContext, final IDisposableComponent metadataComponent,
            final IOnReportComponentGeneratedAction reportGeneratedAction)
    {
        final AbstractExternalDataGrid browser = extractBrowser(metadataComponent);
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
                            showGeneratedReport(service);
                        }
                    }

                }

                private void showMetadataView()
                {
                    reportGeneratedAction.execute(metadataComponent);
                }

                private void showGeneratedReport(DatastoreServiceDescription service)
                {
                    assert service.getServiceKind() == DataStoreServiceKind.QUERIES;

                    IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedAndDisplayedItemsAction =
                            browser.getSelectedAndDisplayedItemsAction();

                    if (browser.getSelectedItems().isEmpty())
                    {
                        // when no data sets were selected perform query on all without asking
                        DisplayedOrSelectedDatasetCriteria criteria =
                                selectedAndDisplayedItemsAction.execute().createCriteria(false);
                        DataSetReportGenerator.generateAndInvoke(
                                viewContext.getCommonViewContext(), service, criteria,
                                reportGeneratedAction);
                    } else
                    {
                        DataSetComputeUtils.createComputeAction(viewContext.getCommonViewContext(),
                                selectedAndDisplayedItemsAction, service, reportGeneratedAction)
                                .execute();
                    }
                }

            };

    }

    public final class LoadProcessingPluginsCallback extends
            AbstractAsyncCallback<List<DatastoreServiceDescription>>
    {
        private final AbstractExternalDataGrid browser;

        public LoadProcessingPluginsCallback(final IViewContext<?> viewContext,
                AbstractExternalDataGrid browser)
        {
            super(viewContext);
            this.browser = browser;
        }

        @Override
        protected void process(List<DatastoreServiceDescription> result)
        {
            if (result.isEmpty() == false)
            {

                DataSetProcessingMenu menu =
                        new DataSetProcessingMenu(viewContext.getCommonViewContext(),
                                browser.getSelectedAndDisplayedItemsAction(), result);
                getHeader().addTool(new SeparatorToolItem());
                getHeader().addTool(menu);
            }
        }
    }

}
