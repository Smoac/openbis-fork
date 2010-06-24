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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.PlateMaterialReviewerColDefKind;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * A grid with a list of material (e.g. gene) locations on the plate with a fast access to images.
 * 
 * @author Tomasz Pylak
 */
public class PlateMaterialReviewer extends AbstractSimpleBrowserGrid<WellContent>
{
    private static final int IMAGE_WIDTH_PX = 200;

    private static final int IMAGE_HEIGHT_PX = 120;

    public static final String BROWSER_ID =
            GenericConstants.ID_PREFIX + "PlateMaterialReviewerGrid";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithIdentifier experiment, String[] materialItemList)
    {
        PlateMaterialsSearchCriteria materialCriteria =
                new PlateMaterialsSearchCriteria(experiment.getId(), materialItemList);
        return create(viewContext, materialCriteria, experiment);
    }

    private static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            PlateMaterialsSearchCriteria materialCriteria,
            IEntityInformationHolderWithIdentifier experiment)
    {
        ToolBar toolbar = new ToolBar();
        toolbar.add(new Label("Channel:"));
        ChannelComboBox chooser = new ChannelComboBox();
        toolbar.add(chooser);
        return new PlateMaterialReviewer(viewContext, materialCriteria, experiment, chooser)
                .asDisposableWithToolbar(toolbar);
    }

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final PlateMaterialsSearchCriteria materialCriteria;

    private final ExperimentIdentifier experiment;

    private final ChannelComboBox channelChooser;

    private PlateMaterialReviewer(IViewContext<IScreeningClientServiceAsync> viewContext,
            PlateMaterialsSearchCriteria materialCriteria,
            IEntityInformationHolderWithIdentifier experiment, ChannelComboBox chooser)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID,
                DisplayTypeIDGenerator.PLATE_MATERIAL_REVIEWER);
        this.viewContext = viewContext;
        this.materialCriteria = materialCriteria;
        channelChooser = chooser;
        this.experiment = new ExperimentIdentifier(experiment.getIdentifier());
        registerClickListeners();
    }

    private void registerClickListeners()
    {
        registerLinkClickListenerFor(PlateMaterialReviewerColDefKind.WELL_NESTED_MATERIAL.id(),
                new ICellListener<WellContent>()
                    {
                        public void handle(WellContent wellContent, boolean specialKeyPressed)
                        {
                            EntityReference nestedMaterial =
                                    wellContent.tryGetNestedMaterialContent();
                            ClientPluginFactory.openGeneMaterialViewer(nestedMaterial, experiment,
                                    viewContext);
                        }
                    });
        registerLinkClickListenerFor(PlateMaterialReviewerColDefKind.WELL_CONTENT_MATERIAL.id(),
                new ICellListener<WellContent>()
                    {
                        public void handle(WellContent wellContent, boolean specialKeyPressed)
                        {
                            showEntityViewer(wellContent.getMaterialContent(), specialKeyPressed);
                        }
                    });
        registerLinkClickListenerFor(PlateMaterialReviewerColDefKind.PLATE.id(),
                new ICellListener<WellContent>()
                    {
                        public void handle(WellContent wellContent, boolean specialKeyPressed)
                        {
                            showEntityViewer(wellContent.getPlate(), specialKeyPressed);
                        }
                    });
        registerLinkClickListenerFor(PlateMaterialReviewerColDefKind.WELL.id(),
                new ICellListener<WellContent>()
                    {
                        public void handle(WellContent wellContent, boolean specialKeyPressed)
                        {
                            showEntityViewer(wellContent.getWell(), specialKeyPressed);
                        }
                    });
        registerLinkClickListenerFor(PlateMaterialReviewerColDefKind.DATASET.id(),
                new ICellListener<WellContent>()
                    {
                        public void handle(WellContent wellContent, boolean specialKeyPressed)
                        {
                            DatasetImagesReference imageDataset = wellContent.tryGetImages();
                            if (imageDataset != null)
                            {
                                showEntityViewer(imageDataset.getDatasetReference(),
                                        specialKeyPressed);
                            }
                        }
                    });

    }

    private void showEntityViewer(IEntityInformationHolder entityOrNull, boolean specialKeyPressed)
    {
        if (entityOrNull != null)
        {
            new OpenEntityDetailsTabAction(entityOrNull, viewContext, specialKeyPressed).execute();
        }
    }

    @Override
    protected ColumnDefsAndConfigs<WellContent> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<WellContent> schema = super.createColumnsDefinition();
        setLinksRenderer(schema, new PlateMaterialReviewerColDefKind[]
            { PlateMaterialReviewerColDefKind.WELL_NESTED_MATERIAL,
                    PlateMaterialReviewerColDefKind.WELL_CONTENT_MATERIAL,
                    PlateMaterialReviewerColDefKind.PLATE, PlateMaterialReviewerColDefKind.WELL,
                    PlateMaterialReviewerColDefKind.DATASET });
        setImageRenderer(schema);
        return schema;
    }

    private void setImageRenderer(ColumnDefsAndConfigs<WellContent> schema)
    {
        GridCellRenderer<BaseEntityModel<?>> render = new GridCellRenderer<BaseEntityModel<?>>()
            {

                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    final WellContent entity = (WellContent) model.getBaseObject();
                    if (entity != null && entity.tryGetImages() != null)
                    {
                        final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
                            {
                                public Widget create(String channel)
                                {
                                    return WellContentDialog.createImageViewerForChannel(
                                            viewContext, entity, IMAGE_WIDTH_PX, IMAGE_HEIGHT_PX,
                                            channel);
                                }
                            };
                        ChannelWidgetWithListener widgetWithListener =
                                new ChannelWidgetWithListener(viewerFactory);
                        widgetWithListener.update(channelChooser.getSimpleValue());
                        channelChooser.addNamesAndListener(entity.tryGetImages()
                                .getImageParameters().getChannelsNames(), widgetWithListener
                                .asSelectionChangedListener());
                        return widgetWithListener.asWidget();
                    }
                    return null;
                }
            };
        schema.setGridCellRendererFor(PlateMaterialReviewerColDefKind.IMAGE.id(), render);
    }

    private void setLinksRenderer(ColumnDefsAndConfigs<WellContent> schema,
            PlateMaterialReviewerColDefKind[] columns)
    {
        GridCellRenderer<BaseEntityModel<?>> linkCellRenderer = createInternalLinkCellRenderer();
        for (PlateMaterialReviewerColDefKind column : columns)
        {
            schema.setGridCellRendererFor(column.id(), linkCellRenderer);
        }
    }

    @Override
    protected IColumnDefinitionKind<WellContent>[] getStaticColumnsDefinition()
    {
        return PlateMaterialReviewerColDefKind.values();

    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, WellContent> resultSetConfig,
            final AbstractAsyncCallback<ResultSet<WellContent>> callback)
    {
        viewContext.getService().listPlateLocations(resultSetConfig, materialCriteria, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<WellContent> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportPlateLocations(exportCriteria, callback);
    }

    @Override
    protected List<IColumnDefinition<WellContent>> getInitialFilters()
    {
        return asColumnFilters(new PlateMaterialReviewerColDefKind[]
            { PlateMaterialReviewerColDefKind.WELL_NESTED_MATERIAL,
                    PlateMaterialReviewerColDefKind.WELL });
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

}
