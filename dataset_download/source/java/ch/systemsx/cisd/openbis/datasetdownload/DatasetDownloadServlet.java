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
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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
    static final String DATA_SET_KEY = "data-set";

    static final String DATASET_CODE_KEY = "dataSetCode";
    
    static final String SESSION_ID_KEY = "sessionID";
    
    private static final long serialVersionUID = 1L;

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatasetDownloadServlet.class);

    protected static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DatasetDownloadServlet.class);
    
    private static final Comparator<File> FILE_COMPARATOR = new Comparator<File>()
        {
            public int compare(File file1, File file2)
            {
                return createSortableName(file1).compareTo(createSortableName(file2));
            }

            private String createSortableName(File file)
            {
                return (file.isDirectory() ? "D" : "F") + file.getName().toUpperCase();
            }
        };

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
            String requestURI = URLDecoder.decode(request.getRequestURI(), "UTF-8");
            String prefix = "/" + applicationContext.getApplicationName() + "/";
            if (requestURI.startsWith(prefix) == false)
            {
                throw new EnvironmentFailureException("Request URI '" + requestURI
                        + "' expected to start with '" + prefix + "'.");
            }
            String pathInfo = requestURI.substring(prefix.length());
            int indexOfFirstSeparator = pathInfo.indexOf('/');
            String dataSetCode;
            if (indexOfFirstSeparator < 0)
            {
                dataSetCode = pathInfo;
                pathInfo = "";
            } else
            {
                dataSetCode = pathInfo.substring(0, indexOfFirstSeparator);
                pathInfo = pathInfo.substring(indexOfFirstSeparator + 1);
            }
            
            obtainDataSetFromServer(dataSetCode, request);
            
            HttpSession session = request.getSession(false);
            if (session == null)
            {
                printSessionExpired(response);
            } else
            {
                
                ExternalData dataSet = tryToGetDataSet(session, dataSetCode);
                if (dataSet == null)
                {
                    throw new UserFailureException("Unknown data set '" + dataSetCode + "'.");
                }
                File rootDir = createDataSetRootDirectory(dataSet);
                RenderingContext context = new RenderingContext(rootDir, requestURI, pathInfo);
                renderPage(response, dataSet, context);
            }
            
        } catch (Exception e)
        {
            printError(request, response, e);
        }
    }

    private void printError(final HttpServletRequest request, final HttpServletResponse response,
            Exception exception) throws IOException
    {
        if (exception instanceof UserFailureException == false)
        {
            StringBuffer url = request.getRequestURL();
            String queryString = request.getQueryString();
            if (StringUtils.isNotBlank(queryString))
            {
                url.append("?").append(queryString);
            }
            operationLog.error("Request " + url + " caused an exception: ", exception);
        } else if (operationLog.isInfoEnabled())
        {
            operationLog.info("User failure: " + exception.getMessage());
        }
        String message = exception.getMessage();
        String errorText = StringUtils.isBlank(message) ? exception.toString() : message;
        PrintWriter writer = response.getWriter();
        writer.println("<html><body><h1>Error</h1>");
        writer.println(errorText);
        writer.println("</body></html>");
        writer.flush();
        writer.close();
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
            operationLog.info("For data set '" + dataSet.getCode() + "' show directory "
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
            Arrays.sort(children, FILE_COMPARATOR);
            for (File child : children)
            {
                String name = child.getName();
                File rootDir = renderingContext.getRootDir();
                String relativePath = FileUtilities.getRelativeFile(rootDir, child);
                String normalizedRelativePath =
                         relativePath.replace('\\', '/');
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
            operationLog.info("For data set '" + dataSet.getCode() + "' deliver file "
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

    private void obtainDataSetFromServer(String dataSetCode, final HttpServletRequest request)
    {
        final String sessionID = request.getParameter(SESSION_ID_KEY);
        if (sessionID != null)
        {
            IDataSetService dataSetService = applicationContext.getDataSetService();
            ExternalData dataSet = dataSetService.getDataSet(sessionID, dataSetCode);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Data set '" + dataSetCode + "' obtained from openBIS server.");
            }
            HttpSession session = request.getSession(true);
            ConfigParameters configParameters = applicationContext.getConfigParameters();
            session.setMaxInactiveInterval(configParameters.getSessionTimeout());
            putDataSetToMap(session, dataSetCode, dataSet);
        }
    }

    private File createDataSetRootDirectory(ExternalData dataSet)
    {
        String path = dataSet.getLocation();
        LocatorType locatorType = dataSet.getLocatorType();
        if (locatorType.getCode().equals(LocatorType.DEFAULT_LOCATOR_TYPE_CODE))
        {
            path = applicationContext.getConfigParameters().getStorePath() + "/" + path;
        }
        File dataSetRootDirectory = new File(path);
        if (dataSetRootDirectory.exists() == false)
        {
            throw new UserFailureException("Data set '" + dataSet.getCode()
                    + "' not found in store at '" + dataSetRootDirectory.getAbsolutePath()
                    + "'.");
        }
        return dataSetRootDirectory;
    }
    
    private void putDataSetToMap(HttpSession session, String dataSetCode, ExternalData dataSet)
    {
        getDataSets(session).put(dataSetCode, dataSet);
    }
    
    private ExternalData tryToGetDataSet(HttpSession session, String dataSetCode)
    {
        return getDataSets(session).get(dataSetCode);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ExternalData> getDataSets(HttpSession session)
    {
        Map<String, ExternalData> map =
                (Map<String, ExternalData>) session.getAttribute(DATA_SET_KEY);
        if (map == null)
        {
            map = new HashMap<String, ExternalData>();
            session.setAttribute(DATA_SET_KEY, map);
        }
        return map;
    }

}
