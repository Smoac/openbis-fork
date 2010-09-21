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
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.DefaultChannelState;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
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
    /** @return widget with plate visualization for a plate without images. */
    public static Widget createVisualization(PlateMetadata plateMetadata,
            ScreeningViewContext viewContext)
    {
        return new PlateLayouter(viewContext, plateMetadata, null).renderVisualizationWidget();
    }

    /** @return widget with plate visualization - all the wells and possibility to browse images. */
    public static Widget createVisualization(PlateImages plateImages,
            ScreeningViewContext viewContext)
    {
        return new PlateLayouter(viewContext, plateImages.getPlateMetadata(),
                plateImages.getImagesDataset()).renderVisualizationWidget();
    }

    // ------- internal state

    private final ScreeningViewContext viewContext;

    private final PlateMetadata plateMetadata;

    // can be changed for the visualization dynamically
    private DatasetImagesReference imageDatasetOrNull;

    public PlateLayouter(ScreeningViewContext viewContext, PlateMetadata plateMetadata,
            DatasetImagesReference imageDatasetOrNull)
    {
        this.viewContext = viewContext;
        this.plateMetadata = plateMetadata;
        this.imageDatasetOrNull = imageDatasetOrNull;
    }

    /**
     * Renders widget which visualizes the plate. The image dataset which is used to display images
     * can be changed afterwards with {@link #changeDisplayedImageDataset} method without
     * re-rendering.
     */
    public Widget renderVisualizationWidget()
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        LayoutContainer wellsMatrix = renderWellsMatrix();
        container.add(new Text(
                "Hold the mouse cursor over a well or click on it to get the details."));
        container.add(wellsMatrix);
        container.add(renderWellsLegend());
        return container;
    }

    public void changeDisplayedImageDataset(DatasetImagesReference newImageDatasetOrNull)
    {
        this.imageDatasetOrNull = newImageDatasetOrNull;
    }

    private LayoutContainer renderWellsMatrix()
    {
        WellData[][] wellMatrix = createMatrix(plateMetadata);
        List<Widget> wellWidgets = createWellWidgets(wellMatrix);

        LayoutContainer plateMatrix = new LayoutContainer();
        plateMatrix.setScrollMode(Scroll.AUTO);
        TableLayout layout = new TableLayout(getColumnsNum(wellMatrix));
        layout.setCellSpacing(2);
        plateMatrix.setLayout(layout);
        for (Widget wellWidget : wellWidgets)
        {
            plateMatrix.add(wellWidget);
        }
        return plateMatrix;
    }

    private List<Widget> createWellWidgets(WellData[][] wellMatrix)
    {
        List<Widget> wellWidgets = new ArrayList<Widget>();
        int rowsNum = wellMatrix.length;
        int colsNum = getColumnsNum(wellMatrix);
        DefaultChannelState channelState = new DefaultChannelState();
        for (int row = 0; row < rowsNum; row++)
        {
            for (int col = 0; col < colsNum; col++)
            {
                Widget wellWidget = tryCreateLabelWidget(row, col);
                if (wellWidget == null)
                {
                    WellData wellData = wellMatrix[row][col];
                    if (hasImages() == false && wellData.tryGetMetadata() == null)
                    {
                        wellWidget = createEmptyWellWidget();
                    } else
                    {
                        wellWidget = createWellWidget(wellData, channelState);
                    }
                }
                wellWidgets.add(wellWidget);
            }
        }
        return wellWidgets;
    }

    private Component createContentWell(WellData wellData)
    {
        WellMetadata metadata = wellData.tryGetMetadata();
        if (metadata == null)
        {
            Component widget = createBox();
            // we may have images but at the same time no metadata
            if (hasImages())
            {
                return PlateStyleSetter.setNoMetadataWellStyle(widget);
            } else
            {
                return PlateStyleSetter.setEmptyWellStyle(widget);
            }
        } else
        {
            boolean isControlWell = isControlWell(metadata);
            return createNonEmptyWell(isControlWell);
        }
    }

    private boolean hasImages()
    {
        return imageDatasetOrNull != null;
    }

    // ----------- static methods

    private static int getColumnsNum(WellData[][] wellMatrix)
    {
        int rowsNum = wellMatrix.length;
        return (rowsNum == 0 ? 0 : wellMatrix[0].length);
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

    private static LayoutContainer renderWellsLegend()
    {
        LayoutContainer legend = new LayoutContainer();
        legend.setLayout(new TableLayout(2));

        TableData mergedColumns = new TableData();
        mergedColumns.setColspan(2);

        Component verticalSeparator = createBox();
        verticalSeparator.setHeight("10");
        legend.add(verticalSeparator, mergedColumns);

        legend.add(createNonEmptyWell(false));
        legend.add(new Text("Non-control Well"));

        legend.add(createNonEmptyWell(true));
        legend.add(new Text("Control Well"));

        legend.add(createEmptyWellWidget());
        legend.add(new Text("Empty Well"));

        legend.add(noMetadataWellWidget());
        legend.add(new Text("Images without Metadata"));

        return legend;
    }

    private static Component createEmptyWellWidget()
    {
        Component widget = createBox();
        widget.setEnabled(false);
        return PlateStyleSetter.setEmptyWellStyle(widget);
    }

    private static Component noMetadataWellWidget()
    {
        Component widget = createBox();
        widget.setEnabled(true);
        return PlateStyleSetter.setNoMetadataWellStyle(widget);
    }

    private Component createWellWidget(final WellData wellData,
            final DefaultChannelState channelState)
    {
        Component widget = createContentWell(wellData);
        widget.addListener(Events.OnMouseDown, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent ce)
                {
                    WellContentDialog.showContentDialog(wellData, imageDatasetOrNull, channelState,
                            viewContext);
                }
            });
        widget.sinkEvents(Events.OnMouseDown.getEventCode());
        setWellDescription(wellData, widget);
        return widget;
    }

    private static Component createNonEmptyWell(boolean isControlWell)
    {
        Component widget = createBox();
        return setNonEmptyWellStyle(widget, isControlWell);
    }

    private static Component createBox()
    {
        return new Text("");
    }

    private static Component setNonEmptyWellStyle(Component widget, boolean isControlWell)
    {
        if (isControlWell)
        {
            return PlateStyleSetter.setControlWellStyle(widget);
        } else
        {
            return PlateStyleSetter.setNonControlWellStyle(widget);
        }
    }

    private static boolean isControlWell(WellMetadata metadata)
    {
        String sampleTypeCode = metadata.getWellSample().getSampleType().getCode();
        return sampleTypeCode.equalsIgnoreCase(ScreeningConstants.CONTROL_WELL_TYPE_CODE);
    }

    private static void setWellDescription(final WellData wellData, Component widget)
    {
        WellMetadata metadata = wellData.tryGetMetadata();
        if (metadata == null)
        {
            return;
        }
        String tooltip = getWellDescription(metadata);

        List<IEntityProperty> properties = metadata.getWellSample().getProperties();
        Collections.sort(properties);
        for (IEntityProperty property : properties)
        {
            PropertyType propertyType = property.getPropertyType();
            tooltip += "<br>" + propertyType.getLabel() + ": " + property.tryGetAsString();
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
        GWTUtils.setToolTip(widget, tooltip);

    }

    private static String getWellDescription(WellMetadata metadata)
    {
        return "Well: " + metadata.getWellSample().getSubCode();
    }

    // Elements will not contain null even if well is empty.
    // Numbering starts with 1 so row and column with index 0 are left empty.
    private static WellData[][] createMatrix(PlateMetadata plateMetadata)
    {
        WellData[][] matrix = createEmptyWellMatrix(plateMetadata);
        List<WellMetadata> wells = plateMetadata.getWells();
        for (WellMetadata well : wells)
        {
            WellLocation location = well.tryGetLocation();
            if (location != null)
            {
                WellData wellData = matrix[location.getRow()][location.getColumn()];
                wellData.setMetadata(well);
            }
        }
        return matrix;
    }

    private static WellData[][] createEmptyWellMatrix(PlateMetadata plateMetadata)
    {
        WellData[][] data =
                new WellData[plateMetadata.getRowsNum() + 1][plateMetadata.getColsNum() + 1];
        for (int row = 1; row < data.length; row++)
        {
            for (int col = 1; col < data[row].length; col++)
            {
                data[row][col] = new WellData(new WellLocation(row, col));
            }
        }
        return data;
    }

    // ---------


    /** @return layout data with big margin */
    public static RowData createRowLayoutMarginData()
    {
        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(10));
        return layoutData;
    }

}
