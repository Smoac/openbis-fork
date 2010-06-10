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

package ch.systemsx.cisd.openbis.dss.generic.server;

import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadServlet extends AbstractDatasetDownloadServlet
{
    private static final long serialVersionUID = 1L;

    private static final String HTML_MODE_DISPLAY = "html";

    private static final String TEXT_MODE_DISPLAY = "txt";

    private static final String SIMPLE_HTML_MODE_DISPLAY = "simpleHtml";

    static final String AUTO_RESOLVE_KEY = "autoResolve";

    static final String MAIN_DATA_SET_PATH_KEY = "mdsPath";

    static final String MAIN_DATA_SET_PATTERN_KEY = "mdsPattern";

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

    public DatasetDownloadServlet()
    {
    }

    DatasetDownloadServlet(ApplicationContext applicationContext)
    {
        super(applicationContext);
    }

    // helper class to store parsed URL request
    private static class RequestParams
    {
        private final String dataSetCode;

        private final String pathInfo;

        private final String sessionIdOrNull;

        private final String urlPrefixWithDataset;

        private final String displayMode;

        private final boolean autoResolve;

        private final String mainDataSetPathOrNull;

        private final String mainDataSetPatternOrNull;

        public RequestParams(String dataSetCode, String pathInfo, String sessionIdOrNull,
                String urlPrefixWithDataset, String displayMode, boolean autoResolve,
                String mainDataSetPathOrNull, String mainDataSetPatternOrNull)
        {
            this.dataSetCode = dataSetCode;
            this.pathInfo = pathInfo;
            this.sessionIdOrNull = sessionIdOrNull;
            this.urlPrefixWithDataset = urlPrefixWithDataset;
            this.displayMode = displayMode;
            this.autoResolve = autoResolve;
            this.mainDataSetPathOrNull = mainDataSetPathOrNull;
            this.mainDataSetPatternOrNull = mainDataSetPatternOrNull;
        }

        public boolean isAutoResolve()
        {
            return autoResolve;
        }

        public String getDataSetCode()
        {
            return dataSetCode;
        }

        public String getPathInfo()
        {
            return pathInfo;
        }

        public String tryGetSessionId()
        {
            return sessionIdOrNull;
        }

        public String getDisplayMode()
        {
            return displayMode;
        }

        public String getURLPrefix()
        {
            return urlPrefixWithDataset;
        }

        public String tryGetMainDataSetPath()
        {
            return mainDataSetPathOrNull;
        }

        public String tryGetMainDataSetPattern()
        {
            return mainDataSetPatternOrNull;
        }
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        IRendererFactory rendererFactory = null;
        try
        {
            RequestParams requestParams =
                    parseRequestURL(request, DATA_STORE_SERVER_WEB_APPLICATION_NAME);
            rendererFactory = createRendererFactory(requestParams.getDisplayMode());

            HttpSession session = tryGetOrCreateSession(request, requestParams.tryGetSessionId());
            if (session == null)
            {
                printSessionExpired(response);
            } else
            {
                printResponse(response, rendererFactory, requestParams, session);
            }
        } catch (Exception e)
        {
            if (rendererFactory == null)
            {
                rendererFactory = new PlainTextRendererFactory();
            }
            printError(rendererFactory, request, response, e);
        }
    }

    private void printResponse(final HttpServletResponse response,
            IRendererFactory rendererFactory, RequestParams requestParams, HttpSession session)
            throws UnsupportedEncodingException, IOException
    {
        String dataSetCode = requestParams.getDataSetCode();
        RenderingContext context = createRenderingContext(requestParams, dataSetCode, session);
        renderPage(rendererFactory, response, dataSetCode, context, requestParams, session);
    }

    private RenderingContext createRenderingContext(RequestParams requestParams,
            String dataSetCode, HttpSession session)
    {
        File rootDir = createDataSetRootDirectory(dataSetCode, session);
        RenderingContext context =
                new RenderingContext(rootDir, requestParams.getURLPrefix(), requestParams
                        .getPathInfo()

                );

        return context;
    }

    private IRendererFactory createRendererFactory(String displayMode)
    {
        if (displayMode.equals(SIMPLE_HTML_MODE_DISPLAY))
        {
            return new SimpleHTMLRendererFactory();
        } else if (displayMode.equals(HTML_MODE_DISPLAY))
        {
            return new HTMLRendererFactory();
        } else
        {
            return new PlainTextRendererFactory();
        }
    }

    private static RequestParams parseRequestURL(HttpServletRequest request, String applicationName)
            throws UnsupportedEncodingException
    {
        final String urlPrefix = "/" + applicationName + "/";
        final String requestURI = URLDecoder.decode(request.getRequestURI(), "UTF-8");
        if (requestURI.startsWith(urlPrefix) == false)
        {
            throw new EnvironmentFailureException("Request URI '" + requestURI
                    + "' expected to start with '" + urlPrefix + "'.");
        }
        final String fullPathInfo = requestURI.substring(urlPrefix.length());
        final int indexOfFirstSeparator = fullPathInfo.indexOf('/');
        final String dataSetCode;
        final String pathInfo;
        if (indexOfFirstSeparator < 0)
        {
            dataSetCode = fullPathInfo;
            pathInfo = "";
        } else
        {
            dataSetCode = fullPathInfo.substring(0, indexOfFirstSeparator);
            pathInfo = fullPathInfo.substring(indexOfFirstSeparator + 1);
        }
        final String urlPrefixWithDataset =
                requestURI.substring(0, requestURI.length() - pathInfo.length());

        final String sessionIDOrNull = request.getParameter(SESSION_ID_PARAM);
        String displayMode = getDisplayMode(request);

        Boolean autoResolveOrNull = Boolean.valueOf(request.getParameter(AUTO_RESOLVE_KEY));
        boolean autoResolve = autoResolveOrNull != null && autoResolveOrNull;
        String mainDataSetPathOrNull = request.getParameter(MAIN_DATA_SET_PATH_KEY);
        String mainDataSetPatternOrNull = request.getParameter(MAIN_DATA_SET_PATTERN_KEY);
        if (autoResolve == false || StringUtils.isBlank(mainDataSetPathOrNull))
        {
            mainDataSetPathOrNull = null;
        }
        if (autoResolve == false || StringUtils.isBlank(mainDataSetPatternOrNull))
        {
            mainDataSetPatternOrNull = null;
        }
        return new RequestParams(dataSetCode, pathInfo, sessionIDOrNull, urlPrefixWithDataset,
                displayMode, autoResolve, mainDataSetPathOrNull, mainDataSetPatternOrNull);
    }

    private static String getDisplayMode(HttpServletRequest request)
    {
        String displayMode = request.getParameter(DISPLAY_MODE_PARAM);
        if (displayMode == null)
        {
            displayMode = HTML_MODE_DISPLAY;
        }
        return displayMode;
    }

    private void printError(IRendererFactory rendererFactory, final HttpServletRequest request,
            final HttpServletResponse response, Exception exception) throws IOException
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
        IErrorRenderer errorRenderer = rendererFactory.createErrorRenderer();
        response.setContentType(rendererFactory.getContentType());
        PrintWriter writer = response.getWriter();
        errorRenderer.setWriter(writer);
        errorRenderer.printErrorMessage(errorText);
        writer.flush();
        writer.close();
    }

    private void renderPage(IRendererFactory rendererFactory, HttpServletResponse response,
            String dataSetCode, RenderingContext renderingContext, RequestParams requestParams,
            HttpSession session) throws IOException
    {

        File file = renderingContext.getFile();
        if (file.exists() == false)
        {
            throw new EnvironmentFailureException("File '" + file.getName() + "' does not exist.");
        }
        // If we want to browse a directory, we need a whole dataset metadata from openbis to
        // display them for the user. But if just a file is needed, then it's much faster to just
        // check the access rights in openbis.
        String sessionIdOrNull = requestParams.tryGetSessionId();
        if (file.isDirectory())
        {
            ExternalData dataSet = getDataSet(dataSetCode, sessionIdOrNull, session);
            if (requestParams.isAutoResolve())
            {
                autoResolve(rendererFactory, response, dataSetCode, renderingContext,
                        requestParams, session, file, dataSet);
            } else
            {
                createPage(rendererFactory, response, dataSet, renderingContext, file);
            }
        } else
        {
            ensureDatasetAccessible(dataSetCode, session, sessionIdOrNull);
            deliverFile(response, dataSetCode, file, requestParams.getDisplayMode());
        }
    }

    private void autoResolve(IRendererFactory rendererFactory, HttpServletResponse response,
            String dataSetCode, RenderingContext renderingContext, RequestParams requestParams,
            HttpSession session, File dir, ExternalData dataSet) throws IOException
    {
        assert dir.exists() && dir.isDirectory();
        List<File> mainDataSets =
                AutoResolveUtils.findSomeMatchingFiles(renderingContext.getRootDir(), requestParams
                        .tryGetMainDataSetPath(), requestParams.tryGetMainDataSetPattern());
        if (mainDataSets.size() == 1)
        {
            String newRelativePath =
                    FileUtilities.getRelativeFile(renderingContext.getRootDir(), new File(
                            mainDataSets.get(0).getPath()));
            RenderingContext newRenderingContext =
                    new RenderingContext(renderingContext.getRootDir(), renderingContext
                            .getUrlPrefix(), newRelativePath);

            renderPage(rendererFactory, response, dataSetCode, newRenderingContext, requestParams,
                    session);
        } else if (AutoResolveUtils.continueAutoResolving(requestParams.tryGetMainDataSetPattern(),
                dir))
        {
            assert dir.listFiles().length == 1;
            String childName = dir.listFiles()[0].getName();
            String oldRelativePathOrNull = renderingContext.getRelativePathOrNull();
            String pathPrefix =
                    StringUtils.isBlank(oldRelativePathOrNull) ? "" : (oldRelativePathOrNull + "/");
            String newRelativePath = pathPrefix + childName;
            RenderingContext newRenderingContext =
                    new RenderingContext(renderingContext.getRootDir(), renderingContext
                            .getUrlPrefix(), newRelativePath);

            renderPage(rendererFactory, response, dataSetCode, newRenderingContext, requestParams,
                    session);
        } else
        {
            createPage(rendererFactory, response, dataSet, renderingContext, dir);
        }
    }

    private void createPage(IRendererFactory rendererFactory, HttpServletResponse response,
            ExternalData dataSet, RenderingContext renderingContext, File file) throws IOException
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set '" + dataSet.getCode() + "' show directory "
                    + file.getAbsolutePath());
        }
        IDirectoryRenderer directoryRenderer =
                rendererFactory.createDirectoryRenderer(renderingContext);
        response.setContentType(rendererFactory.getContentType());
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

    private void deliverFile(final HttpServletResponse response, String dataSetCode, File file,
            String displayMode) throws IOException, FileNotFoundException
    {
        String infoPostfix;
        ResponseContentStream responseStream;
        Size thumbnailSize = tryAsThumbnailDisplayMode(displayMode);
        if (thumbnailSize != null)
        {
            BufferedImage image = createThumbnail(file, thumbnailSize);
            infoPostfix = " as a thumbnail.";
            responseStream = createResponseContentStream(image, file.getName());
        } else
        {
            infoPostfix = ".";
            responseStream = createResponseContentStream(file, displayMode);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set '" + dataSetCode + "' deliver file "
                    + file.getAbsolutePath() + " (" + responseStream.getSize() + " bytes)"
                    + infoPostfix);
        }
        writeResponseContent(responseStream, response);
    }

    private static ResponseContentStream createResponseContentStream(File file, String displayMode)
            throws FileNotFoundException
    {
        String contentType = getMimeType(file, displayMode.equals(TEXT_MODE_DISPLAY));
        String headerContentDisposition = "inline; filename=" + file.getName();
        return new ResponseContentStream(new FileInputStream(file), file.length(), contentType,
                headerContentDisposition);
    }

    private ExternalData getDataSet(String dataSetCode, String sessionIdOrNull, HttpSession session)
    {
        ExternalData dataset = tryToGetCachedDataSet(session, dataSetCode);
        if (dataset != null)
        {
            return dataset;
        }
        ensureSessionIdSpecified(sessionIdOrNull);
        ExternalData dataSet = tryGetDataSetFromServer(dataSetCode, sessionIdOrNull);
        if (dataSet != null)
        {
            putDataSetToMap(session, dataSetCode, dataSet);
            return dataSet;
        } else
        {
            throw new UserFailureException("Unknown data set '" + dataSetCode + "'.");
        }
    }

    private ExternalData tryGetDataSetFromServer(String dataSetCode, String sessionIdOrNull)
    {
        IEncapsulatedOpenBISService dataSetService = applicationContext.getDataSetService();
        ExternalData dataSet = dataSetService.tryGetDataSet(sessionIdOrNull, dataSetCode);
        if (operationLog.isInfoEnabled())
        {
            String actionDesc = (dataSet != null) ? "obtained from" : "not found in";
            operationLog.info(String.format("Data set '%s' %s openBIS server.", dataSetCode,
                    actionDesc));
        }
        return dataSet;
    }

    private void putDataSetToMap(HttpSession session, String dataSetCode, ExternalData dataSet)
    {
        getDataSets(session).put(dataSetCode, dataSet);
    }
}
