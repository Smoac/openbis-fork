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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.HashSet;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModelFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DisplayedAndSelectedEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.AbstractParentSampleColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListEntityDisplayCriteriaKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * A {@link LayoutContainer} which contains the grid where the samples are displayed.
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 */
public class SampleBrowserGrid extends
        AbstractEntityBrowserGrid<Sample, BaseEntityModel<Sample>, ListSampleDisplayCriteria>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "sample-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String MAIN_BROWSER_ID = PREFIX + "_main";

    public static final String MAIN_GRID_ID = createGridId(MAIN_BROWSER_ID);

    public static final String GRID_ID_SUFFIX = "_grid";

    public static final String EDIT_BUTTON_ID_SUFFIX = "_edit-button";

    public static final String SHOW_DETAILS_BUTTON_ID_SUFFIX = "_show-details-button";

    /** Creates a grid without additional toolbar buttons. It can serve as a entity chooser. */
    public static DisposableEntityChooser<Sample> createChooser(
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
                        protected void showEntityViewer(Sample sample, boolean editMode,
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
        final String entityTypeCode = sampleType.getCode();

        final SampleBrowserGrid browserGrid =
                createGridAsComponent(viewContext, browserId, criteria, entityTypeCode,
                        DisplayTypeIDGenerator.SAMPLE_DETAILS_GRID);
        browserGrid.updateCriteriaProviderAndRefresh();
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithoutToolbar();
    }

    public static IDisposableComponent createGridForDerivedSamples(
            final IViewContext<ICommonClientServiceAsync> viewContext, final TechId parentSampleId,
            final String browserId, final SampleType sampleType)
    {
        final ListSampleDisplayCriteria criteria =
                ListSampleDisplayCriteria.createForParent(parentSampleId);
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

    public static interface ISampleCriteriaProvider extends
            ICriteriaProvider<ListSampleDisplayCriteria>, IPropertyTypesProvider
    {
    }

    public static interface IPropertyTypesProvider
    {
        List<PropertyType> tryGetPropertyTypes();

        void setEntityTypes(Set<SampleType> availableEntityTypes);
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

    // property types used in the previous refresh operation or null if it has not occurred yet
    private List<PropertyType> previousPropertyTypes;

    // provides property types which will be used to build property columns in the grid and
    // criteria to filter samples
    private final ISampleCriteriaProvider propertyTypesAndCriteriaProvider;

    private TechId experimentIdOrNull;

    protected SampleBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ISampleCriteriaProvider criteriaProvider, String browserId,
            boolean refreshAutomatically, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, createGridId(browserId), refreshAutomatically, displayTypeIDGenerator);
        this.propertyTypesAndCriteriaProvider = criteriaProvider;
        this.previousPropertyTypes = null;

        registerLinkClickListenerFor(CommonSampleColDefKind.SUBCODE.id(),
                showEntityViewerLinkClickListener);
        registerLinkClickListenerFor(CommonSampleColDefKind.SAMPLE_IDENTIFIER.id(),
                showEntityViewerLinkClickListener);
        ICellListener<Sample> experimentClickListener = new OpenEntityDetailsTabCellClickListener()
            {
                @Override
                protected IEntityInformationHolder getEntity(Sample rowItem)
                {
                    return rowItem.getExperiment();
                }
            };
        registerLinkClickListenerFor(CommonSampleColDefKind.EXPERIMENT.id(),
                experimentClickListener);
        registerLinkClickListenerFor(CommonSampleColDefKind.EXPERIMENT_IDENTIFIER.id(),
                experimentClickListener);
        registerLinkClickListenerFor(CommonSampleColDefKind.PROJECT.id(),
                new ICellListener<Sample>()
                    {
                        public void handle(Sample rowItem, boolean keyPressed)
                        {
                            OpenEntityDetailsTabHelper.open(viewContext, rowItem.getExperiment()
                                    .getProject(), keyPressed);
                        }
                    });
        setId(browserId);
    }

    public static final String createGridId(final String browserId)
    {
        return browserId + GRID_ID_SUFFIX;
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

    private abstract class OpenEntityDetailsTabCellClickListener implements ICellListener<Sample>
    {
        protected abstract IEntityInformationHolder getEntity(Sample rowItem);

        public final void handle(Sample rowItem, boolean keyPressed)
        {
            // don't need to check whether the value is null
            // because there will not be a link for null value
            final IEntityInformationHolder entity = getEntity(rowItem);
            new OpenEntityDetailsTabAction(entity, viewContext, keyPressed).execute();
        }
    }

    @Override
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
                protected Dialog createDialog(List<Sample> samples,
                        IBrowserGridActionInvoker invoker)
                {
                    return new SampleListDeletionConfirmationDialog(viewContext, samples,
                            createDeletionCallbackWithProgressBar(invoker, "Deleting samples..."),
                            getDisplayedAndSelectedItemsAction().execute());
                }
            });
        changeButtonTitleOnSelectedItems(deleteButton, deleteAllTitle, deleteTitle);
        addButton(deleteButton);
        allowMultipleSelection(); // we allow deletion of multiple samples
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

    private void addGridRefreshListener(SampleBrowserToolbar topToolbar)
    {
        topToolbar.setCriteriaChangedListeners(createGridRefreshDelegatedAction());
    }

    @Override
    protected EntityType tryToGetEntityType()
    {
        return criteria == null ? null : criteria.tryGetSampleType();
    }

    @Override
    protected void refresh()
    {
        super.refresh();
        previousPropertyTypes = propertyTypesAndCriteriaProvider.tryGetPropertyTypes();
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Sample> resultSetConfig,
            final AbstractAsyncCallback<ResultSet<Sample>> callback)
    {
        AbstractAsyncCallback<ResultSetWithEntityTypes<Sample>> extendedCallback =
                new AbstractAsyncCallback<ResultSetWithEntityTypes<Sample>>(viewContext)
                    {
                        @Override
                        protected void process(ResultSetWithEntityTypes<Sample> result)
                        {
                            propertyTypesAndCriteriaProvider
                                    .setEntityTypes(extractAvailableSampleTypes(result));
                            callback.onSuccess(result.getResultSet());
                            refreshColumnsSettingsIfNecessary();
                            previousPropertyTypes =
                                    propertyTypesAndCriteriaProvider.tryGetPropertyTypes();
                        }

                        private Set<SampleType> extractAvailableSampleTypes(
                                ResultSetWithEntityTypes<Sample> result)
                        {
                            Set<SampleType> sampleTypes = new HashSet<SampleType>();
                            for (BasicEntityType basicType : result.getAvailableEntityTypes())
                            {
                                assert basicType instanceof SampleType;
                                sampleTypes.add((SampleType) basicType);
                            }
                            return sampleTypes;
                        }

                        @Override
                        public void finishOnFailure(Throwable caught)
                        {
                            callback.finishOnFailure(caught);
                        }

                    };
        criteria.copyPagingConfig(resultSetConfig);
        viewContext.getService().listSamples(criteria, extendedCallback);
    }

    @Override
    protected BaseEntityModel<Sample> createModel(GridRowModel<Sample> entity)
    {
        return SampleModelFactory.createModel(viewContext, entity, criteria.tryGetSampleType(),
                viewContext.getDisplaySettingsManager().getRealNumberFormatingParameters());
    }

    @Override
    protected List<IColumnDefinition<Sample>> getInitialFilters()
    {
        return asColumnFilters(new CommonSampleColDefKind[]
            { CommonSampleColDefKind.CODE, CommonSampleColDefKind.EXPERIMENT,
                    CommonSampleColDefKind.PROJECT });
    }

    @Override
    protected void showEntityViewer(Sample sample, boolean editMode, boolean active)
    {
        showEntityInformationHolderViewer(sample, editMode, active);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Sample> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportSamples(exportCriteria, callback);
    }

    @Override
    protected ColumnDefsAndConfigs<Sample> createColumnsDefinition()
    {
        assert criteria != null : "criteria not set!";
        final List<PropertyType> propertyTypes =
                propertyTypesAndCriteriaProvider.tryGetPropertyTypes();
        assert propertyTypes != null : "propertyTypes not set!";

        final List<AbstractParentSampleColDef> parentColumnsSchema =
                SampleModelFactory.createParentColumnsSchema(viewContext, criteria
                        .tryGetSampleType());
        assert parentColumnsSchema != null : "parentColumnsSchema not set!";

        ColumnDefsAndConfigs<Sample> schema =
                SampleModelFactory.createColumnsSchema(viewContext, propertyTypes,
                        parentColumnsSchema);

        schema.setGridCellRendererFor(CommonSampleColDefKind.SHOW_DETAILS_LINK.id(),
                createShowDetailsLinkCellRenderer());

        GridCellRenderer<BaseEntityModel<?>> linkCellRenderer = createInternalLinkCellRenderer();
        schema.setGridCellRendererFor(CommonSampleColDefKind.SUBCODE.id(), linkCellRenderer);
        schema.setGridCellRendererFor(CommonSampleColDefKind.SAMPLE_IDENTIFIER.id(),
                linkCellRenderer);
        schema.setGridCellRendererFor(CommonSampleColDefKind.EXPERIMENT.id(), linkCellRenderer);
        schema.setGridCellRendererFor(CommonSampleColDefKind.EXPERIMENT_IDENTIFIER.id(),
                linkCellRenderer);
        schema.setGridCellRendererFor(CommonSampleColDefKind.PROJECT.id(), linkCellRenderer);
        // setup link renderers and listeners on parent columns
        for (final AbstractParentSampleColDef parentColDef : parentColumnsSchema)
        {
            schema.setGridCellRendererFor(parentColDef.getIdentifier(), linkCellRenderer);
            registerLinkClickListenerFor(parentColDef.getIdentifier(),
                    new OpenEntityDetailsTabCellClickListener()
                        {
                            @Override
                            protected IEntityInformationHolder getEntity(Sample rowItem)
                            {
                                return parentColDef.tryGetParent(rowItem);
                            }
                        });
        }

        return schema;
    }

    @Override
    protected boolean hasColumnsDefinitionChanged(ListSampleDisplayCriteria newCriteria)
    {
        List<PropertyType> newPropertyTypes =
                propertyTypesAndCriteriaProvider.tryGetPropertyTypes();
        if (newPropertyTypes == null)
        {
            return false; // we are before the first auto-refresh
        }
        if (previousPropertyTypes == null)
        {
            return true; // first refresh
        }
        if (previousPropertyTypes.equals(newPropertyTypes) == false)
        {
            return true;
        }
        EntityType newEntityType = newCriteria.tryGetSampleType();
        EntityType prevEntityType = (criteria == null ? null : criteria.tryGetSampleType());
        return hasColumnsDefinitionChanged(newEntityType, prevEntityType);
    }

    @Override
    protected Set<DatabaseModificationKind> getGridRelevantModifications()
    {
        Set<DatabaseModificationKind> result = getGridRelevantModifications(ObjectKind.SAMPLE);
        result.add(edit(ObjectKind.PROJECT));
        return result;
    }

    @Override
    protected IColumnDefinitionKind<Sample>[] getStaticColumnsDefinition()
    {
        return CommonSampleColDefKind.values();
    }

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    public final class DisplayedAndSelectedSamples extends DisplayedAndSelectedEntities<Sample>
    {
        public DisplayedAndSelectedSamples(List<Sample> selectedItems,
                TableExportCriteria<Sample> displayedItemsConfig, int displayedItemsCount)
        {
            super(selectedItems, displayedItemsConfig, displayedItemsCount);
        }

    }

    protected final IDelegatedActionWithResult<DisplayedAndSelectedSamples> getDisplayedAndSelectedItemsAction()
    {
        return new IDelegatedActionWithResult<DisplayedAndSelectedSamples>()
            {
                public DisplayedAndSelectedSamples execute()
                {
                    return new DisplayedAndSelectedSamples(getSelectedBaseObjects(),
                            createTableExportCriteria(), getTotalCount());
                }
            };
    }

}
