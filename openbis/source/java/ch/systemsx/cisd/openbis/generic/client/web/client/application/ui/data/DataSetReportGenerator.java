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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * @author Tomasz Pylak
 * @auhor Piotr Buczek
 */
public class DataSetReportGenerator
{

    public interface IOnReportComponentGeneratedAction
    {
        void execute(final IDisposableComponent reportComponent);
    }

    /** Generates a report for specified datasets and displays it in a new tab. */
    public static void generate(final IViewContext<ICommonClientServiceAsync> viewContext,
            final DatastoreServiceDescription service,
            final DisplayedOrSelectedDatasetCriteria criteria)
    {
        IOnReportComponentGeneratedAction action =
                createDisplayInTabAction(viewContext, service, criteria);
        generateAndInvoke(viewContext, service, criteria, action);
    }

    /**
     * Generates a report for specified datasets and invokes specified action with component
     * containing this report.
     */
    public static void generateAndInvoke(IViewContext<ICommonClientServiceAsync> viewContext,
            DatastoreServiceDescription service, DisplayedOrSelectedDatasetCriteria criteria,
            IOnReportComponentGeneratedAction action)
    {
        ReportGeneratedCallback callback =
                new ReportGeneratedCallback(viewContext, service, action);
        viewContext.getService().createReportFromDatasets(service, criteria, callback);
    }

    private static IOnReportComponentGeneratedAction createDisplayInTabAction(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final DatastoreServiceDescription service,
            final DisplayedOrSelectedDatasetCriteria criteria)
    {
        return new IOnReportComponentGeneratedAction()
            {

                public void execute(final IDisposableComponent reportComponent)
                {
                    final ITabItemFactory tabFactory = new ITabItemFactory()
                        {
                            public ITabItem create()
                            {
                                final String reportTitle = service.getLabel();
                                return DefaultTabItem.create(reportTitle, reportComponent,
                                        viewContext);
                            }

                            public String getId()
                            {
                                final String reportKey = service.getKey();
                                return DataSetReporterGrid.createId(reportKey);
                            }

                            public HelpPageIdentifier getHelpPageIdentifier()
                            {
                                return new HelpPageIdentifier(HelpPageDomain.DATA_SET,
                                        HelpPageAction.REPORT);
                            }
                        };
                    DispatcherHelper.dispatchNaviEvent(tabFactory);
                }

            };
    }

    private static class ReportGeneratedCallback extends AbstractAsyncCallback<TableModelReference>
    {
        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final Dialog progressBar;

        private final DatastoreServiceDescription service;

        private final IOnReportComponentGeneratedAction action;

        public ReportGeneratedCallback(IViewContext<ICommonClientServiceAsync> viewContext,
                DatastoreServiceDescription service, IOnReportComponentGeneratedAction action)
        {
            super(viewContext);
            this.viewContext = viewContext;
            this.service = service;
            this.action = action;
            this.progressBar = createAndShowProgressBar();
        }

        @Override
        protected void process(final TableModelReference tableModelReference)
        {
            progressBar.hide();
            final IDisposableComponent reportComponent =
                    DataSetReporterGrid.create(viewContext, tableModelReference, service);
            action.execute(reportComponent);
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
        GWTUtils.setToolTip(dialog, title);

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
