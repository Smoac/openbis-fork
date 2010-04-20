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

import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
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

    public static final String SHOW_RELATED_DATASETS_BUTTON_ID =
            GRID_ID + "_show-related-datasets-button";

    private final SearchableEntity searchableEntity;

    private final String queryText;

    private final boolean useWildcardSearchMode;

    public IDisposableComponent asDisposableComponent()
    {
        return asDisposableWithoutToolbar();
    }

    public MatchingEntitiesPanel(IViewContext<ICommonClientServiceAsync> viewContext,
            SearchableEntity searchableEntity, String queryText, boolean useWildcardSearchMode)
    {
        // NOTE: refreshAutomatically is false, refreshing should be called manually
        super(viewContext, GRID_ID, false, DisplayTypeIDGenerator.SEARCH_RESULT_GRID);
        this.searchableEntity = searchableEntity;
        this.queryText = queryText;
        this.useWildcardSearchMode = useWildcardSearchMode;
        setId(createId());

        updateDefaultRefreshButton();
        registerLinkClickListenerFor(MatchingEntityColumnKind.IDENTIFIER.id(),
                new ICellListener<MatchingEntity>()
                    {
                        public void handle(MatchingEntity rowItem, boolean keyPressed)
                        {
                            showEntityViewer(rowItem, false, keyPressed);
                        }
                    });
        extendBottomToolbar();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        String showRelatedDatasetsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_RELATED_DATASETS);
        Button showRelatedDatasetsButton =
                new Button(showRelatedDatasetsTitle, new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            showRelatedDataSets(viewContext, MatchingEntitiesPanel.this);
                        }
                    });
        showRelatedDatasetsButton.setId(SHOW_RELATED_DATASETS_BUTTON_ID);
        addButton(showRelatedDatasetsButton);
        allowMultipleSelection();

        addEntityOperationsSeparator();
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
        super.refresh(false);
    }

    /** used to make a first data refresh, but can be also called many times */
    public final void refresh(final IDataRefreshCallback newRefreshCallback)
    {
        super.refresh(newRefreshCallback, true);
    }

    @Override
    protected void showEntityViewer(MatchingEntity matchingEntity, boolean editMode,
            boolean inBackground)
    {
        final EntityKind entityKind = matchingEntity.getEntityKind();
        BasicEntityType entityType = matchingEntity.getEntityType();
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        entityType);
        // NOTE: createEntityViewer is the only allowed operation for MatchingEntityPanel
        final IClientPlugin<BasicEntityType, IIdentifiable> clientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        final AbstractTabItemFactory tabView =
                clientPlugin.createEntityViewer(entityType, matchingEntity.asIdentifiable());
        tabView.setInBackground(inBackground);
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
    protected MatchingEntityModel createModel(GridRowModel<MatchingEntity> entity)
    {
        return new MatchingEntityModel(entity);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, MatchingEntity> resultSetConfig,
            AbstractAsyncCallback<ResultSet<MatchingEntity>> callback)
    {
        viewContext.getService().listMatchingEntities(searchableEntity, queryText,
                useWildcardSearchMode, resultSetConfig, callback);
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
                    edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    createOrDelete(ObjectKind.VOCABULARY_TERM), edit(ObjectKind.VOCABULARY_TERM) };
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshGridSilently();
    }

}
