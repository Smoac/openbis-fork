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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DisplayedAndSelectedEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListEntityDisplayCriteriaKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SampleBrowserGrid2 extends TypedTableGrid<Sample>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "sample-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String MAIN_BROWSER_ID = PREFIX + "_main";

    public static final String MAIN_GRID_ID = createGridId(MAIN_BROWSER_ID);

    public static final String GRID_ID_SUFFIX = "_grid";

    public static final String EDIT_BUTTON_ID_SUFFIX = "_edit-button";

    public static final String SHOW_DETAILS_BUTTON_ID_SUFFIX = "_show-details-button";

    public static final String createGridId(final String browserId)
    {
        return browserId + GRID_ID_SUFFIX;
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
        final SampleBrowserGrid2 browserGrid =
                new SampleBrowserGrid2(viewContext, criteriaProvider, MAIN_BROWSER_ID, false,
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
        final SampleBrowserGrid2 browserGrid =
                new SampleBrowserGrid2(viewContext, criteriaProvider, MAIN_BROWSER_ID, false,
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
        final SampleBrowserGrid2 browserGrid =
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

        final SampleBrowserGrid2 browserGrid =
                createGridAsComponent(viewContext, browserId, criteria, entityTypeCode,
                        DisplayTypeIDGenerator.EXPERIMENT_DETAILS_GRID);
        browserGrid.experimentIdOrNull = experimentId;
        browserGrid.updateCriteriaProviderAndRefresh();
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithoutToolbar();
    }

    private static SampleBrowserGrid2 createGridAsComponent(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String browserId,
            final ListSampleDisplayCriteria criteria, final String entityTypeCode,
            DisplayTypeIDGenerator displayTypeIDGenerator)
    {
        ISampleCriteriaProvider criteriaProvider =
                new SampleCriteriaProvider(viewContext, criteria);
        // we do not refresh the grid, the criteria provider will do this when property types will
        // be loaded
        boolean refreshAutomatically = false;
        final SampleBrowserGrid2 browserGrid =
                new SampleBrowserGrid2(viewContext, criteriaProvider, browserId,
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

    // property types used in the previous refresh operation or null if it has not occurred yet
    private List<PropertyType> previousPropertyTypes;

    // provides property types which will be used to build property columns in the grid and
    // criteria to filter samples
    private final ISampleCriteriaProvider propertyTypesAndCriteriaProvider;

    private TechId experimentIdOrNull;

    protected SampleBrowserGrid2(final IViewContext<ICommonClientServiceAsync> viewContext,
            ISampleCriteriaProvider criteriaProvider, String browserId,
            boolean refreshAutomatically, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId, displayTypeIDGenerator);
        propertyTypesAndCriteriaProvider = criteriaProvider;
    }
    
    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<Sample>> callback)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        // TODO Auto-generated method stub
        
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
                    AsyncCallback<Void> callback = createDeletionCallback(invoker);
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
    protected void refresh()
    {
        super.refresh();
        previousPropertyTypes = propertyTypesAndCriteriaProvider.tryGetPropertyTypes();
    }

    protected void showEntityViewer(Sample sample, boolean editMode, boolean inBackground)
    {
        showEntityInformationHolderViewer(sample, editMode, inBackground);
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
