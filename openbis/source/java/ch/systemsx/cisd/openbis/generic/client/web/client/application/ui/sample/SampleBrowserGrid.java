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

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.IEditableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.SetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * A {@link LayoutContainer} which contains the grid where the samples are displayed.
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 */
public final class SampleBrowserGrid extends AbstractBrowserGrid<Sample, SampleModel>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "sample-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + "_grid";

    private final ICriteriaProvider criteriaProvider;

    // criteria used in the previous refresh operation or null if it has not occurred yet
    private ListSampleCriteria criteria;

    private interface ICriteriaProvider
    {
        ListSampleCriteria tryGetCriteria();

        IDatabaseModificationObserver tryGetModificationObserver();
    }

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final SampleBrowserToolbar toolbar = new SampleBrowserToolbar(viewContext);
        ICriteriaProvider criteriaProvider = asCriteriaProvider(toolbar);
        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, GRID_ID, true, false);
        browserGrid.extendTopToolbar(toolbar);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    private static ICriteriaProvider asCriteriaProvider(final SampleBrowserToolbar toolbar)
    {
        return new ICriteriaProvider()
            {
                public ListSampleCriteria tryGetCriteria()
                {
                    return toolbar.tryGetCriteria();
                }

                public IDatabaseModificationObserver tryGetModificationObserver()
                {
                    return toolbar;
                }
            };
    }

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final String experimentIdentifier, String gridId)
    {
        ICriteriaProvider criteriaProvider = new ICriteriaProvider()
            {
                public ListSampleCriteria tryGetCriteria()
                {
                    ListSampleCriteria criteria = new ListSampleCriteria();
                    criteria.setExperimentIdentifier(experimentIdentifier);
                    return criteria;
                }

                public IDatabaseModificationObserver tryGetModificationObserver()
                {
                    return null;
                }
            };

        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, gridId, false, true);
        return browserGrid.asDisposableWithoutToolbar();
    }

    private SampleBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ICriteriaProvider criteriaProvider, String gridId, boolean showHeader,
            boolean refreshAutomatically)
    {
        super(viewContext, gridId, showHeader, refreshAutomatically);

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
        this.criteriaProvider = criteriaProvider;
        setId(BROWSER_ID);
    }

    // adds show, show-details and invalidate buttons
    private void extendTopToolbar(SampleBrowserToolbar toolbar)
    {
        String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
        Button showDetailsButton =
                createSelectedItemButton(showDetailsTitle, asShowEntityInvoker(false));

        SelectionChangedListener<?> refreshButtonListener = addRefreshButton(toolbar);
        toolbar.setCriteriaChangedListener(refreshButtonListener);

        toolbar.add(new FillToolItem());
        toolbar.add(new AdapterToolItem(showDetailsButton));

        toolbar.add(new SeparatorToolItem());
        String editTitle = viewContext.getMessage(Dict.BUTTON_EDIT);
        Button editButton = createSelectedItemButton(editTitle, asShowEntityInvoker(true));
        toolbar.add(new AdapterToolItem(editButton));
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return criteriaProvider.tryGetCriteria() != null;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Sample> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Sample>> callback)
    {
        copyPagingConfig(resultSetConfig);
        viewContext.getService().listSamples(criteria, callback);
    }

    private void copyPagingConfig(DefaultResultSetConfig<String, Sample> resultSetConfig)
    {
        criteria.setLimit(resultSetConfig.getLimit());
        criteria.setOffset(resultSetConfig.getOffset());
        criteria.setSortInfo(resultSetConfig.getSortInfo());
        criteria.setFilterInfos(resultSetConfig.getFilterInfos());
        criteria.setResultSetKey(resultSetConfig.getResultSetKey());
    }

    @Override
    protected SampleModel createModel(Sample entity)
    {
        return new SampleModel(entity);
    }

    @Override
    protected List<IColumnDefinition<Sample>> getAvailableFilters()
    {
        return asColumnFilters(new CommonSampleColDefKind[]
            { CommonSampleColDefKind.CODE, CommonSampleColDefKind.EXPERIMENT,
                    CommonSampleColDefKind.PROJECT });
    }

    @Override
    protected void showEntityViewer(SampleModel sampleModel, boolean editMode)
    {
        final Sample sample = sampleModel.getBaseObject();
        final EntityKind entityKind = EntityKind.SAMPLE;
        final ITabItemFactory tabView;
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        sample.getSampleType());
        if (editMode)
        {
            final IClientPlugin<SampleType, SampleTypePropertyType, SampleProperty, IIdentifierHolder, EditableSample> createClientPlugin =
                    clientPluginFactory.createClientPlugin(entityKind);
            EditableSample editableEntity = new EditableSample(sample, criteria.getSampleType());
            tabView = createClientPlugin.createEntityEditor(editableEntity);
        } else
        {

            final IClientPlugin<EntityType, EntityTypePropertyType<EntityType>, EntityProperty<EntityType, EntityTypePropertyType<EntityType>>, IIdentifierHolder, IEditableEntity<EntityType, EntityTypePropertyType<EntityType>, EntityProperty<EntityType, EntityTypePropertyType<EntityType>>>> createClientPlugin =
                    clientPluginFactory.createClientPlugin(entityKind);
            tabView = createClientPlugin.createEntityViewer(sample);
        }
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    private static final String createHeader(ListSampleCriteria criteria)
    {
        final StringBuilder builder = new StringBuilder("Samples");
        if (criteria.getSampleType() != null)
        {
            builder.append(" of type ");
            builder.append(criteria.getSampleType().getCode());
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
        if (criteria.getExperimentIdentifier() != null)
        {
            return null;
        }
        return builder.toString();
    }

    /**
     * Refreshes the sample browser grid up to given parameters.
     * <p>
     * Note that, doing so, the result set associated on the server side with this
     * <code>resultSetKey</code> will be removed.
     * </p>
     */
    @Override
    protected final void refresh()
    {
        ListSampleCriteria newCriteria = criteriaProvider.tryGetCriteria();
        if (newCriteria == null)
        {
            return;
        }
        boolean refreshColumnsDefinition = hasColumnsDefinitionChanged(newCriteria);
        this.criteria = newCriteria;
        String newHeader = createHeader(newCriteria);

        super.refresh(newHeader, refreshColumnsDefinition);
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
        return SampleModel.createColumnsSchema(viewContext, criteria.getSampleType());
    }

    private boolean hasColumnsDefinitionChanged(ListSampleCriteria newCriteria)
    {

        SampleType sampleType = newCriteria.getSampleType();
        return criteria == null || sampleType != null
                && sampleType.equals(criteria.getSampleType()) == false;
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        List<DatabaseModificationKind> relevantModifications =
                new ArrayList<DatabaseModificationKind>();
        IDatabaseModificationObserver criteriaModificationObserver =
                criteriaProvider.tryGetModificationObserver();
        if (criteriaModificationObserver != null)
        {
            SetUtils.addAll(relevantModifications, criteriaModificationObserver
                    .getRelevantModifications());
        }
        relevantModifications.addAll(getGridRelevantModifications());
        return relevantModifications.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    private static Set<DatabaseModificationKind> getGridRelevantModifications()
    {
        Set<DatabaseModificationKind> result = new HashSet<DatabaseModificationKind>();
        result.add(createOrDelete(ObjectKind.SAMPLE));
        result.add(edit(ObjectKind.SAMPLE));
        result.add(createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT));
        return result;
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        IDatabaseModificationObserver criteriaModificationObserver =
                criteriaProvider.tryGetModificationObserver();
        if (criteriaModificationObserver != null)
        {
            criteriaModificationObserver.update(observedModifications);
        }
        if (SetUtils.containsAny(observedModifications, getGridRelevantModifications()))
        {
            refreshGridSilently();
        }
    }
}
