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

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModelFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListEntityDisplayCriteriaKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
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
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + "_grid";

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "_edit-button";

    public static final String SHOW_DETAILS_BUTTON_ID = BROWSER_ID + "_show-details-button";

    /** Creates a grid without additional toolbar buttons. It can serve as a entity chooser. */
    public static DisposableEntityChooser<Sample> createChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, final boolean addShared,
            final boolean excludeWithoutExperiment)
    {
        final SampleBrowserToolbar toolbar =
                new SampleBrowserToolbar(viewContext, addShared, excludeWithoutExperiment);
        ISampleCriteriaProvider criteriaProvider = toolbar;
        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, GRID_ID, BROWSER_ID, true,
                        false)
                    {
                        @Override
                        protected void showEntityViewer(Sample sample, boolean editMode)
                        {
                            // do nothing - avoid showing the details after double click
                        }
                    };
        browserGrid.addGridRefreshListener(toolbar);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final SampleBrowserToolbar toolbar = new SampleBrowserToolbar(viewContext, true, false);
        ISampleCriteriaProvider criteriaProvider = toolbar;
        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, GRID_ID, BROWSER_ID, true,
                        false);
        browserGrid.addGridRefreshListener(toolbar);
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    public static IDisposableComponent createGridForContainerSamples(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final TechId containerSampleId, final String gridId, final SampleType sampleType)
    {
        final ListSampleDisplayCriteria criteria =
                ListSampleDisplayCriteria.createForContainer(containerSampleId);
        final String entityTypeCode = sampleType.getCode();
        final SampleBrowserGrid browserGrid =
                createGridAsComponent(viewContext, gridId, criteria, entityTypeCode);
        browserGrid.updateCriteriaProviderAndRefresh();
        browserGrid.setDisplayTypeIDGenerator(DisplayTypeIDGenerator.SAMPLE_DETAILS_GRID);
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithoutToolbar();
    }

    public static IDisposableComponent createGridForExperimentSamples(
            final IViewContext<ICommonClientServiceAsync> viewContext, final TechId experimentId,
            final String gridId, final ExperimentType experimentType)
    {
        final ListSampleDisplayCriteria criteria =
                ListSampleDisplayCriteria.createForExperiment(experimentId);
        final String entityTypeCode = experimentType.getCode();

        final SampleBrowserGrid browserGrid =
                createGridAsComponent(viewContext, gridId, criteria, entityTypeCode);
        browserGrid.updateCriteriaProviderAndRefresh();
        browserGrid.setDisplayTypeIDGenerator(DisplayTypeIDGenerator.EXPERIMENT_DETAILS_GRID);
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithoutToolbar();
    }

    private static SampleBrowserGrid createGridAsComponent(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String gridId,
            final ListSampleDisplayCriteria criteria, final String entityTypeCode)
    {
        ISampleCriteriaProvider criteriaProvider =
                new SampleCriteriaProvider(viewContext, criteria);
        // we do not refresh the grid, the criteria provider will do this when property types will
        // be loaded
        boolean refreshAutomatically = false;
        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, gridId, BROWSER_ID, false,
                        refreshAutomatically)
                    {
                        @Override
                        protected String getGridDisplayTypeID()
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

        void setEntityTypes(Set<BasicEntityType> availableEntityTypes);
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
        private Set<BasicEntityType> shownEntityTypesOrNull;

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

        public void setEntityTypes(Set<BasicEntityType> entityTypes)
        {
            this.shownEntityTypesOrNull = entityTypes;
        }
    }

    // property types used in the previous refresh operation or null if it has not occurred yet
    private List<PropertyType> previousPropertyTypes;

    // provides property types which will be used to build property columns in the grid and
    // criteria to filter samples
    private final ISampleCriteriaProvider propertyTypesAndCriteriaProvider;

    protected SampleBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ISampleCriteriaProvider criteriaProvider, String gridId, String browserId,
            boolean showHeader, boolean refreshAutomatically)
    {
        super(viewContext, gridId, showHeader, refreshAutomatically);
        this.propertyTypesAndCriteriaProvider = criteriaProvider;
        this.previousPropertyTypes = null;

        registerLinkClickListenerFor(CommonSampleColDefKind.EXPERIMENT.id(),
                new ICellListener<Sample>()
                    {
                        public void handle(Sample rowItem)
                        {
                            // don't need to check whether the value is null
                            // because there will not be a link for null value
                            final Experiment experiment = rowItem.getExperiment();

                            final IEntityInformationHolder entity = experiment;
                            new OpenEntityDetailsTabAction(entity, viewContext).execute();
                        }
                    });
        setId(browserId);
    }

    @Override
    protected ICriteriaProvider<ListSampleDisplayCriteria> getCriteriaProvider()
    {
        return propertyTypesAndCriteriaProvider;
    }

    // adds show, show-details and invalidate buttons
    protected void extendBottomToolbar()
    {
        addEntityOperationsLabel();
        addEntityOperationButtons();
        addEntityOperationsSeparator();
    }

    protected void addEntityOperationButtons()
    {
        final Button addButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Sample"),
                        new SelectionListener<ComponentEvent>()
                            {
                                @Override
                                public void componentSelected(ComponentEvent ce)
                                {
                                    DispatcherHelper.dispatchNaviEvent(new ComponentProvider(
                                            viewContext).getSampleRegistration());
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

        addButton(createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                new AbstractCreateDialogListener()
                    {
                        @Override
                        protected Dialog createDialog(List<Sample> samples,
                                IBrowserGridActionInvoker invoker)
                        {
                            return new SampleListDeletionConfirmationDialog(viewContext, samples,
                                    createDeletionCallback(invoker));
                        }
                    }));
        allowMultipleSelection(); // we allow deletion of multiple samples
    }

    private void addGridRefreshListener(SampleBrowserToolbar topToolbar)
    {
        topToolbar.setCriteriaChangedListener(createGridRefreshListener());
    }

    @Override
    protected EntityType tryToGetEntityType()
    {
        return criteria == null ? null : criteria.tryGetSampleType();
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
                            propertyTypesAndCriteriaProvider.setEntityTypes(result
                                    .getAvailableEntityTypes());
                            callback.onSuccess(result.getResultSet());
                            refreshColumnsSettingsIfNecessary();
                        }
                    };

        criteria.copyPagingConfig(resultSetConfig);
        viewContext.getService().listSamples(criteria, extendedCallback);
    }

    @Override
    protected BaseEntityModel<Sample> createModel(Sample entity)
    {
        return SampleModelFactory.createModel(entity);
    }

    @Override
    protected List<IColumnDefinition<Sample>> getInitialFilters()
    {
        return asColumnFilters(new CommonSampleColDefKind[]
            { CommonSampleColDefKind.CODE, CommonSampleColDefKind.EXPERIMENT,
                    CommonSampleColDefKind.PROJECT });
    }

    @Override
    protected void showEntityViewer(Sample sample, boolean editMode)
    {
        final EntityKind entityKind = EntityKind.SAMPLE;
        final ITabItemFactory tabView;
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        sample.getSampleType());
        final IClientPlugin<SampleType, IIdentifiable> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        if (editMode)
        {
            tabView = createClientPlugin.createEntityEditor(sample);
        } else
        {
            tabView = createClientPlugin.createEntityViewer(sample);
        }
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    @Override
    protected String createHeader()
    {
        assert criteria.getCriteriaKind() == ListEntityDisplayCriteriaKind.BROWSE : "browse criteria expected";
        return doCreateHeader(criteria.getBrowseCriteria());
    }

    private static final String doCreateHeader(ListSampleCriteria criteria)
    {
        if (criteria.getExperimentId() != null || criteria.getContainerSampleId() != null)
        {
            return null;
        }
        SampleType sampleType = criteria.getSampleType();
        final StringBuilder builder = new StringBuilder("Samples");
        if (sampleType != null)
        {
            builder.append(" of type ");
            builder.append(sampleType.getCode());
        }
        if (criteria.isIncludeGroup())
        {
            builder.append(" belonging to the group ");
            builder.append(criteria.getGroupCode());
        }
        if (criteria.isIncludeInstance())
        {
            if (criteria.isIncludeGroup())
            {
                builder.append(" or shared");
            } else
            {
                builder.append(" which are shared among all the groups");
            }
        }
        return builder.toString();
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
        List<PropertyType> propertyTypes = propertyTypesAndCriteriaProvider.tryGetPropertyTypes();
        assert propertyTypes != null : "propertyTypes not set!";

        ColumnDefsAndConfigs<Sample> schema =
                SampleModelFactory.createColumnsSchema(viewContext, propertyTypes, criteria
                        .tryGetSampleType());
        schema.setGridCellRendererFor(CommonSampleColDefKind.SHOW_DETAILS_LINK.id(),
                createShowDetailsLinkCellRenderer());
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

}
