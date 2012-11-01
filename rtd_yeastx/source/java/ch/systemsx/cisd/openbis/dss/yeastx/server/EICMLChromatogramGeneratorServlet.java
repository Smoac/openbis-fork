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

package ch.systemsx.cisd.openbis.dss.yeastx.server;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet;
import ch.systemsx.cisd.openbis.dss.generic.server.Utils;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSourceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.eicml.ChromatogramDTO;
import ch.systemsx.cisd.yeastx.eicml.EICMLChromatogramImageGenerator;
import ch.systemsx.cisd.yeastx.eicml.IEICMSRunDAO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class EICMLChromatogramGeneratorServlet extends AbstractDatasetDownloadServlet
{

    private static final long serialVersionUID = 1L;

    // Required servlet parameters

    public final static String DATASET_CODE_PARAM = "dataset";

    public final static String CHROMATOGRAM_CODE_PARAM = "chromatogram";

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

        private final long chromatogramId;

        private final int width;

        private final int height;

        public RequestParams(HttpServletRequest request)
        {
            sessionId = getParam(request, Utils.SESSION_ID_PARAM);
            datasetCode = getParam(request, DATASET_CODE_PARAM);
            chromatogramId = getLongParam(request, CHROMATOGRAM_CODE_PARAM);
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

        private static long getLongParam(HttpServletRequest request, String paramName)
        {
            String value = getParam(request, paramName);
            try
            {
                return Long.valueOf(value);
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

        long getChromatogramId()
        {
            return chromatogramId;
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

    private DataSource dataSource;

    @Override
    protected synchronized void doSpecificInitialization(Enumeration<String> parameterNames,
            ServletConfig servletConfig)
    {
        // Only initialize the dataSource once
        if (dataSource != null)
            return;
        String dataSourceName = servletConfig.getInitParameter(DataSourceProvider.DATA_SOURCE_KEY);
        if (dataSourceName == null)
        {
            throw new ConfigurationFailureException("Data source not defined.");
        }
        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(dataSourceName);
    }

    // remember to close the query after using it!
    private IEICMSRunDAO createQuery()
    {
        return DBUtils.getQuery(dataSource, IEICMSRunDAO.class);
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
            ChromatogramDTO chromatogram = getChromatogramForParameters(params);

            // Generate a chromatogram image into the stream
            EICMLChromatogramImageGenerator generator =
                    new EICMLChromatogramImageGenerator(chromatogram, response.getOutputStream(),
                            width, height);
            generator.generateImage();

        } catch (Exception e)
        {
            printErrorResponse(response, "Invalid Request");
            e.printStackTrace();
        }
    }

    private ChromatogramDTO getChromatogramForParameters(RequestParams params)
    {
        IEICMSRunDAO query = createQuery();
        try
        {
            long chromatogramId = params.getChromatogramId();
            ChromatogramDTO chromatogram = query.getChromatogramById(chromatogramId);
            return chromatogram;
        } finally
        {
            query.close();
        }
    }
}
