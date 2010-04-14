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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.DefaultXYDataset;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;

/**
 * Abstract superclass for the different kinds of graphs.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractTabularDataGraph<T extends TabularDataGraphConfiguration>
{
    protected final T configuration;

    protected final DatasetFileLines fileLines;

    protected final OutputStream out;

    protected AbstractTabularDataGraph(T configuration, DatasetFileLines fileLines, OutputStream out)
    {
        this.configuration = configuration;
        this.fileLines = fileLines;
        this.out = out;
    }

    // Public API
    /**
     * Create an image from the file lines and write it to the output stream.
     * 
     * @throws IOException
     */
    public void generateImage() throws IOException
    {
        JFreeChart chart = createChart();
        ChartUtilities.writeChartAsPNG(out, chart, configuration.getImageWidth(), configuration
                .getImageHeight());
    }

    // For Subclasses
    /**
     * Return the configuration for this graph.
     */
    protected T getConfiguration()
    {
        return configuration;
    }

    /**
     * Get the title (delegates to the configuration).
     */
    protected String getTitle()
    {
        return configuration.getTitle();
    }

    /**
     * Return the column number for the X column or -1 if none was found
     */
    protected int tryXColumnNumber()
    {
        return tryColumnNumberForHeader(configuration.getXAxisColumn());
    }

    /**
     * Return the column number for the Y column or -1 if none was found
     */
    protected int tryYColumnNumber()
    {
        return tryColumnNumberForHeader(configuration.getYAxisColumn());
    }

    /**
     * Return the column number for the column header or -1 if none was found
     */
    protected int tryColumnNumberForHeader(String columnHeader)
    {
        String[] headers = fileLines.getHeaderTokens();
        int i = 0;
        for (String header : headers)
        {
            if (columnHeader.equals(header))
            {
                return i;
            }
            ++i;
        }

        return -1;
    }

    /**
     * Create a chart with an error message.
     */
    protected JFreeChart createErrorChart()
    {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart =
                ChartFactory.createXYAreaChart(
                        "Error : Could not find requested columns in dataset.", // title
                        configuration.getXAxisColumn(), // x-axis label
                        configuration.getYAxisColumn(), // y-axis label
                        dataset, // data
                        PlotOrientation.VERTICAL, // plot orientation
                        false, // create legend?
                        false, // generate tooltips?
                        false // generate URLs?
                        );

        return chart;
    }

    /**
     * Convert the data to a {@link Dataset}. If there were problems converting, create an error
     * chart. If conversion was successful, create a chart using the data set.
     */
    protected JFreeChart createChart()
    {
        Dataset dataset = tryCreateChartDataset();
        if (null == dataset)
        {
            return createErrorChart();
        }

        return createDataChart(dataset);
    }

    /**
     * Create a chart using the supplied dataset.
     */
    protected abstract JFreeChart createDataChart(Dataset dataset);

    /**
     * Try to convert the DatasetFileLines into a {@link Dataset}. Return null on failure.
     */
    protected abstract Dataset tryCreateChartDataset();

    /**
     * An interface used to abstract iterating and processing tabular data.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    protected interface ILineProcessor
    {
        public void processLine(String xString, String yString, int index);
    }

    /**
     * Try to iterate over the data file lines, passing the columns specified in the configuration
     * into the line processor. Return false if I could not iterate (e.g., because the file was not
     * of the expected format); return true on success.
     */
    protected boolean tryIterateOverFileLinesUsing(ILineProcessor proc)
    {
        int xColumn = tryXColumnNumber();
        int yColumn = tryYColumnNumber();
        // We could not find the necessary columns in the dataset
        if (xColumn < 0 || yColumn < 0)
        {
            return false;
        }

        List<String[]> lines = fileLines.getDataLines();
        int i = 0;
        for (String[] line : lines)
        {
            proc.processLine(line[xColumn], line[yColumn], i++);
        }

        return true;
    }

}
