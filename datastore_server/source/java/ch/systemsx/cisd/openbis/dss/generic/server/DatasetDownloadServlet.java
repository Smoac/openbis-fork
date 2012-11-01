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

import static ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.HierarchicalContentUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.generic.shared.util.HttpRequestUtils;

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

    static final String FORCE_AUTO_RESOLVE_KEY = "forceAutoResolve";

    static final String MAIN_DATA_SET_PATH_KEY = "mdsPath";

    static final String MAIN_DATA_SET_PATTERN_KEY = "mdsPattern";

    private static String DOWNLOAD_URL;

    static void setDownloadUrl(String downloadUrl)
    {
        if (downloadUrl.endsWith("/"))
        {
            DOWNLOAD_URL = downloadUrl.substring(0, downloadUrl.length() - 1);
        } else
        {
            DOWNLOAD_URL = downloadUrl;
        }
    }

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

        private final boolean forceAutoResolve;

        public RequestParams(String dataSetCode, String pathInfo, String sessionIdOrNull,
                String urlPrefixWithDataset, String displayMode, boolean autoResolve,
                String mainDataSetPathOrNull, String mainDataSetPatternOrNull,
                boolean forceAutoResolve)
        {
            this.dataSetCode = dataSetCode;
            this.pathInfo = pathInfo;
            this.sessionIdOrNull = sessionIdOrNull;
            this.urlPrefixWithDataset = urlPrefixWithDataset;
            this.displayMode = displayMode;
            this.autoResolve = autoResolve;
            this.mainDataSetPathOrNull = mainDataSetPathOrNull;
            this.mainDataSetPatternOrNull = mainDataSetPatternOrNull;
            this.forceAutoResolve = forceAutoResolve;
        }

        public boolean isAutoResolve()
        {
            return autoResolve;
        }

        public boolean isForceAutoResolve()
        {
            return forceAutoResolve;
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
        try
        {
            renderPage(rendererFactory, response, dataSetCode, context, requestParams, session);
        } finally
        {
            // close the root content when we are done working with it
            if (context != null && context.getRootContent() != null)
            {
                context.getRootContent().close();
            }
        }
    }

    private RenderingContext createRenderingContext(RequestParams requestParams,
            String dataSetCode, HttpSession session)
    {
        IHierarchicalContent rootContent = null;
        try
        {
            rootContent =
                    applicationContext.getHierarchicalContentProvider().asContent(dataSetCode);
        } catch (IllegalArgumentException ex)
        {
            throw new UserFailureException(ex.getMessage());
        }
        try
        {
            RenderingContext context =
                    new RenderingContext(rootContent, requestParams.getURLPrefix(),
                            requestParams.getPathInfo(), requestParams.tryGetSessionId());
            return context;
        } catch (IllegalArgumentException ex)
        {
            if (rootContent != null)
            {
                rootContent.close();
            }
            throw ex; // rethrow
        }
    }

    private IRendererFactory createRendererFactory(String displayMode)
    {
        if (displayMode.equals(SIMPLE_HTML_MODE_DISPLAY) || displayMode.equals(HTML_MODE_DISPLAY))
        {
            return new SimpleHTMLRendererFactory();
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

        final String sessionIDOrNull = request.getParameter(Utils.SESSION_ID_PARAM);
        String displayMode = getDisplayMode(request);

        Boolean autoResolveOrNull = Boolean.valueOf(request.getParameter(AUTO_RESOLVE_KEY));
        boolean autoResolve = autoResolveOrNull != null && autoResolveOrNull;
        Boolean forceAutoResolveOrNull =
                Boolean.valueOf(request.getParameter(FORCE_AUTO_RESOLVE_KEY));
        boolean forceAutoResolve = forceAutoResolveOrNull != null && forceAutoResolveOrNull;
        String mainDataSetPathOrNull = request.getParameter(MAIN_DATA_SET_PATH_KEY);
        String mainDataSetPatternOrNull = request.getParameter(MAIN_DATA_SET_PATTERN_KEY);
        boolean shouldSetMainDataSetParamsToNull =
                autoResolve == false && forceAutoResolve == false;
        if (shouldSetMainDataSetParamsToNull || StringUtils.isBlank(mainDataSetPathOrNull))
        {
            mainDataSetPathOrNull = null;
        }
        if (shouldSetMainDataSetParamsToNull || StringUtils.isBlank(mainDataSetPatternOrNull))
        {
            mainDataSetPatternOrNull = null;
        }
        return new RequestParams(dataSetCode, pathInfo, sessionIDOrNull, urlPrefixWithDataset,
                displayMode, autoResolve, mainDataSetPathOrNull, mainDataSetPatternOrNull,
                forceAutoResolve);
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
        if (exception instanceof HighLevelException)
        {
            operationLog.error(exception.getMessage());
        } else if (operationLog.isInfoEnabled())
        {
            StringBuffer url = request.getRequestURL();
            String queryString = request.getQueryString();
            if (StringUtils.isNotBlank(queryString))
            {
                url.append("?").append(queryString);
            }
            operationLog.error("Request " + url + " caused an exception: ", exception);
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

        IHierarchicalContentNode node = renderingContext.getContentNode();
        if (node.exists() == false)
        {
            throw new EnvironmentFailureException("Resource '" + node.getRelativePath()
                    + "' does not exist in data set '" + dataSetCode + "'.");
        }

        String sessionIdOrNull = requestParams.tryGetSessionId();
        ensureDatasetAccessible(dataSetCode, session, sessionIdOrNull);
        if (node.isDirectory())
        {
            if (requestParams.isAutoResolve())
            {
                autoResolve(rendererFactory, response, dataSetCode, renderingContext,
                        requestParams, session, node, false);
            } else if (requestParams.isForceAutoResolve())
            {
                autoResolve(rendererFactory, response, dataSetCode, renderingContext,
                        requestParams, session, node, true);
            } else
            {
                createPage(rendererFactory, response, dataSetCode, renderingContext, node);
            }
        } else
        {
            deliverFile(response, dataSetCode, node, requestParams.getDisplayMode());
        }
    }

    private void autoResolve(IRendererFactory rendererFactory, HttpServletResponse response,
            String dataSetCode, RenderingContext renderingContext, RequestParams requestParams,
            HttpSession session, IHierarchicalContentNode dirNode, boolean shouldForce)
            throws IOException
    {
        assert dirNode.exists() && dirNode.isDirectory();
        List<IHierarchicalContentNode> mainDataSets =
                AutoResolveUtils.findSomeMatchingFiles(renderingContext.getRootContent(),
                        requestParams.tryGetMainDataSetPath(),
                        requestParams.tryGetMainDataSetPattern());
        if (mainDataSets.size() == 1 || (mainDataSets.size() > 1 && shouldForce))
        {
            String newRelativePath = mainDataSets.get(0).getRelativePath();
            RenderingContext newRenderingContext =
                    new RenderingContext(renderingContext, newRelativePath);
            autoResolveRedirect(response, newRenderingContext);
        } else if (AutoResolveUtils.continueAutoResolving(requestParams.tryGetMainDataSetPattern(),
                dirNode))
        {
            assert dirNode.getChildNodes().size() == 1;
            String childName = dirNode.getChildNodes().get(0).getName();
            String oldRelativePath = renderingContext.getRelativePath();
            String pathPrefix = StringUtils.isBlank(oldRelativePath) ? "" : (oldRelativePath + "/");
            String newRelativePath = pathPrefix + childName;
            RenderingContext newRenderingContext =
                    new RenderingContext(renderingContext, newRelativePath);
            autoResolveRedirect(response, newRenderingContext);
        } else
        {
            createPage(rendererFactory, response, dataSetCode, renderingContext, dirNode);
        }
    }

    private static void autoResolveRedirect(HttpServletResponse response,
            RenderingContext newContext) throws IOException
    {
        String urlPrefix = newContext.getUrlPrefix();
        String relativePath = newContext.getRelativePath();
        String sessionIdOrNull = newContext.getSessionIdOrNull();
        final String newLocation =
                DOWNLOAD_URL + urlPrefix + "/" + relativePath
                        + Utils.createUrlParameterForSessionId("?", sessionIdOrNull);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Auto resolve redirect: '%s', context: %s",
                    newLocation, newContext));
        }
        response.sendRedirect(newLocation);
    }

    private void createPage(IRendererFactory rendererFactory, HttpServletResponse response,
            String dataSetCode, RenderingContext renderingContext, IHierarchicalContentNode dirNode)
            throws IOException
    {
        assert dirNode.isDirectory();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("For data set '%s' show directory '%s'", dataSetCode,
                    (dirNode.getRelativePath() == null) ? "/" : dirNode.getRelativePath()));
        }
        IDirectoryRenderer directoryRenderer =
                rendererFactory.createDirectoryRenderer(renderingContext);
        response.setContentType(rendererFactory.getContentType());
        PrintWriter writer = null;
        try
        {
            HttpRequestUtils.setNoCacheHeaders(response);
            writer = response.getWriter();
            directoryRenderer.setWriter(writer);
            directoryRenderer.printHeader();
            String relativeParentPath = renderingContext.getRelativeParentPath();
            if (relativeParentPath != null)
            {
                directoryRenderer.printLinkToParentDirectory(relativeParentPath);
            }
            List<IHierarchicalContentNode> children = dirNode.getChildNodes();
            HierarchicalContentUtils.sortNodes(children);
            for (IHierarchicalContentNode childNode : children)
            {
                String name = childNode.getName();
                String relativePath = childNode.getRelativePath();
                String normalizedRelativePath = relativePath.replace('\\', '/');
                if (childNode.isDirectory())
                {
                    directoryRenderer.printDirectory(name, normalizedRelativePath);
                } else
                {
                    Integer checksumOrNull =
                            childNode.isChecksumCRC32Precalculated() ? childNode.getChecksumCRC32()
                                    : null;
                    directoryRenderer.printFile(name, normalizedRelativePath,
                            childNode.getFileLength(), checksumOrNull);
                }
            }
            directoryRenderer.printFooter();
            writer.flush();

        } finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    private void deliverFile(final HttpServletResponse response, String dataSetCode,
            IHierarchicalContentNode fileNode, String displayMode) throws IOException,
            FileNotFoundException
    {
        assert fileNode.isDirectory() == false;

        String infoPostfix;
        ResponseContentStream responseStream;
        Size thumbnailSize = tryAsThumbnailDisplayMode(displayMode);
        if (thumbnailSize != null)
        {
            BufferedImage image = createThumbnail(fileNode, thumbnailSize);
            infoPostfix = " as a thumbnail.";
            responseStream = createResponseContentStream(image, fileNode.getName());
        } else
        {
            infoPostfix = ".";
            responseStream = createResponseContentStream(fileNode, displayMode);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set '" + dataSetCode + "' deliver file '"
                    + fileNode.getRelativePath() + "' (" + responseStream.getSize() + " bytes)"
                    + infoPostfix);
        }
        writeResponseContent(responseStream, response);
    }

    /**
     * @param image is the content of the response
     * @param fileNameOrNull specified if image was generated from one file
     */
    private final static ResponseContentStream createResponseContentStream(BufferedImage image,
            String fileNameOrNull) throws IOException
    {
        return ResponseContentStream.createPNG(image, fileNameOrNull);
    }

    private static ResponseContentStream createResponseContentStream(
            IHierarchicalContentNode contentNode, String displayMode) throws FileNotFoundException
    {
        String contentType = Utils.getMimeType(contentNode, displayMode.equals(TEXT_MODE_DISPLAY));

        return ResponseContentStream.create(contentNode.getInputStream(),
                contentNode.getFileLength(), contentType, contentNode.getName());
    }

}
