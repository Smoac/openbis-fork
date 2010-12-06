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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.List;

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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.RealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateStyleSetter;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellContentDialog;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.SimpleModelComboBox.SimpleComboboxItem;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;

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

    private HeatmapPresenter.IHeatmapViewManipulator createViewManipulator(
            final Component[][] renderedWells, final LayoutContainer legendContainer)
    {
        return new HeatmapPresenter.IHeatmapViewManipulator()
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

    /** @return widget for the plate layout */
    public Widget getView()
    {
        return view;
    }

    /** changes the image dataset from which images on the well detail view are displayed */
    public void changeDisplayedImageDataset(DatasetImagesReference newImageDatasetOrNull)
    {
        this.model.setImageDataset(newImageDatasetOrNull);
    }

    /** changes the feature vector dataset presented on the plate layout */
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
        // container.setScrollMode(Scroll.AUTO);
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
        separator
                .setPixelSize(PlateStyleSetter.WELL_BOX_SIZE_PX, PlateStyleSetter.WELL_BOX_SIZE_PX);
        plateContainer.add(separator);
        plateContainer.add(legendContainer);
        container.add(plateContainer);

        return container;
    }

    private static LayoutContainer renderPlateLayout(Component[][] renderedWells)
    {
        LayoutContainer plateMatrix = new LayoutContainer();
        int columnsNum = getColumnsNum(renderedWells) + 1;
        TableLayout layout = new TableLayout(columnsNum);
        layout.setCellSpacing(2);
        plateMatrix.setLayout(layout);

        plateMatrix.setScrollMode(Scroll.AUTO);

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

        for (int row = 0; row < rowsNum; row++)
        {
            for (int col = 0; col < colsNum; col++)
            {
                WellData wellData = wellMatrix[row][col];
                wells[row][col] = createWellWidget(wellData, model, viewContext);
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
            final PlateLayouterModel model, final ScreeningViewContext screeningViewContext)
    {
        Component widget = createWellBox(wellData);

        widget.addListener(Events.OnMouseDown, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent ce)
                {
                    IScreeningClientServiceAsync service = screeningViewContext.getService();
                    DatasetImagesReference dataset = model.tryGetImageDataset();
                    if (dataset == null)
                    {
                        WellContentDialog.showContentDialog(wellData, null, screeningViewContext);
                    } else
                    {
                        // Reload meta data because they might be out dated especially when
                        // image transformer factory has changed. For the image URL the
                        // signature of the factory is needed to distinguish them. This is important
                        // because Web browser cache images.
                        service.getPlateContentForDataset(new TechId(dataset.getDatasetId()),
                                new AbstractAsyncCallback<PlateImages>(screeningViewContext)
                                    {
                                        @Override
                                        protected void process(PlateImages plateContent)
                                        {
                                            DatasetImagesReference ds =
                                                    plateContent.getImagesDataset();
                                            WellContentDialog.showContentDialog(wellData, ds,
                                                    screeningViewContext);
                                        }
                                    });
                    }
                }
            });
        widget.sinkEvents(Events.OnMouseDown.getEventCode());
        return widget;
    }

    private static Component createBox()
    {
        return new Text("");
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

    /** @return layout data with big margin on left and right */
    public static RowData createRowLayoutVerticalMargin()
    {
        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(0, MARGIN_SIZE_PX, 0, MARGIN_SIZE_PX));
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
}
