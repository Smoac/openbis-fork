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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.ProgressBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.IReportInformationProvider;

/**
 * @author Piotr Buczek
 */
public class ReportGeneratedCallback extends AbstractAsyncCallback<TableModelReference>
{
    public interface IOnReportComponentGeneratedAction
    {
        void execute(final IDisposableComponent reportComponent);
    }

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final Dialog progressBar;

    private final IOnReportComponentGeneratedAction action;

    private final IReportInformationProvider reportInformationProvider;

    public ReportGeneratedCallback(IViewContext<ICommonClientServiceAsync> viewContext,
            IReportInformationProvider reportInformationProvider,
            IOnReportComponentGeneratedAction action)
    {
        super(viewContext);
        this.viewContext = viewContext;
        this.reportInformationProvider = reportInformationProvider;
        this.action = action;
        this.progressBar = createAndShowProgressBar();
    }

    @Override
    protected void process(final TableModelReference tableModelReference)
    {
        progressBar.hide();
        final IDisposableComponent reportComponent =
                ReportGrid.create(viewContext, tableModelReference, reportInformationProvider);
        action.execute(reportComponent);
    }

    @Override
    public void finishOnFailure(Throwable caught)
    {
        progressBar.hide();
        super.finishOnFailure(caught);
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
