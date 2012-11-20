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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DisplayedAndSelectedEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDirectlyConnectedController;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectCriteria;
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
public class SampleBrowserGrid extends AbstractEntityGrid<Sample>
{
    protected static final IDirectlyConnectedController DUMMY_DIRECTLY_CONNECTED_CONTROLLER =
            new IDirectlyConnectedController()
                {
                    @Override
                    public void setOnChangeAction(IDelegatedAction onChangeAction)
                    {
                    }

                    @Override
                    public boolean isOnlyDirectlyConnected()
                    {
                        return true;
                    }
                };

    private static final String PREFIX = GenericConstants.ID_PREFIX + "sample-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String MAIN_BROWSER_ID = PREFIX + "_main";

    public static final String MAIN_GRID_ID = createGridId(MAIN_BROWSER_ID);

    public static final String ADD_BUTTON_ID_SUFFIX = "_add-button";

    public static final String ADD_METAPROJECTS_BUTTON_ID_SUFFIX = "_add-metaprojects";

    public static final String REMOVE_METAPROJECTS_BUTTON_ID_SUFFIX = "_remove-metaprojects";

    public static final String EDIT_BUTTON_ID_SUFFIX = "_edit-button";

    public static final String SHOW_DETAILS_BUTTON_ID_SUFFIX = "_show-details-button";

    public static final String METAPROJECT_TYPE = "metaproject";

    public static final String createGridId(final String browserId)
    {
        return browserId + GRID_POSTFIX;
    }

    /** Creates a grid without additional toolbar buttons. It can serve as a entity chooser. */
    public static DisposableEntityChooser<TableModelRowWithObject<Sample>> createChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, final boolean addShared,
            boolean addAll, final boolean excludeWithoutExperiment,
            SampleTypeDisplayID sampleTypeID, boolean multipleSelection)
    {
        final SampleBrowserToolbar toolbar =
                new SampleBrowserToolbar(viewContext, addShared, addAll, excludeWithoutExperiment,
                        sampleTypeID);
        ISampleCriteriaProvider criteriaProvider = toolbar;
        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, MAIN_BROWSER_ID, false,
                        DisplayTypeIDGenerator.ENTITY_BROWSER_GRID,
                        DUMMY_DIRECTLY_CONNECTED_CONTROLLER)
                    {
                        @Override
                        protected ICellListenerAndLinkGenerator<Sample> tryGetCellListenerAndLinkGenerator(
                                String columnId)
                        {
                            // No links in choosers needed
                            return null;
                        }

                        @Override
                        protected boolean isEditable(
                                BaseEntityModel<TableModelRowWithObject<Sample>> model,
                                String columnID)
                        {
                            return false;
                        }

                        @Override
                        protected void showNonEditableTableCellMessage(
                                BaseEntityModel<TableModelRowWithObject<Sample>> model,
                                String columnID)
                        {
                            // Do not show a message because in a chooser nobody is expecting
                            // editable table cells.
                        }
                    };
        if (multipleSelection)
        {
            browserGrid.allowMultipleSelection();
        }
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
                        DisplayTypeIDGenerator.ENTITY_BROWSER_GRID,
                        DUMMY_DIRECTLY_CONNECTED_CONTROLLER);
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
                        DisplayTypeIDGenerator.SAMPLE_DETAILS_GRID,
                        DUMMY_DIRECTLY_CONNECTED_CONTROLLER);
        browserGrid.updateCriteriaProviderAndRefresh();
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithoutToolbar();
    }

    public static IDisposableComponent createGridForExperimentSamples(
            final IViewContext<ICommonClientServiceAsync> viewContext, final TechId experimentId,
            final String browserId, final BasicEntityType experimentType,
            IDirectlyConnectedController directlyConnectedController)
    {
        final ListSampleDisplayCriteria criteria =
                ListSampleDisplayCriteria.createForExperiment(experimentId);
        final String entityTypeCode = experimentType.getCode();

        final SampleBrowserGrid browserGrid =
                createGridAsComponent(viewContext, browserId, criteria, entityTypeCode,
                        DisplayTypeIDGenerator.EXPERIMENT_DETAILS_GRID, directlyConnectedController);
        browserGrid.experimentIdOrNull = experimentId;
        browserGrid.updateCriteriaProviderAndRefresh();
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithoutToolbar();
    }

    public static IDisposableComponent createGridForMetaprojectSamples(
            final IViewContext<ICommonClientServiceAsync> viewContext, final TechId metaprojectId,
            final String browserId)
    {
        final ListSampleDisplayCriteria criteria =
                ListSampleDisplayCriteria.createForMetaproject(new MetaprojectCriteria(
                        metaprojectId.getId()));

        final SampleBrowserGrid browserGrid =
                createGridAsComponent(viewContext, browserId, criteria, METAPROJECT_TYPE,
                        DisplayTypeIDGenerator.METAPROJECT_DETAILS_GRID,
                        DUMMY_DIRECTLY_CONNECTED_CONTROLLER);

        browserGrid.updateCriteriaProviderAndRefresh();
        browserGrid.addEntityOperationsLabel();
        browserGrid.addTaggingButtons();
        browserGrid.addEntityOperationsSeparator();
        browserGrid.allowMultipleSelection();
        return browserGrid.asDisposableWithoutToolbar();
    }

    private static SampleBrowserGrid createGridAsComponent(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String browserId,
            final ListSampleDisplayCriteria criteria, final String entityTypeCode,
            DisplayTypeIDGenerator displayTypeIDGenerator,
            IDirectlyConnectedController directlyConnectedController)
    {
        ISampleCriteriaProvider criteriaProvider =
                new SampleCriteriaProvider(viewContext, criteria);
        // we do not refresh the grid, the criteria provider will do this when property types will
        // be loaded
        boolean refreshAutomatically = false;
        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, browserId,
                        refreshAutomatically, displayTypeIDGenerator, directlyConnectedController)
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

        @Override
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

        @Override
        public ListSampleDisplayCriteria tryGetCriteria()
        {
            return criteria;
        }

        @Override
        public void update(Set<DatabaseModificationKind> observedModifications,
                IDataRefreshCallback dataRefreshCallback)
        {
            propertyTypeProvider.update(observedModifications, dataRefreshCallback);
        }

        @Override
        public DatabaseModificationKind[] getRelevantModifications()
        {
            return propertyTypeProvider.getRelevantModifications();
        }

        @Override
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

    private final IDirectlyConnectedController directlyConnectedController;

    protected SampleBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ISampleCriteriaProvider criteriaProvider, String browserId,
            boolean refreshAutomatically, IDisplayTypeIDGenerator displayTypeIDGenerator,
            IDirectlyConnectedController directlyConnectedController)
    {
        super(viewContext, browserId, displayTypeIDGenerator);
        propertyTypesAndCriteriaProvider = criteriaProvider;
        this.directlyConnectedController = directlyConnectedController;
        directlyConnectedController.setOnChangeAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    refresh();
                }
            });
        // NOTE: links to sample, container and experiment are handled by EntityTableCell
        linkProject();
    }

    @Override
    protected boolean supportsExportForUpdate()
    {
        return true;
    }

    @Override
    public String getGridDisplayTypeID()
    {
        ListSampleDisplayCriteria criteria = getCriteriaProvider().tryGetCriteria();
        String suffix =
                createDisplayIdSuffix(EntityKind.SAMPLE,
                        criteria == null ? null : criteria.tryGetSampleType());
        return createGridDisplayTypeID(suffix);
    }

    private void linkProject()
    {
        registerListenerAndLinkGenerator(SampleGridColumnIDs.PROJECT,
                new ICellListenerAndLinkGenerator<Sample>()
                    {
                        @Override
                        public void handle(TableModelRowWithObject<Sample> rowItem,
                                boolean specialKeyPressed)
                        {
                            final Project project =
                                    rowItem.getObjectOrNull().getExperiment().getProject();
                            final String href = LinkExtractor.tryExtract(project);
                            OpenEntityDetailsTabHelper.open(viewContext, project,
                                    specialKeyPressed, href);
                        }

                        @Override
                        public String tryGetLink(Sample entity,
                                ISerializableComparable comparableValue)
                        {
                            final Experiment exp = entity.getExperiment();
                            return exp == null ? null : LinkExtractor.tryExtract(exp.getProject());
                        }
                    });
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
        definitions.setGridCellRendererFor(CommonGridColumnIDs.MODIFIER,
                PersonRenderer.MODIFIER_RENDERER);
        definitions.setGridCellRendererFor(SampleGridColumnIDs.SHOW_DETAILS_LINK_COLUMN_NAME,
                createShowDetailsLinkCellRenderer());
        return definitions;
    }

    @Override
    protected EntityKind getEntityKindOrNull()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(SampleGridColumnIDs.CODE, SampleGridColumnIDs.EXPERIMENT,
                SampleGridColumnIDs.PROJECT);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Sample>> callback)
    {
        ListSampleDisplayCriteria c1 = getCriteriaProvider().tryGetCriteria();
        ListSampleDisplayCriteria2 criteria;
        if (c1.getCriteriaKind() == ListEntityDisplayCriteriaKind.BROWSE)
        {
            criteria = new ListSampleDisplayCriteria2(c1.getBrowseCriteria());
        } else if (c1.getCriteriaKind() == ListEntityDisplayCriteriaKind.SEARCH)
        {
            criteria = new ListSampleDisplayCriteria2(c1.getSearchCriteria());
        } else if (c1.getCriteriaKind() == ListEntityDisplayCriteriaKind.METAPROJECT)
        {
            criteria = new ListSampleDisplayCriteria2(c1.getMetaprojectCriteria());
        } else
        {
            throw new IllegalArgumentException("Unsupported criteria kind: " + c1.getCriteriaKind());
        }
        criteria.copyPagingConfig(resultSetConfig);
        if (criteria.getCriteriaKind() == ListEntityDisplayCriteriaKind.BROWSE)
        {
            criteria.getBrowseCriteria().setOnlyDirectlyConnected(
                    directlyConnectedController.isOnlyDirectlyConnected());
        }
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
        ICriteriaProvider<?> criteriaProvider = getCriteriaProvider();
        return GridUtils.getRelevantModifications(ObjectKind.SAMPLE, criteriaProvider);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        ICriteriaProvider<ListSampleDisplayCriteria> criteriaProvider = getCriteriaProvider();
        if (criteriaProvider.tryGetCriteria() != null)
        {
            criteriaProvider.update(observedModifications, new IDataRefreshCallback()
                {
                    @Override
                    public void postRefresh(boolean wasSuccessful)
                    {
                    }
                });
            super.update(observedModifications);
        }
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
                @Override
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
        if (viewContext.isSimpleOrEmbeddedMode())
        {
            return;
        }
        addEntityOperationsLabel();
        addEntityOperationButtons();
        addEntityOperationsSeparator();
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
                            List<Long> sampleIds = new ArrayList<Long>();
                            for (BaseEntityModel<TableModelRowWithObject<Sample>> item : getSelectedItems())
                            {
                                sampleIds.add(item.getBaseObject().getObjectOrNull().getId());
                            }

                            List<Long> metaProjectIds = new ArrayList<Long>();
                            for (TableModelRowWithObject<Metaproject> row : entities)
                            {
                                metaProjectIds.add(row.getObjectOrNull().getId());
                            }
                            viewContext.getCommonService().assignSamplesToMetaProjects(
                                    metaProjectIds, sampleIds,
                                    createRefreshCallback(asActionInvoker()));

                        }
                    });

        tagButton.setId(createChildComponentId(ADD_METAPROJECTS_BUTTON_ID_SUFFIX));
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
                            List<Long> sampleIds = new ArrayList<Long>();
                            for (BaseEntityModel<TableModelRowWithObject<Sample>> item : getSelectedItems())
                            {
                                sampleIds.add(item.getBaseObject().getObjectOrNull().getId());
                            }

                            List<Long> metaProjectIds = new ArrayList<Long>();
                            for (TableModelRowWithObject<Metaproject> row : entities)
                            {
                                metaProjectIds.add(row.getObjectOrNull().getId());
                            }
                            viewContext.getCommonService()
                                    .removeSamplesFromMetaProjects(
                                            metaProjectIds, sampleIds,
                                            createRefreshCallback(asActionInvoker()));

                        }
                    });

        untagButton.setId(createChildComponentId(REMOVE_METAPROJECTS_BUTTON_ID_SUFFIX));
        untagButton.setText(viewContext.getMessage(Dict.BUTTON_UNTAG));
        enableButtonOnSelectedItems(untagButton);
        addButton(untagButton);

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
        addButton.setId(createChildComponentId(ADD_BUTTON_ID_SUFFIX));
        addButton(addButton);

        addTaggingButtons();

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
                    AbstractAsyncCallback<Void> callback = createRefreshCallback(invoker);
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
                @Override
                public DisplayedAndSelectedEntities<TableModelRowWithObject<Sample>> execute()
                {
                    TableExportCriteria<TableModelRowWithObject<Sample>> tableExportCriteria =
                            createTableExportCriteria();
                    List<TableModelRowWithObject<Sample>> selectedBaseObjects =
                            getSelectedBaseObjects();
                    return new DisplayedAndSelectedEntities<TableModelRowWithObject<Sample>>(
                            selectedBaseObjects, tableExportCriteria, getTotalCount());
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

    protected final IDelegatedAction createGridRefreshDelegatedAction()
    {
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    if (getCriteriaProvider().tryGetCriteria() != null)
                    {
                        refreshGridWithFilters();
                    }
                }
            };
    }

    private List<Taggable> taggables(List<BaseEntityModel<TableModelRowWithObject<Sample>>> data)
    {
        List<Taggable> list = new ArrayList<Taggable>();
        for (final BaseEntityModel<TableModelRowWithObject<Sample>> item : data)
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
