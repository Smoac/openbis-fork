/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.EntityGridModelFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DisplayedAndSelectedEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.material.CommonMaterialColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * A {@link LayoutContainer} which contains the grid where the materials are displayed.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialBrowserGrid extends
        AbstractEntityBrowserGrid<Material, BaseEntityModel<Material>, ListMaterialDisplayCriteria>
{
    private static final String PREFIX = "material-browser";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String GRID_ID = BROWSER_ID + "_grid";

    /**
     * Creates a browser with a toolbar which allows to choose the material type. Allows to show or
     * edit material details.
     * 
     * @param initialMaterialTypeOrNull
     */
    public static DisposableEntityChooser<Material> createWithTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            String initialMaterialTypeOrNull)
    {
        return createWithTypeChooser(viewContext, true, initialMaterialTypeOrNull);
    }

    private static DisposableEntityChooser<Material> createWithTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, boolean detailsAvailable,
            String initialMaterialTypeOrNull)
    {
        final MaterialBrowserToolbar toolbar =
                new MaterialBrowserToolbar(viewContext, initialMaterialTypeOrNull, null);
        final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider = toolbar;
        final MaterialBrowserGrid browserGrid =
                createBrowserGrid(viewContext, criteriaProvider, detailsAvailable);
        browserGrid.addGridRefreshListener(toolbar);
        browserGrid.extendBottomToolbar(detailsAvailable);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    /**
     * If the material type is given, does not show the toolbar with material type selection and
     * refreshes the grid automatically.<br>
     * Does not allow to show or edit the material details.
     */
    public static DisposableEntityChooser<Material> create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final MaterialType initValueOrNull)
    {
        if (initValueOrNull == null)
        {
            return createWithTypeChooser(viewContext, false, null);
        } else
        {
            return createWithoutTypeChooser(viewContext, initValueOrNull);
        }
    }

    private static DisposableEntityChooser<Material> createWithoutTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, final MaterialType initValue)
    {
        final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider =
                createUnrefreshableCriteriaProvider(ListMaterialDisplayCriteria
                        .createForMaterialType(initValue));
        boolean detailsAvailable = false;
        final MaterialBrowserGrid browserGrid =
                createBrowserGrid(viewContext, criteriaProvider, detailsAvailable);
        return browserGrid.asDisposableWithoutToolbar();
    }

    private static MaterialBrowserGrid createBrowserGrid(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider,
            boolean detailsAvailable)
    {
        if (detailsAvailable)
        {
            return new MaterialBrowserGrid(viewContext, true, criteriaProvider);
        } else
        {
            return new MaterialBrowserGrid(viewContext, true, criteriaProvider)
                {
                    @Override
                    protected void showEntityViewer(Material material, boolean editMode,
                            boolean active)
                    {
                        // do nothing - avoid showing the details after double click
                    }
                };
        }

    }

    protected final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider;

    protected MaterialBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            boolean refreshAutomatically,
            ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider)
    {
        super(viewContext, GRID_ID, refreshAutomatically,
                DisplayTypeIDGenerator.ENTITY_BROWSER_GRID);
        this.criteriaProvider = criteriaProvider;
        setId(BROWSER_ID);
    }

    @Override
    protected ICriteriaProvider<ListMaterialDisplayCriteria> getCriteriaProvider()
    {
        return criteriaProvider;
    }

    protected void extendBottomToolbar(boolean detailsAvailable)
    {
        if (detailsAvailable && viewContext.isSimpleOrEmbeddedMode() == false)
        {
            addEntityOperationsLabel();
            addEntityOperationButtons();
            addEntityOperationsSeparator();
        }
    }

    private void addEntityOperationButtons()
    {
        String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
        Button showDetailsButton =
                createSelectedItemButton(showDetailsTitle, asShowEntityInvoker(false));
        addButton(showDetailsButton);

        String editTitle = viewContext.getMessage(Dict.BUTTON_EDIT);
        Button editButton = createSelectedItemButton(editTitle, asShowEntityInvoker(true));
        addButton(editButton);

        final String deleteTitle = viewContext.getMessage(Dict.BUTTON_DELETE);
        final String deleteAllTitle = deleteTitle + " All";
        final Button deleteButton = new Button(deleteAllTitle, new AbstractCreateDialogListener()
            {
                @Override
                protected Dialog createDialog(List<Material> materials,
                        IBrowserGridActionInvoker invoker)
                {
                    return new MaterialListDeletionConfirmationDialog(viewContext, materials,
                            createRefreshCallback(invoker), getDisplayedAndSelectedItemsAction()
                                    .execute());
                }
            });
        changeButtonTitleOnSelectedItems(deleteButton, deleteAllTitle, deleteTitle);
        addButton(deleteButton);
        allowMultipleSelection(); // we allow deletion of multiple materials
    }

    protected void addGridRefreshListener(MaterialBrowserToolbar toolbar)
    {
        toolbar.setCriteriaChangedListeners(createGridRefreshDelegatedAction());
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Material> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Material>> callback)
    {
        criteria.copyPagingConfig(resultSetConfig);
        viewContext.getService().listMaterials(criteria, callback);
    }

    @Override
    protected BaseEntityModel<Material> createModel(GridRowModel<Material> entity)
    {
        return getColumnsFactory().createModel(entity,
                viewContext.getDisplaySettingsManager().getRealNumberFormatingParameters());
    }

    @Override
    protected ColumnDefsAndConfigs<Material> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<Material> schema =
                getColumnsFactory().createColumnsSchema(viewContext,
                        criteria.getListCriteria().getMaterialType());
        schema.setGridCellRendererFor(CommonMaterialColDefKind.CODE.id(),
                createInternalLinkCellRenderer());
        return schema;
    }

    private EntityGridModelFactory<Material> getColumnsFactory()
    {
        return new EntityGridModelFactory<Material>(viewContext, getStaticColumnsDefinition());
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Material> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportMaterials(exportCriteria, callback);
    }

    @Override
    protected EntityType tryToGetEntityType()
    {
        return criteria == null ? null : criteria.getListCriteria().getMaterialType();
    }

    @Override
    protected boolean hasColumnsDefinitionChanged(ListMaterialDisplayCriteria newCriteria)
    {
        EntityType newEntityType = newCriteria.getListCriteria().getMaterialType();
        EntityType prevEntityType =
                (criteria == null ? null : criteria.getListCriteria().getMaterialType());
        return hasColumnsDefinitionChanged(newEntityType, prevEntityType);
    }

    @Override
    protected Set<DatabaseModificationKind> getGridRelevantModifications()
    {
        return getGridRelevantModifications(ObjectKind.MATERIAL);
    }

    @Override
    protected List<IColumnDefinition<Material>> getInitialFilters()
    {
        return asColumnFilters(new CommonMaterialColDefKind[]
            { CommonMaterialColDefKind.CODE });
    }

    @Override
    protected void showEntityViewer(Material material, boolean editMode, boolean inBackground)
    {
        showEntityInformationHolderViewer(material, editMode, inBackground);
    }

    @Override
    protected IColumnDefinitionKind<Material>[] getStaticColumnsDefinition()
    {
        return CommonMaterialColDefKind.values();
    }

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.MATERIAL;
    }

    public final class DisplayedAndSelectedMaterials extends DisplayedAndSelectedEntities<Material>
    {

        public DisplayedAndSelectedMaterials(List<Material> selectedItems,
                TableExportCriteria<Material> displayedItemsConfig, int displayedItemsCount)
        {
            super(selectedItems, displayedItemsConfig, displayedItemsCount);
        }
    }

    protected final IDelegatedActionWithResult<DisplayedAndSelectedMaterials> getDisplayedAndSelectedItemsAction()
    {
        return new IDelegatedActionWithResult<DisplayedAndSelectedMaterials>()
            {
                public DisplayedAndSelectedMaterials execute()
                {
                    return new DisplayedAndSelectedMaterials(getSelectedBaseObjects(),
                            createTableExportCriteria(), getTotalCount());
                }
            };
    }

}
