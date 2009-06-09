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

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.EntityGridModelFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.experiment.CommonExperimentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * A {@link LayoutContainer} which contains the grid where the experiments are displayed.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentBrowserGrid extends
        AbstractEntityBrowserGrid<Experiment, BaseEntityModel<Experiment>, ListExperimentsCriteria>
{
    public static final String ID_SUFFIX_EDIT_BUTTON = "_edit-button";

    private static final String PREFIX = "experiment-browser";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String GRID_ID = BROWSER_ID + "_grid";

    /**
     * Creates a grid without additional toolbar buttons. It can serve as a entity chooser.
     */
    public static DisposableEntityChooser<Experiment> createChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ProjectSelectionTreeWidget tree = new ProjectSelectionTreeWidget(viewContext);
        final SectionPanel treeSection = new ProjectSelectionSection(tree);
        final ExperimentBrowserToolbar toolbar = new ExperimentBrowserToolbar(viewContext, tree);
        final ExperimentBrowserGrid browserGrid = new ExperimentBrowserGrid(viewContext, toolbar)
            {
                @Override
                protected void showEntityViewer(Experiment experiment, boolean editMode)
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
        final ProjectSelectionTreeWidget tree = new ProjectSelectionTreeWidget(viewContext);
        final SectionPanel treeSection = new ProjectSelectionSection(tree);
        final ExperimentBrowserToolbar toolbar = new ExperimentBrowserToolbar(viewContext, tree);
        final ExperimentBrowserGrid browserGrid = new ExperimentBrowserGrid(viewContext, toolbar);
        browserGrid.addGridRefreshListener(toolbar);
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithToolbarAndTree(toolbar, treeSection);
    }

    private ExperimentBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ICriteriaProvider<ListExperimentsCriteria> criteriaProvider)
    {
        super(viewContext, GRID_ID, criteriaProvider);
        setId(BROWSER_ID);
        setEntityKindForDisplayTypeIDGeneration(EntityKind.EXPERIMENT);
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
        Button showDetailsButton =
                createSelectedItemButton(showDetailsTitle, asShowEntityInvoker(false));
        addButton(showDetailsButton);

        String editTitle = viewContext.getMessage(Dict.BUTTON_EDIT);
        Button editButton = createSelectedItemButton(editTitle, asShowEntityInvoker(true));
        editButton.setId(GRID_ID + ID_SUFFIX_EDIT_BUTTON);
        addButton(editButton);

        addButton(createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                new AbstractCreateDialogListener()
                    {
                        @Override
                        protected Dialog createDialog(List<Experiment> experiments,
                                IBrowserGridActionInvoker invoker)
                        {
                            return new ExperimentDeletionConfirmationDialog(experiments, invoker);
                        }
                    }));
        allowMultipleSelection(); // we allow deletion of multiple samples

        addEntityOperationsSeparator();
    }

    private void addGridRefreshListener(ExperimentBrowserToolbar topToolbar)
    {
        topToolbar.setCriteriaChangedListener(createGridRefreshListener());
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Experiment> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Experiment>> callback)
    {
        copyPagingConfig(resultSetConfig);
        viewContext.getService().listExperiments(criteria, callback);
    }

    @Override
    protected void showEntityViewer(Experiment experiment, boolean editMode)
    {
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final ITabItemFactory tabView;
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        experiment.getExperimentType());
        final IClientPlugin<ExperimentType, IIdentifiable> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        if (editMode)
        {
            tabView = createClientPlugin.createEntityEditor(experiment);
        } else
        {
            tabView = createClientPlugin.createEntityViewer(experiment);
        }
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    @Override
    protected BaseEntityModel<Experiment> createModel(Experiment entity)
    {
        return getColumnsFactory().createModel(entity);
    }

    @Override
    protected ColumnDefsAndConfigs<Experiment> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<Experiment> schema =
                getColumnsFactory().createColumnsSchema(viewContext, criteria.getExperimentType());
        schema.setGridCellRendererFor(CommonExperimentColDefKind.CODE.id(), LinkRenderer
                .createGridCellRenderer());
        schema.setGridCellRendererFor(CommonSampleColDefKind.SHOW_DETAILS_LINK.id(),
                createShowDetailsLinkCellRenderer());
        return schema;
    }

    private EntityGridModelFactory<Experiment> getColumnsFactory()
    {
        return new EntityGridModelFactory<Experiment>(getStaticColumnsDefinition());
    }

    @Override
    protected IColumnDefinitionKind<Experiment>[] getStaticColumnsDefinition()
    {
        return CommonExperimentColDefKind.values();
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

    @Override
    protected EntityType tryToGetEntityType()
    {
        return criteria == null ? null : criteria.getExperimentType();
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
        EntityType newEntityType = newCriteria.getExperimentType();
        EntityType prevEntityType = (criteria == null ? null : criteria.getExperimentType());
        return hasColumnsDefinitionChanged(newEntityType, prevEntityType);
    }

    @Override
    protected List<IColumnDefinition<Experiment>> getInitialFilters()
    {
        return asColumnFilters(new CommonExperimentColDefKind[]
            { CommonExperimentColDefKind.CODE });
    }

    @Override
    protected Set<DatabaseModificationKind> getGridRelevantModifications()
    {
        return getGridRelevantModifications(ObjectKind.EXPERIMENT);
    }

    //
    // Helpers
    //

    private final class ExperimentDeletionConfirmationDialog extends DeletionConfirmationDialog
    {
        public ExperimentDeletionConfirmationDialog(List<Experiment> experiments,
                IBrowserGridActionInvoker invoker)
        {
            super(experiments, invoker);
        }

        @Override
        protected void executeConfirmedAction()
        {
            viewContext.getCommonService().deleteExperiments(TechId.createList(data),
                    reason.getValue(), new DeletionCallback(viewContext, invoker));
        }

        @Override
        protected String getEntityName()
        {
            return EntityKind.EXPERIMENT.getDescription();
        }

    }
}
