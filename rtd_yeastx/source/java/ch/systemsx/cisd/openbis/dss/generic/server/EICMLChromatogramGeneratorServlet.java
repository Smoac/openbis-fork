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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet;
import ch.systemsx.cisd.yeastx.eicml.ChromatogramDTO;
import ch.systemsx.cisd.yeastx.eicml.EICMLChromatogramImageGenerator;

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

    /**
     * A utility class for dealing with the parameters required to generate an image from a
     * chromatogram.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class RequestParams
    {
        private final String sessionId;

        private final String datasetCode;

        private final int width;

        private final int height;

        public RequestParams(HttpServletRequest request)
        {
            sessionId = getParam(request, SESSION_ID_PARAM);
            datasetCode = getParam(request, DATASET_CODE_PARAM);
            width = getIntParam(request, IMAGE_WIDTH_PARAM);
            height = getIntParam(request, IMAGE_HEIGHT_PARAM);
        }

        private static int getIntParam(HttpServletRequest request, String paramName)
        {
            String value = getParam(request, paramName);
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

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }
    }

    // private static IEICMSRunDAO createQuery(Properties properties)
    // {
    // final DatabaseConfigurationContext dbContext = DBUtils.createAndInitDBContext(properties);
    // DataSource dataSource = dbContext.getDataSource();
    // return QueryTool.getQuery(dataSource, IEICMSRunDAO.class);
    // }

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

            // TODO Get the chromatogram data
            ChromatogramDTO chromatogram = getChromatogramForId(null);

            // Generate a chromatogram image into the stream
            EICMLChromatogramImageGenerator generator =
                    new EICMLChromatogramImageGenerator(chromatogram, response.getOutputStream(),
                            width, height);
            generator.generateImage();

        } catch (Exception e)
        {
            printErrorResponse(response, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    ChromatogramDTO getChromatogramForId(String id)
    {
        return null;
    }
}
