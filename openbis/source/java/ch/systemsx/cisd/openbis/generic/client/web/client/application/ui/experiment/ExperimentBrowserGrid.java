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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.EntityGridModelFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DisplayedAndSelectedEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.experiment.CommonExperimentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentDataSetArchivingMenu.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * A {@link LayoutContainer} which contains the grid where the experiments are displayed.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentBrowserGrid extends
        AbstractEntityBrowserGrid<Experiment, BaseEntityModel<Experiment>, ListExperimentsCriteria>
{

    private static final String PREFIX = "experiment-browser";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "_edit-button";

    public static final String SHOW_DETAILS_BUTTON_ID = BROWSER_ID + "_show-details-button";

    /**
     * Creates a grid without additional toolbar buttons. It can serve as a entity chooser.
     */
    public static DisposableEntityChooser<Experiment> createChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ProjectSelectionTreeGridContainer tree =
                new ProjectSelectionTreeGridContainer(viewContext);
        final ExperimentBrowserToolbar toolbar = new ExperimentBrowserToolbar(viewContext, tree);
        final ExperimentBrowserGrid browserGrid = new ExperimentBrowserGrid(viewContext, toolbar)
            {
                @Override
                protected void showEntityViewer(Experiment experiment, boolean editMode,
                        boolean active)
                {
                    // do nothing - avoid showing the details after double click
                }
            };
        browserGrid.addGridRefreshListener(toolbar);
        return createExperimentBrowser(tree, toolbar, browserGrid, viewContext);
    }

    private static DisposableEntityChooser<Experiment> createExperimentBrowser(
            final ProjectSelectionTreeGridContainer tree, final ExperimentBrowserToolbar toolbar,
            final ExperimentBrowserGrid browserGrid, IMessageProvider messageProvider)
    {
        return browserGrid.asDisposableWithToolbarAndTree(toolbar, tree,
                messageProvider.getMessage(Dict.EXPEIRMENTS_GRID_HEADER));
    }

    /**
     * Create a grid with the toolbar and a tree and optional initial selection of experiment type
     * and project.
     */
    public static DisposableEntityChooser<Experiment> create(
            final IViewContext<ICommonClientServiceAsync> viewContext, String initialProjectOrNull,
            String initialExperimentTypeOrNull)
    {
        final ProjectSelectionTreeGridContainer tree =
                new ProjectSelectionTreeGridContainer(viewContext, initialProjectOrNull);
        final ExperimentBrowserToolbar toolbar =
                new ExperimentBrowserToolbar(viewContext, tree, initialExperimentTypeOrNull);
        final ExperimentBrowserGrid browserGrid = new ExperimentBrowserGrid(viewContext, toolbar);
        browserGrid.addGridRefreshListener(toolbar);
        browserGrid.extendBottomToolbar();
        return createExperimentBrowser(tree, toolbar, browserGrid, viewContext);
    }

    /** Create a grid with the toolbar and a tree with no initial selection. */
    public static DisposableEntityChooser<Experiment> create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return create(viewContext, null, null);
    }

    private final ICriteriaProvider<ListExperimentsCriteria> criteriaProvider;

    private ExperimentBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ICriteriaProvider<ListExperimentsCriteria> criteriaProvider)
    {
        super(viewContext, GRID_ID, DisplayTypeIDGenerator.ENTITY_BROWSER_GRID);
        this.criteriaProvider = criteriaProvider;
        registerLinkClickListenerFor(CommonExperimentColDefKind.EXPERIMENT_IDENTIFIER.id(),
                showEntityViewerLinkClickListener);
        registerLinkClickListenerFor(CommonExperimentColDefKind.PROJECT.id(),
                new ICellListener<Experiment>()
                    {
                        public void handle(Experiment rowItem, boolean keyPressed)
                        {
                            final Project project = rowItem.getProject();
                            final String href = LinkExtractor.tryExtract(project);
                            OpenEntityDetailsTabHelper.open(viewContext, project, keyPressed, href);
                        }
                    });
        setId(BROWSER_ID);
    }

    @Override
    protected ICriteriaProvider<ListExperimentsCriteria> getCriteriaProvider()
    {
        return criteriaProvider;
    }

    private void extendBottomToolbar()
    {
        if (viewContext.isSimpleOrEmbeddedMode())
        {
            return;
        }

        addEntityOperationsLabel();

        final Button addButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Experiment"),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    openExperimentRegistrationTab();
                                }

                            });
        addButton(addButton);

        String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
        Button showDetailsButton =
                createSelectedItemButton(showDetailsTitle, asShowEntityInvoker(false));
        showDetailsButton.setId(SHOW_DETAILS_BUTTON_ID);
        addButton(showDetailsButton);

        String editTitle = viewContext.getMessage(Dict.BUTTON_EDIT);
        Button editButton = createSelectedItemButton(editTitle, asShowEntityInvoker(true));
        editButton.setId(EDIT_BUTTON_ID);
        addButton(editButton);

        final String deleteTitle = viewContext.getMessage(Dict.BUTTON_DELETE);
        final String deleteAllTitle = deleteTitle + " All";
        final Button deleteButton = new Button(deleteAllTitle, new AbstractCreateDialogListener()
            {
                @Override
                protected Dialog createDialog(List<Experiment> experiments,
                        IBrowserGridActionInvoker invoker)
                {
                    return new ExperimentListDeletionConfirmationDialog(viewContext,
                            createRefreshCallback(invoker), getDisplayedAndSelectedItemsAction()
                                    .execute());
                }
            });
        changeButtonTitleOnSelectedItems(deleteButton, deleteAllTitle, deleteTitle);
        addButton(deleteButton);

        if (viewContext.getModel().getApplicationInfo().isArchivingConfigured()
                && viewContext.isSimpleOrEmbeddedMode() == false)
        {
            addButton(createArchivingMenu());
        }

        allowMultipleSelection(); // we allow deletion of multiple samples

        addEntityOperationsSeparator();
    }

    private final TextToolItem createArchivingMenu()
    {
        return new ExperimentDataSetArchivingMenu(viewContext,
                getSelectedAndDisplayedItemsAction(), createRefreshGridAction());
    }

    private final IDelegatedActionWithResult<SelectedAndDisplayedItems> getSelectedAndDisplayedItemsAction()
    {
        return new IDelegatedActionWithResult<SelectedAndDisplayedItems>()
            {
                public SelectedAndDisplayedItems execute()
                {
                    return new SelectedAndDisplayedItems(getSelectedBaseObjects(),
                            createTableExportCriteria(), getTotalCount());
                }
            };
    }

    private void openExperimentRegistrationTab()
    {
        final ActionContext context = new ActionContext();
        ListExperimentsCriteria criteriaOrNull = getCriteriaProvider().tryGetCriteria();
        if (criteriaOrNull != null)
        {
            final ExperimentType experimentType = criteriaOrNull.getExperimentType();
            context.setExperimentType(experimentType);
            final Project project = criteriaOrNull.getProject();
            context.setProject(project);
        }
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext)
                .getExperimentRegistration(context));
    }

    private void addGridRefreshListener(ExperimentBrowserToolbar topToolbar)
    {
        topToolbar.setCriteriaChangedListeners(createGridRefreshDelegatedAction());
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Experiment> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Experiment>> callback)
    {
        criteria.copyPagingConfig(resultSetConfig);
        viewContext.getService().listExperiments(criteria, callback);
    }

    @Override
    protected void showEntityViewer(Experiment experiment, boolean editMode, boolean inBackground)
    {
        showEntityInformationHolderViewer(experiment, editMode, inBackground);
    }

    @Override
    protected BaseEntityModel<Experiment> createModel(GridRowModel<Experiment> entity)
    {
        return getColumnsFactory().createModel(entity,
                viewContext.getDisplaySettingsManager().getRealNumberFormatingParameters());
    }

    @Override
    protected ColumnDefsAndConfigs<Experiment> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<Experiment> schema =
                getColumnsFactory().createColumnsSchema(viewContext, criteria.getExperimentType(),
                        viewContext.getDisplaySettingsManager().getRealNumberFormatingParameters());
        schema.setGridCellRendererFor(CommonExperimentColDefKind.SHOW_DETAILS_LINK.id(),
                createShowDetailsLinkCellRenderer());
        GridCellRenderer<BaseEntityModel<?>> linkCellRenderer = createInternalLinkCellRenderer();
        schema.setGridCellRendererFor(CommonExperimentColDefKind.EXPERIMENT_IDENTIFIER.id(),
                linkCellRenderer);
        schema.setGridCellRendererFor(CommonExperimentColDefKind.PROJECT.id(), linkCellRenderer);
        return schema;
    }

    private EntityGridModelFactory<Experiment> getColumnsFactory()
    {
        return new EntityGridModelFactory<Experiment>(viewContext, getStaticColumnsDefinition());
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
    protected EntityType tryToGetEntityType()
    {
        return criteria == null ? null : criteria.getExperimentType();
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

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    public final class DisplayedAndSelectedExperiments extends
            DisplayedAndSelectedEntities<Experiment>
    {

        public DisplayedAndSelectedExperiments(List<Experiment> selectedItems,
                TableExportCriteria<Experiment> displayedItemsConfig, int displayedItemsCount)
        {
            super(selectedItems, displayedItemsConfig, displayedItemsCount);
        }

    }

    protected final IDelegatedActionWithResult<DisplayedAndSelectedExperiments> getDisplayedAndSelectedItemsAction()
    {
        return new IDelegatedActionWithResult<DisplayedAndSelectedExperiments>()
            {
                public DisplayedAndSelectedExperiments execute()
                {
                    return new DisplayedAndSelectedExperiments(getSelectedBaseObjects(),
                            createTableExportCriteria(), getTotalCount());
                }
            };
    }
}
