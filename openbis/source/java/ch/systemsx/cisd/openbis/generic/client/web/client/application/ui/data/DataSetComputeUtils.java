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

import com.extjs.gxt.ui.client.widget.Window;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * @author Piotr Buczek
 */
public class DataSetComputeUtils
{

    public static IDelegatedAction createComputeAction(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final SelectedAndDisplayedItems selectedAndDisplayedItems,
            final DatastoreServiceDescription service, final DataStoreServiceKind dssTaskKind,
            final IOnReportComponentGeneratedAction reportGeneratedAction)
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    final IComputationAction computationAction =
                            createComputationAction(viewContext, selectedAndDisplayedItems,
                                    dssTaskKind, reportGeneratedAction);
                    final ComputationData data =
                            new ComputationData(dssTaskKind, computationAction,
                                    selectedAndDisplayedItems);
                    createPerformComputationDialog(data).show();
                }

                private Window createPerformComputationDialog(ComputationData data)
                {
                    final String title = "Perform " + dssTaskKind.getDescription();
                    return new PerformComputationDialog(viewContext, data, title, service);
                }
            };
    }

    private static IComputationAction createComputationAction(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final SelectedAndDisplayedItems selectedAndDisplayedItems,
            final DataStoreServiceKind dssTaskKind,
            final IOnReportComponentGeneratedAction reportGeneratedAction)
    {
        return new IComputationAction()
            {
                public void execute(DatastoreServiceDescription service, boolean computeOnSelected)
                {
                    DisplayedOrSelectedDatasetCriteria criteria =
                            selectedAndDisplayedItems.createCriteria(computeOnSelected);
                    switch (dssTaskKind)
                    {
                        case QUERIES:
                            DataSetReportGenerator.generateAndInvoke(viewContext, service,
                                    criteria, reportGeneratedAction);
                            break;
                        case PROCESSING:
                            viewContext.getService().processDatasets(
                                    service,
                                    criteria,
                                    AsyncCallbackWithProgressBar.decorate(
                                            new ProcessingDisplayCallback(viewContext),
                                            "Scheduling processing..."));
                            break;
                    }
                }

            };
    }

}
