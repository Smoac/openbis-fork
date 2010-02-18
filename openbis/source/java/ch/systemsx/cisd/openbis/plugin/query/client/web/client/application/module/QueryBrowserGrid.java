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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Constants;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.ui.columns.QueryColDefKind;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class QueryBrowserGrid extends AbstractSimpleBrowserGrid<GridCustomFilter>
{
    private static final String BROWSER_ID = Constants.QUERY_ID_PREFIX + "queries_browser";
    private static final String GRID_ID = BROWSER_ID + "_grid";
    
    private static class DeletionConfirmationDialog extends
            AbstractDataConfirmationDialog<List<GridCustomFilter>>
    {
        private static final int LABEL_WIDTH = 60;

        private static final int FIELD_WIDTH = 180;

        private final IViewContext<IQueryClientServiceAsync> viewContext;

        private final AbstractAsyncCallback<Void> callback;

        public DeletionConfirmationDialog(IViewContext<IQueryClientServiceAsync> viewContext,
                List<GridCustomFilter> data, AbstractAsyncCallback<Void> callback)
        {
            super(viewContext, data, viewContext.getMessage(Dict.DELETE_CONFIRMATION_TITLE));
            this.callback = callback;
            this.viewContext = viewContext;
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
            List<String> names = new ArrayList<String>();
            for (GridCustomFilter query : data)
            {
                names.add(query.getName());
            }
            return viewContext.getMessage(Dict.QUERY_DELETION_CONFIRMATION, names);
        }

        @Override
        protected void executeConfirmedAction()
        {
            viewContext.getService().deleteQueries(TechId.createList(data), callback);
        }

    }

    public static DatabaseModificationAwareComponent create(
            IViewContext<IQueryClientServiceAsync> viewContext)
    {
        QueryBrowserGrid browser = new QueryBrowserGrid(viewContext);
        return new DatabaseModificationAwareComponent(browser, browser);
    }
    
    private final IViewContext<IQueryClientServiceAsync> viewContext;
    
    QueryBrowserGrid(IViewContext<IQueryClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.QUERY_EDITOR);
        this.viewContext = viewContext;
        extendBottomToolbar();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();
        final Button addButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD_QUERY),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    new QueryEditor(viewContext, createRefreshGridAction()).show();
                                }

                            });
        addButton(addButton);
        final Button editButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                        new ISelectedEntityInvoker<BaseEntityModel<GridCustomFilter>>()
                            {
                                public void invoke(BaseEntityModel<GridCustomFilter> selectedItem)
                                {
                                    System.out.println("edit");
                                }

                            });
        addButton(editButton);
        Button deleteButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(List<GridCustomFilter> selected,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new DeletionConfirmationDialog(viewContext, selected,
                                            createDeletionCallback(invoker));
                                }
                            });
        addButton(deleteButton);
        allowMultipleSelection();
        addEntityOperationsSeparator();
    }

    @Override
    protected IColumnDefinitionKind<GridCustomFilter>[] getStaticColumnsDefinition()
    {
        return QueryColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<GridCustomFilter>> getInitialFilters()
    {
        return asColumnFilters(new QueryColDefKind[]
            { QueryColDefKind.NAME, QueryColDefKind.PUBLIC });
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, GridCustomFilter> resultSetConfig,
            AbstractAsyncCallback<ResultSet<GridCustomFilter>> callback)
    {
        viewContext.getService().listQueries(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<GridCustomFilter> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportQueries(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        // TODO Auto-generated method stub
        return new DatabaseModificationKind[] {};
    }
}
