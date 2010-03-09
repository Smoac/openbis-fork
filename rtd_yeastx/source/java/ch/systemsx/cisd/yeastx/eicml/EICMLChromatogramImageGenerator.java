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

package ch.systemsx.cisd.yeastx.eicml;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;

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

/**
 * Generates a chromatogram from EICML data. Can be configured to generate the chromatogram in
 * several different styles -- Line (the standard), Area (filled in contour), Spline (Line using
 * spline curves), and Bar (3-D bar graph).
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class EICMLChromatogramImageGenerator
{
    public static enum ChartType
    {
        AREA, BAR, LINE, SPLINE
    }

    public final static ChartType DEFAULT_CHART_TYPE = ChartType.LINE;

    private final ChromatogramDTO chromatogram;

    private final OutputStream out;

    private final int imageWidth;

    private final int imageHeight;

    private final ChartType chartType;

    /**
     * Initialize a chromatogram generator. The chart type is defaulted to DEFAULT_CHART_TYPE.
     * 
     * @param chromatogram The data to generate a chromatogram image from
     * @param out The stream to write the image to
     * @param imageWidth The width in pixels of the generated image
     * @param imageHeight The height in pixels of the generated image
     */
    public EICMLChromatogramImageGenerator(ChromatogramDTO chromatogram, OutputStream out,
            int imageWidth, int imageHeight)
    {
        this(chromatogram, out, imageWidth, imageHeight, DEFAULT_CHART_TYPE);
    }

    /**
     * Initialize a chromatogram generator.
     * 
     * @param chromatogram The data to generate a chromatogram image from.
     * @param out The stream to write the image to.
     * @param imageWidth The width in pixels of the generated image
     * @param imageHeight The height in pixels of the generated image
     * @param chartType One of AREA, BAR, LINE, or SPLINE
     */
    public EICMLChromatogramImageGenerator(ChromatogramDTO chromatogram, OutputStream out,
            int imageWidth, int imageHeight, ChartType chartType)
    {
        this.chromatogram = chromatogram;
        this.out = out;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.chartType = chartType;
    }

    /**
     * Create an image from the chromatogram and write it to the output stream.
     * 
     * @throws IOException
     */
    public void generateImage() throws IOException
    {
        JFreeChart chart = createChart(createDataset());
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
        JFreeChart chart = ChartFactory.createXYBarChart(chromatogram.getLabel(), // title
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
        JFreeChart chart = ChartFactory.createXYLineChart(chromatogram.getLabel(), // title
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
        JFreeChart chart = ChartFactory.createXYLineChart(chromatogram.getLabel(), // title
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
        JFreeChart chart = ChartFactory.createXYAreaChart(chromatogram.getLabel(), // title
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
    private IntervalXYDataset createDataset()
    {
        XYSeries s1 = new XYSeries(chromatogram.getLabel());
        float[] runTimes = chromatogram.getRunTimes();
        float[] intensities = chromatogram.getIntensities();
        for (int i = 0; i < intensities.length; ++i)
        {
            s1.add(runTimes[i], intensities[i]);
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
