/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datasetdownload;

import static ch.systemsx.cisd.openbis.datasetdownload.DatasetDownloadService.APPLICATION_CONTEXT_KEY;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.lims.base.ExternalData;

/**
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadServlet extends HttpServlet
{
    static final String DATASET_CODE_KEY = "dataSetCode";
    
    static final String SESSION_ID_KEY = "sessionID";
    
    private static final long serialVersionUID = 1L;

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatasetDownloadServlet.class);

    protected static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DatasetDownloadServlet.class);

    private ApplicationContext applicationContext;
    
    @Override
    public final void init(final ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
        try
        {
            ServletContext context = servletConfig.getServletContext();
            applicationContext = (ApplicationContext) context.getAttribute(APPLICATION_CONTEXT_KEY);
        } catch (Exception ex)
        {
            notificationLog.fatal("Failure during '" + servletConfig.getServletName()
                    + "' servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        final String dataSetCode = request.getParameter(DATASET_CODE_KEY);
        final String sessionID = request.getParameter(SESSION_ID_KEY);
        ExternalData dataSet = applicationContext.getDataSetService().getDataSet(sessionID, dataSetCode);
        final PrintWriter writer = response.getWriter();
        writer.write("<html><body>Download dataset " + dataSetCode + " (sessionID:" + sessionID + "):" + dataSet + "</body></html>");
        writer.flush();
        writer.close();
    }
}
