/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.aggregation;

import java.util.HashMap;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A panel that shows the results of an aggregation service.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AggregationServicePanel extends ContentPanel
{
    private static final String SERVICE_KEY_PARAM = "serviceKey";

    private static final String DSS_CODE_PARAM = "dss";

    private static class AggregationServiceGeneratedAction implements
            IOnReportComponentGeneratedAction
    {
        private final LayoutContainer layoutContainer;

        private AggregationServiceGeneratedAction(LayoutContainer layoutContainer)
        {
            this.layoutContainer = layoutContainer;
        }

        @Override
        public void execute(final IDisposableComponent reportComponent)
        {
            layoutContainer.removeAll();
            layoutContainer.add(reportComponent.getComponent());
            layoutContainer.layout();
        }
    };

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final ViewLocator viewLocator;

    public AggregationServicePanel(IViewContext<ICommonClientServiceAsync> viewContext,
            String idPrefix, ViewLocator viewLocator)
    {
        super(new FitLayout());
        setId(idPrefix + "aggregation_service");
        this.viewContext = viewContext;
        this.viewLocator = viewLocator;
        callAggregationService();
    }

    protected void callAggregationService()
    {
        String serviceKey = viewLocator.getParameters().get(SERVICE_KEY_PARAM);
        String dataStoreCode = viewLocator.getParameters().get(DSS_CODE_PARAM);
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("name", "foo");
        DatastoreServiceDescription description =
                DatastoreServiceDescription.reporting(serviceKey, "", new String[0], dataStoreCode,
                        ReportingPluginType.AGGREGATION_TABLE_MODEL);

        AsyncCallback<TableModelReference> callback =
                ReportGeneratedCallback.create(viewContext, description,
                        new AggregationServiceGeneratedAction(this));

        viewContext.getCommonService().createReportFromAggregationService(description,
                parameterMap, callback);
    }
}
