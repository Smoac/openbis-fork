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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.RealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.DefaultChannelState;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.HeatmapScaleFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.IHeatmapRenderer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.NumberHeatmapRenderer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.StringHeatmapRenderer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.SimpleModelComboBox.SimpleComboboxItem;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Utilities to create plate visualization. Visualizes wells metadata and at most one image dataset.
 * 
 * @author Tomasz Pylak
 */
public class PlateLayouter
{
    /** @return widget with plate visualization - all the wells and possibility to browse images. */
    public static Widget createVisualization(PlateImages plateImages,
            ScreeningViewContext viewContext)
    {
        PlateLayouter plateLayouter =
                new PlateLayouter(viewContext, plateImages.getPlateMetadata());
        plateLayouter.changeDisplayedImageDataset(plateImages.getImagesDataset());
        return plateLayouter.getView();
    }

    // -------

    private static final String UNKNOWN_WELL_MSG = "No metadata available";

    private static final String HEATMAP_KIND_CHOOSER_LABEL_MSG = "Choose heatmap kind:";

    private static final String METADATA_HEATMAP_KIND_MSG = "Metadata";

    private static final String FEATURE_HEATMAP_KIND_PREFIX_MSG = "Feature ";

    private static final int HEATMAP_KIND_COMBOBOX_CHOOSER_WIDTH_PX = 200;

    private static final int MARGIN_SIZE_PX = 10;

    // ------- internal fixed state

    private final PlateLayouterModel model;

    private final HeatmapPresenter presenter;

    private final Widget view;

    private final SimpleModelComboBox<Integer> heatmapKindChooser;

    public PlateLayouter(ScreeningViewContext viewContext, PlateMetadata plateMetadata)
    {
        this.model = new PlateLayouterModel(plateMetadata);
        Component[][] renderedWells = renderWells(model, viewContext);
        LayoutContainer legendContainer = new LayoutContainer();
        IRealNumberRenderer realNumberRenderer = createRealNumberRenderer(viewContext);
        this.presenter =
                new HeatmapPresenter(model, realNumberRenderer, createViewManipulator(
                        renderedWells, legendContainer));
        this.heatmapKindChooser = createHeatmapKindComboBox(presenter, viewContext);
        this.view = renderView(renderedWells, heatmapKindChooser, legendContainer);
    }

    private IRealNumberRenderer createRealNumberRenderer(ScreeningViewContext viewContext)
    {
        return new RealNumberRenderer(viewContext.getDisplaySettingsManager()
                .getRealNumberFormatingParameters());
    }

    private HeatmapPresenter.IViewManipulator createViewManipulator(
            final Component[][] renderedWells, final LayoutContainer legendContainer)
    {
        return new HeatmapPresenter.IViewManipulator()
            {
                public void refreshWellStyle(int rowIx, int colIx, Color color, String tooltipOrNull)
                {
                    Component wellComponent = renderedWells[rowIx][colIx];
                    PlateStyleSetter.setBackgroudColor(wellComponent, color.getHexColor());
                    GWTUtils.setToolTip(wellComponent, tooltipOrNull);
                }

                public void updateLegend(Widget legend)
                {
                    legendContainer.removeAll();
                    legendContainer.add(legend);
                    legendContainer.layout();
                }

            };
    }

    public Widget getView()
    {
        return view;
    }

    public void changeDisplayedImageDataset(DatasetImagesReference newImageDatasetOrNull)
    {
        this.model.setImageDataset(newImageDatasetOrNull);
    }

    public void changeDisplayedFeatureVectorDataset(FeatureVectorDataset dataset)
    {
        this.model.setFeatureVectorDataset(dataset);
        updateHeatmapKindComboBox(heatmapKindChooser, model.tryGetFeatureLabels());
    }

    /**
     * Renders widget which visualizes the plate. The image dataset which is used to display images
     * can be changed afterwards with {@link #changeDisplayedImageDataset} method without
     * re-rendering.
     */
    private static Widget renderView(Component[][] renderedWells,
            SimpleModelComboBox<Integer> heatmapKindChooser, LayoutContainer legendContainer)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        container.add(new Text(
                "Hold the mouse cursor over a well or click on it to get the details."),
                createRowLayoutHorizontalMargin());
        container.add(GuiUtils.renderInRow(new Text(HEATMAP_KIND_CHOOSER_LABEL_MSG),
                heatmapKindChooser));

        LayoutContainer plateContainer = new LayoutContainer();
        plateContainer.setLayout(new ColumnLayout());
        plateContainer.add(renderPlateLayout(renderedWells));

        // space between the well's matrix and the legend
        Widget separator = createBox();
        separator.setWidth("20");
        plateContainer.add(separator);

        plateContainer.add(legendContainer);
        container.add(plateContainer);

        return container;
    }

    private static LayoutContainer renderPlateLayout(Component[][] renderedWells)
    {
        LayoutContainer plateMatrix = new LayoutContainer();
        plateMatrix.setScrollMode(Scroll.AUTO);
        TableLayout layout = new TableLayout(getColumnsNum(renderedWells) + 1);
        layout.setCellSpacing(2);
        plateMatrix.setLayout(layout);

        addPlateWidgets(plateMatrix, renderedWells);
        return plateMatrix;
    }

    private static Component[][] renderWells(PlateLayouterModel model,
            ScreeningViewContext viewContext)
    {
        WellData[][] wellMatrix = model.getWellMatrix();
        int rowsNum = wellMatrix.length;
        int colsNum = getColumnsNum(wellMatrix);

        Component[][] wells = new Component[rowsNum][colsNum];
        DefaultChannelState channelState = new DefaultChannelState();
        for (int row = 0; row < rowsNum; row++)
        {
            for (int col = 0; col < colsNum; col++)
            {
                WellData wellData = wellMatrix[row][col];
                wells[row][col] = createWellWidget(wellData, channelState, model, viewContext);
            }
        }
        return wells;
    }

    // renders wells and axis row/column descriptions
    private static void addPlateWidgets(LayoutContainer plateTable, Component[][] wellMatrix)
    {
        int rowsNum = wellMatrix.length;
        int colsNum = getColumnsNum(wellMatrix);
        for (int row = 0; row <= rowsNum; row++)
        {
            for (int col = 0; col <= colsNum; col++)
            {
                if (row != 0 && col != 0)
                {
                    plateTable.add(wellMatrix[row - 1][col - 1]);
                } else
                {
                    Widget labelWidget = tryCreateLabelWidget(row, col);
                    assert labelWidget != null : "Label widget is null";
                    plateTable.add(labelWidget);
                }
            }
        }
    }

    private static Component createWellBox(WellData wellData)
    {
        Component widget = createBox();
        return PlateStyleSetter.setWellStyle(widget);
    }

    // ----------- static methods

    private static <T> int getColumnsNum(T[][] matrix)
    {
        int rowsNum = matrix.length;
        return (rowsNum == 0 ? 0 : matrix[0].length);
    }

    // creates column or row label. Returns null if the coordinates do not point to the first column
    // or row.
    private static Component tryCreateLabelWidget(int row, int col)
    {
        String text = null;
        if (row == 0)
        {
            if (col == 0)
            {
                text = "";
            } else
            {
                text = "" + col;
            }
        } else if (col == 0)
        {
            text = PlateUtils.translateRowNumberIntoLetterCode(row);
        }
        if (text != null)
        {
            Component widget = new Text(text);
            return PlateStyleSetter.setWellLabelStyle(widget);
        } else
        {
            return null;
        }
    }

    private static Component createWellWidget(final WellData wellData,
            final DefaultChannelState channelState, final PlateLayouterModel model,
            final ScreeningViewContext viewContext)
    {
        Component widget = createWellBox(wellData);
        widget.addListener(Events.OnMouseDown, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent ce)
                {
                    WellContentDialog.showContentDialog(wellData, model.tryGetImageDataset(),
                            channelState, viewContext);
                }
            });
        widget.sinkEvents(Events.OnMouseDown.getEventCode());
        return widget;
    }

    private static Component createBox()
    {
        return new Text("");
    }

    private static WellData[][] createEmptyWellMatrix(PlateMetadata plateMetadata)
    {
        WellData[][] data = new WellData[plateMetadata.getRowsNum()][plateMetadata.getColsNum()];
        Experiment experiment = plateMetadata.getPlate().getExperiment();
        for (int row = 0; row < data.length; row++)
        {
            for (int col = 0; col < data[row].length; col++)
            {
                data[row][col] = new WellData(new WellLocation(row + 1, col + 1), experiment);
            }
        }
        return data;
    }

    // ---------

    /** @return layout data with big margin on all sides */
    public static RowData createRowLayoutSurroundingData()
    {
        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(MARGIN_SIZE_PX));
        return layoutData;
    }

    /** @return layout data with big margin on top and bottom */
    public static RowData createRowLayoutHorizontalMargin()
    {
        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(MARGIN_SIZE_PX, 0, MARGIN_SIZE_PX, 0));
        return layoutData;
    }

    private static SimpleModelComboBox<Integer> createHeatmapKindComboBox(
            final HeatmapPresenter presenter, IMessageProvider messageProvider)
    {
        List<SimpleComboboxItem<Integer>> items = createHeatmapKindModel(null);
        final SimpleModelComboBox<Integer> chooser =
                new SimpleModelComboBox<Integer>(messageProvider, items,
                        HEATMAP_KIND_COMBOBOX_CHOOSER_WIDTH_PX);
        chooser.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<SimpleComboboxItem<Integer>>>()
            {
                @Override
                public void selectionChanged(
                        SelectionChangedEvent<SimpleComboValue<SimpleComboboxItem<Integer>>> se)
                {
                    Integer value = SimpleModelComboBox.getChosenItem(se);
                    if (value == null)
                    {
                        presenter.setWellMetadataMode();
                    } else
                    {
                        presenter.setFeatureValueMode(value);
                    }
                }
            });
        return chooser;
    }

    private static void updateHeatmapKindComboBox(SimpleModelComboBox<Integer> chooser,
            List<String> featureLabelsOrNull)
    {
        List<SimpleComboboxItem<Integer>> items = createHeatmapKindModel(featureLabelsOrNull);
        chooser.removeAll();
        chooser.add(items);
        GuiUtils.autoselect(chooser, false);
    }

    private static List<SimpleComboboxItem<Integer>> createHeatmapKindModel(
            List<String> featureLabelsOrNull)
    {
        List<SimpleComboboxItem<Integer>> items = new ArrayList<SimpleComboboxItem<Integer>>();
        items.add(new SimpleComboboxItem<Integer>(null, METADATA_HEATMAP_KIND_MSG));
        if (featureLabelsOrNull != null)
        {
            int i = 0;
            for (String featureLabel : featureLabelsOrNull)
            {
                String label = FEATURE_HEATMAP_KIND_PREFIX_MSG + featureLabel;
                items.add(new SimpleComboboxItem<Integer>(i, label));
                i++;
            }
        }
        return items;
    }

    private abstract static class DetegatingFloatHeatmapRenderer<T> implements IHeatmapRenderer<T>
    {
        protected abstract Float convert(T value);

        private final IHeatmapRenderer<Float> delegator;

        public DetegatingFloatHeatmapRenderer(float min, float max,
                IRealNumberRenderer realNumberRenderer)
        {
            this.delegator = new NumberHeatmapRenderer(min, max, realNumberRenderer);
        }

        public Color getColor(T value)
        {
            return delegator.getColor(convert(value));
        }

        public List<HeatmapScaleElement> calculateScale()
        {
            return delegator.calculateScale();
        }

        public String tryGetFirstLabel()
        {
            return delegator.tryGetFirstLabel();
        }
    }

    private abstract static class DetegatingStringHeatmapRenderer<T> implements IHeatmapRenderer<T>
    {
        protected abstract String extractLabel(T value);

        private final IHeatmapRenderer<String> delegator;

        public DetegatingStringHeatmapRenderer(List<String> uniqueValues)
        {
            this.delegator = new StringHeatmapRenderer(uniqueValues);
        }

        public Color getColor(T value)
        {
            return delegator.getColor(extractLabel(value));
        }

        public List<HeatmapScaleElement> calculateScale()
        {
            return delegator.calculateScale();
        }

        public String tryGetFirstLabel()
        {
            return null;
        }
    }

    private static class WellMetadataHeatmapRenderer extends
            DetegatingStringHeatmapRenderer<WellData>
    {
        public static IHeatmapRenderer<WellData> create(List<WellData> wells)
        {
            List<String> uniqueValues = extractUniqueSortedValues(wells);
            return new WellMetadataHeatmapRenderer(uniqueValues);
        }

        private WellMetadataHeatmapRenderer(List<String> uniqueValues)
        {
            super(uniqueValues);
        }

        private static List<String> extractUniqueSortedValues(List<WellData> wells)
        {
            Set<String> uniqueValues = new HashSet<String>();
            boolean emptyWellsEncountered = false;
            for (WellData well : wells)
            {
                String label = tryExtractLabel(well);
                if (label != null)
                {
                    uniqueValues.add(label);
                } else
                {
                    emptyWellsEncountered = true;
                }
            }
            ArrayList<String> result = new ArrayList<String>(uniqueValues);
            Collections.sort(result);
            if (emptyWellsEncountered)
            {
                result.add(UNKNOWN_WELL_MSG);
            }
            return result;
        }

        @Override
        protected final String extractLabel(WellData well)
        {
            String label = tryExtractLabel(well);
            if (label == null)
            {
                return UNKNOWN_WELL_MSG;
            } else
            {
                return label;
            }
        }

        private static final String tryExtractLabel(WellData well)
        {
            WellMetadata metadata = well.tryGetMetadata();
            if (metadata == null)
            {
                return null;
            } else
            {
                return metadata.getWellSample().getEntityType().getCode();
            }
        }
    }

    private static class PlateLayouterModel
    {
        private final WellData[][] wellMatrix;

        private final List<WellData> wellList; // the same wells as in the matrix

        // ------- internal dynamix state

        private DatasetImagesReference imageDatasetOrNull;

        private List<String> featureLabelsOrNull;

        public PlateLayouterModel(PlateMetadata plateMetadata)
        {
            this.wellMatrix = createWellMatrix(plateMetadata);
            this.wellList = asList(wellMatrix);
        }

        public WellData[][] getWellMatrix()
        {
            return wellMatrix;
        }

        public List<WellData> getWellList()
        {
            return wellList;
        }

        public DatasetImagesReference tryGetImageDataset()
        {
            return imageDatasetOrNull;
        }

        public void setImageDataset(DatasetImagesReference imageDataset)
        {
            this.imageDatasetOrNull = imageDataset;
        }

        public void setFeatureVectorDataset(FeatureVectorDataset featureVectorDatasetOrNull)
        {
            unsetFeatureVectors();
            if (featureVectorDatasetOrNull == null)
            {
                this.featureLabelsOrNull = null;
            } else
            {
                this.featureLabelsOrNull = featureVectorDatasetOrNull.getFeatureLabels();
                List<? extends FeatureVectorValues> features =
                        featureVectorDatasetOrNull.getDatasetFeatures();
                for (FeatureVectorValues featureVector : features)
                {
                    WellLocation loc = featureVector.getWellLocation();
                    WellData wellData = tryGetWellData(loc);
                    if (wellData != null)
                    {
                        wellData.setFeatureValues(featureVector.getFeatureValues());
                    }
                }
            }
        }

        public List<String> tryGetFeatureLabels()
        {
            return featureLabelsOrNull;
        }

        private WellData tryGetWellData(WellLocation loc)
        {
            int rowIx = loc.getRow() - 1;
            int colIx = loc.getColumn() - 1;
            if (rowIx < wellMatrix.length && colIx < wellMatrix[rowIx].length)
            {
                return wellMatrix[rowIx][colIx];
            } else
            {
                // can happen if the plate geometry is not in sync with the feature vector database
                return null;
            }
        }

        private void unsetFeatureVectors()
        {
            for (WellData well : wellList)
            {
                well.setFeatureValues(null);
            }
        }

        // ------------------------

        // Elements will NOT contain null even if well is empty.
        private static WellData[][] createWellMatrix(PlateMetadata plateMetadata)
        {
            WellData[][] matrix = createEmptyWellMatrix(plateMetadata);
            List<WellMetadata> wells = plateMetadata.getWells();
            for (WellMetadata well : wells)
            {
                WellLocation location = well.tryGetLocation();
                if (location != null)
                {
                    WellData wellData = matrix[location.getRow() - 1][location.getColumn() - 1];
                    wellData.setMetadata(well);
                }
            }
            return matrix;
        }

        private static <T> List<T> asList(T[][] matrix)
        {
            List<T> result = new ArrayList<T>();
            for (int row = 0; row < matrix.length; row++)
            {
                for (int col = 0; col < matrix[row].length; col++)
                {
                    result.add(matrix[row][col]);
                }
            }
            return result;
        }

    }

    /** Manages heatmap presentation, controls view and model. */
    private static class HeatmapPresenter
    {
        /** Triggers changes in the view */
        static interface IViewManipulator
        {
            /** updates background color and tooltip of the well with given coordinates */
            void refreshWellStyle(int rowIx, int colIx, Color bakgroundColor, String tooltipOrNull);

            /** updates legend of the heatmap to the new one */
            void updateLegend(Widget legend);
        }

        // this renderer does not change, so we can cache it
        private final IHeatmapRenderer<WellData> cachedMetadataHeatmapRenderer;

        private final PlateLayouterModel model;

        private final IViewManipulator viewManipulations;

        private final IRealNumberRenderer realNumberRenderer;

        // ---

        public HeatmapPresenter(PlateLayouterModel model, IRealNumberRenderer realNumberRenderer,
                IViewManipulator viewManipulations)
        {
            this.model = model;
            this.viewManipulations = viewManipulations;
            this.realNumberRenderer = realNumberRenderer;
            this.cachedMetadataHeatmapRenderer =
                    WellMetadataHeatmapRenderer.create(model.getWellList());
            setWellMetadataMode();
        }

        /** Changes the presented heatmap to the one which shows well types. */
        public void setWellMetadataMode()
        {
            IHeatmapRenderer<WellData> renderer = cachedMetadataHeatmapRenderer;
            refreshHeatmap(renderer, null);
        }

        /**
         * Changes the presented heatmap to the one which shows the feature which appears in all
         * {@link WellData} on a specified index.
         */
        public void setFeatureValueMode(int featureIndex)
        {
            IHeatmapRenderer<WellData> renderer =
                    createFeatureHeatmapRenderer(model.getWellList(), featureIndex,
                            realNumberRenderer);
            refreshHeatmap(renderer, featureIndex);
        }

        // updates color of all well components
        private void refreshHeatmap(IHeatmapRenderer<WellData> renderer, Integer featureIndexOrNull)
        {
            WellData[][] wellMatrix = model.getWellMatrix();
            for (int row = 0; row < wellMatrix.length; row++)
            {
                for (int col = 0; col < wellMatrix[row].length; col++)
                {
                    WellData wellData = wellMatrix[row][col];
                    Color color = renderer.getColor(wellData);
                    String tooltipOrNull = tryGenerateTooltip(wellData, featureIndexOrNull);
                    viewManipulations.refreshWellStyle(row, col, color, tooltipOrNull);
                }
            }
            refreshLegend(renderer);
        }

        private void refreshLegend(IHeatmapRenderer<WellData> renderer)
        {
            // creates the heatmap legend using current settings
            List<HeatmapScaleElement> scale = renderer.calculateScale();
            Widget legend = HeatmapScaleFactory.create(renderer.tryGetFirstLabel(), scale);
            viewManipulations.updateLegend(legend);
        }

        private static IHeatmapRenderer<WellData> createFeatureHeatmapRenderer(
                List<WellData> wells, final int featureIndex, IRealNumberRenderer realNumberRenderer)
        {
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            for (WellData well : wells)
            {
                Float value = well.tryGetFeatureValue(featureIndex);
                if (value != null && Float.isNaN(value) == false
                        && Float.isInfinite(value) == false)
                {
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                }
            }
            return new DetegatingFloatHeatmapRenderer<WellData>(min, max, realNumberRenderer)
                {
                    @Override
                    protected Float convert(WellData well)
                    {
                        return well.tryGetFeatureValue(featureIndex);
                    }
                };
        }

        private String tryGenerateTooltip(WellData wellData, Integer featureIndexOrNull)
        {
            if (featureIndexOrNull != null)
            {
                return tryGenerateFeatureTooltip(wellData, featureIndexOrNull);
            } else
            {
                return tryGenerateMetadataTooltip(wellData);
            }
        }

        private String tryGenerateFeatureTooltip(WellData wellData, int featureIndex)
        {
            Float value = wellData.tryGetFeatureValue(featureIndex);
            List<String> featureLabels = model.tryGetFeatureLabels();
            assert featureLabels != null : "feature labels not set";
            String tooltip = featureLabels.get(featureIndex) + ": " + value;
            String metadataTooltip = tryGenerateMetadataTooltip(wellData);
            if (metadataTooltip != null)
            {
                tooltip += "\n" + metadataTooltip;
            }
            return tooltip;
        }

        private static String tryGenerateMetadataTooltip(WellData wellData)
        {
            WellMetadata metadata = wellData.tryGetMetadata();
            if (metadata == null)
            {
                return null;
            }
            String tooltip = getWellDescription(metadata);

            List<IEntityProperty> properties = metadata.getWellSample().getProperties();
            Collections.sort(properties);
            for (IEntityProperty property : properties)
            {
                PropertyType propertyType = property.getPropertyType();
                tooltip += "\n" + propertyType.getLabel() + ": " + getPropertyDisplayText(property);
                Material material = property.getMaterial();
                if (material != null
                        && material.getMaterialType().getCode()
                                .equalsIgnoreCase(ScreeningConstants.GENE_PLUGIN_TYPE_CODE))
                {
                    List<IEntityProperty> geneProperties = material.getProperties();
                    for (IEntityProperty geneProperty : geneProperties)
                    {
                        if (geneProperty.getPropertyType().getCode()
                                .equalsIgnoreCase(ScreeningConstants.GENE_SYMBOLS))
                        {
                            tooltip += " [" + geneProperty.tryGetAsString() + "]";
                        }
                    }
                }
            }
            return tooltip;
        }

        private static String getPropertyDisplayText(IEntityProperty property)
        {
            Material material = property.getMaterial();
            if (material != null)
            {
                return material.getCode();
            } else
            {
                return property.tryGetAsString();
            }
        }

        private static String getWellDescription(WellMetadata metadata)
        {
            return "Well: " + metadata.getWellSample().getSubCode();
        }

    }
}
