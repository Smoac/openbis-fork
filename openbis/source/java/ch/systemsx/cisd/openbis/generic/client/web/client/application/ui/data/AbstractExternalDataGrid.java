/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.EntityGridModelFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.CommonExternalDataColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.DatasetImageOverviewUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExternalDataGrid
        extends
        AbstractEntityBrowserGrid<ExternalData, BaseEntityModel<ExternalData>, PropertyTypesCriteria>
{
    /** lists datasets and collects statistics about all datasets types */
    abstract protected void listDatasets(
            DefaultResultSetConfig<String, ExternalData> resultSetConfig,
            AbstractAsyncCallback<ResultSetWithEntityTypes<ExternalData>> callback);

    public static final String SHOW_DETAILS_BUTTON_ID_SUFFIX = "_show-details-button";

    // Set of entity types which are currently shown in this grid.
    // Used to decide which property columns should be shown.
    // Note: content depends on the current grid content.
    private Set<BasicEntityType> shownEntityTypesOrNull;

    private final ICriteriaProvider<PropertyTypesCriteria> criteriaProvider;

    protected AbstractExternalDataGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId, DisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, gridId, false, displayTypeIDGenerator);
        this.criteriaProvider = createCriteriaProvider();
        setId(browserId);
        updateCriteriaProviderAndRefresh();

        extendBottomToolbar();

        ICellListener<ExternalData> containerClickListener =
                new OpenEntityDetailsTabCellClickListener()
                    {
                        @Override
                        protected IEntityInformationHolderWithPermId getEntity(ExternalData rowItem)
                        {
                            return rowItem.tryGetContainer();
                        }
                    };
        registerLinkClickListenerFor(CommonExternalDataColDefKind.CONTAINER.id(),
                containerClickListener);

        ICellListener<ExternalData> experimentClickListener =
                new OpenEntityDetailsTabCellClickListener()
                    {
                        @Override
                        protected IEntityInformationHolderWithPermId getEntity(ExternalData rowItem)
                        {
                            return rowItem.getExperiment();
                        }
                    };
        registerLinkClickListenerFor(CommonExternalDataColDefKind.EXPERIMENT.id(),
                experimentClickListener);
        registerLinkClickListenerFor(CommonExternalDataColDefKind.EXPERIMENT_IDENTIFIER.id(),
                experimentClickListener);

        ICellListener<ExternalData> sampleClickListener =
                new OpenEntityDetailsTabCellClickListener()
                    {
                        @Override
                        protected IEntityInformationHolderWithPermId getEntity(ExternalData rowItem)
                        {
                            return rowItem.getSample();
                        }
                    };
        registerLinkClickListenerFor(CommonExternalDataColDefKind.SAMPLE.id(), sampleClickListener);
        registerLinkClickListenerFor(CommonExternalDataColDefKind.SAMPLE_IDENTIFIER.id(),
                sampleClickListener);
    }

    // adds show, show-details and invalidate buttons
    protected void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        if (viewContext.isSimpleOrEmbeddedMode() == false)
        {
            addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS),
                    getId() + SHOW_DETAILS_BUTTON_ID_SUFFIX, asShowEntityInvoker(false)));
            addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                    asShowEntityInvoker(true)));

            final String deleteTitle = viewContext.getMessage(Dict.BUTTON_DELETE);
            final String deleteAllTitle = deleteTitle + " All";
            final Button deleteButton =
                    new Button(deleteAllTitle, new AbstractCreateDialogListener()
                        {

                            @Override
                            protected Dialog createDialog(List<ExternalData> dataSets,
                                    IBrowserGridActionInvoker invoker)
                            {
                                return new DataSetListDeletionConfirmationDialog(viewContext,
                                        createRefreshCallback(invoker),
                                        getSelectedAndDisplayedItemsAction().execute());
                            }
                        });
            changeButtonTitleOnSelectedItems(deleteButton, deleteAllTitle, deleteTitle);
            addButton(deleteButton);
        }

        Button uploadButton =
                new Button(viewContext.getMessage(Dict.BUTTON_UPLOAD_DATASETS),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(List<ExternalData> dataSets,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new DataSetUploadConfirmationDialog(dataSets,
                                            getSelectedAndDisplayedItemsAction(), getTotalCount(),
                                            viewContext);
                                }
                            });
        if (StringUtils.isBlank(viewContext.getModel().getApplicationInfo().getCIFEXURL()))
        {
            uploadButton.disable();
        }
        addButton(uploadButton);
        if (viewContext.getModel().getApplicationInfo().isArchivingConfigured()
                && viewContext.isSimpleOrEmbeddedMode() == false)
        {
            addButton(createArchivingMenu());
        }
        addEntityOperationsSeparator();

        allowMultipleSelection();
    }

    @Override
    protected ICriteriaProvider<PropertyTypesCriteria> getCriteriaProvider()
    {
        return criteriaProvider;
    }

    /*
     * Provides property types which should be shown as the grid columns. Takes into account what
     * types of datasets are displayed and does not show property types which are not assigned to
     * any of those types.
     */
    private ICriteriaProvider<PropertyTypesCriteria> createCriteriaProvider()
    {
        final EntityKind entityKind = getEntityKind();
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
    protected final void listEntities(DefaultResultSetConfig<String, ExternalData> resultSetConfig,
            final AbstractAsyncCallback<ResultSet<ExternalData>> callback)
    {
        AbstractAsyncCallback<ResultSetWithEntityTypes<ExternalData>> extendedCallback =
                new AbstractAsyncCallback<ResultSetWithEntityTypes<ExternalData>>(viewContext)
                    {
                        @Override
                        protected void process(ResultSetWithEntityTypes<ExternalData> result)
                        {
                            shownEntityTypesOrNull = result.getAvailableEntityTypes();
                            callback.onSuccess(result.getResultSet());
                            refreshColumnsSettingsIfNecessary();
                        }

                        @Override
                        public void finishOnFailure(Throwable caught)
                        {
                            callback.finishOnFailure(caught);
                        }
                    };
        listDatasets(resultSetConfig, extendedCallback);
    }

    private abstract class OpenEntityDetailsTabCellClickListener implements
            ICellListener<ExternalData>
    {
        protected abstract IEntityInformationHolderWithPermId getEntity(ExternalData rowItem);

        public final void handle(ExternalData rowItem, boolean keyPressed)
        {
            final IEntityInformationHolderWithPermId entity = getEntity(rowItem);
            new OpenEntityDetailsTabAction(entity, viewContext, keyPressed).execute();
        }

    }

    private final TextToolItem createArchivingMenu()
    {
        return new DataSetArchivingMenu(viewContext, getSelectedAndDisplayedItemsAction(),
                createRefreshGridAction());
    }

    public final static class SelectedAndDisplayedItems
    {
        // describes all items which are displayed in the grid (including all grid pages)
        private final TableExportCriteria<ExternalData> displayedItemsConfig;

        // currently selected items
        private final List<ExternalData> selectedItems;

        private final int displayedItemsCount;

        public SelectedAndDisplayedItems(List<ExternalData> selectedItems,
                TableExportCriteria<ExternalData> displayedItemsConfig, int displayedItemsCount)
        {
            this.displayedItemsConfig = displayedItemsConfig;
            this.selectedItems = selectedItems;
            this.displayedItemsCount = displayedItemsCount;
        }

        public TableExportCriteria<ExternalData> getDisplayedItemsConfig()
        {
            return displayedItemsConfig;
        }

        public int getDisplayedItemsCount()
        {
            return displayedItemsCount;
        }

        public List<ExternalData> getSelectedItems()
        {
            return selectedItems;
        }

        public DisplayedOrSelectedDatasetCriteria createCriteria(boolean selected)
        {
            if (selected)
            {
                List<ExternalData> items = getSelectedItems();
                List<String> datasetCodes = Code.extractCodes(items);
                return DisplayedOrSelectedDatasetCriteria.createSelectedItems(datasetCodes);
            } else
            {
                return DisplayedOrSelectedDatasetCriteria
                        .createDisplayedItems(getDisplayedItemsConfig());
            }
        }
    }

    public final IDelegatedActionWithResult<SelectedAndDisplayedItems> getSelectedAndDisplayedItemsAction()
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

    @Override
    protected BaseEntityModel<ExternalData> createModel(GridRowModel<ExternalData> entity)
    {
        return getColumnsFactory().createModel(entity,
                viewContext.getDisplaySettingsManager().getRealNumberFormatingParameters());
    }

    protected ColumnDefsAndConfigs<ExternalData> createColumnsSchema()
    {
        return getColumnsFactory().createColumnsSchema(viewContext, criteria.tryGetPropertyTypes());
    }

    @Override
    protected ColumnDefsAndConfigs<ExternalData> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<ExternalData> schema = createColumnsSchema();
        GridCellRenderer<BaseEntityModel<?>> linkRenderer = createInternalLinkCellRenderer();
        schema.setGridCellRendererFor(CommonExternalDataColDefKind.CONTAINER.id(), linkRenderer);
        schema.setGridCellRendererFor(CommonExternalDataColDefKind.SAMPLE.id(), linkRenderer);
        schema.setGridCellRendererFor(CommonExternalDataColDefKind.SAMPLE_IDENTIFIER.id(),
                linkRenderer);
        schema.setGridCellRendererFor(CommonExternalDataColDefKind.EXPERIMENT.id(), linkRenderer);
        schema.setGridCellRendererFor(CommonExternalDataColDefKind.EXPERIMENT_IDENTIFIER.id(),
                linkRenderer);
        schema.setGridCellRendererFor(CommonExternalDataColDefKind.PROJECT.id(), linkRenderer);
        schema.setGridCellRendererFor(CommonExternalDataColDefKind.SHOW_DETAILS_LINK.id(),
                createShowDetailsLinkCellRenderer());
        schema.setGridCellRendererFor(CommonExternalDataColDefKind.OVERVIEW.id(),
                createOverviewCellRenderer());
        return schema;
    }

    private GridCellRenderer<BaseEntityModel<?>> createOverviewCellRenderer()
    {
        final String sessionID = viewContext.getModel().getSessionContext().getSessionID();
        return new GridCellRenderer<BaseEntityModel<?>>()
            {

                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    ExternalData dataset = (ExternalData) model.getBaseObject();
                    return tryCreateOverviewLink(dataset);
                }

                private String tryCreateOverviewLink(ExternalData dataset)
                {
                    final String permId = dataset.getPermId();
                    final String dssBaseURL = dataset.getDataStore().getHostUrl();
                    final String typeCode = dataset.getDataSetType().getCode();
                    final Set<String> typePatternsWithImageOverview =
                            getWebClientConfiguration().getDataSetTypePatternsWithImageOverview();
                    if (matches(typePatternsWithImageOverview, typeCode))
                    {
                        return DatasetImageOverviewUtilities.createEmbededImageHtml(dssBaseURL,
                                permId, typeCode, sessionID);
                    } else
                    {
                        return null;
                    }
                }
            };

    }

    private static boolean matches(Set<String> patternsSet, String value)
    {
        for (String pattern : patternsSet)
        {
            if (value.matches(pattern))
            {
                return true;
            }
        }
        return false;
    }

    private EntityGridModelFactory<ExternalData> getColumnsFactory()
    {
        return new EntityGridModelFactory<ExternalData>(viewContext, getStaticColumnsDefinition());
    }

    @Override
    protected IColumnDefinitionKind<ExternalData>[] getStaticColumnsDefinition()
    {
        return CommonExternalDataColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<ExternalData>> getInitialFilters()
    {
        return asColumnFilters(new CommonExternalDataColDefKind[]
            { CommonExternalDataColDefKind.CODE, CommonExternalDataColDefKind.FILE_FORMAT_TYPE });
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<ExternalData> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDataSetSearchHits(exportCriteria, callback);
    }

    @Override
    public Set<DatabaseModificationKind> getGridRelevantModifications()
    {
        final Set<DatabaseModificationKind> relevantMods =
                getGridRelevantModifications(ObjectKind.DATA_SET);
        relevantMods.add(edit(ObjectKind.EXPERIMENT));
        relevantMods.add(edit(ObjectKind.SAMPLE));
        DatabaseModificationKind.addAny(relevantMods, ObjectKind.VOCABULARY_TERM);
        return relevantMods;
    }

    @Override
    protected boolean hasColumnsDefinitionChanged(PropertyTypesCriteria newCriteria)
    {
        List<PropertyType> newPropertyTypes = newCriteria.tryGetPropertyTypes();
        List<PropertyType> prevPropertyTypes =
                (criteria == null ? null : criteria.tryGetPropertyTypes());
        if (newPropertyTypes == null)
        {
            return false; // nothing chosen
        }
        if (prevPropertyTypes == null)
        {
            return true; // first selection
        }
        return newPropertyTypes.equals(prevPropertyTypes) == false;
    }

    @Override
    protected void showEntityViewer(ExternalData dataSet, boolean editMode, boolean inBackground)
    {
        showEntityInformationHolderViewer(dataSet, editMode, inBackground);
    }

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    protected EntityType tryToGetEntityType()
    {
        return null;
    }
}
