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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DisplayedAndSelectedEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentDataSetArchivingMenu.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MetaprojectChooserButton;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.GridUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A {@link LayoutContainer} which contains the grid where the experiments are displayed.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentBrowserGrid extends AbstractEntityGrid<Experiment>
{

    private static final String PREFIX = "experiment-browser";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "_edit-button";

    public static final String SHOW_DETAILS_BUTTON_ID = BROWSER_ID + "_show-details-button";

    public static final String DELETE_BUTTON_ID = BROWSER_ID + "_delete-button";

    public static final String ADD_METAPROJECTS_BUTTON_ID = BROWSER_ID + "_add-metaprojects";

    public static final String REMOVE_METAPROJECTS_BUTTON_ID = BROWSER_ID + "_remove-metaprojects";

    /**
     * Creates a grid without additional toolbar buttons. It can serve as a entity chooser.
     */
    public static DisposableEntityChooser<TableModelRowWithObject<Experiment>> createChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ProjectSelectionTreeGridContainer tree =
                new ProjectSelectionTreeGridContainer(viewContext);
        final ExperimentBrowserToolbar toolbar = new ExperimentBrowserToolbar(viewContext, tree);
        final ExperimentBrowserGrid browserGrid =
                new ExperimentBrowserGrid(viewContext, toolbar, false)
                    {
                        @Override
                        protected ICellListenerAndLinkGenerator<Experiment> tryGetCellListenerAndLinkGenerator(
                                String columnId)
                        {
                            // No links in choosers needed
                            return null;
                        }

                        @Override
                        protected boolean isEditable(
                                BaseEntityModel<TableModelRowWithObject<Experiment>> model,
                                String columnID)
                        {
                            return false;
                        }

                        @Override
                        protected void showNonEditableTableCellMessage(
                                BaseEntityModel<TableModelRowWithObject<Experiment>> model,
                                String columnID)
                        {
                            // Do not show a message because in a chooser nobody is expecting
                            // editable table
                            // cells.
                        }
                    };
        return createExperimentBrowser(tree, toolbar, browserGrid, viewContext);
    }

    public static DisposableEntityChooser<TableModelRowWithObject<Experiment>> createForMetaproject(
            IViewContext<?> viewContext, TechId metaprojectId)
    {
        final ListExperimentsCriteria criteria = new ListExperimentsCriteria();
        criteria.setMetaprojectId(metaprojectId);

        ExperimentBrowserGrid browserGrid =
                new ExperimentBrowserGrid(viewContext.getCommonViewContext(),
                        new ICriteriaProvider<ListExperimentsCriteria>()
                            {
                                @Override
                                public ListExperimentsCriteria tryGetCriteria()
                                {
                                    return criteria;
                                }

                                @Override
                                public void update(
                                        Set<DatabaseModificationKind> observedModifications,
                                        IDataRefreshCallback dataRefreshCallback)
                                {
                                    dataRefreshCallback.postRefresh(true);
                                }

                                @Override
                                public DatabaseModificationKind[] getRelevantModifications()
                                {
                                    return new DatabaseModificationKind[0];
                                }
                            }, true);

        browserGrid.addEntityOperationsLabel();
        browserGrid.addTaggingButtons();
        browserGrid.addEntityOperationsSeparator();
        browserGrid.allowMultipleSelection();
        return browserGrid.asDisposableWithoutToolbar();
    }

    private static DisposableEntityChooser<TableModelRowWithObject<Experiment>> createExperimentBrowser(
            final ProjectSelectionTreeGridContainer tree, final ExperimentBrowserToolbar toolbar,
            final ExperimentBrowserGrid browserGrid, IMessageProvider messageProvider)
    {
        browserGrid.addGridRefreshListener(toolbar);
        return browserGrid.asDisposableWithToolbarAndTree(toolbar, tree,
                messageProvider.getMessage(Dict.EXPEIRMENTS_GRID_HEADER));
    }

    /**
     * Create a grid with the toolbar and a tree and optional initial selection of experiment type
     * and project.
     */
    public static DisposableEntityChooser<TableModelRowWithObject<Experiment>> create(
            final IViewContext<ICommonClientServiceAsync> viewContext, String initialSpaceOrNull,
            String initialProjectOrNull, String initialExperimentTypeOrNull)
    {
        final ProjectSelectionTreeGridContainer tree =
                new ProjectSelectionTreeGridContainer(viewContext, initialSpaceOrNull,
                        initialProjectOrNull);
        final ExperimentBrowserToolbar toolbar =
                new ExperimentBrowserToolbar(viewContext, tree, initialExperimentTypeOrNull);
        final ExperimentBrowserGrid browserGrid =
                new ExperimentBrowserGrid(viewContext, toolbar, false);
        browserGrid.extendBottomToolbar();
        return createExperimentBrowser(tree, toolbar, browserGrid, viewContext);
    }

    /** Create a grid with the toolbar and a tree with no initial selection. */
    public static DisposableEntityChooser<TableModelRowWithObject<Experiment>> create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return create(viewContext, null, null, null);
    }

    private final ICriteriaProvider<ListExperimentsCriteria> criteriaProvider;

    private ExperimentBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ICriteriaProvider<ListExperimentsCriteria> criteriaProvider,
            boolean refreshAutomatically)
    {
        super(viewContext, GRID_ID, refreshAutomatically,
                DisplayTypeIDGenerator.ENTITY_BROWSER_GRID);
        this.criteriaProvider = criteriaProvider;
        registerLinkClickListenerFor(ExperimentBrowserGridColumnIDs.EXPERIMENT_IDENTIFIER,
                showEntityViewerLinkClickListener);
        // NOTE: links to experiment are handled by EntityTableCell
        linkProject();
        setId(BROWSER_ID);
    }

    @Override
    protected boolean supportsExportForUpdate()
    {
        return true;
    }

    private void linkProject()
    {
        registerListenerAndLinkGenerator(ExperimentBrowserGridColumnIDs.PROJECT,
                new ICellListenerAndLinkGenerator<Experiment>()
                    {
                        @Override
                        public void handle(TableModelRowWithObject<Experiment> rowItem,
                                boolean specialKeyPressed)
                        {
                            final Project project = rowItem.getObjectOrNull().getProject();
                            final String href = LinkExtractor.tryExtract(project);
                            OpenEntityDetailsTabHelper.open(viewContext, project,
                                    specialKeyPressed, href);
                        }

                        @Override
                        public String tryGetLink(Experiment entity,
                                ISerializableComparable comparableValue)
                        {
                            return LinkExtractor.tryExtract(entity.getProject());
                        }
                    });
    }

    protected void addTaggingButtons()
    {
        final MetaprojectChooserButton tagButton =
                new MetaprojectChooserButton(viewContext, getId(),
                        new IChosenEntitiesProvider<String>()
                            {
                                @Override
                                public List<String> getEntities()
                                {
                                    return getMetaProjectsReferencedyByEachOf(taggables(getSelectedItems()));
                                }

                                @Override
                                public boolean isBlackList()
                                {
                                    return true;
                                }
                            });

        tagButton
                .addChosenEntityListener(new IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>()
                    {
                        @Override
                        public void entitiesChosen(
                                List<TableModelRowWithObject<Metaproject>> entities)
                        {
                            List<Long> experimentIds = new ArrayList<Long>();
                            for (BaseEntityModel<TableModelRowWithObject<Experiment>> item : getSelectedItems())
                            {
                                experimentIds.add(item.getBaseObject().getObjectOrNull().getId());
                            }

                            List<Long> metaProjectIds = new ArrayList<Long>();
                            for (TableModelRowWithObject<Metaproject> row : entities)
                            {
                                metaProjectIds.add(row.getObjectOrNull().getId());
                            }
                            viewContext.getCommonService().assignExperimentsToMetaProjects(
                                    metaProjectIds, experimentIds,
                                    createRefreshCallback(asActionInvoker()));

                        }
                    });

        tagButton.setId(ADD_METAPROJECTS_BUTTON_ID);
        tagButton.setText(viewContext.getMessage(Dict.BUTTON_TAG));
        enableButtonOnSelectedItems(tagButton);
        addButton(tagButton);

        final MetaprojectChooserButton untagButton =
                new MetaprojectChooserButton(viewContext, getId(),
                        new IChosenEntitiesProvider<String>()
                            {
                                @Override
                                public List<String> getEntities()
                                {
                                    return getMetaProjectsReferencedByAtLeastOneOf(taggables(getSelectedItems()));
                                }

                                @Override
                                public boolean isBlackList()
                                {
                                    return false;
                                }
                            });

        untagButton
                .addChosenEntityListener(new IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>()
                    {
                        @Override
                        public void entitiesChosen(
                                List<TableModelRowWithObject<Metaproject>> entities)
                        {
                            List<Long> experimentIds = new ArrayList<Long>();
                            for (BaseEntityModel<TableModelRowWithObject<Experiment>> item : getSelectedItems())
                            {
                                experimentIds.add(item.getBaseObject().getObjectOrNull().getId());
                            }

                            List<Long> metaProjectIds = new ArrayList<Long>();
                            for (TableModelRowWithObject<Metaproject> row : entities)
                            {
                                metaProjectIds.add(row.getObjectOrNull().getId());
                            }
                            viewContext.getCommonService()
                                    .removeExperimentsFromMetaProjects(
                                            metaProjectIds, experimentIds,
                                            createRefreshCallback(asActionInvoker()));

                        }
                    });

        untagButton.setId(REMOVE_METAPROJECTS_BUTTON_ID);
        untagButton.setText(viewContext.getMessage(Dict.BUTTON_UNTAG));
        enableButtonOnSelectedItems(untagButton);
        addButton(untagButton);
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

        addTaggingButtons();

        String editTitle = viewContext.getMessage(Dict.BUTTON_EDIT);
        Button editButton = createSelectedItemButton(editTitle, asShowEntityInvoker(true));
        editButton.setId(EDIT_BUTTON_ID);
        addButton(editButton);

        final String deleteTitle = viewContext.getMessage(Dict.BUTTON_DELETE);
        final String deleteAllTitle = deleteTitle + " All";
        final Button deleteButton = new Button(deleteAllTitle, new AbstractCreateDialogListener()
            {
                @Override
                protected Dialog createDialog(
                        List<TableModelRowWithObject<Experiment>> experiments,
                        IBrowserGridActionInvoker invoker)
                {
                    return new ExperimentListDeletionConfirmationDialog(viewContext,
                            createRefreshCallback(invoker), getDisplayedAndSelectedItemsAction()
                                    .execute());
                }
            });
        changeButtonTitleOnSelectedItems(deleteButton, deleteAllTitle, deleteTitle);
        deleteButton.setId(DELETE_BUTTON_ID);
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
                @Override
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
        ListExperimentsCriteria criteriaOrNull = criteriaProvider.tryGetCriteria();
        if (criteriaOrNull != null)
        {
            final ExperimentType experimentType = criteriaOrNull.getExperimentType();
            context.setExperimentType(experimentType);
            final Project projectOrNull = criteriaOrNull.tryGetProject();
            context.setProject(projectOrNull);
            final String spaceCodeOrNull = criteriaOrNull.getSpaceCode();
            context.setSpaceCode(spaceCodeOrNull);
        }
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext)
                .getExperimentRegistration(context));
    }

    private void addGridRefreshListener(ExperimentBrowserToolbar topToolbar)
    {
        topToolbar.setCriteriaChangedListeners(createGridRefreshDelegatedAction());
    }

    private final IDelegatedAction createGridRefreshDelegatedAction()
    {
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    if (criteriaProvider.tryGetCriteria() != null)
                    {
                        refreshGridWithFilters();
                    }
                }
            };
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Experiment>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Experiment>> callback)
    {
        ListExperimentsCriteria criteria = criteriaProvider.tryGetCriteria();
        criteria.copyPagingConfig(resultSetConfig);
        viewContext.getService().listExperiments(criteria, callback);
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<Experiment>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<Experiment>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(ExperimentBrowserGridColumnIDs.REGISTRATOR,
                PersonRenderer.REGISTRATOR_RENDERER);
        schema.setGridCellRendererFor(CommonGridColumnIDs.MODIFIER,
                PersonRenderer.MODIFIER_RENDERER);
        schema.setGridCellRendererFor(ExperimentBrowserGridColumnIDs.SHOW_DETAILS_LINK,
                createShowDetailsLinkCellRenderer());
        return schema;
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Experiment>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportExperiments(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(ExperimentBrowserGridColumnIDs.CODE);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return GridUtils.getRelevantModifications(ObjectKind.EXPERIMENT, criteriaProvider);
    }

    public final class DisplayedAndSelectedExperiments extends
            DisplayedAndSelectedEntities<TableModelRowWithObject<Experiment>>
    {

        public DisplayedAndSelectedExperiments(
                List<TableModelRowWithObject<Experiment>> selectedItems,
                TableExportCriteria<TableModelRowWithObject<Experiment>> displayedItemsConfig,
                int displayedItemsCount)
        {
            super(selectedItems, displayedItemsConfig, displayedItemsCount);
        }

        public List<Experiment> getExperiments()
        {
            ArrayList<Experiment> experiments = new ArrayList<Experiment>();
            List<TableModelRowWithObject<Experiment>> rows = getSelectedItems();
            for (TableModelRowWithObject<Experiment> row : rows)
            {
                experiments.add(row.getObjectOrNull());
            }
            return experiments;
        }
    }

    @Override
    protected EntityKind getEntityKindOrNull()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    public void update(final Set<DatabaseModificationKind> observedModifications)
    {
        criteriaProvider.update(observedModifications, new IDataRefreshCallback()
            {
                @Override
                public void postRefresh(boolean wasSuccessful)
                {
                    // update the browser
                    ExperimentBrowserGrid.super.update(observedModifications);
                }
            });
    }

    protected final IDelegatedActionWithResult<DisplayedAndSelectedExperiments> getDisplayedAndSelectedItemsAction()
    {
        return new IDelegatedActionWithResult<DisplayedAndSelectedExperiments>()
            {
                @Override
                public DisplayedAndSelectedExperiments execute()
                {
                    return new DisplayedAndSelectedExperiments(getSelectedBaseObjects(),
                            createTableExportCriteria(), getTotalCount());
                }
            };
    }

    private List<Taggable> taggables(List<BaseEntityModel<TableModelRowWithObject<Experiment>>> data)
    {
        List<Taggable> list = new ArrayList<Taggable>();
        for (final BaseEntityModel<TableModelRowWithObject<Experiment>> item : data)
        {
            list.add(new Taggable()
                {
                    @Override
                    public Collection<Metaproject> getMetaprojects()
                    {
                        return item.getBaseObject().getObjectOrNull()
                                .getMetaprojects();
                    }
                });
        }
        return list;
    }

}
