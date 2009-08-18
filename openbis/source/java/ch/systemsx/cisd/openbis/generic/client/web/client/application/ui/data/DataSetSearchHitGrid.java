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

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetSearchHitColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid with data set search results.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetSearchHitGrid extends AbstractExternalDataGrid
{

    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID =
            GenericConstants.ID_PREFIX + "data-set-search-hit-browser";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        DataSetSearchHitGrid grid = new DataSetSearchHitGrid(viewContext);
        final DataSetSearchWindow searchWindow = new DataSetSearchWindow(viewContext);
        final DataSetSearchToolbar toolbar =
                new DataSetSearchToolbar(grid, viewContext.getMessage(Dict.BUTTON_CHANGE_QUERY),
                        searchWindow);
        searchWindow.setUpdateListener(toolbar);
        return grid.asDisposableWithToolbar(toolbar);
    }

    private DataSetSearchCriteria chosenSearchCriteria;

    private DataSetSearchHitGrid(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID, false);
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.DATA_SET_SEARCH_RESULT_GRID);
        registerLinkClickListenerFor(DataSetSearchHitColDefKind.PARENT.id(),
                new OpenEntityDetailsTabCellClickListener()
                    {
                        @Override
                        protected IEntityInformationHolder getEntity(ExternalData rowItem)
                        {
                            return rowItem.getParent();
                        }
                    });
        registerLinkClickListenerFor(DataSetSearchHitColDefKind.EXPERIMENT.id(),
                new OpenEntityDetailsTabCellClickListener()
                    {
                        @Override
                        protected IEntityInformationHolder getEntity(ExternalData rowItem)
                        {
                            return rowItem.getExperiment();
                        }
                    });
        ICellListener<ExternalData> sampleClickListener =
                new OpenEntityDetailsTabCellClickListener()
                    {
                        @Override
                        protected IEntityInformationHolder getEntity(ExternalData rowItem)
                        {
                            return rowItem.getSample();
                        }
                    };
        registerLinkClickListenerFor(DataSetSearchHitColDefKind.SAMPLE.id(), sampleClickListener);
        registerLinkClickListenerFor(DataSetSearchHitColDefKind.SAMPLE_IDENTIFIER.id(),
                sampleClickListener);
    }

    private abstract class OpenEntityDetailsTabCellClickListener implements
            ICellListener<ExternalData>
    {
        protected abstract IEntityInformationHolder getEntity(ExternalData rowItem);

        public final void handle(ExternalData rowItem)
        {
            final IEntityInformationHolder entity = getEntity(rowItem);
            new OpenEntityDetailsTabAction(entity, viewContext).execute();
        }

    }

    @Override
    protected List<IColumnDefinition<ExternalData>> getInitialFilters()
    {
        return asColumnFilters(new DataSetSearchHitColDefKind[]
            { DataSetSearchHitColDefKind.CODE, DataSetSearchHitColDefKind.LOCATION,
                    DataSetSearchHitColDefKind.FILE_TYPE });
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, ExternalData> resultSetConfig,
            AbstractAsyncCallback<ResultSet<ExternalData>> callback)
    {
        viewContext.getService().searchForDataSets(getBaseIndexURL(), chosenSearchCriteria,
                resultSetConfig, callback);
    }

    public void refresh(DataSetSearchCriteria newCriteria, List<PropertyType> propertyTypes)
    {
        chosenSearchCriteria = newCriteria;
        if (criteria != null)
        {
            criteria.setPropertyTypes(propertyTypes);
        }
        refresh();
    }

    // Will not be called, we override the model and column definitions creation methods.
    @Override
    protected IColumnDefinitionKind<ExternalData>[] getStaticColumnsDefinition()
    {
        return null;
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
    protected DataSetSearchHitModel createModel(ExternalData entity)
    {
        return new DataSetSearchHitModel(entity);
    }

    @Override
    protected ColumnDefsAndConfigs<ExternalData> createColumnsDefinition()
    {
        List<PropertyType> propertyTypes = criteria == null ? null : criteria.tryGetPropertyTypes();
        ColumnDefsAndConfigs<ExternalData> schema =
                DataSetSearchHitModel.createColumnsSchema(viewContext, propertyTypes);
        GridCellRenderer<BaseEntityModel<?>> linkRenderer = LinkRenderer.createLinkRenderer();
        schema.setGridCellRendererFor(DataSetSearchHitColDefKind.PARENT.id(), linkRenderer);
        schema.setGridCellRendererFor(DataSetSearchHitColDefKind.SAMPLE.id(), linkRenderer);
        schema.setGridCellRendererFor(DataSetSearchHitColDefKind.SAMPLE_IDENTIFIER.id(),
                linkRenderer);
        schema.setGridCellRendererFor(DataSetSearchHitColDefKind.EXPERIMENT.id(), linkRenderer);
        schema.setGridCellRendererFor(DataSetSearchHitColDefKind.SHOW_DETAILS_LINK.id(),
                createShowDetailsLinkCellRenderer());
        return schema;
    }

    @Override
    public Set<DatabaseModificationKind> getGridRelevantModifications()
    {
        Set<DatabaseModificationKind> relevantMods = super.getGridRelevantModifications();
        relevantMods.add(edit(ObjectKind.EXPERIMENT));
        relevantMods.add(edit(ObjectKind.SAMPLE));
        relevantMods.add(edit(ObjectKind.VOCABULARY_TERM));
        relevantMods.add(createOrDelete(ObjectKind.VOCABULARY_TERM));
        return relevantMods;
    }
}