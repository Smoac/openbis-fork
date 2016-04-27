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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DeletionGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid displaying deletions.
 * 
 * @author Piotr Buczek
 */
public class DeletionGrid extends TypedTableGrid<Deletion>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "deletion-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final DeletionGrid grid = new DeletionGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private DeletionGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.DELETION_BROWSER_GRID);
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();
        final Button revertButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_REVERT_DELETION),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(
                                        List<TableModelRowWithObject<Deletion>> data,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    List<Deletion> deletions = new ArrayList<Deletion>();
                                    for (TableModelRowWithObject<Deletion> row : data)
                                    {
                                        deletions.add(row.getObjectOrNull());
                                    }
                                    return new RevertDeletionConfirmationDialog(viewContext,
                                            deletions, createRefreshCallback(invoker));
                                }
                            });

        addButton(revertButton);

        EmptyTrashButton emptyTrashButtonMenu =
                new EmptyTrashButton(viewContext, createRefreshCallback(asActionInvoker()));
        emptyTrashButtonMenu.setId("empty-trash-button");
        addButton(emptyTrashButtonMenu);

        final Button deletePermanentlyButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE_PERMANENTLY),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(
                                        List<TableModelRowWithObject<Deletion>> data,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    List<Deletion> deletions = new ArrayList<Deletion>();
                                    for (TableModelRowWithObject<Deletion> row : data)
                                    {
                                        deletions.add(row.getObjectOrNull());
                                    }
                                    return new PermanentDeletionConfirmationDialog(viewContext,
                                            deletions, createRefreshCallback(invoker));
                                }
                            });

        addButton(deletePermanentlyButton);
        allowMultipleSelection(); // we allow revert of multiple deletions
        addGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
            {
                @Override
                public void handleEvent(SelectionChangedEvent<ModelData> be)
                {
                    deletePermanentlyButton.setEnabled(be.getSelection().size() == 1);
                }
            });

        addEntityOperationsSeparator();
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<Deletion>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<Deletion>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(DeletionGridColumnIDs.ENTITIES,
                createMultilineStringCellRenderer());
        schema.setGridCellRendererFor(DeletionGridColumnIDs.REASON,
                createMultilineStringCellRenderer());
        schema.setGridCellRendererFor(DeletionGridColumnIDs.DELETER,
                PersonRenderer.REGISTRATOR_RENDERER);
        return schema;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Deletion>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Deletion>> callback)
    {
        viewContext.getService().listDeletions(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Deletion>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDeletions(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(DeletionGridColumnIDs.DELETER, DeletionGridColumnIDs.REASON);
    }

    @Override
    protected void showEntityViewer(final TableModelRowWithObject<Deletion> row, boolean editMode,
            boolean inBackground)
    {
        throw new UserFailureException("Operation is not supported.");
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { DatabaseModificationKind.createOrDelete(ObjectKind.DELETION) };
    }
}
