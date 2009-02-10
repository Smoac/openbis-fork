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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ToolBarEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataSetSearchHitModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetSearchHit;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteria;

/**
 * Grid with data set search results.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetSearchHitGrid extends
        AbstractSimpleBrowserGrid<DataSetSearchHit, DataSetSearchHitModel>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID =
            GenericConstants.ID_PREFIX + "data-set-search-hit-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    private final DataSetSearchWindow searchWindow;

    public static DisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        DataSetSearchHitGrid grid = new DataSetSearchHitGrid(viewContext);
        return grid.asDisposableWithToolbar(new DataSetSearchToolbar(grid, viewContext
                .getMessage(Dict.BUTTON_CHANGE_QUERY)));
    }

    private SearchCriteria criteria;

    private DataSetSearchHitGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
        searchWindow = new DataSetSearchWindow();

    }

    @Override
    protected IColumnDefinitionKind<DataSetSearchHit>[] getStaticColumnsDefinition()
    {
        return DataSetSearchHitColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<DataSetSearchHit>> getAvailableFilters()
    {
        return asColumnFilters(new DataSetSearchHitColDefKind[]
            { DataSetSearchHitColDefKind.CODE, DataSetSearchHitColDefKind.LOCATION });
    }

    @Override
    protected DataSetSearchHitModel createModel(DataSetSearchHit entity)
    {
        return new DataSetSearchHitModel(entity, getStaticColumnsDefinition());
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, DataSetSearchHit> resultSetConfig,
            AbstractAsyncCallback<ResultSet<DataSetSearchHit>> callback)
    {
        viewContext.getService().searchForDataSets(criteria, resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<DataSetSearchHit> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDataSetSearchHits(exportCriteria, callback);
    }

    private void refresh(SearchCriteria newCriteria)
    {
        criteria = newCriteria;
        refresh();
    }

    @Override
    protected void refresh()
    {
        if (criteria == null)
        {
            showSearchDialog();
            return;
        }
        super.refresh();
    }

    private void showSearchDialog()
    {
        searchWindow.show();
    }

    //
    // Helper classes
    //

    /**
     * Contains a button opening {@link DataSetSearchWindow}.
     */
    private static class DataSetSearchToolbar extends ToolBar
    {
        public DataSetSearchToolbar(final DataSetSearchHitGrid grid, String buttonName)
        {
            add(new FillToolItem());
            add(new SeparatorToolItem());
            add(new TextToolItem(buttonName, new SelectionListener<ToolBarEvent>()
                {
                    @Override
                    public void componentSelected(ToolBarEvent ce)
                    {
                        grid.showSearchDialog();
                    }
                }));
        }
    }

    /**
     * Shows {@link CriteriaWidget}, allowing to specify search criteria.
     * 
     * @author Izabela Adamczyk
     */
    private class DataSetSearchWindow extends Dialog
    {
        private static final int MARGIN = 5;

        private static final int HEIGHT = 400;

        private static final int WIDTH = 550;

        private CriteriaWidget criteriaWidget;

        public DataSetSearchWindow()
        {
            setSize(WIDTH, HEIGHT);
            setModal(true);
            setScrollMode(Scroll.AUTOY);
            setLayout(new FitLayout());
            setResizable(false);
            add(criteriaWidget = new CriteriaWidget(viewContext), new FitData(MARGIN));
            final ButtonBar bar = new ButtonBar();
            bar.add(new Button(viewContext.getMessage(Dict.BUTTON_CANCEL))
                {
                    @Override
                    protected void onClick(ComponentEvent ce)
                    {
                        super.onClick(ce);
                        DataSetSearchWindow.this.hide();
                    }
                });
            bar.add(new Button(viewContext.getMessage(Dict.BUTTON_RESET))
                {
                    @Override
                    protected void onClick(ComponentEvent ce)
                    {
                        criteriaWidget.reset();
                    }
                });
            bar.add(new Button(viewContext.getMessage(Dict.SEARCH_BUTTON))
                {
                    @Override
                    protected void onClick(ComponentEvent ce)
                    {
                        super.onClick(ce);
                        DataSetSearchHitGrid.this.refresh(criteriaWidget.tryGetCriteria());
                        DataSetSearchWindow.this.hide();
                    }
                });
            setButtonBar(bar);
            setButtons("");
        }
    }

}