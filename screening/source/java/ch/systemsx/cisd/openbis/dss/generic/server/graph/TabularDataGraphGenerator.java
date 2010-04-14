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
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;

/**
 * Generates a graph from tabular data. Both the graph type and the columns used for the graph can
 * be configured.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphGenerator
{
    public static enum ChartType
    {
        AREA, BAR, LINE, SPLINE
    }

    public final static ChartType DEFAULT_CHART_TYPE = ChartType.LINE;

    private final String title;

    private final String xAxisHeader;

    private final String yAxisHeader;

    private final DatasetFileLines fileLines;

    private final int imageWidth;

    private final int imageHeight;

    private final ChartType chartType;

    private final OutputStream out;

    /**
     * Initialize a chromatogram generator. The chart type is defaulted to DEFAULT_CHART_TYPE.
     * 
     * @param fileLines The data to generate a graph from.
     * @param out The stream to write the image to
     * @param imageWidth The width in pixels of the generated image
     * @param imageHeight The height in pixels of the generated image
     */
    public TabularDataGraphGenerator(DatasetFileLines fileLines, OutputStream out,
            int imageWidth, int imageHeight)
    {
        this("Chart", "TotalCells", "InfectedCells", imageWidth, imageHeight, DEFAULT_CHART_TYPE,
                fileLines, out);
    }

    /**
     * Initialize a chromatogram generator.
     * 
     * @param title The title of the graph
     * @param xAxisColumnHeader The header of the data column used to create the x-axis
     * @param yAxisColumnHeader The header of the data column used to create the y-axis
     * @param imageWidth The width in pixels of the generated image
     * @param imageHeight The height in pixels of the generated image
     * @param chartType One of AREA, BAR, LINE, or SPLINE
     * @param fileLines The data to generate a graph from.
     * @param out The stream to write the image to.
     */
    public TabularDataGraphGenerator(String title, String xAxisColumnHeader,
            String yAxisColumnHeader, int imageWidth, int imageHeight, ChartType chartType,
            DatasetFileLines fileLines, OutputStream out)
    {
        this.title = title;
        this.xAxisHeader = xAxisColumnHeader;
        this.yAxisHeader = yAxisColumnHeader;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.chartType = chartType;
        this.fileLines = fileLines;
        this.out = out;
    }

    /**
     * Create an image from the chromatogram and write it to the output stream.
     * 
     * @throws IOException
     */
    public void generateImage() throws IOException
    {
        JFreeChart chart = createChart(tryCreateDataset());
        ChartUtilities.writeChartAsPNG(out, chart, imageWidth, imageHeight);
    }

    private JFreeChart createChart(IntervalXYDataset dataset)
    {
        JFreeChart chart = null;
        switch (chartType)
        {
            case AREA:
                chart = createAreaChart(dataset);
                break;
            case BAR:
                chart = createBarChart(dataset);
                break;
            case LINE:
                chart = createLineChart(dataset);
                break;
            case SPLINE:
                chart = createSplineChart(dataset);
                break;
        }

        return chart;
    }

    private JFreeChart createBarChart(IntervalXYDataset dataset)
    {
        JFreeChart chart = ChartFactory.createXYBarChart(title, // title
                "Run Time", // x-axis label
                false, // use date axis?
                "Intensity", // y-axis label
                dataset, // data
                PlotOrientation.VERTICAL, // plot orientation
                false, // create legend?
                false, // generate tooltips?
                false // generate URLs?
                );

        XYPlot plot = configureChart(chart);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYBarRenderer)
        {
            XYBarRenderer renderer = (XYBarRenderer) r;
            renderer.setShadowVisible(false);
        }

        return chart;
    }

    private JFreeChart createLineChart(IntervalXYDataset dataset)
    {
        JFreeChart chart = ChartFactory.createXYLineChart(title, // title
                "run time", // x-axis label
                "intensity", // y-axis label
                dataset, // data
                PlotOrientation.VERTICAL, // plot orientation
                false, // create legend?
                false, // generate tooltips?
                false // generate URLs?
                );

        configureChart(chart);

        return chart;
    }

    private JFreeChart createSplineChart(IntervalXYDataset dataset)
    {
        JFreeChart chart = ChartFactory.createXYLineChart(title, // title
                "run time", // x-axis label
                "intensity", // y-axis label
                dataset, // data
                PlotOrientation.VERTICAL, // plot orientation
                false, // create legend?
                false, // generate tooltips?
                false // generate URLs?
                );

        XYPlot plot = configureChart(chart);

        XYSplineRenderer renderer = new XYSplineRenderer();
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);

        return chart;
    }

    private JFreeChart createAreaChart(IntervalXYDataset dataset)
    {
        JFreeChart chart = ChartFactory.createXYAreaChart(title, // title
                "run time", // x-axis label
                "intensity", // y-axis label
                dataset, // data
                PlotOrientation.VERTICAL, // plot orientation
                false, // create legend?
                false, // generate tooltips?
                false // generate URLs?
                );

        configureChart(chart);

        return chart;
    }

    /**
     * Create a JFreeChart dataset from the chromatogram data.
     */
    private IntervalXYDataset tryCreateDataset()
    {
        XYSeries s1 = new XYSeries(title);
        String[] headers = fileLines.getHeaderTokens();
        // figure out which indices are used for the x- and y-axes
        int xIndex = -1, yIndex = -1, i = 0;
        for (String header : headers)
        {
            if (xAxisHeader.equals(header))
            {
                xIndex = i;
            } else if (yAxisHeader.equals(header))
            {
                yIndex = i;
            }
            ++i;
        }

        // The data set did not contain the expected headers.
        if (xIndex < 0 || yIndex < 0)
        {
            return null;
        }

        List<String[]> lines = fileLines.getDataLines();
        for (String[] line : lines)
        {
            float x = Float.valueOf(line[xIndex]);
            float y = Float.valueOf(line[yIndex]);
            s1.add(x, y);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1);

        return dataset;
    }

    /**
     * Apply the visual preferences to the chart.
     */
    private XYPlot configureChart(JFreeChart chart)
    {
        Font font = JFreeChart.DEFAULT_TITLE_FONT;
        chart.getTitle().setFont(cloneFontWithNewSize(font, Math.max(11, imageHeight / 30)));
        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = (XYPlot) chart.getPlot();
        setAxisLabelFontSize(plot.getDomainAxis());
        setAxisLabelFontSize(plot.getRangeAxis());
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);

        return plot;
    }

    private void setAxisLabelFontSize(Axis axis)
    {
        Font labelFont = axis.getLabelFont();
        axis.setLabelFont(cloneFontWithNewSize(labelFont, Math.max(10, imageHeight / 40)));
    }

    private Font cloneFontWithNewSize(Font font, int newSize)
    {
        return new Font(font.getName(), font.getStyle(), newSize);
    }
}
