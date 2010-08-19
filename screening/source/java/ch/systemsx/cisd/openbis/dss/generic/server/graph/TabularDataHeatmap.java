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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import java.awt.Color;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ITabularData;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataHeatmap extends AbstractTabularDataGraph<TabularDataHeatmapConfiguration>
{

    /**
     * @param configuration
     */
    public TabularDataHeatmap(TabularDataHeatmapConfiguration configuration,
            ITabularData fileLines, OutputStream out)
    {
        super(configuration, fileLines, out);
    }

    @Override
    protected XYZDataset tryCreateChartDataset()
    {
        // DUPLICATES LOGIC IN tryIterateOverFileLinesUsing
        int xColumn = tryXColumnNumber();
        int yColumn = tryYColumnNumber();
        int zColumn = tryColumnNumberForHeader(configuration.getZAxisColumn());
        // We could not find the necessary columns in the dataset
        if (xColumn < 0 || yColumn < 0 || zColumn < 0)
        {
            return null;
        }

        // first parse the data into HeatmapElements
        HeatmapData data = parseData(xColumn, yColumn, zColumn);
        double[][] dataArray = convertHeatmapDataToArray(data);

        DefaultXYZDataset simpleDataset = new DefaultXYZDataset();
        simpleDataset.addSeries(getTitle(), dataArray);
        HeatmapDataset dataset = new HeatmapDataset(simpleDataset);
        dataset.setRange(new Range(data.minZ, data.maxZ));
        return dataset;
    }

    @Override
    protected JFreeChart createDataChart(Dataset dataset)
    {
        JFreeChart chart = createHeatmap(getTitle(), // title
                // don't use the use-provided label for the wells, just use a blank string
                "", // x-axis label
                "", // y-axis label
                (HeatmapDataset) dataset, // data
                PlotOrientation.HORIZONTAL, // plot orientation
                false, // create legend?
                false, // generate tooltips?
                false // generate URLs?
                );

        return chart;
    }

    private static JFreeChart createHeatmap(String title, String xAxisLabel, String yAxisLabel,
            HeatmapDataset dataset, PlotOrientation orientation, boolean legend, boolean tooltips,
            boolean urls)
    {
        if (orientation == null)
        {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setTickUnit(new NumberTickUnit(1.0));
        xAxis.setInverted(true);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);

        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setBlockAnchor(RectangleAnchor.CENTER);
        PaintScale paintScale = getPaintScale(dataset);
        renderer.setPaintScale(paintScale);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        plot.setOrientation(orientation);
        plot.setForegroundAlpha(1.f);
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
        ChartFactory.getChartTheme().apply(chart);

        NumberAxis scaleAxis = new NumberAxis("Scale");
        scaleAxis.setRange(dataset.getRange());
        scaleAxis.setStandardTickUnits(new TabularDataTickUnitSource());
        PaintScaleLegend psl = new PaintScaleLegend(paintScale, scaleAxis);
        psl.setMargin(new RectangleInsets(5, 5, 5, 5));
        psl.setPosition(RectangleEdge.RIGHT);
        psl.setAxisOffset(5.0);
        chart.addSubtitle(psl);
        return chart;
    }

    /**
     * Create a LookupPaintScale based on the <a href="http://colorbrewer.org/">Color Brewer</a>
     * RdBu color scheme.
     */
    private static PaintScale getPaintScale(HeatmapDataset dataset)
    {
        // Use the Color Brewer RdBu color scheme with 11 steps
        Range range = dataset.getRange();
        double lowerBound = range.getLowerBound();
        double upperBound = range.getUpperBound();

        // Handle the degenerate case
        if (lowerBound == upperBound)
        {
            LookupPaintScale paintScale =
                    new LookupPaintScale(lowerBound, lowerBound + 1, Color.WHITE);
            paintScale.add(lowerBound, new Color(247, 247, 247));
            return paintScale;
        }

        LookupPaintScale paintScale = new LookupPaintScale(lowerBound, upperBound, Color.WHITE);
        double binMin = range.getLowerBound();
        double binStep = range.getLength() / 11;
        // 1
        paintScale.add(binMin, new Color(5, 48, 97));
        // 2
        binMin += binStep;
        paintScale.add(binMin, new Color(33, 102, 172));
        // 3
        binMin += binStep;
        paintScale.add(binMin, new Color(67, 147, 195));
        // 4
        binMin += binStep;
        paintScale.add(binMin, new Color(146, 197, 222));
        // 5
        binMin += binStep;
        paintScale.add(binMin, new Color(209, 229, 240));
        // 6
        binMin += binStep;
        paintScale.add(binMin, new Color(247, 247, 247));
        // 7
        binMin += binStep;
        paintScale.add(binMin, new Color(253, 219, 199));
        // 8
        binMin += binStep;
        paintScale.add(binMin, new Color(244, 165, 130));
        // 9
        binMin += binStep;
        paintScale.add(binMin, new Color(214, 96, 77));
        // 10
        binMin += binStep;
        paintScale.add(binMin, new Color(178, 24, 43));
        // 11
        binMin += binStep;
        paintScale.add(binMin, new Color(103, 0, 31));
        return paintScale;
    }

    private HeatmapData parseData(int xColumn, int yColumn, int zColumn)
    {
        HeatmapData heatmapData = new HeatmapData();

        // Note what the max x and max y values are, so we can convert to an array
        heatmapData.maxX = 0;
        heatmapData.maxY = 0;
        boolean areZBoundsInitialized = false;
        List<String[]> lines = fileLines.getDataLines();
        for (String[] line : lines)
        {
            HeatmapElement element = new HeatmapElement();
            if (configuration.isXYSplit())
            {
                try
                {
                    element.x = Integer.parseInt(line[xColumn]);
                } catch (NumberFormatException ex)
                {
                    // handle a case when X is alphanumeric
                    element.x =
                            Location.tryCreateLocationFromTransposedMatrixCoordinate(
                                    line[xColumn] + "1").getY();
                }
                element.y = Integer.parseInt(line[yColumn]);
            } else
            {
                Location loc =
                        Location.tryCreateLocationFromTransposedMatrixCoordinate(line[xColumn]);
                // Transpose the x and y
                element.x = loc.getY();
                element.y = loc.getX();
            }
            element.z = Double.parseDouble(line[zColumn]);
            if (false == areZBoundsInitialized)
            {
                heatmapData.minZ = element.z;
                heatmapData.maxZ = element.z;
                areZBoundsInitialized = true;
            }
            heatmapData.elements.add(element);

            if (element.x > heatmapData.maxX)
            {
                heatmapData.maxX = element.x;
            }
            if (element.y > heatmapData.maxY)
            {
                heatmapData.maxY = element.y;
            }
            if (element.z < heatmapData.minZ)
            {
                heatmapData.minZ = element.z;
            }
            if (element.z > heatmapData.maxZ)
            {
                heatmapData.maxZ = element.z;
            }
        }

        return heatmapData;
    }

    private double[][] convertHeatmapDataToArray(HeatmapData data)
    {
        double[][] dataArray = new double[3][];
        double[] xArray = new double[data.elements.size()];
        double[] yArray = new double[data.elements.size()];
        double[] zArray = new double[data.elements.size()];
        dataArray[0] = xArray;
        dataArray[1] = yArray;
        dataArray[2] = zArray;

        int i = 0;
        for (HeatmapElement elt : data.elements)
        {
            xArray[i] = elt.x;
            yArray[i] = elt.y;
            zArray[i++] = elt.z;
        }
        return dataArray;
    }

    private class HeatmapElement
    {
        private int x;

        private int y;

        private double z;
    }

    private class HeatmapData
    {
        private int maxX;

        private int maxY;

        private double minZ = 0;

        private double maxZ = 0;

        private final ArrayList<HeatmapElement> elements = new ArrayList<HeatmapElement>();
    }

}
