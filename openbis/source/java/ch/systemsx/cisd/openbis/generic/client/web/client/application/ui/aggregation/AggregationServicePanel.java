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
import java.util.Map;
import java.util.Map.Entry;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;

/**
 * A panel that shows the results of an aggregation service.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AggregationServicePanel extends ContentPanel
{
    static final String SERVICE_KEY_PARAM = "serviceKey";

    private static final String DSS_CODE_PARAM = "dss";

    private static final String DISPLAY_SETTINGS_ID = "displaySettingsId";

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
    }

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final ViewLocator viewLocator;

    private final String serviceKey;

    private final String dataStoreCode;

    private final String displaySettingsId;

    public AggregationServicePanel(IViewContext<ICommonClientServiceAsync> viewContext,
            String idPrefix, ViewLocator viewLocator)
    {
        super(new FitLayout());
        setId(idPrefix + "aggregation_service");
        this.viewContext = viewContext;
        this.viewLocator = viewLocator;
        serviceKey = viewLocator.getParameters().get(SERVICE_KEY_PARAM);
        dataStoreCode = viewLocator.getParameters().get(DSS_CODE_PARAM);
        displaySettingsId = viewLocator.getParameters().get(DISPLAY_SETTINGS_ID);

        if (areRequiredParametersSpecified())
        {
            // All ivars must be initialized before the aggregation service is called
            callAggregationService();
        } else
        {
            showErrorPage();
        }
    }

    private void showErrorPage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Missing Required Parameters</h1>\n");
        sb.append("<p>");
        if (null == serviceKey && null == dataStoreCode)
        {
            sb.append("The aggregation service and data store code must be specified in the URL query parameters. E.g:");

        } else if (null == serviceKey)
        {
            sb.append("The aggregation service must be specified in the URL query parameters. E.g:");
        } else
        {
            sb.append("The data store code must be specified in the URL query parameters. E.g:");
        }

        sb.append("<blockquote>");
        // Append the inital part of the URL to this openBIS instance
        sb.append(Window.Location.getProtocol());
        sb.append("//");
        sb.append(Window.Location.getHost());
        sb.append("?viewMode=EMBEDDED#action=");
        sb.append(AggregationServiceLocatorResolver.ACTION);
        sb.append("serviceKey=[service key]&dss=[dss code]</blockquote>");
        sb.append("</p>");
        HTML content = new HTML(sb.toString());
        add(content);
        layout();
    }

    private boolean areRequiredParametersSpecified()
    {
        return serviceKey != null && dataStoreCode != null;
    }

    private void callAggregationService()
    {
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();
        // Add all the remaining parameters to the parameter map
        Map<String, String> urlParameters = viewLocator.getParameters();
        for (Entry<String, String> entry : urlParameters.entrySet())
        {
            if (SERVICE_KEY_PARAM.equals(entry.getKey()))
            {
                continue;
            }
            if (DSS_CODE_PARAM.equals(entry.getKey()))
            {
                continue;
            }
            if (DISPLAY_SETTINGS_ID.equals(entry.getKey()))
            {
                continue;
            }
            parameterMap.put(entry.getKey(), entry.getValue());
        }
        DatastoreServiceDescription description =
                DatastoreServiceDescription.reporting(serviceKey, "", new String[0], dataStoreCode,
                        ReportingPluginType.AGGREGATION_TABLE_MODEL);

        String notNullDisplaySettingsId =
                displaySettingsId != null ? displaySettingsId : serviceKey;

        AsyncCallback<TableModelReference> callback =
                ReportGeneratedCallback.create(viewContext, description, notNullDisplaySettingsId,
                        new AggregationServiceGeneratedAction(this));

        viewContext.getCommonService().createReportFromAggregationService(description,
                parameterMap, callback);
    }
}
