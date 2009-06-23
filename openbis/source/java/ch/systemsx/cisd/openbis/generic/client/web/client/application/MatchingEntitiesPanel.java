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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.XDOM;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MatchingEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MatchingEntityModel.MatchingEntityColumnKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * A {@link LayoutContainer} extension which displays the matching entities.
 * 
 * @author Christian Ribeaud
 */
final class MatchingEntitiesPanel extends AbstractBrowserGrid<MatchingEntity, MatchingEntityModel>
{
    static final String PREFIX = GenericConstants.ID_PREFIX + "matching-entities-panel_";

    static final String GRID_ID = PREFIX + "grid";

    private final SearchableEntity searchableEntity;

    private final String queryText;

    public IDisposableComponent asDisposableComponent()
    {
        return asDisposableWithoutToolbar();
    }

    public MatchingEntitiesPanel(IViewContext<ICommonClientServiceAsync> viewContext,
            SearchableEntity searchableEntity, String queryText)
    {
        // NOTE: refreshAutomatically is false, refreshing should be called manually
        super(viewContext, GRID_ID, false, false);
        this.searchableEntity = searchableEntity;
        this.queryText = queryText;
        setId(createId());
        updateDefaultRefreshButton();
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.SEARCH_RESULT_GRID);
        registerLinkClickListenerFor(MatchingEntityColumnKind.IDENTIFIER.id(),
                new ICellListener<MatchingEntity>()
                    {
                        public void handle(MatchingEntity rowItem)
                        {
                            showEntityViewer(rowItem, false);
                        }
                    });
    }

    private static String createId()
    {
        return PREFIX + XDOM.getUniqueId();
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return true;
    }

    /** used to refresh the results of the previously executed query */
    @Override
    protected final void refresh()
    {
        super.refresh(null, null, false);
    }

    /** used to make a first data refresh, but can be also called many times */
    public final void refresh(final IDataRefreshCallback newRefreshCallback)
    {
        super.refresh(newRefreshCallback, null, true);
    }

    @Override
    protected void showEntityViewer(MatchingEntity matchingEntity, boolean editMode)
    {
        final EntityKind entityKind = matchingEntity.getEntityKind();
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        matchingEntity.getEntityType());
        final IClientPlugin<EntityType, IIdentifiable> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        final ITabItemFactory tabView =
                createClientPlugin.createEntityViewer(matchingEntity.asIdentifiable());
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    @Override
    protected ColumnDefsAndConfigs<MatchingEntity> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<MatchingEntity> schema =
                BaseEntityModel.createColumnConfigs(MatchingEntityModel
                        .getStaticColumnsDefinition(), viewContext);
        schema.setGridCellRendererFor(MatchingEntityColumnKind.IDENTIFIER.id(), LinkRenderer
                .createLinkRenderer());
        return schema;
    }

    @Override
    protected MatchingEntityModel createModel(MatchingEntity entity)
    {
        return new MatchingEntityModel(entity);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, MatchingEntity> resultSetConfig,
            AbstractAsyncCallback<ResultSet<MatchingEntity>> callback)
    {
        viewContext.getService().listMatchingEntities(searchableEntity, queryText, resultSetConfig,
                callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<MatchingEntity> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportMatchingEntities(exportCriteria, callback);
    }

    @Override
    protected List<IColumnDefinition<MatchingEntity>> getInitialFilters()
    {
        return asColumnFilters(new MatchingEntityColumnKind[]
            { MatchingEntityColumnKind.ENTITY_TYPE, MatchingEntityColumnKind.IDENTIFIER,
                    MatchingEntityColumnKind.MATCHING_FIELD });
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { createOrDelete(ObjectKind.MATERIAL), edit(ObjectKind.MATERIAL),
                    createOrDelete(ObjectKind.SAMPLE), edit(ObjectKind.SAMPLE),
                    createOrDelete(ObjectKind.EXPERIMENT), edit(ObjectKind.EXPERIMENT),
                    createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    createOrDelete(ObjectKind.VOCABULARY_TERM) };
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshGridSilently();
    }
}
