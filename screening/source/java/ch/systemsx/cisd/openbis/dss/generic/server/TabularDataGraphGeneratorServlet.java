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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphGenerator;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphGeneratorServlet extends AbstractDatasetDownloadServlet
{

    private static final long serialVersionUID = 1L;

    // Required servlet parameters

    public final static String DATASET_CODE_PARAM = "dataset";

    public final static String GRAPH_TYPE_CODE = "type";

    // Optional, but recommended servlet parameters

    public final static String IMAGE_WIDTH_PARAM = "w";

    public final static String IMAGE_HEIGHT_PARAM = "h";

    // Default values for optional parameters
    public final static int DEFAULT_WIDTH = 300;

    public final static int DEFAULT_HEIGHT = 200;

    /**
     * A utility class for dealing with the parameters required to generate an image from a
     * chromatogram. This class makes sure all the required parameters are in the request (it throws
     * exceptions otherwise), and it defaults values for all optional parameters if they are not in
     * the request.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class RequestParams
    {
        private final String sessionId;

        private final String datasetCode;

        private final String graphTypeCode;

        private final int width;

        private final int height;

        public RequestParams(HttpServletRequest request)
        {
            sessionId = getParam(request, SESSION_ID_PARAM);
            datasetCode = getParam(request, DATASET_CODE_PARAM);
            graphTypeCode = getParam(request, GRAPH_TYPE_CODE);
            width = getIntParam(request, IMAGE_WIDTH_PARAM, DEFAULT_WIDTH);
            height = getIntParam(request, IMAGE_HEIGHT_PARAM, DEFAULT_HEIGHT);
        }

        private static int getIntParam(HttpServletRequest request, String paramName,
                int defaultValue)
        {
            String value = request.getParameter(paramName);
            if (value == null)
                return defaultValue;

            try
            {
                return Integer.valueOf(value);
            } catch (NumberFormatException e)
            {
                throw new UserFailureException("parameter " + paramName
                        + " should be an integer, but is: " + value);
            }
        }

        private static String getParam(final HttpServletRequest request, String paramName)
        {
            String value = request.getParameter(paramName);
            if (value == null)
            {
                throw new UserFailureException("no value for the parameter " + paramName
                        + " found in the URL");
            }
            return value;
        }

        public String getSessionId()
        {
            return sessionId;
        }

        public String getDatasetCode()
        {
            return datasetCode;
        }

        public String getGraphTypeCode()
        {
            return graphTypeCode;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }
    }

    // The properties needed for connecting to the database
    private Properties dbProperties;

    @Override
    protected synchronized void doSpecificInitialization(Enumeration<String> parameterNames,
            ServletConfig servletConfig)
    {
        // Only initialize the db properties once
        if (dbProperties != null)
            return;

        dbProperties = new Properties();
        String name;
        while (parameterNames.hasMoreElements())
        {
            name = parameterNames.nextElement();
            dbProperties.setProperty(name, servletConfig.getInitParameter(name));
        }
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Get the parameters from the request
            RequestParams params = new RequestParams(request);
            String sessionId = params.getSessionId();
            String datasetCode = params.getDatasetCode();
            int height = params.getHeight();
            int width = params.getWidth();

            // Get the session and user from the request
            HttpSession session = tryGetOrCreateSession(request, sessionId);
            if (session == null)
            {
                printSessionExpired(response);
                return;
            }
            // Check that the user has view access to the chromatogram data
            // NOTE: This throws an exception -- it may be nicer to return an image for a
            // non-accessible chromatogram...
            ensureDatasetAccessible(datasetCode, session, sessionId);

            // Get the chromatogram data
            DatasetFileLines fileLines = tryDatasetLinesForParameters(params);

            // Generate a chromatogram image into the stream
            TabularDataGraphGenerator generator =
                    new TabularDataGraphGenerator(fileLines, response.getOutputStream(), width,
                            height);
            generator.generateImage();

        } catch (Exception e)
        {
            printErrorResponse(response, "Invalid Request");
            e.printStackTrace();
        }
    }

    private DatasetFileLines tryDatasetLinesForParameters(RequestParams params)
    {
        return null;
    }
}
