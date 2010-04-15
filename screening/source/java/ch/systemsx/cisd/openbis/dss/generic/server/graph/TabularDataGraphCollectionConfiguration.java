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
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphCollectionConfiguration
{
    private static final String SEPARATOR_PROPERTY_KEY = "separator";

    private static final String IGNORE_COMMENTS_PROPERTY_KEY = "ignore-comments";

    // the seperator in the file
    private final char separator;

    private final boolean ignoreComments;

    // the comment marker in the file
    private final char comment;

    private final ArrayList<String> graphTypeCodes;

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

        this.separator = PropertyUtils.getChar(properties, SEPARATOR_PROPERTY_KEY, ';');
        this.ignoreComments =
                PropertyUtils.getBoolean(properties, IGNORE_COMMENTS_PROPERTY_KEY, true);
        graphTypeCodes = new ArrayList<String>();
        graphTypeMap = new HashMap<String, TabularDataGraphConfiguration>();
        initialzeGraphTypeMap(properties);

    }

    private void initialzeGraphTypeMap(Properties properties)
    {
        TabularDataGraphConfiguration config =
                new TabularDataGraphConfiguration("Test", "TotalCells", "InfectedCells", 300, 200);
        graphTypeMap.put("scatter", config);
        graphTypeCodes.add("scatter");
    }

    /**
     * Return the graph configuration associated with the graphTypeCode.
     * 
     * @param graphTypeCode The name of the graph type
     */
    public TabularDataGraphConfiguration getGraphConfiguration(String graphTypeCode)
    {
        TabularDataGraphConfiguration config = graphTypeMap.get(graphTypeCode);
        if (null == config)
        {
            throw new IllegalArgumentException("No graph associated with code " + graphTypeCode);
        }
        return config;
    }

    /**
     * Return the graph generator associated with the graphTypeCode, initialized by the fileLines
     * and out.
     * 
     * @param graphTypeCode The name of the graph type
     * @param fileLines The data to generate a graph from
     * @param out The stream to write the graph to
     */
    public ITabularDataGraph getGraph(String graphTypeCode, DatasetFileLines fileLines,
            OutputStream out)
    {
        TabularDataGraphConfiguration config = graphTypeMap.get(graphTypeCode);
        if (null == config)
        {
            throw new IllegalArgumentException("No graph associated with code " + graphTypeCode);
        }
        return new TabularDataScatterplot(config, fileLines, out);
    }

    public char getColumnDelimiter()
    {
        return separator;
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
        return 800;
    }

    public int getImageHeight()
    {
        return 600;
    }

    public int getThumbnailWidth()
    {
        return 300;
    }

    public int getThumbnailHeight()
    {
        return 200;
    }

    public List<String> getGraphTypeCodes()
    {
        return graphTypeCodes;
    }
}
