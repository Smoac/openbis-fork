/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DisplayedAndSelectedEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.SetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListEntityDisplayCriteriaKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria2;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid showing {@link Sample} instances.
 *
 * @author Franz-Josef Elmer
 */
public class SampleBrowserGrid extends TypedTableGrid<Sample>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "sample-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String MAIN_BROWSER_ID = PREFIX + "_main";

    public static final String MAIN_GRID_ID = createGridId(MAIN_BROWSER_ID);

    public static final String EDIT_BUTTON_ID_SUFFIX = "_edit-button";

    public static final String SHOW_DETAILS_BUTTON_ID_SUFFIX = "_show-details-button";

    public static final String createGridId(final String browserId)
    {
        return browserId + GRID_POSTFIX;
    }

    /** Creates a grid without additional toolbar buttons. It can serve as a entity chooser. */
    public static DisposableEntityChooser<TableModelRowWithObject<Sample>> createChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, final boolean addShared,
            boolean addAll, final boolean excludeWithoutExperiment, SampleTypeDisplayID sampleTypeID)
    {
        final SampleBrowserToolbar toolbar =
                new SampleBrowserToolbar(viewContext, addShared, addAll, excludeWithoutExperiment,
                        sampleTypeID);
        ISampleCriteriaProvider criteriaProvider = toolbar;
        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, MAIN_BROWSER_ID, false,
                        DisplayTypeIDGenerator.ENTITY_BROWSER_GRID)
                    {
                        @Override
                        protected void showEntityViewer(TableModelRowWithObject<Sample> row, boolean editMode,
                                boolean active)
                        {
                            // do nothing - avoid showing the details after double click
                        }
                    };
        browserGrid.addGridRefreshListener(toolbar);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    /**
     * Create a grid with a toolbar with no initial selection and optional initial selection of
     * sample type and group.
     */
    public static IDisposableComponent create(IViewContext<ICommonClientServiceAsync> viewContext,
            String initialGroupOrNull, String initialSampleTypeOrNull)
    {
        final SampleBrowserToolbar toolbar =
                new SampleBrowserToolbar(viewContext, true, true, false, initialGroupOrNull,
                        initialSampleTypeOrNull, SampleTypeDisplayID.MAIN_SAMPLE_BROWSER);
        ISampleCriteriaProvider criteriaProvider = toolbar;
        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, MAIN_BROWSER_ID, false,
                        DisplayTypeIDGenerator.ENTITY_BROWSER_GRID);
        browserGrid.addGridRefreshListener(toolbar);
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    public static IDisposableComponent createGridForContainerSamples(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final TechId containerSampleId, final String browserId, final SampleType sampleType)
    {
        final ListSampleDisplayCriteria criteria =
                ListSampleDisplayCriteria.createForContainer(containerSampleId);
        return createGridForRelatedSamples(viewContext, criteria, browserId, sampleType);
    }

    public static IDisposableComponent createGridForDerivedSamples(
            final IViewContext<ICommonClientServiceAsync> viewContext, final TechId parentSampleId,
            final String browserId, final SampleType sampleType)
    {
        final ListSampleDisplayCriteria criteria =
                ListSampleDisplayCriteria.createForParent(parentSampleId);
        return createGridForRelatedSamples(viewContext, criteria, browserId, sampleType);
    }

    public static IDisposableComponent createGridForParentSamples(
            final IViewContext<ICommonClientServiceAsync> viewContext, final TechId childSampleId,
            final String browserId, final SampleType sampleType)
    {
        final ListSampleDisplayCriteria criteria =
                ListSampleDisplayCriteria.createForChild(childSampleId);
        return createGridForRelatedSamples(viewContext, criteria, browserId, sampleType);
    }

    private static IDisposableComponent createGridForRelatedSamples(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final ListSampleDisplayCriteria criteria, final String browserId,
            final SampleType sampleType)
    {
        final String entityTypeCode = sampleType.getCode();
        final SampleBrowserGrid browserGrid =
                createGridAsComponent(viewContext, browserId, criteria, entityTypeCode,
                        DisplayTypeIDGenerator.SAMPLE_DETAILS_GRID);
        browserGrid.updateCriteriaProviderAndRefresh();
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithoutToolbar();
    }

    public static IDisposableComponent createGridForExperimentSamples(
            final IViewContext<ICommonClientServiceAsync> viewContext, final TechId experimentId,
            final String browserId, final BasicEntityType experimentType)
    {
        final ListSampleDisplayCriteria criteria =
                ListSampleDisplayCriteria.createForExperiment(experimentId);
        final String entityTypeCode = experimentType.getCode();

        final SampleBrowserGrid browserGrid =
                createGridAsComponent(viewContext, browserId, criteria, entityTypeCode,
                        DisplayTypeIDGenerator.EXPERIMENT_DETAILS_GRID);
        browserGrid.experimentIdOrNull = experimentId;
        browserGrid.updateCriteriaProviderAndRefresh();
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithoutToolbar();
    }

    private static SampleBrowserGrid createGridAsComponent(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String browserId,
            final ListSampleDisplayCriteria criteria, final String entityTypeCode,
            DisplayTypeIDGenerator displayTypeIDGenerator)
    {
        ISampleCriteriaProvider criteriaProvider =
                new SampleCriteriaProvider(viewContext, criteria);
        // we do not refresh the grid, the criteria provider will do this when property types will
        // be loaded
        boolean refreshAutomatically = false;
        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, browserId,
                        refreshAutomatically, displayTypeIDGenerator)
                    {
                        @Override
                        public String getGridDisplayTypeID()
                        {
                            return super.getGridDisplayTypeID() + "-" + entityTypeCode;
                        }
                    };
        return browserGrid;
    }

    /**
     * Besides providing the static {@link ListSampleCriteria} this class provides all property
     * types which should be used to build the grid property columns. It is also able to refresh
     * these properties from the server.
     */
    protected static class SampleCriteriaProvider implements ISampleCriteriaProvider
    {
        private final ICriteriaProvider<PropertyTypesCriteria> propertyTypeProvider;

        private final ListSampleDisplayCriteria criteria;

        // Set of entity types which are currently shown in this grid.
        // Used to decide which property columns should be shown.
        // Note: content depends on the current grid content.
        private Set<SampleType> shownEntityTypesOrNull;

        public SampleCriteriaProvider(IViewContext<?> viewContext,
                ListSampleDisplayCriteria criteria)
        {
            this.propertyTypeProvider =
                    createPropertyTypesCriteriaProvider(viewContext, EntityKind.SAMPLE);
            this.criteria = criteria;
        }

        /*
         * Provides property types which should be shown as the grid columns. Takes into account
         * what types of entities are displayed and does not show property types which are not
         * assigned to any of those types.
         */
        private ICriteriaProvider<PropertyTypesCriteria> createPropertyTypesCriteriaProvider(
                IViewContext<?> viewContext, final EntityKind entityKind)
        {
            return new PropertyTypesCriteriaProvider(viewContext, entityKind)
                {
                    @Override
                    public PropertyTypesCriteria tryGetCriteria()
                    {
                        PropertyTypesCriteria propertyTypesCriteria = super.tryGetCriteria();
                        return PropertyTypesFilterUtil.filterPropertyTypesForEntityTypes(
                                propertyTypesCriteria, entityKind, shownEntityTypesOrNull);
                    }
                };
        }

        public List<PropertyType> tryGetPropertyTypes()
        {
            PropertyTypesCriteria propertyTypesCriteria = propertyTypeProvider.tryGetCriteria();
            if (propertyTypesCriteria != null)
            {
                return propertyTypesCriteria.tryGetPropertyTypes();
            } else
            {
                return null;
            }
        }

        public ListSampleDisplayCriteria tryGetCriteria()
        {
            return criteria;
        }

        public void update(Set<DatabaseModificationKind> observedModifications,
                IDataRefreshCallback dataRefreshCallback)
        {
            propertyTypeProvider.update(observedModifications, dataRefreshCallback);
        }

        public DatabaseModificationKind[] getRelevantModifications()
        {
            return propertyTypeProvider.getRelevantModifications();
        }

        public void setEntityTypes(Set<SampleType> entityTypes)
        {
            criteria.setAllSampleType(SampleType.createAllSampleType(entityTypes, false));
            this.shownEntityTypesOrNull = entityTypes;
        }

    }

    // provides property types which will be used to build property columns in the grid and
    // criteria to filter samples
    private final ISampleCriteriaProvider propertyTypesAndCriteriaProvider;

    private TechId experimentIdOrNull;

    protected SampleBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ISampleCriteriaProvider criteriaProvider, String browserId,
            boolean refreshAutomatically, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId, displayTypeIDGenerator);
        propertyTypesAndCriteriaProvider = criteriaProvider;
        linkSample();
        linkExperiment();
        linkProject();
        linkParent();
        linkContainer();
    }

    @Override
    public String getGridDisplayTypeID()
    {
        ListSampleDisplayCriteria criteria = getCriteriaProvider().tryGetCriteria();
        String suffix = createDisplayIdSuffix(EntityKind.SAMPLE, criteria == null ? null : criteria.tryGetSampleType());
        return createGridDisplayTypeID(suffix);
    }
    
    private static String createDisplayIdSuffix(EntityKind entityKindOrNull,
            EntityType entityTypeOrNull)
    {
        String suffix = "";
        if (entityKindOrNull != null)
        {
            suffix += "-" + entityKindOrNull.toString();
        }
        if (entityTypeOrNull != null)
        {
            suffix += "-" + entityTypeOrNull.getCode();
        }
        return suffix;
    }
    
    private void linkSample()
    {
        ICellListenerAndLinkGenerator<Sample> listenerLinkGenerator =
                new ICellListenerAndLinkGenerator<Sample>()
                    {
                        public void handle(TableModelRowWithObject<Sample> rowItem,
                                boolean specialKeyPressed)
                        {
                            showEntityInformationHolderViewer(rowItem.getObjectOrNull(), false, specialKeyPressed);
                        }

                        public String tryGetLink(Sample entity,
                                ISerializableComparable comparableValue)
                        {
                            return LinkExtractor.tryExtract(entity);
                        }
                    };
        registerListenerAndLinkGenerator(SampleGridColumnIDs.CODE, listenerLinkGenerator);
        registerListenerAndLinkGenerator(SampleGridColumnIDs.SUBCODE, listenerLinkGenerator);
        registerListenerAndLinkGenerator(SampleGridColumnIDs.SAMPLE_IDENTIFIER,
                listenerLinkGenerator);
    }

    private void linkExperiment()
    {
        ICellListenerAndLinkGenerator<Sample> listenerLinkGenerator =
                new ICellListenerAndLinkGenerator<Sample>()
                    {
                        public void handle(TableModelRowWithObject<Sample> rowItem,
                                boolean specialKeyPressed)
                        {
                            Experiment experiment = rowItem.getObjectOrNull().getExperiment();
                            new OpenEntityDetailsTabAction(experiment, viewContext,
                                    specialKeyPressed).execute();
                        }

                        public String tryGetLink(Sample entity,
                                ISerializableComparable comparableValue)
                        {
                            return LinkExtractor.tryExtract(entity.getExperiment());
                        }
                    };
        registerListenerAndLinkGenerator(SampleGridColumnIDs.EXPERIMENT, listenerLinkGenerator);
        registerListenerAndLinkGenerator(SampleGridColumnIDs.EXPERIMENT_IDENTIFIER,
                listenerLinkGenerator);
    }
    
    private void linkProject()
    {
        registerListenerAndLinkGenerator(SampleGridColumnIDs.PROJECT,
                new ICellListenerAndLinkGenerator<Sample>()
                    {
                        public void handle(TableModelRowWithObject<Sample> rowItem,
                                boolean specialKeyPressed)
                        {
                            final Project project =
                                    rowItem.getObjectOrNull().getExperiment().getProject();
                            final String href = LinkExtractor.tryExtract(project);
                            OpenEntityDetailsTabHelper.open(viewContext, project,
                                    specialKeyPressed, href);
                        }

                        public String tryGetLink(Sample entity,
                                ISerializableComparable comparableValue)
                        {
                            final Experiment exp = entity.getExperiment();
                            return exp == null ? null : LinkExtractor.tryExtract(exp.getProject());
                        }
                    });
    }

    private void linkParent()
    {
        registerListenerAndLinkGenerator(SampleGridColumnIDs.PARENTS,
                new ICellListenerAndLinkGenerator<Sample>()
                    {
                        public void handle(TableModelRowWithObject<Sample> rowItem,
                                boolean specialKeyPressed)
                        {
                            Sample parent = getParentOrNull(rowItem.getObjectOrNull());
                            if (parent != null)
                            {
                                showEntityInformationHolderViewer(parent, false, specialKeyPressed);
                            }
                        }

                        public String tryGetLink(Sample entity,
                                ISerializableComparable comparableValue)
                        {
                            Sample parent = getParentOrNull(entity);
                            return LinkExtractor.tryExtract(parent);
                        }

                        private Sample getParentOrNull(Sample entity)
                        {
                            if (entity.getParents().size() == 1)
                            {
                                return entity.getGeneratedFrom();
                            }
                            return null;
                        }
                    });
    }

    private void linkContainer()
    {
        registerListenerAndLinkGenerator(SampleGridColumnIDs.CONTAINER_SAMPLE,
                new ICellListenerAndLinkGenerator<Sample>()
                    {
                        public void handle(TableModelRowWithObject<Sample> rowItem,
                                boolean specialKeyPressed)
                        {
                            showEntityInformationHolderViewer(rowItem.getObjectOrNull().getContainer(), false,
                                    specialKeyPressed);
                        }

                        public String tryGetLink(Sample entity,
                                ISerializableComparable comparableValue)
                        {
                            Sample container = entity.getContainer();
                            return LinkExtractor.tryExtract(container);
                        }
                    });
    }

    

    @Override
    protected GridCellRenderer<BaseEntityModel<?>> createInternalLinkCellRenderer()
    {
        return LinkRenderer.createLinkRenderer(true);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<Sample>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<Sample>> definitions =
                super.createColumnsDefinition();
        definitions.setGridCellRendererFor(SampleGridColumnIDs.REGISTRATOR,
                PersonRenderer.REGISTRATOR_RENDERER);
        definitions.setGridCellRendererFor(SampleGridColumnIDs.SHOW_DETAILS_LINK_COLUMN_NAME,
                createShowDetailsLinkCellRenderer());
        return definitions;
    }

    protected final GridCellRenderer<BaseEntityModel<?>> createShowDetailsLinkCellRenderer()
    {
        return LinkRenderer.createExternalLinkRenderer(viewContext
                .getMessage(Dict.SHOW_DETAILS_LINK_TEXT_VALUE));

    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(SampleGridColumnIDs.CODE, SampleGridColumnIDs.EXPERIMENT, SampleGridColumnIDs.PROJECT);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<Sample>> callback)
    {
        ListSampleDisplayCriteria c1 = getCriteriaProvider().tryGetCriteria();
        ListSampleDisplayCriteria2 criteria;
        if (c1.getCriteriaKind() == ListEntityDisplayCriteriaKind.BROWSE)
        {
            criteria = new ListSampleDisplayCriteria2(c1.getBrowseCriteria());
        } else
        {
            criteria = new ListSampleDisplayCriteria2(c1.getSearchCriteria());
        }
        criteria.copyPagingConfig(resultSetConfig);
        viewContext.getService().listSamples2(criteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportSamples(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        List<DatabaseModificationKind> relevantModifications =
                new ArrayList<DatabaseModificationKind>();
        SetUtils.addAll(relevantModifications, getCriteriaProvider().getRelevantModifications());
        relevantModifications.addAll(getGridRelevantModifications());
        return relevantModifications.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    protected Set<DatabaseModificationKind> getGridRelevantModifications()
    {
        Set<DatabaseModificationKind> result = getGridRelevantModifications(ObjectKind.SAMPLE);
        result.add(edit(ObjectKind.PROJECT));
        return result;
    }

    protected final static Set<DatabaseModificationKind> getGridRelevantModifications(
            ObjectKind entity)
    {
        Set<DatabaseModificationKind> result = new HashSet<DatabaseModificationKind>();
        result.add(createOrDelete(entity));
        result.add(edit(entity));
        result.add(createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT));
        result.add(edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT));
        result.add(edit(ObjectKind.VOCABULARY_TERM));
        return result;
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        getCriteriaProvider().update(observedModifications, new IDataRefreshCallback()
            {
                public void postRefresh(boolean wasSuccessful)
                {
                }
            });
        super.update(observedModifications);
    }

    /**
     * Initializes criteria and refreshes the grid when criteria are fetched. <br>
     * Note that in this way we do not refresh the grid automatically, but we wait until all the
     * property types will be fetched from the server (criteria provider will be updated), to set
     * the available grid columns.
     */
    protected void updateCriteriaProviderAndRefresh()
    {
        HashSet<DatabaseModificationKind> observedModifications =
                new HashSet<DatabaseModificationKind>();
        getCriteriaProvider().update(observedModifications, new IDataRefreshCallback()
            {
                public void postRefresh(boolean wasSuccessful)
                {
                    refresh();
                }
            });
    }

    private void addGridRefreshListener(SampleBrowserToolbar topToolbar)
    {
        topToolbar.setCriteriaChangedListeners(createGridRefreshDelegatedAction());
    }

    protected ICriteriaProvider<ListSampleDisplayCriteria> getCriteriaProvider()
    {
        return propertyTypesAndCriteriaProvider;
    }

    // adds show, show-details and invalidate buttons
    protected void extendBottomToolbar()
    {
        if (viewContext.isSimpleMode())
        {
            return;
        }
        addEntityOperationsLabel();
        addEntityOperationButtons();
        addEntityOperationsSeparator();
    }

    protected void addEntityOperationButtons()
    {

        final Button addButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Sample"),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    openSampleRegistrationTab();
                                }
                            });
        addButton(addButton);

        String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
        Button showDetailsButton =
                createSelectedItemButton(showDetailsTitle, asShowEntityInvoker(false));
        showDetailsButton.setId(createChildComponentId(SHOW_DETAILS_BUTTON_ID_SUFFIX));
        addButton(showDetailsButton);

        String editTitle = viewContext.getMessage(Dict.BUTTON_EDIT);
        Button editButton = createSelectedItemButton(editTitle, asShowEntityInvoker(true));
        editButton.setId(createChildComponentId(EDIT_BUTTON_ID_SUFFIX));
        addButton(editButton);

        final String deleteTitle = viewContext.getMessage(Dict.BUTTON_DELETE);
        final String deleteAllTitle = deleteTitle + " All";
        final Button deleteButton = new Button(deleteAllTitle, new AbstractCreateDialogListener()
            {
                @Override
                protected Dialog createDialog(List<TableModelRowWithObject<Sample>> samples,
                        IBrowserGridActionInvoker invoker)
                {
                    AsyncCallback<Void> callback = createRefreshCallback(invoker);
                    DisplayedAndSelectedEntities<TableModelRowWithObject<Sample>> s =
                            getDisplayedAndSelectedItemsAction().execute();
                    return new SampleListDeletionConfirmationDialog<TableModelRowWithObject<Sample>>(
                            viewContext, samples, callback, s);
                }
            });
        changeButtonTitleOnSelectedItems(deleteButton, deleteAllTitle, deleteTitle);
        addButton(deleteButton);
        allowMultipleSelection(); // we allow deletion of multiple samples
    }

    protected final IDelegatedActionWithResult<DisplayedAndSelectedEntities<TableModelRowWithObject<Sample>>> getDisplayedAndSelectedItemsAction()
    {
        return new IDelegatedActionWithResult<DisplayedAndSelectedEntities<TableModelRowWithObject<Sample>>>()
            {
                public DisplayedAndSelectedEntities<TableModelRowWithObject<Sample>> execute()
                {
                    TableExportCriteria<TableModelRowWithObject<Sample>> tableExportCriteria =
                            createTableExportCriteria();
                    List<TableModelRowWithObject<Sample>> selectedBaseObjects = getSelectedBaseObjects();
                    return new DisplayedAndSelectedEntities<TableModelRowWithObject<Sample>>(selectedBaseObjects,
                            tableExportCriteria, getTotalCount());
                }
            };
    }
    public static final String createChildComponentId(final String browserId,
            final String childSuffix)
    {
        return browserId + childSuffix;
    }

    private final String createChildComponentId(final String childSuffix)
    {
        return createChildComponentId(getId(), childSuffix);
    }

    private void openSampleRegistrationTab()
    {
        if (experimentIdOrNull != null)
        {
            viewContext.getService().getExperimentInfo(experimentIdOrNull,
                    new SampleRegistrationWithExperimentInfoCallback(viewContext));
        } else
        {
            final ActionContext context = new ActionContext();
            final ListSampleDisplayCriteria criteriaOrNull = getCriteriaProvider().tryGetCriteria();
            if (criteriaOrNull != null
                    && criteriaOrNull.getCriteriaKind() == ListEntityDisplayCriteriaKind.BROWSE)
            {
                final ListSampleCriteria browseCriteria = criteriaOrNull.getBrowseCriteria();
                final SampleType sampleType = browseCriteria.getSampleType();
                context.setSampleType(sampleType);
                final String spaceCode = browseCriteria.getSpaceCode();
                context.setSpaceCode(spaceCode);
            }
            DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext)
                    .getSampleRegistration(context));
        }
    }

    private final class SampleRegistrationWithExperimentInfoCallback extends
            AbstractAsyncCallback<Experiment>
    {
        public SampleRegistrationWithExperimentInfoCallback(
                IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Experiment result)
        {
            ActionContext experimentContext = new ActionContext(result);
            DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext
                    .getCommonViewContext()).getSampleRegistration(experimentContext));
        }
    }

    @Override
    protected void showEntityViewer(TableModelRowWithObject<Sample> row, boolean editMode, boolean inBackground)
    {
        showEntityInformationHolderViewer(row.getObjectOrNull(), editMode, inBackground);
    }
    
    protected final IDelegatedAction createGridRefreshDelegatedAction()
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    if (getCriteriaProvider().tryGetCriteria() != null)
                    {
                        refreshGridWithFilters();
                    }
                }
            };
    }

}
