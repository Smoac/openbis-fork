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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.RealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns.SampleAbundanceColDefKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.model.SampleAbundanceModelFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListSampleAbundanceByProteinCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;

/**
 * A {@link LayoutContainer} which contains the grid where {@link SampleWithPropertiesAndAbundance}s
 * of specified Protein are displayed.
 * 
 * @author Piotr Buczek
 */
public class SampleAbundanceBrowserGrid
        extends
        AbstractEntityBrowserGrid<SampleWithPropertiesAndAbundance, BaseEntityModel<SampleWithPropertiesAndAbundance>, ListSampleAbundanceByProteinCriteria>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "sample_abundance-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + "_grid";

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "_edit-button";

    public static final String SHOW_DETAILS_BUTTON_ID = BROWSER_ID + "_show-details-button";

    private final IViewContext<IPhosphoNetXClientServiceAsync> phosphoViewContext;

    public static IDisposableComponent createGridForProteinSamples(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            final TechId proteinReferenceID, Long experimentIDOrNull, final String gridId)
    {
        final ListSampleAbundanceByProteinCriteria criteria =
                new ListSampleAbundanceByProteinCriteria();
        criteria.setProteinReferenceID(proteinReferenceID);
        criteria.setExperimentID(experimentIDOrNull);

        ISampleAbundanceCriteriaProvider criteriaProvider =
                new SampleAbundanceCriteriaProvider(viewContext, criteria);
        // we do not refresh the grid, the criteria provider will do this when property types will
        // be loaded
        boolean refreshAutomatically = false;
        final SampleAbundanceBrowserGrid browserGrid =
                new SampleAbundanceBrowserGrid(viewContext, criteriaProvider, gridId,
                        refreshAutomatically);
        browserGrid.updateCriteriaProviderAndRefresh();
        browserGrid.extendBottomToolbar();
        return browserGrid.asDisposableWithoutToolbar();
    }

    public static interface ISampleAbundanceCriteriaProvider extends
            ICriteriaProvider<ListSampleAbundanceByProteinCriteria>, IPropertyTypesProvider
    {
    }

    public static interface IPropertyTypesProvider
    {
        public List<PropertyType> tryGetPropertyTypes();
    }

    /**
     * Besides providing the static {@link ListSampleAbundanceByProteinCriteria} this class provides
     * all property types which should be used to build the grid property columns. It is also able
     * to refresh these properties from the server.
     */
    private static class SampleAbundanceCriteriaProvider implements
            ISampleAbundanceCriteriaProvider
    {
        private final ICriteriaProvider<PropertyTypesCriteria> propertyTypeProvider;

        private final ListSampleAbundanceByProteinCriteria criteria;

        public SampleAbundanceCriteriaProvider(IViewContext<?> viewContext,
                ListSampleAbundanceByProteinCriteria criteria)
        {
            this.propertyTypeProvider =
                    new PropertyTypesCriteriaProvider(viewContext, EntityKind.SAMPLE);
            this.criteria = criteria;
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

        public ListSampleAbundanceByProteinCriteria tryGetCriteria()
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

    }

    // property types used in the previous refresh operation or null if it has not occurred yet
    private final List<PropertyType> previousPropertyTypes;

    // provides property types which will be used to build property columns in the grid and
    // criteria to filter samples
    private final ISampleAbundanceCriteriaProvider propertyTypesAndCriteriaProvider;

    private SampleAbundanceBrowserGrid(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            ISampleAbundanceCriteriaProvider criteriaProvider, String gridId,
            boolean refreshAutomatically)
    {
        super(viewContext.getCommonViewContext(), gridId, refreshAutomatically,
                DisplayTypeIDGenerator.SAMPLE_DETAILS_GRID);
        this.phosphoViewContext = viewContext;
        this.propertyTypesAndCriteriaProvider = criteriaProvider;
        this.previousPropertyTypes = null;
        setId(BROWSER_ID);
    }

    @Override
    protected ICriteriaProvider<ListSampleAbundanceByProteinCriteria> getCriteriaProvider()
    {
        return propertyTypesAndCriteriaProvider;
    }

    // adds show, show-details and invalidate buttons
    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

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
                        protected Dialog createDialog(
                                List<SampleWithPropertiesAndAbundance> samples,
                                IBrowserGridActionInvoker invoker)
                        {
                            return new SampleListDeletionConfirmationDialog(viewContext, samples,
                                    createDeletionCallback(invoker));
                        }
                    }));
        allowMultipleSelection(); // we allow deletion of multiple samples

        addEntityOperationsSeparator();
    }

    private final static class SampleListDeletionConfirmationDialog extends
            AbstractDataListDeletionConfirmationDialog<SampleWithPropertiesAndAbundance>
    {

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final AbstractAsyncCallback<Void> callback;

        public SampleListDeletionConfirmationDialog(
                IViewContext<ICommonClientServiceAsync> viewContext,
                List<SampleWithPropertiesAndAbundance> data, AbstractAsyncCallback<Void> callback)
        {
            super(viewContext, data);
            this.viewContext = viewContext;
            this.callback = callback;
        }

        @Override
        protected void executeConfirmedAction()
        {
            viewContext.getCommonService().deleteSamples(TechId.createList(data),
                    reason.getValue(), callback);
        }

        @Override
        protected String getEntityName()
        {
            return EntityKind.SAMPLE.getDescription();
        }

    }

    @Override
    protected EntityType tryToGetEntityType()
    {
        return null;
    }

    @Override
    protected void listEntities(
            DefaultResultSetConfig<String, SampleWithPropertiesAndAbundance> resultSetConfig,
            AbstractAsyncCallback<ResultSet<SampleWithPropertiesAndAbundance>> callback)
    {
        criteria.copyPagingConfig(resultSetConfig);
        phosphoViewContext.getService().listSamplesWithAbundanceByProtein(criteria, callback);
    }

    @Override
    protected BaseEntityModel<SampleWithPropertiesAndAbundance> createModel(
            GridRowModel<SampleWithPropertiesAndAbundance> entity)
    {
        return SampleAbundanceModelFactory.createModel(viewContext, entity, viewContext
                .getDisplaySettingsManager().getRealNumberFormatingParameters());
    }

    @Override
    protected List<IColumnDefinition<SampleWithPropertiesAndAbundance>> getInitialFilters()
    {
        return asColumnFilters(new SampleAbundanceColDefKind[]
            { SampleAbundanceColDefKind.CODE, SampleAbundanceColDefKind.SAMPLE_IDENTIFIER,
                    SampleAbundanceColDefKind.ABUNDANCE });
    }

    @Override
    protected void showEntityViewer(SampleWithPropertiesAndAbundance sample, boolean editMode,
            boolean active)
    {
        showEntityInformationHolderViewer(sample, editMode, active);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<SampleWithPropertiesAndAbundance> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        phosphoViewContext.getService().prepareExportSamplesWithAbundance(exportCriteria, callback);
    }

    @Override
    protected ColumnDefsAndConfigs<SampleWithPropertiesAndAbundance> createColumnsDefinition()
    {
        assert criteria != null : "criteria not set!";
        List<PropertyType> propertyTypes = propertyTypesAndCriteriaProvider.tryGetPropertyTypes();
        assert propertyTypes != null : "propertyTypes not set!";

        ColumnDefsAndConfigs<SampleWithPropertiesAndAbundance> schema =
                SampleAbundanceModelFactory.createColumnsSchema(viewContext, propertyTypes);
        schema.setGridCellRendererFor(SampleAbundanceColDefKind.CODE.id(),
                createInternalLinkCellRenderer());
        schema.setGridCellRendererFor(SampleAbundanceColDefKind.ABUNDANCE.id(),
                new RealNumberRenderer(viewContext.getDisplaySettingsManager()
                        .getRealNumberFormatingParameters()));
        return schema;
    }

    @Override
    protected boolean hasColumnsDefinitionChanged(ListSampleAbundanceByProteinCriteria newCriteria)
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
        EntityType newEntityType = null;
        EntityType prevEntityType = null;
        return hasColumnsDefinitionChanged(newEntityType, prevEntityType);
    }

    @Override
    protected Set<DatabaseModificationKind> getGridRelevantModifications()
    {
        Set<DatabaseModificationKind> result = getGridRelevantModifications(ObjectKind.SAMPLE);
        // TODO 2009-07-31, Piotr Buczek: can abundance change?
        return result;
    }

    @Override
    protected IColumnDefinitionKind<SampleWithPropertiesAndAbundance>[] getStaticColumnsDefinition()
    {
        return SampleAbundanceColDefKind.values();
    }

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

}
