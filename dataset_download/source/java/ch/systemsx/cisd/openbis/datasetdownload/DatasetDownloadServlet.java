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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.lims.base.ExternalData;
import ch.systemsx.cisd.lims.base.IDataSetService;
import ch.systemsx.cisd.lims.base.LocatorType;

/**
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadServlet extends HttpServlet
{
    static final String DATA_SET_ROOT_DIR_KEY = "data-set-root-dir";

    static final String DATA_SET_KEY = "data-set";

    static final String DATASET_CODE_KEY = "dataSetCode";
    
    static final String SESSION_ID_KEY = "sessionID";
    
    private static final long serialVersionUID = 1L;

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatasetDownloadServlet.class);

    protected static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DatasetDownloadServlet.class);

    private ApplicationContext applicationContext;
    
    public DatasetDownloadServlet()
    {
    }

    DatasetDownloadServlet(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }
    
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
        try
        {
            obtainDataSetFromServer(request);
            
            HttpSession session = request.getSession(false);
            if (session == null)
            {
                printSessionExpired(response);
            } else
            {
                ExternalData dataSet = (ExternalData) session.getAttribute(DATA_SET_KEY);
                File rootDir = (File) session.getAttribute(DATA_SET_ROOT_DIR_KEY);
                String pathInfo = request.getPathInfo();
                if (pathInfo != null && pathInfo.startsWith("/"))
                {
                    pathInfo = pathInfo.substring(1);
                }
                String requestURI = request.getRequestURI();
                RenderingContext context = new RenderingContext(rootDir, requestURI, pathInfo);
                renderPage(response, dataSet, context);
            }
            
        } catch (Exception e)
        {
            if (e instanceof UserFailureException == false)
            {
                operationLog.error("Request " + request.getRequestURL() + "?"
                        + request.getQueryString() + " caused an exception: ", e);
            }
            PrintWriter writer = response.getWriter();
            writer.println("<html><body><h1>Error</h1>");
            String message = e.getMessage();
            writer.println(StringUtils.isBlank(message) ? e.toString() : message);
            writer.println("</body></html>");
            writer.flush();
            writer.close();
        }
    }
    
    private void renderPage(HttpServletResponse response, ExternalData dataSet,
            RenderingContext renderingContext) throws IOException
    {
        File file = renderingContext.getFile();
        if (file.exists() == false)
        {
            throw new EnvironmentFailureException("File '" + file.getName() + "' does not exist.");
        }
        if (file.isDirectory())
        {
            createPage(response, dataSet, renderingContext, file);
        } else
        {
            deliverFile(response, dataSet, file);
        }
    }

    private void createPage(HttpServletResponse response, ExternalData dataSet,
            RenderingContext renderingContext, File file) throws IOException
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set ' " + dataSet.getCode() + "' show directory "
                    + file.getAbsolutePath());
        }
        IDirectoryRenderer directoryRenderer = new HTMLDirectoryRenderer(renderingContext);
        response.setContentType(directoryRenderer.getContentType());
        PrintWriter writer = null;
        try
        {
            writer = response.getWriter();
            directoryRenderer.setWriter(writer);
            directoryRenderer.printHeader(dataSet);
            String relativeParentPath = renderingContext.getRelativeParentPath();
            if (relativeParentPath != null)
            {
                directoryRenderer.printLinkToParentDirectory(relativeParentPath);
            }
            File[] children = file.listFiles();
            for (File child : children)
            {
                String name = child.getName();
                File rootDir = renderingContext.getRootDir();
                String relativePath = FileUtilities.getRelativeFile(rootDir, child);
                String normalizedRelativePath = relativePath.replace('\\', '/');
                if (child.isDirectory())
                {
                    directoryRenderer.printDirectory(name, normalizedRelativePath);
                } else
                {
                    directoryRenderer.printFile(name, normalizedRelativePath, child.length());
                }
            }
            directoryRenderer.printFooter();
            writer.flush();
            
        } finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    private void deliverFile(final HttpServletResponse response, ExternalData dataSet, File file)
            throws IOException, FileNotFoundException
    {
        long size = file.length();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set ' " + dataSet.getCode() + "' deliver file "
                    + file.getAbsolutePath() + " (" + size + " bytes).");
        }
        response.setContentLength((int) size);
        response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
        ServletOutputStream outputStream = null;
        FileInputStream fileInputStream = null;
        try
        {
            outputStream = response.getOutputStream();
            fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, outputStream);
        } finally
        {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void printSessionExpired(final HttpServletResponse response) throws IOException
    {
        PrintWriter writer = response.getWriter();
        writer.write("<html><body>Download session expired.</body></html>");
        writer.flush();
        writer.close();
    }

    private void obtainDataSetFromServer(final HttpServletRequest request)
    {
        final String dataSetCode = request.getParameter(DATASET_CODE_KEY);
        final String sessionID = request.getParameter(SESSION_ID_KEY);
        if (dataSetCode != null && sessionID != null)
        {
            IDataSetService dataSetService = applicationContext.getDataSetService();
            ExternalData dataSet = dataSetService.getDataSet(sessionID, dataSetCode);
            File dataSetRootDirectory = new File(createDataSetPath(dataSet));
            if (dataSetRootDirectory.exists() == false)
            {
                throw new UserFailureException("Data set '" + dataSetCode
                        + "' not found in store at '" + dataSetRootDirectory.getAbsolutePath()
                        + "'.");
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Data set '" + dataSetCode + "' obtained from openBIS server.");
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(DATA_SET_KEY, dataSet);
            session.setAttribute(DATA_SET_ROOT_DIR_KEY, dataSetRootDirectory);
        }
    }

    private String createDataSetPath(ExternalData dataSet)
    {
        String location = dataSet.getLocation();
        LocatorType locatorType = dataSet.getLocatorType();
        if (locatorType.getCode().equals(LocatorType.DEFAULT_LOCATOR_TYPE_CODE))
        {
            return applicationContext.getConfigParameters().getStorePath() + "/" + location;
        }
        return location;
    }
}
