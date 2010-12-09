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

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.WELL;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.WELL_CONTENT_MATERIAL;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionProvider;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplaySettingsManager;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCodesCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds;

/**
 * @author Franz-Josef Elmer
 */
public class WellSearchGrid extends TypedTableGrid<WellContent>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX
            + "PlateMaterialReviewer2Grid";

    private static final String CHANNEL_CHOOSER_LABEL = "Channel:";

    private static final String SINGLE_EXPERIMENT_TEXT = "Single experiment";

    private static final String ALL_EXPERIMENTS_TEXT = "All experiments";

    private static final String CHOOSE_ONE_EXPERIMENT_TEXT = "Choose one experiment...";

    private static final int IMAGE_WIDTH_PX = 200;

    private static final int IMAGE_HEIGHT_PX = 120;

    // by experiment perm id
    public static void openTab(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            final String experimentPermId, final MaterialSearchCodesCriteria materialCodesCriteria)
    {
        screeningViewContext.getCommonService().getEntityInformationHolder(EntityKind.EXPERIMENT,
                experimentPermId,
                new AbstractAsyncCallback<IEntityInformationHolderWithPermId>(screeningViewContext)
                    {
                        @Override
                        protected void process(
                                IEntityInformationHolderWithPermId experimentIdentifier)
                        {
                            TechId experimentId = new TechId(experimentIdentifier.getId());
                            WellSearchGrid.openTab(screeningViewContext, experimentId,
                                    MaterialSearchCriteria.create(materialCodesCriteria));
                        }
                    });
    }

    // by experiment tech id
    private static void openTab(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            TechId experimentId, final MaterialSearchCriteria materialSearchCriteria)
    {
        screeningViewContext.getCommonService().getExperimentInfo(experimentId,
                new AbstractAsyncCallback<Experiment>(screeningViewContext)
                    {
                        @Override
                        protected void process(Experiment experiment)
                        {
                            ExperimentSearchCriteria experimentCriteria =
                                    ExperimentSearchCriteria.createExperiment(experiment.getId(),
                                            experiment.getPermId(), experiment.getIdentifier());
                            WellSearchGrid.openTab(screeningViewContext, experimentCriteria,
                                    materialSearchCriteria);
                        }
                    });
    }

    public static void openTab(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final ExperimentSearchCriteria experimentCriteria,
            final MaterialSearchCriteria materialCriteria)
    {
        final AbstractTabItemFactory tabFactory = new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent reviewer =
                            WellSearchGrid
                                    .create(viewContext, experimentCriteria, materialCriteria);
                    return DefaultTabItem.create(getTabTitle(), reviewer, viewContext);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return HelpPageIdentifier.createSpecific("Well Reviewing Panel");
                }

                @Override
                public String getId()
                {
                    final String reportDate =
                            DateTimeFormat.getMediumTimeFormat().format(new Date());
                    return GenericConstants.ID_PREFIX + "-PlateMaterialReviewer-" + reportDate;
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(Dict.PLATE_MATERIAL_REVIEWER_TITLE);
                }

                @Override
                public String tryGetLink()
                {
                    SingleExperimentSearchCriteria experimentCriteriaOrNull =
                            experimentCriteria.tryGetExperiment();
                    return ScreeningLinkExtractor.createWellsSearchLink(
                            (experimentCriteriaOrNull != null ? experimentCriteriaOrNull
                                    .getExperimentPermId() : null), materialCriteria
                                    .tryGetMaterialCodesOrProperties());
                }
            };
        DispatcherHelper.dispatchNaviEvent(tabFactory);
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
        WellSearchGrid reviewer =
                new WellSearchGrid(viewContext, experimentCriteriaOrNull, materialCriteria);
        return reviewer.asDisposableWithToolbar(reviewer.createToolbar());
    }

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final MaterialSearchCriteria materialCriteria;

    private ExperimentSearchCriteria experimentCriteriaOrNull;

    private SingleExperimentSearchCriteria singleExperimentChooserStateOrNull;

    private ChannelComboBox channelChooser;

    private WellSearchGrid(IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            MaterialSearchCriteria materialCriteria)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, experimentCriteriaOrNull != null,
                DisplayTypeIDGenerator.PLATE_MATERIAL_REVIEWER);
        this.viewContext = viewContext;
        this.experimentCriteriaOrNull = experimentCriteriaOrNull;
        this.materialCriteria = materialCriteria;

        final IDefaultChannelState defaultChannelState =
                createDefaultChannelState(viewContext, experimentCriteriaOrNull);
        channelChooser = new ChannelComboBox(defaultChannelState);
        linkWellContent();
        linkExperiment();
        linkPlate();
        linkWell();
        linkImageDataSet();
        linkImageAnalysisDataSet();
    }

    private static IDefaultChannelState createDefaultChannelState(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        final ScreeningDisplaySettingsManager screeningDisplaySettingManager =
                ScreeningViewContext.getTechnologySpecificDisplaySettingsManager(viewContext);

        // If there is a single experiment set in criteria reuse default channel settings,
        // otherwise use global settings.
        final ScreeningDisplayTypeIDGenerator displayTypeIdGenerator =
                ScreeningDisplayTypeIDGenerator.EXPERIMENT_CHANNEL;
        final String displayTypeId;
        if (experimentCriteriaOrNull != null && experimentCriteriaOrNull.tryGetExperiment() != null)
        {
            final String experimentPermId =
                    experimentCriteriaOrNull.tryGetExperiment().getExperimentPermId();
            displayTypeId = displayTypeIdGenerator.createID(experimentPermId);
        } else
        {
            displayTypeId = displayTypeIdGenerator.createID(null);
        }
        return new IDefaultChannelState()
            {
                public void setDefaultChannel(String channel)
                {
                    screeningDisplaySettingManager.setDefaultChannel(displayTypeId, channel);
                }

                public String tryGetDefaultChannel()
                {
                    return screeningDisplaySettingManager.tryGetDefaultChannel(displayTypeId);
                }
            };
    }

    private void linkWellContent()
    {
        registerListenerAndLinkGenerator(WELL_CONTENT_MATERIAL,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            Material material = entity.getMaterialContent();
                            String experimentIdentifier =
                                    entity.getExperiment().getExperimentIdentifier();
                            return ScreeningLinkExtractor.tryExtractMaterialWithExperiment(
                                    material, experimentIdentifier);
                        }

                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            Material contentMaterial =
                                    wellContent.getObjectOrNull().getMaterialContent();
                            ExperimentReference experiment =
                                    wellContent.getObjectOrNull().getExperiment();
                            ExperimentSearchCriteria experimentCriteria =
                                    ExperimentSearchCriteria.createExperiment(experiment.getId(),
                                            experiment.getPermId(),
                                            experiment.getExperimentIdentifier());

                            ClientPluginFactory.openPlateLocationsMaterialViewer(contentMaterial,
                                    experimentCriteria, viewContext);
                        }
                    });
    }

    private void linkExperiment()
    {
        registerListenerAndLinkGenerator(WellSearchGridColumnIds.EXPERIMENT,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            return LinkExtractor.tryExtract(entity.getExperiment());
                        }

                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            showEntityViewer(wellContent.getObjectOrNull().getExperiment(),
                                    specialKeyPressed);
                        }
                    });
    }

    private void linkPlate()
    {
        registerListenerAndLinkGenerator(WellSearchGridColumnIds.PLATE,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            return LinkExtractor.tryExtract(entity.getPlate());
                        }

                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            showEntityViewer(wellContent.getObjectOrNull().getPlate(),
                                    specialKeyPressed);
                        }
                    });
    }

    private void linkWell()
    {
        registerListenerAndLinkGenerator(WellSearchGridColumnIds.WELL,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            return LinkExtractor.tryExtract(entity.getWell());
                        }

                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            showEntityViewer(wellContent.getObjectOrNull().getWell(),
                                    specialKeyPressed);
                        }
                    });
    }

    private void linkImageDataSet()
    {
        registerListenerAndLinkGenerator(WellSearchGridColumnIds.IMAGE_DATA_SET,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            DatasetImagesReference imageDataset = entity.tryGetImageDataset();
                            if (imageDataset != null)
                            {
                                return LinkExtractor.tryExtract(imageDataset.getDatasetReference());
                            } else
                            {
                                return null;
                            }
                        }

                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            DatasetImagesReference dataset =
                                    wellContent.getObjectOrNull().tryGetImageDataset();
                            if (dataset != null)
                            {
                                showEntityViewer(dataset.getDatasetReference(), specialKeyPressed);
                            }
                        }
                    });
    }

    private void linkImageAnalysisDataSet()
    {
        registerListenerAndLinkGenerator(WellSearchGridColumnIds.IMAGE_ANALYSIS_DATA_SET,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            DatasetReference dataset = entity.tryGetFeatureVectorDataset();
                            if (dataset != null)
                            {
                                return LinkExtractor.tryExtract(dataset);
                            } else
                            {
                                return null;
                            }
                        }

                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            DatasetReference dataset =
                                    wellContent.getObjectOrNull().tryGetFeatureVectorDataset();
                            if (dataset != null)
                            {
                                showEntityViewer(dataset, specialKeyPressed);
                            }
                        }
                    });
    }

    private void showEntityViewer(IEntityInformationHolderWithPermId entityOrNull,
            boolean specialKeyPressed)
    {
        if (entityOrNull != null)
        {
            new OpenEntityDetailsTabAction(entityOrNull, viewContext, specialKeyPressed).execute();
        }
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
                new SingleExperimentSearchCriteria(experiment.getId(), experiment.getPermId(),
                        experiment.getIdentifier());
        updateSingleExperimentChooser(chooserField, singleExperiment);
        this.experimentCriteriaOrNull = ExperimentSearchCriteria.createExperiment(singleExperiment);
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
                        refresh();
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
                            refresh();
                        }
                    }
                }
            });
        return experimentRadio;
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<WellContent>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<WellContent>> columnDefs =
                super.createColumnsDefinition();
        setImageRenderer(columnDefs);
        return columnDefs;
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID;
    }

    private void setImageRenderer(ColumnDefsAndConfigs<TableModelRowWithObject<WellContent>> schema)
    {
        GridCellRenderer<BaseEntityModel<?>> render = new GridCellRenderer<BaseEntityModel<?>>()
            {

                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    final WellContent entity = getTableModel(model).getObjectOrNull();
                    if (entity == null)
                    {
                        return null;
                    }
                    DatasetImagesReference images = entity.tryGetImageDataset();
                    if (images == null)
                    {
                        return null;
                    }
                    ImageDatasetParameters imageParameters = images.getImageParameters();
                    final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
                        {
                            public Widget create(String channel)
                            {
                                return WellContentDialog.createImageViewerForChannel(viewContext,
                                        entity, IMAGE_WIDTH_PX, IMAGE_HEIGHT_PX, channel);
                            }

                            public void setChannelChooser(
                                    SelectionProvider<SimpleComboValue<String>> selectionProvider)
                            {
                                // TODO 2010-11-09, felmer: Auto-generated method stub
                            }
                        };
                    ChannelWidgetWithListener widgetWithListener =
                            new ChannelWidgetWithListener(viewerFactory);
                    widgetWithListener.update(channelChooser.getSimpleValue());

                    channelChooser.addCodesAndListener(imageParameters.getChannelsCodes(),
                            widgetWithListener.asSelectionChangedListener());
                    return widgetWithListener.asWidget();
                }

                @SuppressWarnings("unchecked")
                private TableModelRowWithObject<WellContent> getTableModel(BaseEntityModel<?> model)
                {
                    return ((TableModelRowWithObject<WellContent>) model.getBaseObject());
                }
            };
        schema.setGridCellRendererFor(WellSearchGridColumnIds.WELL_IMAGES, render);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<WellContent>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<WellContent>> callback)
    {
        assert experimentCriteriaOrNull != null : "experiment not specified";

        WellSearchCriteria searchCriteria =
                new WellSearchCriteria(experimentCriteriaOrNull, materialCriteria);
        viewContext.getService().listPlateWells(resultSetConfig, searchCriteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<WellContent>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportPlateLocations2(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(WELL_CONTENT_MATERIAL, WELL);
    }

}
