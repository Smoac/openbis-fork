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
import java.util.Set;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.IEditableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.experiment.CommonExperimentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.AbstractEntityBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * A {@link LayoutContainer} which contains the grid where the experiments are displayed.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentBrowserGrid extends
        AbstractEntityBrowserGrid<Experiment, ExperimentModel, ListExperimentsCriteria>
{
    public static final String ID_SUFFIX_EDIT_BUTTON = "_edit-button";

    private static final String PREFIX = "experiment-browser";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String GRID_ID = BROWSER_ID + "_grid";

    /**
     * Creates a grid without additional toolbar buttons. It can serve as a entity chooser.
     * 
     * @param groupOrNull if specified, only projects from that group will be presented
     */
    public static DisposableEntityChooser<Experiment> createChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, Group groupOrNull)
    {
        final ProjectSelectionTreeWidget tree =
                new ProjectSelectionTreeWidget(viewContext, groupOrNull);
        final SectionPanel treeSection = new ProjectSelectionSection(tree);
        final ExperimentBrowserToolbar toolbar =
                new ExperimentBrowserToolbar(viewContext, tree);
        final ExperimentBrowserGrid browserGrid = new ExperimentBrowserGrid(viewContext, toolbar)
            {
                @Override
                protected void showEntityViewer(ExperimentModel experimentModel, boolean editMode)
                {
                    // do nothing - avoid showing the details after double click
                }
            };
        browserGrid.addGridRefreshListener(toolbar);
        return browserGrid.asDisposableWithToolbarAndTree(toolbar, treeSection);
    }

    /** Create a grid with the toolbar and a tree. */
    public static DisposableEntityChooser<Experiment> create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ProjectSelectionTreeWidget tree = new ProjectSelectionTreeWidget(viewContext, null);
        final SectionPanel treeSection = new ProjectSelectionSection(tree);
        final ExperimentBrowserToolbar toolbar =
                new ExperimentBrowserToolbar(viewContext, tree);
        final ExperimentBrowserGrid browserGrid = new ExperimentBrowserGrid(viewContext, toolbar);
        browserGrid.addGridRefreshListener(toolbar);
        browserGrid.extendToolbar(toolbar);
        return browserGrid.asDisposableWithToolbarAndTree(toolbar, treeSection);
    }

    private ExperimentBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ICriteriaProvider<ListExperimentsCriteria> criteriaProvider)
    {
        super(viewContext, GRID_ID, criteriaProvider);
        setId(BROWSER_ID);
    }

    private void extendToolbar(ExperimentBrowserToolbar topToolbar)
    {
        String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
        Button showDetailsButton =
                createSelectedItemButton(showDetailsTitle, asShowEntityInvoker(false));
        topToolbar.add(new AdapterToolItem(showDetailsButton));
        topToolbar.add(new SeparatorToolItem());
        String editTitle = viewContext.getMessage(Dict.BUTTON_EDIT);
        Button editButton = createSelectedItemButton(editTitle, asShowEntityInvoker(true));
        editButton.setId(GRID_ID + ID_SUFFIX_EDIT_BUTTON);
        topToolbar.add(new AdapterToolItem(editButton));
    }

    private void addGridRefreshListener(ExperimentBrowserToolbar topToolbar)
    {
        topToolbar.setCriteriaChangedListener(createGridRefreshListener());
    }

    private <D extends ModelData> SelectionChangedListener<D> createGridRefreshListener()
    {
        final ExperimentBrowserGrid grid = this;

        return new SelectionChangedListener<D>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<D> se)
                {
                    grid.refresh();
                }
            };
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Experiment> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Experiment>> callback)
    {
        copyPagingConfig(resultSetConfig);
        viewContext.getService().listExperiments(criteria, callback);
    }

    private EditableExperiment createEditableEntity(Experiment experiment,
            ExperimentType selectedType)
    {
        return new EditableExperiment(selectedType.getExperimentTypePropertyTypes(), experiment
                .getProperties(), selectedType, experiment.getIdentifier(), experiment.getId(),
                experiment.getModificationDate(), experiment.getProject().getIdentifier(),
                experiment.getCode());
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
            final IClientPlugin<ExperimentType, ExperimentTypePropertyType, ExperimentProperty, IIdentifierHolder, EditableExperiment> createClientPlugin =
                    clientPluginFactory.createClientPlugin(entityKind);
            tabView =
                    createClientPlugin.createEntityEditor(createEditableEntity(experiment, criteria
                            .getExperimentType()));
        } else
        {
            final IClientPlugin<EntityType, EntityTypePropertyType<EntityType>, EntityProperty<EntityType, EntityTypePropertyType<EntityType>>, IIdentifierHolder, IEditableEntity<EntityType, EntityTypePropertyType<EntityType>, EntityProperty<EntityType, EntityTypePropertyType<EntityType>>>> createClientPlugin =
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

    @Override
    protected final String createHeader()
    {
        return doCreateHeader(criteria);
    }

    private static final String doCreateHeader(ListExperimentsCriteria criteria)
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
    protected boolean hasColumnsDefinitionChanged(ListExperimentsCriteria newCriteria)
    {
        ExperimentType newEntityType = newCriteria.getExperimentType();
        if (newEntityType == null)
        {
            return false; // nothing chosen
        }
        if (criteria == null)
        {
            return true; // first selection
        }
        ExperimentType prevEntityType = criteria.getExperimentType();
        return newEntityType.equals(prevEntityType) == false
                || propertiesEqual(newEntityType, prevEntityType) == false;
    }

    private boolean propertiesEqual(ExperimentType entityType1, ExperimentType entityType2)
    {
        return entityType1.getExperimentTypePropertyTypes().equals(
                entityType2.getExperimentTypePropertyTypes());
    }

    @Override
    protected List<IColumnDefinition<Experiment>> getAvailableFilters()
    {
        return asColumnFilters(new CommonExperimentColDefKind[]
            { CommonExperimentColDefKind.CODE });
    }

    @Override
    protected Set<DatabaseModificationKind> getGridRelevantModifications()
    {
        return getGridRelevantModifications(ObjectKind.EXPERIMENT);
    }
}
