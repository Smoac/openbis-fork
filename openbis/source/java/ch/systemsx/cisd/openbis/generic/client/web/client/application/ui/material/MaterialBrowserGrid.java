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

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.EntityGridModelFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.material.CommonMaterialColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Material;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * A {@link LayoutContainer} which contains the grid where the materials are displayed.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialBrowserGrid extends
        AbstractEntityBrowserGrid<Material, BaseEntityModel<Material>, ListMaterialCriteria>
{
    private static final String PREFIX = "material-browser";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String GRID_ID = BROWSER_ID + "_grid";

    /**
     * Creates a browser with a toolbar which allows to choose the material type. Allows to show or
     * edit material details.
     */
    public static DisposableEntityChooser<Material> createWithTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return createWithTypeChooser(viewContext, true);
    }

    private static DisposableEntityChooser<Material> createWithTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, boolean detailsAvailable)
    {
        final MaterialBrowserToolbar toolbar = new MaterialBrowserToolbar(viewContext, null);
        final ICriteriaProvider<ListMaterialCriteria> criteriaProvider = toolbar;
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
            return createWithTypeChooser(viewContext, false);
        } else
        {
            return createWithoutTypeChooser(viewContext, initValueOrNull);
        }
    }

    private static DisposableEntityChooser<Material> createWithoutTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, final MaterialType initValue)
    {
        final ICriteriaProvider<ListMaterialCriteria> criteriaProvider =
                createUnrefreshableCriteriaProvider(new ListMaterialCriteria(initValue));
        boolean detailsAvailable = false;
        final MaterialBrowserGrid browserGrid =
                createBrowserGrid(viewContext, criteriaProvider, detailsAvailable);
        return browserGrid.asDisposableWithoutToolbar();
    }

    private static MaterialBrowserGrid createBrowserGrid(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final ICriteriaProvider<ListMaterialCriteria> criteriaProvider, boolean detailsAvailable)
    {
        if (detailsAvailable)
        {
            return new MaterialBrowserGrid(viewContext, true, criteriaProvider);
        } else
        {
            return new MaterialBrowserGrid(viewContext, true, criteriaProvider)
                {
                    @Override
                    protected void showEntityViewer(Material material, boolean editMode)
                    {
                        // do nothing - avoid showing the details after double click
                    }
                };
        }

    }

    private MaterialBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            boolean refreshAutomatically, ICriteriaProvider<ListMaterialCriteria> criteriaProvider)
    {
        super(viewContext, GRID_ID, true, refreshAutomatically, criteriaProvider);
        setId(BROWSER_ID);
        setEntityKindForDisplayTypeIDGeneration(EntityKind.MATERIAL);
    }

    private void extendBottomToolbar(boolean detailsAvailable)
    {
        if (detailsAvailable)
        {
            addEntityOperationsLabel();

            String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
            Button showDetailsButton =
                    createSelectedItemButton(showDetailsTitle, asShowEntityInvoker(false));
            pagingToolbar.add(new AdapterToolItem(showDetailsButton));

            String editTitle = viewContext.getMessage(Dict.BUTTON_EDIT);
            Button editButton = createSelectedItemButton(editTitle, asShowEntityInvoker(true));
            pagingToolbar.add(new AdapterToolItem(editButton));

            addEntityOperationsSeparator();
        }
    }

    private void addGridRefreshListener(MaterialBrowserToolbar toolbar)
    {
        toolbar.setCriteriaChangedListener(createGridRefreshListener());
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Material> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Material>> callback)
    {
        copyPagingConfig(resultSetConfig);
        viewContext.getService().listMaterials(criteria, callback);
    }

    @Override
    protected BaseEntityModel<Material> createModel(Material entity)
    {
        return getColumnsFactory().createModel(entity);
    }

    @Override
    protected ColumnDefsAndConfigs<Material> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<Material> schema =
                getColumnsFactory().createColumnsSchema(viewContext, criteria.getMaterialType());
        schema.setGridCellRendererFor(CommonMaterialColDefKind.CODE.id(), LinkRenderer
                .createGridCellRenderer());
        return schema;
    }

    private EntityGridModelFactory<Material> getColumnsFactory()
    {
        return new EntityGridModelFactory<Material>(getStaticColumnsDefinition());
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Material> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportMaterials(exportCriteria, callback);
    }

    @Override
    protected final String createHeader()
    {
        return doCreateHeader(criteria);
    }

    @Override
    protected EntityType tryToGetEntityType()
    {
        return criteria == null ? null : criteria.getMaterialType();
    }

    private static final String doCreateHeader(ListMaterialCriteria criteria)
    {
        final StringBuilder builder = new StringBuilder("Materials");
        builder.append(" of type ");
        builder.append(criteria.getMaterialType().getCode());
        return builder.toString();
    }

    @Override
    protected boolean hasColumnsDefinitionChanged(ListMaterialCriteria newCriteria)
    {
        EntityType newEntityType = newCriteria.getMaterialType();
        EntityType prevEntityType = (criteria == null ? null : criteria.getMaterialType());
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
    protected void showEntityViewer(Material material, boolean editMode)
    {
        final EntityKind entityKind = EntityKind.MATERIAL;
        ITabItemFactory tabView;
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        material.getMaterialType());

        final IClientPlugin<MaterialType, IIdentifiable> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        if (editMode)
        {
            tabView = createClientPlugin.createEntityEditor(material);
        } else
        {
            tabView = createClientPlugin.createEntityViewer(material);
        }
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    @Override
    protected IColumnDefinitionKind<Material>[] getStaticColumnsDefinition()
    {
        return CommonMaterialColDefKind.values();
    }
}
