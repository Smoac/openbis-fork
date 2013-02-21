/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphCollectionConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphCollectionConfiguration.DynamicTabularDataGraphCollectionConfiguration;

/**
 * @author cramakri
 */
public class DynamicFileTabularDataGraphServlet extends FileTabularDataGraphServlet
{

    private static final String DYNAMIC_GRAPH_NAME = "dynamic";

    private static final long serialVersionUID = 1L;

    @Override
    protected TabularDataGraphCollectionConfiguration getConfiguration(HttpServletRequest request)
    {
        // TODO Implement this properly
        DynamicTabularDataGraphCollectionConfiguration config =
                new DynamicTabularDataGraphCollectionConfiguration();
        config.setColumnDelimiter('\t');
        Properties props = new Properties();
        props.setProperty(TabularDataGraphCollectionConfiguration.GRAPHS_KEY,
                DynamicTabularDataGraphCollectionConfiguration.DYNAMIC_GRAPH_NAME);
        props.setProperty(DYNAMIC_GRAPH_NAME + "."
                + TabularDataGraphCollectionConfiguration.TITLE_KEY, "Title");
        props.setProperty(DYNAMIC_GRAPH_NAME + "."
                + TabularDataGraphCollectionConfiguration.GRAPHS_TYPES_KEY, "SCATTERPLOT");
        props.setProperty(DYNAMIC_GRAPH_NAME + "."
                + TabularDataGraphCollectionConfiguration.X_AXIS_KEY, "col1");
        props.setProperty(DYNAMIC_GRAPH_NAME + "."
                + TabularDataGraphCollectionConfiguration.Y_AXIS_KEY, "col2");
        config.setProperties(props);

        return TabularDataGraphCollectionConfiguration.getConfiguration(config);
    }

    @Override
    protected synchronized void doSpecificInitialization(Enumeration<String> parameterNames,
            ServletConfig servletConfig)
    {
        // Do not initialize the configuration variable -- we never use it
        configuration = null;
    }

}
