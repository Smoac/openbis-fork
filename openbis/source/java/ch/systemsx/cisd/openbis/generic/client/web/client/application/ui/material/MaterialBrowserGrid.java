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

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MaterialModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.material.CommonMaterialColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Material;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * A {@link LayoutContainer} which contains the grid where the materials are displayed.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialBrowserGrid extends AbstractBrowserGrid<Material, MaterialModel>
{
    private static final String PREFIX = "material-browser";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String GRID_ID = BROWSER_ID + "_grid";

    // criteria used in the previous refresh operation or null if it has not occurred yet
    private ListMaterialCriteria criteria;

    private ICriteriaProvider criteriaProvider;

    private interface ICriteriaProvider
    {
        /**
         * @return criteria which should be used to display materials or null if they are not yet
         *         set.
         */
        ListMaterialCriteria tryGetCriteria();
    }

    public static DisposableEntityChooser<Material> createWithTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final MaterialBrowserToolbar toolbar = new MaterialBrowserToolbar(viewContext, null);
        final MaterialBrowserGrid browserGrid =
                new MaterialBrowserGrid(viewContext, false, new ICriteriaProvider()
                    {
                        public ListMaterialCriteria tryGetCriteria()
                        {
                            return toolbar.tryGetCriteria();
                        }
                    });
        browserGrid.extendTopToolbar(toolbar);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    /**
     * If the material type is given, does not show the toolbar with material type selection and
     * refreshes the grid automatically.
     */
    public static DisposableEntityChooser<Material> create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final MaterialType initValueOrNull)
    {
        if (initValueOrNull == null)
        {
            return createWithTypeChooser(viewContext);
        } else
        {
            return createWithoutTypeChooser(viewContext, initValueOrNull);
        }
    }

    private static DisposableEntityChooser<Material> createWithoutTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, final MaterialType initValue)
    {
        final MaterialBrowserGrid browserGrid =
                new MaterialBrowserGrid(viewContext, true, new ICriteriaProvider()
                    {
                        public ListMaterialCriteria tryGetCriteria()
                        {
                            return new ListMaterialCriteria(initValue);
                        }
                    });
        return browserGrid.asDisposableWithoutToolbar();
    }

    private MaterialBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            boolean refreshAutomatically, ICriteriaProvider criteriaProvider)
    {
        super(viewContext, GRID_ID, true, refreshAutomatically);
        this.criteriaProvider = criteriaProvider;
        setId(BROWSER_ID);
    }

    private void extendTopToolbar(MaterialBrowserToolbar toolbar)
    {
        SelectionChangedListener<?> refreshButtonListener = addRefreshButton(toolbar);
        toolbar.setCriteriaChangedListener(refreshButtonListener);
        toolbar.add(new FillToolItem());

        String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
        Button showDetailsButton =
                createSelectedItemButton(showDetailsTitle, asShowEntityInvoker(false));
        toolbar.add(new AdapterToolItem(showDetailsButton));

    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return criteriaProvider.tryGetCriteria() != null;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Material> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Material>> callback)
    {
        copyPagingConfig(resultSetConfig);
        viewContext.getService().listMaterials(criteria, callback);
    }

    private void copyPagingConfig(DefaultResultSetConfig<String, Material> resultSetConfig)
    {
        criteria.setLimit(resultSetConfig.getLimit());
        criteria.setOffset(resultSetConfig.getOffset());
        criteria.setSortInfo(resultSetConfig.getSortInfo());
        criteria.setFilterInfos(resultSetConfig.getFilterInfos());
        criteria.setResultSetKey(resultSetConfig.getResultSetKey());
    }

    @Override
    protected MaterialModel createModel(Material entity)
    {
        return new MaterialModel(entity);
    }

    @Override
    protected ColumnDefsAndConfigs<Material> createColumnsDefinition()
    {
        return MaterialModel.createColumnsSchema(viewContext, criteria.getMaterialType());
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Material> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportMaterials(exportCriteria, callback);
    }

    private static final String createHeader(ListMaterialCriteria criteria)
    {
        final StringBuilder builder = new StringBuilder("Materials");
        builder.append(" of type ");
        builder.append(criteria.getMaterialType().getCode());
        return builder.toString();
    }

    @Override
    protected final void refresh()
    {
        ListMaterialCriteria newCriteria = criteriaProvider.tryGetCriteria();
        if (newCriteria == null)
        {
            return;
        }
        boolean refreshColumnsDefinition =
                hasColumnsDefinitionChanged(newCriteria.getMaterialType());
        this.criteria = newCriteria;
        String newHeader = createHeader(criteria);

        super.refresh(newHeader, refreshColumnsDefinition);
    }

    private boolean hasColumnsDefinitionChanged(MaterialType entityType)
    {
        return criteria == null || entityType.equals(criteria.getMaterialType()) == false;
    }

    @Override
    protected List<IColumnDefinition<Material>> getAvailableFilters()
    {
        return asColumnFilters(new CommonMaterialColDefKind[]
            { CommonMaterialColDefKind.CODE });
    }

    @Override
    protected final void showEntityViewer(MaterialModel modelData, boolean editMode)
    {
        // do nothing
    }
}
