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

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.PlateMaterialReviewerColDefKind;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.MaterialSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.SingleExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * A grid with a list of material (e.g. gene) locations on the plate with a fast access to images.
 * 
 * @author Tomasz Pylak
 */
public class PlateMaterialReviewer extends AbstractSimpleBrowserGrid<WellContent>
{
    private static final String CHANNEL_CHOOSER_LABEL = "Channel:";

    private static final String ALL_EXPERIMENTS_TEXT = "All experiments";

    private static final String SINGLE_EXPERIMENT_TEXT = "Single experiment";

    private static final String CHOOSE_ONE_EXPERIMENT_TEXT = "Choose one experiment...";

    private static final int IMAGE_WIDTH_PX = 200;

    private static final int IMAGE_HEIGHT_PX = 120;

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX
            + "PlateMaterialReviewerGrid";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithIdentifier experiment, String[] materialItemList,
            String[] materialTypeCodes, boolean exactMatchOnly)
    {
        ExperimentSearchCriteria experimentCriteria =
                ExperimentSearchCriteria.createExperiment(experiment.getId(),
                        experiment.getIdentifier());
        MaterialSearchCriteria materialCriteria =
                MaterialSearchCriteria.createCodesCriteria(materialItemList, materialTypeCodes,
                        exactMatchOnly);
        return create(viewContext, experimentCriteria, materialCriteria);
    }

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull, TechId materialId)
    {
        return create(viewContext, experimentCriteriaOrNull,
                MaterialSearchCriteria.createIdCriteria(materialId));
    }

    private static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            MaterialSearchCriteria materialCriteria)
    {
        PlateMaterialReviewer reviewer =
                new PlateMaterialReviewer(viewContext, experimentCriteriaOrNull, materialCriteria);
        return reviewer.asDisposableWithToolbar(reviewer.createToolbar());
    }

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final ChannelComboBox channelChooser;

    private final MaterialSearchCriteria materialCriteria;

    // null if experiment has not been chosen
    private ExperimentSearchCriteria experimentCriteriaOrNull;

    // stores the state of the single experiment chooser, even when we are looking in all
    // experiments
    private SingleExperimentSearchCriteria singleExperimentChooserStateOrNull;

    private PlateMaterialReviewer(IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            MaterialSearchCriteria materialCriteria)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID,
                experimentCriteriaOrNull != null, DisplayTypeIDGenerator.PLATE_MATERIAL_REVIEWER);
        this.viewContext = viewContext;
        this.experimentCriteriaOrNull = experimentCriteriaOrNull;
        this.singleExperimentChooserStateOrNull = null;
        this.materialCriteria = materialCriteria;
        this.channelChooser = new ChannelComboBox();
        registerClickListeners();
    }

    private ToolBar createToolbar()
    {
        ToolBar toolbar = new ToolBar();
        toolbar.add(createExperimentChooser());
        toolbar.add(new Label(CHANNEL_CHOOSER_LABEL));
        toolbar.add(channelChooser);
        return toolbar;
    }

    private Component createExperimentChooser()
    {
        LayoutContainer container = new LayoutContainer();
        container.setWidth(400);

        ExperimentChooserFieldAdaptor singleExperimentChooser = createSingleExperimentChooser();
        RadioGroup experimentRadioChooser = createExperimentRadio(singleExperimentChooser);

        container.add(experimentRadioChooser);
        container.add(singleExperimentChooser.getField());
        return container;
    }

    private ExperimentChooserFieldAdaptor createSingleExperimentChooser()
    {
        ExperimentChooserFieldAdaptor experimentChooser =
                ExperimentChooserField.create("", true, null, viewContext.getCommonViewContext());
        final ExperimentChooserField chooserField = experimentChooser.getChooserField();
        chooserField.addChosenEntityListener(new IChosenEntityListener<Experiment>()
            {
                public void entityChosen(Experiment experiment)
                {
                    if (experiment != null)
                    {
                        chooseSingleExperiment(chooserField, experiment);
                    }
                }
            });

        chooserField.setEditable(false);
        if (experimentCriteriaOrNull != null && experimentCriteriaOrNull.tryGetExperiment() != null)
        {
            updateSingleExperimentChooser(chooserField, experimentCriteriaOrNull.tryGetExperiment());
        } else
        {
            // we search in all experiments or single experiment has not been chosen
            this.singleExperimentChooserStateOrNull = null;
            chooserField.reset();
        }
        if (experimentCriteriaOrNull == null || experimentCriteriaOrNull.tryGetExperiment() != null)
        {
            chooserField.setEmptyText(CHOOSE_ONE_EXPERIMENT_TEXT);
        } else
        {
            chooserField.setEmptyText(ALL_EXPERIMENTS_TEXT);
        }
        return experimentChooser;
    }

    private void chooseSingleExperiment(final ExperimentChooserField chooserField,
            Experiment experiment)
    {
        SingleExperimentSearchCriteria singleExperiment =
                new SingleExperimentSearchCriteria(experiment.getId(), experiment.getIdentifier());
        updateSingleExperimentChooser(chooserField, singleExperiment);
        PlateMaterialReviewer.this.experimentCriteriaOrNull =
                ExperimentSearchCriteria.createExperiment(singleExperiment);
        refresh();
    }

    private void updateSingleExperimentChooser(ExperimentChooserField chooserField,
            SingleExperimentSearchCriteria singleExperiment)
    {
        this.singleExperimentChooserStateOrNull = singleExperiment;
        chooserField.updateValue(new ExperimentIdentifier(singleExperiment
                .getExperimentIdentifier()));
    }

    private boolean isAllExperimentsChoosen()
    {
        return experimentCriteriaOrNull != null
                && experimentCriteriaOrNull.tryGetExperiment() == null;
    }

    private RadioGroup createExperimentRadio(
            final ExperimentChooserFieldAdaptor singleExperimentChooser)
    {
        final RadioGroup experimentRadio = new RadioGroup();
        experimentRadio.setSelectionRequired(true);
        experimentRadio.setOrientation(Orientation.HORIZONTAL);

        final Radio allExps = new Radio();
        allExps.setBoxLabel(ALL_EXPERIMENTS_TEXT);
        experimentRadio.add(allExps);

        final Radio oneExps = new Radio();
        oneExps.setBoxLabel(SINGLE_EXPERIMENT_TEXT);
        experimentRadio.add(oneExps);

        experimentRadio.setValue(isAllExperimentsChoosen() ? allExps : oneExps);
        experimentRadio.setAutoHeight(true);
        experimentRadio.addListener(Events.Change, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    if (allExps.getValue())
                    {
                        singleExperimentChooser.getChooserField().setEnabled(false);
                        singleExperimentChooser.getChooserField()
                                .setEmptyText(ALL_EXPERIMENTS_TEXT);
                        experimentCriteriaOrNull = ExperimentSearchCriteria.createAllExperiments();
                        PlateMaterialReviewer.this.refresh();
                    } else
                    {
                        singleExperimentChooser.getChooserField().setEmptyText(
                                CHOOSE_ONE_EXPERIMENT_TEXT);

                        singleExperimentChooser.getChooserField().setEnabled(true);
                        if (singleExperimentChooserStateOrNull == null)
                        {
                            experimentCriteriaOrNull = null;
                        } else
                        {
                            experimentCriteriaOrNull =
                                    ExperimentSearchCriteria
                                            .createExperiment(singleExperimentChooserStateOrNull);
                            PlateMaterialReviewer.this.refresh();
                        }
                    }
                }
            });
        return experimentRadio;
    }

    private void registerClickListeners()
    {
        registerLinkClickListenerFor(PlateMaterialReviewerColDefKind.WELL_CONTENT_MATERIAL.id(),
                new ICellListener<WellContent>()
                    {
                        public void handle(WellContent wellContent, boolean specialKeyPressed)
                        {
                            Material contentMaterial = wellContent.getMaterialContent();
                            ExperimentReference experiment = wellContent.getExperiment();
                            ExperimentSearchCriteria experimentCriteria =
                                    ExperimentSearchCriteria.createExperiment(experiment.getId(),
                                            experiment.getExperimentIdentifier());

                            ClientPluginFactory.openPlateLocationsMaterialViewer(contentMaterial,
                                    experimentCriteria, viewContext);
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
                            DatasetImagesReference dataset = wellContent.tryGetImageDataset();
                            if (dataset != null)
                            {
                                showEntityViewer(dataset.getDatasetReference(), specialKeyPressed);
                            }
                        }
                    });
        registerLinkClickListenerFor(PlateMaterialReviewerColDefKind.IMAGE_ANALYSIS_DATASET.id(),
                new ICellListener<WellContent>()
                    {
                        public void handle(WellContent wellContent, boolean specialKeyPressed)
                        {
                            DatasetReference dataset = wellContent.tryGetFeatureVectorDataset();
                            if (dataset != null)
                            {
                                showEntityViewer(dataset, specialKeyPressed);
                            }
                        }
                    });
        registerLinkClickListenerFor(PlateMaterialReviewerColDefKind.EXPERIMENT.id(),
                new ICellListener<WellContent>()
                    {
                        public void handle(WellContent wellContent, boolean specialKeyPressed)
                        {
                            showEntityViewer(wellContent.getExperiment(), specialKeyPressed);
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
            { PlateMaterialReviewerColDefKind.WELL_CONTENT_MATERIAL,
                    PlateMaterialReviewerColDefKind.PLATE, PlateMaterialReviewerColDefKind.WELL,
                    PlateMaterialReviewerColDefKind.DATASET,
                    PlateMaterialReviewerColDefKind.IMAGE_ANALYSIS_DATASET });
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
                    if (entity == null)
                    {
                        return null;
                    }
                    DatasetImagesReference images = entity.tryGetImageDataset();
                    if (images == null)
                    {
                        return null;
                    }
                    PlateImageParameters imageParameters = images.getImageParameters();
                    final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
                        {
                            public Widget create(String channel)
                            {
                                return WellContentDialog.createImageViewerForChannel(viewContext,
                                        entity, IMAGE_WIDTH_PX, IMAGE_HEIGHT_PX, channel);
                            }
                        };
                    ChannelWidgetWithListener widgetWithListener =
                            new ChannelWidgetWithListener(viewerFactory);
                    widgetWithListener.update(channelChooser.getSimpleValue());

                    channelChooser.addCodesAndListener(imageParameters.getChannelsCodes(),
                            widgetWithListener.asSelectionChangedListener());
                    return widgetWithListener.asWidget();
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
    protected boolean isRefreshEnabled()
    {
        return experimentCriteriaOrNull != null;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, WellContent> resultSetConfig,
            final AbstractAsyncCallback<ResultSet<WellContent>> callback)
    {
        assert experimentCriteriaOrNull != null : "experiment not specified";

        PlateMaterialsSearchCriteria searchCriteria =
                new PlateMaterialsSearchCriteria(experimentCriteriaOrNull, materialCriteria);
        viewContext.getService().listPlateWells(resultSetConfig, searchCriteria, callback);
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
            { PlateMaterialReviewerColDefKind.WELL_CONTENT_MATERIAL,
                    PlateMaterialReviewerColDefKind.WELL });
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

}
