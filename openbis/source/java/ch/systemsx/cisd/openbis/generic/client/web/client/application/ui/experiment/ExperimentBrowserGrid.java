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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.experiment.CommonExperimentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EditableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEditableEntity;

/**
 * A {@link LayoutContainer} which contains the grid where the experiments are displayed.
 * 
 * @author Tomasz Pylak
 */
public final class ExperimentBrowserGrid extends AbstractBrowserGrid<Experiment, ExperimentModel>
{
    private static final String PREFIX = "experiment-browser";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String GRID_ID = BROWSER_ID + "_grid";

    private final ExperimentBrowserToolbar topToolbar;

    private ListExperimentsCriteria criteria;

    public static DisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ExperimentBrowserToolbar toolbar = new ExperimentBrowserToolbar(viewContext);
        final ExperimentBrowserGrid browserGrid = new ExperimentBrowserGrid(viewContext, toolbar);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    private ExperimentBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ExperimentBrowserToolbar topToolbar)
    {
        super(viewContext, GRID_ID);
        this.topToolbar = topToolbar;
        extendToolbar();
        setId(BROWSER_ID);
    }

    private void extendToolbar()
    {
        SelectionChangedListener<?> refreshButtonListener = addRefreshButton(topToolbar);
        this.topToolbar.setCriteriaChangedListener(refreshButtonListener);
        this.topToolbar.add(new FillToolItem());

        String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
        Button showDetailsButton =
                createSelectedItemButton(showDetailsTitle, asShowEntityInvoker(false));
        this.topToolbar.add(new AdapterToolItem(showDetailsButton));
        this.topToolbar.add(new SeparatorToolItem());
        String editTitle = viewContext.getMessage(Dict.BUTTON_EDIT);
        Button editButton = createSelectedItemButton(editTitle, asShowEntityInvoker(true));
        this.topToolbar.add(new AdapterToolItem(editButton));
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return topToolbar.tryGetCriteria() != null;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Experiment> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Experiment>> callback)
    {
        copyPagingConfig(resultSetConfig);
        viewContext.getService().listExperiments(criteria, callback);
    }

    private void copyPagingConfig(DefaultResultSetConfig<String, Experiment> resultSetConfig)
    {
        criteria.setLimit(resultSetConfig.getLimit());
        criteria.setOffset(resultSetConfig.getOffset());
        criteria.setSortInfo(resultSetConfig.getSortInfo());
        criteria.setFilterInfos(resultSetConfig.getFilterInfos());
        criteria.setResultSetKey(resultSetConfig.getResultSetKey());
    }

    private IEditableEntity<ExperimentType, ExperimentTypePropertyType, ExperimentProperty> createEditableEntity(
            Experiment experiment, ExperimentType selectedType)
    {
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        return new EditableEntity<ExperimentType, ExperimentTypePropertyType, ExperimentProperty>(
                entityKind, selectedType.getExperimentTypePropertyTypes(), experiment
                        .getProperties(), selectedType, experiment.getIdentifier());
    }

    @Override
    protected void showEntityViewer(ExperimentModel experimentModel, boolean editMode)
    {
        final Experiment experiment = experimentModel.getBaseObject();
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final ITabItemFactory tabView;
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        experiment.getExperimentType());
        if (editMode)
        {
            final IClientPlugin<ExperimentType, ExperimentTypePropertyType, ExperimentProperty, IIdentifierHolder> createClientPlugin =
                    clientPluginFactory.createClientPlugin(entityKind);
            tabView =
                    createClientPlugin.createEntityEditor(createEditableEntity(experiment, criteria
                            .getExperimentType()));
        } else
        {
            final IClientPlugin<EntityType, EntityTypePropertyType<EntityType>, EntityProperty<EntityType, EntityTypePropertyType<EntityType>>, IIdentifierHolder> createClientPlugin =
                    clientPluginFactory.createClientPlugin(entityKind);
            tabView = createClientPlugin.createEntityViewer(experiment);
        }
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    @Override
    protected ExperimentModel createModel(Experiment entity)
    {
        return new ExperimentModel(entity);
    }

    @Override
    protected ColumnDefsAndConfigs<Experiment> createColumnsDefinition()
    {
        return ExperimentModel.createColumnsSchema(viewContext, criteria.getExperimentType());
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Experiment> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportExperiments(exportCriteria, callback);
    }

    private static final String createHeader(ListExperimentsCriteria criteria)
    {
        final StringBuilder builder = new StringBuilder("Experiments");
        builder.append(" of type ");
        builder.append(criteria.getExperimentType().getCode());
        builder.append(" belonging to the project ");
        builder.append(criteria.getProjectCode());
        builder.append(" from group ");
        builder.append(criteria.getGroupCode());
        return builder.toString();
    }

    @Override
    protected final void refresh()
    {
        ListExperimentsCriteria newCriteria = topToolbar.tryGetCriteria();
        if (newCriteria == null)
        {
            return;
        }
        boolean refreshColumnsDefinition =
                hasColumnsDefinitionChanged(newCriteria.getExperimentType());
        this.criteria = newCriteria;
        String newHeader = createHeader(criteria);

        super.refresh(newHeader, refreshColumnsDefinition);
    }

    private boolean hasColumnsDefinitionChanged(ExperimentType entityType)
    {
        return criteria == null || entityType.equals(criteria.getExperimentType()) == false;
    }

    @Override
    protected List<IColumnDefinition<Experiment>> getAvailableFilters()
    {
        return asColumnFilters(new CommonExperimentColDefKind[]
            { CommonExperimentColDefKind.CODE });
    }
}
