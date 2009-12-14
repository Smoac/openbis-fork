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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.ProgressBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * @author Tomasz Pylak
 */
public class DataSetReportGenerator
{
    /** Generates a report for specified datasets and displays it in a new tab. */
    public static void generate(DatastoreServiceDescription service,
            DisplayedOrSelectedDatasetCriteria criteria,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        ReportDisplayCallback callback = new ReportDisplayCallback(viewContext, service);
        viewContext.getService().createReportFromDatasets(service, criteria, callback);
    }

    private static final class ReportDisplayCallback extends
            AbstractAsyncCallback<TableModelReference>
    {
        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final Dialog progressBar;

        private final DatastoreServiceDescription service;

        public ReportDisplayCallback(IViewContext<ICommonClientServiceAsync> viewContext,
                DatastoreServiceDescription service)
        {
            super(viewContext);
            this.viewContext = viewContext;
            this.progressBar = createAndShowProgressBar();
            this.service = service;
        }

        @Override
        protected void process(final TableModelReference tableModelReference)
        {
            progressBar.hide();
            final ITabItemFactory tabFactory = new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        IDisposableComponent component =
                                DataSetReporterGrid.create(viewContext, tableModelReference,
                                        service);
                        String reportTitle = service.getLabel();
                        return DefaultTabItem.create(reportTitle, component, viewContext);
                    }

                    public String getId()
                    {
                        return DataSetReporterGrid.createId(tableModelReference.getResultSetKey());
                    }
                };
            DispatcherHelper.dispatchNaviEvent(tabFactory);
        }

        @Override
        public void finishOnFailure(Throwable caught)
        {
            progressBar.hide();
            super.finishOnFailure(caught);
        }
    }

    private static Dialog createAndShowProgressBar()
    {
        ProgressBar progressBar = new ProgressBar();
        progressBar.auto();

        Dialog dialog = new Dialog();
        String title = "Generating the report...";
        dialog.setTitle(title);

        dialog.add(progressBar);
        dialog.setButtons("");
        dialog.setAutoHeight(true);
        dialog.setClosable(false);
        dialog.addText(title);
        dialog.setResizable(false);
        dialog.show();
        return dialog;
    }
}
