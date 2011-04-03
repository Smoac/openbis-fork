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
 * 
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ShowResultSetCutInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.DetailedDataSetSearchToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.DetailedSearchToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.DetailedSearchWindow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.IDetailedSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Grid with detailed data set search results.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetSearchHitGrid extends AbstractExternalDataGrid implements
        IDetailedSearchHitGrid
{

    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX
            + "data-set-search-hit-browser";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    private static IDisposableComponent disposableComponentOrNull = null;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return create(viewContext, null);
    }

    public static DisposableEntityChooser<ExternalData> createWithInitialSearchCriteria(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            DetailedSearchCriteria searchCriteria)
    {
        return create(viewContext, searchCriteria);
    }

    private static DisposableEntityChooser<ExternalData> create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            DetailedSearchCriteria searchCriteriaOrNull)
    {
        DataSetSearchHitGrid grid = new DataSetSearchHitGrid(viewContext);
        final DetailedSearchWindow searchWindow =
                new DetailedSearchWindow(viewContext, EntityKind.DATA_SET);

        if (searchCriteriaOrNull != null)
        {
            grid.chosenSearchCriteria = searchCriteriaOrNull;
            // Set the initial search string before creating the toolbar
            // because the toolbar will use the initial search string in its own initialization.
            searchWindow.setInitialSearchCriteria(searchCriteriaOrNull);
        }

        final LayoutContainer container = createContainer();
        final IOnReportComponentGeneratedAction reportGeneratedAction =
                createReportGeneratedAction(container, grid);
        final ReportingPluginSelectionWidget reportSelectionWidget =
                new ReportingPluginSelectionWidget(viewContext, null);

        SelectionChangedListener<DatastoreServiceDescriptionModel> reportChangedListener =
                DataSetGridUtils.createReportSelectionChangedListener(viewContext,
                        grid.asDisposableWithoutToolbar(), reportGeneratedAction);
        reportSelectionWidget.addSelectionChangedListener(reportChangedListener);

        final DetailedSearchToolbar toolbar =
                new DetailedDataSetSearchToolbar(viewContext, grid,
                        viewContext.getMessage(Dict.BUTTON_CHANGE_QUERY), searchWindow,
                        reportSelectionWidget, searchCriteriaOrNull != null);
        searchWindow.setUpdateListener(toolbar);

        return grid.asDisposableWithToolbar(container, toolbar);
    }

    private static LayoutContainer createContainer()
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        return container;
    }

    private static IOnReportComponentGeneratedAction createReportGeneratedAction(
            final LayoutContainer container, final DataSetSearchHitGrid grid)
    {
        return new IOnReportComponentGeneratedAction()
            {
                public void execute(IDisposableComponent gridComponent)
                {
                    if (gridComponent != null)
                    {
                        // remove second widget (first is the toolbar)
                        Widget widget = container.getWidget(1);
                        container.remove(widget);

                        // dispose if it wasn't the main grid component
                        if (disposableComponentOrNull != null
                                && disposableComponentOrNull.getComponent().equals(grid) == false)
                        {
                            disposableComponentOrNull.dispose();
                        }

                        // update new component
                        disposableComponentOrNull = gridComponent;
                        container.add(gridComponent.getComponent(), new RowData(1, 1));
                        container.layout();
                    }
                }
            };
    }

    private DetailedSearchCriteria chosenSearchCriteria;

    private DataSetSearchHitGrid(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.DATA_SET_SEARCH_RESULT_GRID);

    }

    @Override
    protected void listDatasets(DefaultResultSetConfig<String, ExternalData> resultSetConfig,
            final AbstractAsyncCallback<ResultSetWithEntityTypes<ExternalData>> callback)
    {
        callback.addOnSuccessAction(new ShowResultSetCutInfo<ResultSetWithEntityTypes<ExternalData>>(
                viewContext));
        viewContext.getService().searchForDataSets(chosenSearchCriteria, resultSetConfig, callback);
    }

    public void refresh(DetailedSearchCriteria newCriteria, List<PropertyType> propertyTypes)
    {
        chosenSearchCriteria = newCriteria;
        if (criteria != null)
        {
            criteria.setPropertyTypes(propertyTypes);
        }
        refresh();
    }

    @Override
    protected void refresh()
    {
        if (chosenSearchCriteria == null)
        {
            return;
        }
        super.refresh();
    }

    @Override
    protected ColumnDefsAndConfigs<ExternalData> createColumnsSchema()
    {
        List<PropertyType> propertyTypes = criteria == null ? null : criteria.tryGetPropertyTypes();
        return DataSetSearchHitModel.createColumnsSchema(viewContext, propertyTypes);
    }

    /** @return this grid as a disposable component with a specified toolbar at the top. */
    private DisposableEntityChooser<ExternalData> asDisposableWithToolbar(
            final LayoutContainer container, final IDisposableComponent toolbar)
    {
        container.add(toolbar.getComponent());
        container.add(this, new RowData(1, 1));

        // TODO 2011-03-10, Piotr Buczek: dispose report
        return asDisposableEntityChooser(container, toolbar);
    }

}
