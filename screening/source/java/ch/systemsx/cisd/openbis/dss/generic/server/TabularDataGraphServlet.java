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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.ITabularDataGraph;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphCollectionConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphServlet extends AbstractDatasetDownloadServlet
{
    // Required servlet parameters
    public final static String DATASET_CODE_PARAM = "dataset";

    public final static String FILE_PATH_PARAM = "file";

    public final static String GRAPH_TYPE_CODE = "type";

    private static final long serialVersionUID = 1L;

    private TabularDataGraphCollectionConfiguration configuration;

    private final static String PROPERTIES_FILE_KEY = "properties-file";

    /**
     * A utility class for dealing with the URL parameters required to generate an image. This class
     * makes sure all the required parameters are in the request (it throws exceptions otherwise),
     * and it defaults values for all optional parameters if they are not in the request.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class RequestParams
    {
        private final String sessionId;

        private final String datasetCode;

        private final String filePath;

        private final String graphTypeCode;

        public RequestParams(HttpServletRequest request)
        {
            sessionId = getRequiredParameter(request, SESSION_ID_PARAM);
            datasetCode = getRequiredParameter(request, DATASET_CODE_PARAM);
            filePath = getRequiredParameter(request, FILE_PATH_PARAM);
            graphTypeCode = getRequiredParameter(request, GRAPH_TYPE_CODE);
        }

        private static String getRequiredParameter(final HttpServletRequest request,
                String paramName)
        {
            String value = request.getParameter(paramName);
            if (value == null)
            {
                throw new UserFailureException("no value for the parameter " + paramName
                        + " found in the URL");
            }
            return value;
        }
    }

    @Override
    protected synchronized void doSpecificInitialization(Enumeration<String> parameterNames,
            ServletConfig servletConfig)
    {
        // Only initialize the db properties once
        if (configuration != null)
            return;

        String propertiesFilePath = servletConfig.getInitParameter(PROPERTIES_FILE_KEY);
        configuration =
                TabularDataGraphCollectionConfiguration.getConfiguration(propertiesFilePath);
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Get the parameters from the request
            RequestParams params = new RequestParams(request);
            String sessionId = params.sessionId;
            String datasetCode = params.datasetCode;
            String filePath = params.filePath;

            // Get the session and user from the request
            HttpSession session = tryGetOrCreateSession(request, sessionId);
            if (session == null)
            {
                printSessionExpired(response);
                return;
            }
            // Check that the user has view access to the data
            // NOTE: This throws an exception -- it may be nicer to return an image for a
            // non-accessible dataset...
            ensureDatasetAccessible(datasetCode, session, sessionId);

            // Get the tabular data
            DatasetFileLines fileLines = getDatasetFileLines(filePath);

            // Generate an image image into the stream
            ITabularDataGraph generator =
                    configuration.getGraph(params.graphTypeCode, fileLines, response
                            .getOutputStream());
            generator.generateImage();

        } catch (Exception e)
        {
            printErrorResponse(response, "Invalid Request");
            e.printStackTrace();
        }
    }

    /**
     * Return the tabular data as a DatasetFileLines.
     */
    private DatasetFileLines getDatasetFileLines(String path) throws IOException
    {
        File file = new File(path);
        CsvReader reader = getCsvReader(file);
        List<String[]> lines = new ArrayList<String[]>();
        while (reader.readRecord())
        {
            lines.add(reader.getValues());
        }

        return new DatasetFileLines(file, path, lines);
    }

    /**
     * Get a CsvReader for parsing a tabular data file.
     */
    private CsvReader getCsvReader(File file) throws IOException
    {
        if (file.isFile() == false)
        {
            throw new UserFailureException(file + " does not exist or is not a file.");
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        CsvReader csvReader = new CsvReader(fileInputStream, Charset.defaultCharset());
        csvReader.setDelimiter(configuration.getColumnDelimiter());
        csvReader.setSkipEmptyRecords(true);
        csvReader.setUseComments(configuration.isIgnoreComments());
        csvReader.setComment(configuration.getCommentDelimiter());

        return csvReader;
    }
}
