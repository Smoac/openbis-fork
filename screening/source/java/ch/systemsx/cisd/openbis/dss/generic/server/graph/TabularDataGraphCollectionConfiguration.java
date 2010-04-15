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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphConfiguration.GraphType;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil.SectionProperties;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphCollectionConfiguration
{
    private static final String SEPARATOR_PROPERTY_KEY = "separator";

    private static final String IGNORE_COMMENTS_PROPERTY_KEY = "ignore-comments";

    // the full width of the graphs when requested
    private static final String IMAGE_WIDTH_KEY = "full-width";

    // the full height of the graphs when requested
    private static final String IMAGE_HEIGHT_KEY = "full-height";

    // the width of the thumbnails shown in the report
    private static final String THUMBNAIL_WIDTH_KEY = "column-width";

    // the height of the thumbnails shown in the report
    private static final String THUMBNAIL_HEIGHT_KEY = "column-height";

    // the graphs to display -- each one is shown in a column.
    private static final String GRAPHS_KEY = "graphs";

    // the type of graph. See @{link GraphType} for valid types.
    private static final String GRAPHS_TYPES_KEY = "graph-type";

    // keys for the different kinds of graphs
    private static final String TITLE_KEY = "title";

    private static final String X_AXIS_KEY = "x-axis";

    private static final String Y_AXIS_KEY = "y-axis";

    private static final String COLUMN_KEY = "column";

    private static final String NUMBER_OF_BINS_KEY = "number-of-bins";

    private final char columnDelimiter;

    private final boolean ignoreComments;

    // the comment marker in the file
    private final char comment;

    private final int imageWidth;

    private final int imageHeight;

    private final int thumbnailWidth;

    private final int thumbnailHeight;

    private final ArrayList<String> graphNames;

    private final HashMap<String, TabularDataGraphConfiguration> graphTypeMap;

    /**
     * Create a configuration from the properties file located at path.
     * 
     * @param path Path to the properties file.
     */
    public static TabularDataGraphCollectionConfiguration getConfiguration(String path)
            throws EnvironmentFailureException
    {
        Properties configurationProps = new Properties();
        try
        {
            configurationProps.load(new FileInputStream(path));
        } catch (FileNotFoundException ex)
        {
            throw new EnvironmentFailureException("Could not find the configuration file "
                    + new File(path).getAbsolutePath());
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("Could not read the configuration file " + path);
        }

        return new TabularDataGraphCollectionConfiguration(configurationProps);
    }

    /**
     * Initialize the configuration based on the properties object.
     */
    private TabularDataGraphCollectionConfiguration(Properties properties)
    {
        comment = '#';

        this.columnDelimiter = PropertyUtils.getChar(properties, SEPARATOR_PROPERTY_KEY, ';');
        this.ignoreComments =
                PropertyUtils.getBoolean(properties, IGNORE_COMMENTS_PROPERTY_KEY, true);

        imageWidth = PropertyUtils.getInt(properties, IMAGE_WIDTH_KEY, 800);

        imageHeight = PropertyUtils.getInt(properties, IMAGE_HEIGHT_KEY, 600);

        thumbnailWidth = PropertyUtils.getInt(properties, THUMBNAIL_WIDTH_KEY, 300);

        thumbnailHeight = PropertyUtils.getInt(properties, THUMBNAIL_HEIGHT_KEY, 200);

        graphNames = new ArrayList<String>();
        initializeGraphTypeCodes(properties);
        graphTypeMap = new HashMap<String, TabularDataGraphConfiguration>();
        initialzeGraphTypeMap(properties);

    }

    private void initializeGraphTypeCodes(Properties properties)
    {
        String graphTypeCodesString = properties.getProperty(GRAPHS_KEY, "");
        String[] typeCodeArray = graphTypeCodesString.split(",");
        for (String typeCode : typeCodeArray)
        {
            graphNames.add(typeCode.trim());
        }
    }

    private void initialzeGraphTypeMap(Properties properties)
    {
        SectionProperties[] pluginServicesProperties =
                PropertyParametersUtil.extractSectionProperties(properties, GRAPHS_KEY, false);

        for (SectionProperties sectionProp : pluginServicesProperties)
        {
            TabularDataGraphConfiguration config = getConfiguration(sectionProp);
            graphTypeMap.put(sectionProp.getKey(), config);
        }
    }

    private TabularDataGraphConfiguration getConfiguration(SectionProperties sectionProp)
    {
        Properties props = sectionProp.getProperties();
        String graphTypeValue = PropertyUtils.getMandatoryProperty(props, GRAPHS_TYPES_KEY);
        GraphType type = GraphType.valueOf(graphTypeValue.toUpperCase());
        String title = props.getProperty(TITLE_KEY, sectionProp.getKey());
        switch (type)
        {
            case HEATMAP:
                String xAxis = PropertyUtils.getMandatoryProperty(props, X_AXIS_KEY);
                String yAxis = PropertyUtils.getMandatoryProperty(props, Y_AXIS_KEY);
                String zAxis = PropertyUtils.getMandatoryProperty(props, COLUMN_KEY);
                if (xAxis.equals(yAxis))
                {
                    return new TabularDataHeatmapConfiguration(title, xAxis, zAxis,
                            getThumbnailWidth(), getThumbnailHeight());
                } else
                {
                    return new TabularDataHeatmapConfiguration(title, xAxis, yAxis, zAxis,
                            getThumbnailWidth(), getThumbnailHeight());
                }
            case HISTOGRAM:
                return new TabularDataHistogramConfiguration(title, PropertyUtils
                        .getMandatoryProperty(props, COLUMN_KEY), getThumbnailWidth(),
                        getThumbnailHeight(), PropertyUtils.getInt(props, NUMBER_OF_BINS_KEY, 10));
            case SCATTERPLOT:
                return new TabularDataScatterplotConfiguration(title, PropertyUtils
                        .getMandatoryProperty(props, X_AXIS_KEY), PropertyUtils
                        .getMandatoryProperty(props, Y_AXIS_KEY), getThumbnailWidth(),
                        getThumbnailHeight());
        }

        // should never get here
        return null;
    }

    /**
     * Return the graph configuration associated with the graphTypeCode.
     * 
     * @param graphName The name of the graph type
     */
    public TabularDataGraphConfiguration getGraphConfiguration(String graphName)
    {
        TabularDataGraphConfiguration config = graphTypeMap.get(graphName);
        if (null == config)
        {
            throw new IllegalArgumentException("No graph associated with code " + graphName);
        }
        return config;
    }

    /**
     * Return the graph generator associated with the graphTypeCode, initialized by the fileLines
     * and out.
     * 
     * @param graphName The name of the graph type
     * @param fileLines The data to generate a graph from
     * @param out The stream to write the graph to
     */
    public ITabularDataGraph getGraph(String graphName, DatasetFileLines fileLines, OutputStream out)
    {
        TabularDataGraphConfiguration config = graphTypeMap.get(graphName);
        if (null == config)
        {
            throw new IllegalArgumentException("No graph associated with code " + graphName);
        }
        GraphType type = config.getGraphType();
        switch (type)
        {
            case HEATMAP:
                return new TabularDataHeatmap((TabularDataHeatmapConfiguration) config, fileLines,
                        out);
            case HISTOGRAM:
                return new TabularDataHistogram((TabularDataHistogramConfiguration) config,
                        fileLines, out);
            case SCATTERPLOT:
                return new TabularDataScatterplot((TabularDataScatterplotConfiguration) config,
                        fileLines, out);

        }

        // should never get here
        return null;
    }

    public char getColumnDelimiter()
    {
        return columnDelimiter;
    }

    /**
     * Should comments be ignored?
     */
    public boolean isIgnoreComments()
    {
        return ignoreComments;
    }

    public char getCommentDelimiter()
    {
        return comment;
    }

    public int getImageWidth()
    {
        return imageWidth;
    }

    public int getImageHeight()
    {
        return imageHeight;
    }

    public int getThumbnailWidth()
    {
        return thumbnailWidth;
    }

    public int getThumbnailHeight()
    {
        return thumbnailHeight;
    }

    public List<String> getGraphNames()
    {
        return graphNames;
    }
}
