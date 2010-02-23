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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IReportInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Constants;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Piotr Buczek
 */
public class QueryViewer extends ContentPanel implements IDatabaseModificationObserver
{

    public static DatabaseModificationAwareComponent create(
            IViewContext<IQueryClientServiceAsync> viewContext, AbstractCustomQueryToolbar toolbar)
    {
        final QueryViewer panel = new QueryViewer(viewContext, toolbar);
        return new DatabaseModificationAwareComponent(panel, panel);
    }

    public static final String ID = Constants.QUERY_ID_PREFIX + "_custom-query-viewer";

    private final IViewContext<IQueryClientServiceAsync> viewContext;

    private Component currentGridOrNull;

    private IQueryProvider queryProvider;

    private QueryViewer(IViewContext<IQueryClientServiceAsync> viewContext,
            AbstractCustomQueryToolbar toolBar)
    {
        this.viewContext = viewContext;
        this.queryProvider = toolBar;
        setHeaderVisible(false);
        setCollapsible(false);
        setAnimCollapse(false);
        setBodyBorder(true);
        setTopComponent(toolBar);
        toolBar.setRefreshViewerAction(new IDelegatedAction()
            {
                public void execute()
                {
                    refresh();
                }
            });
        setLayout(new FitLayout());
    }

    @Override
    protected void onRender(Element parent, int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    private void refresh()
    {
        Long queryIdOrNull = queryProvider.tryGetQueryId();
        String sqlQueryOrNull = queryProvider.tryGetSQLQuery();
        QueryParameterBindings bindingsOrNull = queryProvider.tryGetQueryParameterBindings();
        if (queryIdOrNull == null && sqlQueryOrNull == null)
        {
            return;
        }
        ReportGeneratedCallback callback =
                new ReportGeneratedCallback(viewContext.getCommonViewContext(),
                        createReportInformationProvider(sqlQueryOrNull),
                        createDisplayQueryResultsAction());
        if (queryIdOrNull != null)
        {
            viewContext.getService().createQueryResultsReport(new TechId(queryIdOrNull),
                    bindingsOrNull, callback);
        } else if (sqlQueryOrNull != null)
        {
            viewContext.getService().createQueryResultsReport(sqlQueryOrNull, bindingsOrNull,
                    callback);
        }
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return queryProvider.getRelevantModifications();
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        queryProvider.update(observedModifications);
    }

    private IReportInformationProvider createReportInformationProvider(final String sqlQuery)
    {
        return new IReportInformationProvider()
            {

                public String getDownloadURL()
                {
                    return null;
                }

                public String getKey()
                {
                    return Integer.toString(sqlQuery.hashCode());
                }

            };
    }

    private IOnReportComponentGeneratedAction createDisplayQueryResultsAction()
    {
        return new IOnReportComponentGeneratedAction()
            {
                public void execute(final IDisposableComponent reportComponent)
                {
                    if (currentGridOrNull != null)
                    {
                        remove(currentGridOrNull);
                    }
                    currentGridOrNull = reportComponent.getComponent();
                    add(currentGridOrNull);
                    layout();
                }
            };
    }

}
