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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataHeatmap extends AbstractTabularDataGraph<TabularDataHeatmapConfiguration>
{

    private final Pattern xySplitterPattern;

    /**
     * @param configuration
     */
    public TabularDataHeatmap(TabularDataHeatmapConfiguration configuration,
            DatasetFileLines fileLines, OutputStream out)
    {
        super(configuration, fileLines, out);
        xySplitterPattern = Pattern.compile("([A-Z])([0-9]*)");
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

        DefaultXYZDataset dataset = new DefaultXYZDataset();
        dataset.addSeries(getTitle(), dataArray);
        return dataset;
    }

    @Override
    protected JFreeChart createDataChart(Dataset dataset)
    {
        JFreeChart chart = createHeatmap(getTitle(), // title
                configuration.getXAxisColumn(), // x-axis label
                configuration.getYAxisColumn(), // y-axis label
                (XYZDataset) dataset, // data
                PlotOrientation.HORIZONTAL, // plot orientation
                false, // create legend?
                false, // generate tooltips?
                false // generate URLs?
                );

        return chart;
    }

    private static JFreeChart createHeatmap(String title, String xAxisLabel, String yAxisLabel,
            XYZDataset dataset, PlotOrientation orientation, boolean legend, boolean tooltips,
            boolean urls)
    {
        if (orientation == null)
        {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        xAxis.setTickUnit(new NumberTickUnit(1.));
        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        yAxis.setAutoRangeIncludesZero(false);

        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        PaintScale paintScale = new GrayPaintScale();
        renderer.setPaintScale(paintScale);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        plot.setOrientation(orientation);
        plot.setForegroundAlpha(0.5f);
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
        ChartFactory.getChartTheme().apply(chart);

        NumberAxis scaleAxis = new NumberAxis("Scale");
        PaintScaleLegend psl = new PaintScaleLegend(paintScale, scaleAxis);
        psl.setMargin(new RectangleInsets(5, 5, 5, 5));
        psl.setPosition(RectangleEdge.RIGHT);
        psl.setAxisOffset(5.0);
        chart.addSubtitle(psl);
        return chart;
    }

    private HeatmapData parseData(int xColumn, int yColumn, int zColumn)
    {
        HeatmapData heatmapData = new HeatmapData();

        // Note what the max x and max y values are, so we can convert to an array
        heatmapData.maxX = 0;
        heatmapData.maxY = 0;
        List<String[]> lines = fileLines.getDataLines();
        for (String[] line : lines)
        {
            HeatmapElement element = new HeatmapElement();
            if (configuration.isXYSplit())
            {
                element.x = Integer.parseInt(line[xColumn]);
                element.y = Integer.parseInt(line[yColumn]);
            } else
            {
                splitColumnIntoXandY(line[xColumn], element);
            }
            element.z = Double.parseDouble(line[zColumn]);
            heatmapData.elements.add(element);

            if (element.x > heatmapData.maxX)
            {
                heatmapData.maxX = element.x;
            }
            if (element.y > heatmapData.maxY)
            {
                heatmapData.maxY = element.y;
            }
        }

        return heatmapData;
    }

    private void splitColumnIntoXandY(String string, HeatmapElement element)
    {
        Pattern p = xySplitterPattern;
        Matcher m = p.matcher(string);
        if (m.matches())
        {
            String wellX = m.group(1);
            // The x index is the index of the letter minus the index of 'A'
            element.x = (wellX.charAt(0) - 'A') + 1;
            element.y = Integer.parseInt(m.group(2));
        }
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

        private final ArrayList<HeatmapElement> elements = new ArrayList<HeatmapElement>();
    }

}
