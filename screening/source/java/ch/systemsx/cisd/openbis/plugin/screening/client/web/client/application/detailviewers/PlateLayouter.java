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
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.DefaultChannelState;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Utilities to create plate visualization.
 * 
 * @author Tomasz Pylak
 */
public class PlateLayouter
{
    /** @return widget with plate visualization - all the wells and possibility to browse images. */
    public static Widget createVisualization(PlateImages plateImages,
            ScreeningViewContext viewContext)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        LayoutContainer wellsMatrix = renderWellsMatrix(plateImages, viewContext);
        container.add(wellsMatrix);
        container.add(renderWellsLegend());
        return container;
    }

    private static LayoutContainer renderWellsMatrix(PlateImages plateContent,
            ScreeningViewContext viewContext)
    {
        WellData[][] wellMatrix = createMatrix(plateContent);
        List<Widget> wellWidgets = createWellWidgets(wellMatrix, plateContent, viewContext);

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

    private static List<Widget> createWellWidgets(WellData[][] wellMatrix,
            PlateImages plateContent, ScreeningViewContext viewContext)
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
                    if (wellData.tryGetImages() == null && wellData.tryGetMetadata() == null)
                    {
                        wellWidget = createEmptyWellWidget();
                    } else
                    {
                        wellWidget =
                                createWellWidget(wellData, plateContent, channelState, viewContext);
                    }
                }
                wellWidgets.add(wellWidget);
            }
        }
        return wellWidgets;
    }

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

        legend.add(
                new Text("Hold the mouse cursor over a well or click on it to get the details."),
                mergedColumns);

        legend.add(createNonEmptyWell(false));
        legend.add(new Text("Non-empty well"));

        legend.add(createNonEmptyWell(true));
        legend.add(new Text("Control well"));

        legend.add(createEmptyWellWidget());
        legend.add(new Text("Empty well"));

        legend.add(noMetadataWellWidget());
        legend.add(new Text("No metadata well"));

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

    private static Component createWellWidget(final WellData wellData,
            final PlateImages plateContent, final DefaultChannelState channelState,
            final ScreeningViewContext viewContext)
    {
        Component widget = createContentWell(wellData);
        widget.addListener(Events.OnMouseDown, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent ce)
                {
                    WellContentDialog.showContentDialog(wellData, channelState, viewContext);
                }
            });
        widget.sinkEvents(Events.OnMouseDown.getEventCode());
        setWellDescription(wellData, widget);
        return widget;
    }

    private static Component createContentWell(WellData wellData)
    {
        WellMetadata metadata = wellData.tryGetMetadata();
        if (metadata == null)
        {
            Component widget = createBox();
            // we may have images but at the same time no metadata
            if (wellData.tryGetImages() != null)
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
        List<IEntityProperty> properties = metadata.getWellSample().getProperties();

        String tooltip = "Well: " + wellData.getWellDescription();

        for (IEntityProperty property : properties)
        {
            tooltip +=
                    "<br>" + property.getPropertyType().getLabel() + ": "
                            + property.tryGetAsString();
        }
        GWTUtils.setToolTip(widget, tooltip);

    }

    // Elements will not contain null even if well is empty.
    // Numbering starts with 1 so row and column with index 0 are left empty.
    private static WellData[][] createMatrix(PlateImages plateContent)
    {
        WellData[][] matrix = createWellData(plateContent);
        List<WellMetadata> wells = plateContent.getWells();
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

    private static WellData[][] createWellData(PlateImages plateContent)
    {
        WellData[][] data =
                new WellData[plateContent.getRowsNum() + 1][plateContent.getColsNum() + 1];
        for (int row = 1; row < data.length; row++)
        {
            for (int col = 1; col < data[row].length; col++)
            {
                data[row][col] = WellData.create(plateContent, new WellLocation(row, col));
            }
        }
        return data;
    }

    // ---------

    /** @return a button which shows a grid with the plate metadata */
    public static Button createPlateMetadataButton(final Sample plate,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        return new Button("Show Plate Report", new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    DispatcherHelper.dispatchNaviEvent(createPlateMetadataTabFactory());
                }

                private AbstractTabItemFactory createPlateMetadataTabFactory()
                {
                    return new AbstractTabItemFactory()
                        {
                            @Override
                            public ITabItem create()
                            {
                                return DefaultTabItem.create("Plate Report: " + plate.getCode(),
                                        PlateMetadataBrowser.create(viewContext, new TechId(plate
                                                .getId())), viewContext);
                            }

                            @Override
                            public String getId()
                            {
                                return GenericConstants.ID_PREFIX + "plate-metadata-"
                                        + plate.getId();
                            }

                            @Override
                            public HelpPageIdentifier getHelpPageIdentifier()
                            {
                                return new HelpPageIdentifier(HelpPageDomain.SAMPLE,
                                        HelpPageAction.VIEW);
                            }
                        };
                }
            });
    }

    /** @return layout data with big margin */
    public static RowData createRowLayoutMarginData()
    {
        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(10));
        return layoutData;
    }

}
