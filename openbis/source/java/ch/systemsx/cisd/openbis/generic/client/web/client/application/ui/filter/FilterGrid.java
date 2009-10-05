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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter;

import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.FilterColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid displaying filters.
 * 
 * @author Izabela Adamczyk
 */
public class FilterGrid extends AbstractSimpleBrowserGrid<GridCustomFilter>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "filter-browser";

    private static final String GRID_SUFFIX = "_grid";

    private final String gridId;

    private final List<ColumnDataModel> columnModels;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String gridId,
            List<ColumnDataModel> columnModels)
    {
        final FilterGrid grid = new FilterGrid(viewContext, gridId, columnModels);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private FilterGrid(IViewContext<ICommonClientServiceAsync> viewContext, String gridId,
            List<ColumnDataModel> columnModels)
    {
        super(viewContext, createBrowserId(gridId), createGridId(gridId),
                DisplayTypeIDGenerator.FILTER_BROWSER_GRID);
        this.gridId = gridId;
        this.columnModels = columnModels;
    }

    public static final String createAddButtonId(String gridId)
    {
        return createGridId(gridId) + "_ADD_BUTTON";
    }

    public static final String createGridId(String gridId)
    {
        return createBrowserId(gridId) + GRID_SUFFIX;
    }

    private static final String createBrowserId(String gridId)
    {
        return BROWSER_ID + (gridId != null ? ("_" + gridId) : "");
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();
        final Button addButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Filter"),
                        new SelectionListener<ComponentEvent>()
                            {
                                @Override
                                public void componentSelected(ComponentEvent ce)
                                {
                                    createAddDialog().show();
                                }

                            });
        addButton.setId(createAddButtonId(gridId));
        addButton(addButton);
        final Button editButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                        new ISelectedEntityInvoker<BaseEntityModel<GridCustomFilter>>()
                            {
                                public void invoke(BaseEntityModel<GridCustomFilter> selectedItem)
                                {
                                    final GridCustomFilter filter = selectedItem.getBaseObject();
                                    createEditDialog(filter).show();
                                }

                            });
        addButton(editButton);
        Button deleteButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(List<GridCustomFilter> filters,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new FilterListDeletionConfirmationDialog(viewContext,
                                            filters, createDeletionCallback(invoker));
                                }
                            });
        addButton(deleteButton);
        allowMultipleSelection();
        addEntityOperationsSeparator();
    }

    private Window createAddDialog()
    {
        return new AddFilterDialog(viewContext, createRefreshGridAction(), gridId, columnModels);
    }

    private Window createEditDialog(GridCustomFilter filter)
    {
        return new EditFilterDialog(viewContext, createRefreshGridAction(), gridId, columnModels,
                filter);
    }

    @Override
    protected IColumnDefinitionKind<GridCustomFilter>[] getStaticColumnsDefinition()
    {
        return FilterColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<GridCustomFilter> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<GridCustomFilter> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(FilterColDefKind.DESCRIPTION.id(),
                createMultilineStringCellRenderer());
        schema.setGridCellRendererFor(FilterColDefKind.EXPRESSION.id(),
                createMultilineStringCellRenderer());
        return schema;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, GridCustomFilter> resultSetConfig,
            AbstractAsyncCallback<ResultSet<GridCustomFilter>> callback)
    {
        viewContext.getService().listFilters(gridId, resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<GridCustomFilter> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportFilters(exportCriteria, callback);
    }

    @Override
    protected List<IColumnDefinition<GridCustomFilter>> getInitialFilters()
    {
        return asColumnFilters(new FilterColDefKind[]
            { FilterColDefKind.NAME, FilterColDefKind.PUBLIC });
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.GRID_CUSTOM_FILTER),
                    DatabaseModificationKind.edit(ObjectKind.GRID_CUSTOM_FILTER) };
    }

    // it would be better to implement AbstractDataListDeletionConfirmationDialog with 'reason'
    public class FilterListDeletionConfirmationDialog extends
            AbstractDataConfirmationDialog<List<GridCustomFilter>>
    {
        private static final int LABEL_WIDTH = 60;

        private static final int FIELD_WIDTH = 180;

        private final AbstractAsyncCallback<Void> callback;

        public FilterListDeletionConfirmationDialog(IMessageProvider messageProvider,
                List<GridCustomFilter> data, AbstractAsyncCallback<Void> callback)
        {
            super(messageProvider, data, messageProvider.getMessage(Dict.DELETE_CONFIRMATION_TITLE));
            this.callback = callback;
        }

        @Override
        protected void extendForm()
        {
            formPanel.setLabelWidth(LABEL_WIDTH);
            formPanel.setFieldWidth(FIELD_WIDTH);
        }

        @Override
        protected String createMessage()
        {
            return "Do you really want to delete selected (" + data.size() + ") filter(s)?";
        }

        @Override
        protected void executeConfirmedAction()
        {
            viewContext.getCommonService().deleteFilters(TechId.createList(data), callback);
        }

    }

}
