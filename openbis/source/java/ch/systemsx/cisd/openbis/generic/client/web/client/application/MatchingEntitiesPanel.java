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

import java.util.List;

import com.extjs.gxt.ui.client.XDOM;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MatchingEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

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

    MatchingEntitiesPanel(final IViewContext<ICommonClientServiceAsync> viewContext,
            final SearchableEntity searchableEntity, final String queryText)
    {
        // NOTE: refreshAutomatically is false, refreshing should be called manually
        super(viewContext, GRID_ID, false, false);
        this.searchableEntity = searchableEntity;
        this.queryText = queryText;
        setId(PREFIX + XDOM.getUniqueId());
        updateDefaultRefreshButton();
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
    protected void showEntityViewer(MatchingEntityModel matchingEntityModel)
    {
        final MatchingEntity matchingEntity = matchingEntityModel.getBaseObject();
        final EntityKind entityKind = matchingEntity.getEntityKind();
        final ITabItemFactory tabView =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        matchingEntity.getEntityType()).createClientPlugin(entityKind)
                        .createEntityViewer(matchingEntity);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    @Override
    protected ColumnDefsAndConfigs<MatchingEntity> createColumnsDefinition()
    {
        List<IColumnDefinitionUI<MatchingEntity>> list =
                MatchingEntityModel.createColumnsSchema(viewContext);
        return ColumnDefsAndConfigs.create(list);
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

}
