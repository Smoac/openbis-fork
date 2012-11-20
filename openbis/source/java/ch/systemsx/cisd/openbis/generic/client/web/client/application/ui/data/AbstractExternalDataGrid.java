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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MetaprojectChooserButton;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.GridUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.DatasetImageOverviewUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExternalDataGrid extends AbstractEntityGrid<ExternalData>
{
    public static final String SHOW_DETAILS_BUTTON_ID_SUFFIX = "_show-details-button";

    public static final String ADD_METAPROJECTS_BUTTON_ID_SUFFIX = "_add-metaprojects-button";

    public static final String REMOVE_METAPROJECTS_BUTTON_ID_SUFFIX = "_remove-metaprojects-button";

    private final ICriteriaProvider<PropertyTypesCriteria> criteriaProvider;

    protected AbstractExternalDataGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId, DisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId, true, displayTypeIDGenerator);
        this.criteriaProvider = createCriteriaProvider();
        setId(browserId);

        extendBottomToolbar();
        linkProject();
    }

    private void linkProject()
    {
        registerListenerAndLinkGenerator(ExternalDataGridColumnIDs.PROJECT,
                new ICellListenerAndLinkGenerator<ExternalData>()
                    {
                        @Override
                        public void handle(TableModelRowWithObject<ExternalData> rowItem,
                                boolean specialKeyPressed)
                        {
                            final Project project =
                                    rowItem.getObjectOrNull().getExperiment().getProject();
                            final String href = LinkExtractor.tryExtract(project);
                            OpenEntityDetailsTabHelper.open(viewContext, project,
                                    specialKeyPressed, href);
                        }

                        @Override
                        public String tryGetLink(ExternalData entity,
                                ISerializableComparable comparableValue)
                        {
                            final Experiment exp = entity.getExperiment();
                            return exp == null ? null : LinkExtractor.tryExtract(exp.getProject());
                        }
                    });
    }

    @Override
    protected boolean supportsExportForUpdate()
    {
        return true;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<ExternalData>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<ExternalData>> callback)
    {
        // TODO Auto-generated method stub

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
                            List<Long> dataSetIds = new ArrayList<Long>();
                            for (BaseEntityModel<TableModelRowWithObject<ExternalData>> item : getSelectedItems())
                            {
                                dataSetIds.add(item.getBaseObject().getObjectOrNull().getId());
                            }

                            List<Long> metaProjectIds = new ArrayList<Long>();
                            for (TableModelRowWithObject<Metaproject> row : entities)
                            {
                                metaProjectIds.add(row.getObjectOrNull().getId());
                            }
                            viewContext.getCommonService().assignDataSetsToMetaProjects(
                                    metaProjectIds, dataSetIds,
                                    createRefreshCallback(asActionInvoker()));

                        }
                    });

        tagButton.setId(getId() + ADD_METAPROJECTS_BUTTON_ID_SUFFIX);
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
                            List<Long> dataSetIds = new ArrayList<Long>();
                            for (BaseEntityModel<TableModelRowWithObject<ExternalData>> item : getSelectedItems())
                            {
                                dataSetIds.add(item.getBaseObject().getObjectOrNull().getId());
                            }

                            List<Long> metaProjectIds = new ArrayList<Long>();
                            for (TableModelRowWithObject<Metaproject> row : entities)
                            {
                                metaProjectIds.add(row.getObjectOrNull().getId());
                            }
                            viewContext.getCommonService()
                                    .removeDataSetsFromMetaProjects(
                                            metaProjectIds, dataSetIds,
                                            createRefreshCallback(asActionInvoker()));

                        }
                    });

        untagButton.setId(getId() + REMOVE_METAPROJECTS_BUTTON_ID_SUFFIX);
        untagButton.setText(viewContext.getMessage(Dict.BUTTON_UNTAG));
        enableButtonOnSelectedItems(untagButton);
        addButton(untagButton);
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

            addTaggingButtons();

            final String deleteTitle = viewContext.getMessage(Dict.BUTTON_DELETE);
            final String deleteAllTitle = deleteTitle + " All";
            final Button deleteButton =
                    new Button(deleteAllTitle, new AbstractCreateDialogListener()
                        {

                            @Override
                            protected Dialog createDialog(
                                    List<TableModelRowWithObject<ExternalData>> dataSets,
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
                                protected Dialog createDialog(
                                        List<TableModelRowWithObject<ExternalData>> dataSets,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new DataSetUploadConfirmationDialog(dataSets,
                                            getSelectedAndDisplayedItemsAction(), getTotalCount(),
                                            viewContext);
                                }
                            });
        if (StringUtils.isBlank(viewContext.getModel().getApplicationInfo().getCifexURL()))
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
        final EntityKind entityKind = getEntityKindOrNull();
        return new PropertyTypesCriteriaProvider(viewContext, entityKind)
            {
                @Override
                public PropertyTypesCriteria tryGetCriteria()
                {
                    PropertyTypesCriteria propertyTypesCriteria = super.tryGetCriteria();
                    return propertyTypesCriteria;
                }
            };
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    private final TextToolItem createArchivingMenu()
    {
        return new DataSetArchivingMenu(viewContext, getSelectedAndDisplayedItemsAction(),
                createRefreshGridAction());
    }

    public final static class SelectedAndDisplayedItems
    {
        // describes all items which are displayed in the grid (including all grid pages)
        private final TableExportCriteria<TableModelRowWithObject<ExternalData>> displayedItemsConfig;

        // currently selected items
        private final List<TableModelRowWithObject<ExternalData>> selectedItems;

        private final int displayedItemsCount;

        public SelectedAndDisplayedItems(List<TableModelRowWithObject<ExternalData>> selectedItems,
                TableExportCriteria<TableModelRowWithObject<ExternalData>> displayedItemsConfig,
                int displayedItemsCount)
        {
            this.displayedItemsConfig = displayedItemsConfig;
            this.selectedItems = selectedItems;
            this.displayedItemsCount = displayedItemsCount;
        }

        public TableExportCriteria<TableModelRowWithObject<ExternalData>> getDisplayedItemsConfig()
        {
            return displayedItemsConfig;
        }

        public int getDisplayedItemsCount()
        {
            return displayedItemsCount;
        }

        public List<TableModelRowWithObject<ExternalData>> getSelectedItems()
        {
            return selectedItems;
        }

        public List<ExternalData> getSelectedDataSets()
        {
            List<ExternalData> dataSets = new ArrayList<ExternalData>();
            for (TableModelRowWithObject<ExternalData> item : selectedItems)
            {
                dataSets.add(item.getObjectOrNull());
            }
            return dataSets;
        }

        public DisplayedOrSelectedDatasetCriteria createCriteria(boolean selected)
        {
            if (selected)
            {
                List<TableModelRowWithObject<ExternalData>> items = getSelectedItems();
                List<String> datasetCodes = new ArrayList<String>();
                for (TableModelRowWithObject<ExternalData> row : items)
                {
                    datasetCodes.add(row.getObjectOrNull().getCode());
                }
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
                @Override
                public SelectedAndDisplayedItems execute()
                {
                    return new SelectedAndDisplayedItems(getSelectedBaseObjects(),
                            createTableExportCriteria(), getTotalCount());
                }
            };
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<ExternalData>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<ExternalData>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(ExternalDataGridColumnIDs.REGISTRATOR,
                PersonRenderer.REGISTRATOR_RENDERER);
        schema.setGridCellRendererFor(CommonGridColumnIDs.MODIFIER,
                PersonRenderer.MODIFIER_RENDERER);
        schema.setGridCellRendererFor(ExternalDataGridColumnIDs.SHOW_DETAILS_LINK,
                createShowDetailsLinkCellRenderer());
        schema.setGridCellRendererFor(ExternalDataGridColumnIDs.OVERVIEW,
                createOverviewCellRenderer());
        return schema;
    }

    private GridCellRenderer<BaseEntityModel<?>> createOverviewCellRenderer()
    {
        final String sessionID = viewContext.getModel().getSessionContext().getSessionID();
        return new GridCellRenderer<BaseEntityModel<?>>()
            {

                @Override
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

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(ExternalDataGridColumnIDs.CODE,
                ExternalDataGridColumnIDs.FILE_FORMAT_TYPE);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<ExternalData>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDataSetSearchHits(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return GridUtils.getRelevantModifications(ObjectKind.DATA_SET, getCriteriaProvider());
    }

    @Override
    protected void showEntityViewer(TableModelRowWithObject<ExternalData> dataSet,
            boolean editMode, boolean inBackground)
    {
        showEntityInformationHolderViewer(dataSet.getObjectOrNull(), editMode, inBackground);
    }

    @Override
    protected EntityKind getEntityKindOrNull()
    {
        return EntityKind.DATA_SET;
    }

    private List<Taggable> taggables(
            List<BaseEntityModel<TableModelRowWithObject<ExternalData>>> data)
    {
        List<Taggable> list = new ArrayList<Taggable>();
        for (final BaseEntityModel<TableModelRowWithObject<ExternalData>> item : data)
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
