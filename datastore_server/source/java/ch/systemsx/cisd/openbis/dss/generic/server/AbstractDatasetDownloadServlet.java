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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * Superclass for dataset download servlets. Provides functionality to deliver content of files and
 * images, does not deal with browsing directories.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractDatasetDownloadServlet extends HttpServlet
{
    protected static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, AbstractDatasetDownloadServlet.class);

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractDatasetDownloadServlet.class);

    private static final long serialVersionUID = 1L;

    private static final String CONTENT_TYPE_PNG = "image/png";

    private static final Size DEFAULT_THUMBNAIL_SIZE = new Size(100, 60);

    private static final String THUMBNAIL_MODE_DISPLAY = "thumbnail";

    protected static final String SESSION_ID_PARAM = "sessionID";

    static final String DISPLAY_MODE_PARAM = "mode";

    static final String BINARY_CONTENT_TYPE = "binary";

    static final String PLAIN_TEXT_CONTENT_TYPE = "text/plain";

    private static final MimetypesFileTypeMap MIMETYPES = new MimetypesFileTypeMap();

    static final String DATABASE_INSTANCE_SESSION_KEY = "database-instance";

    static final String DATA_SET_ACCESS_SESSION_KEY = "data-set-access";

    static final String DATA_SET_SESSION_KEY = "data-set";

    protected ApplicationContext applicationContext;

    public AbstractDatasetDownloadServlet()
    {
    }

    // for tests only
    AbstractDatasetDownloadServlet(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void init(final ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
        try
        {
            ServletContext context = servletConfig.getServletContext();
            applicationContext =
                    (ApplicationContext) context
                            .getAttribute(DataStoreServer.APPLICATION_CONTEXT_KEY);

            // Look for the additional configuration parameters and initialize the servlet using
            // them
            Enumeration<String> e = servletConfig.getInitParameterNames();
            if (e.hasMoreElements())
                doSpecificInitialization(e, servletConfig);
        } catch (Exception ex)
        {
            notificationLog.fatal("Failure during '" + servletConfig.getServletName()
                    + "' servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }

    /**
     * Do any additional initialization using information from the properties passed in. Subclasses
     * may override.
     */
    protected synchronized void doSpecificInitialization(Enumeration<String> parameterNames,
            ServletConfig servletConfig)
    {
        return;
    }

    protected final HttpSession tryGetOrCreateSession(final HttpServletRequest request,
            String sessionIdOrNull)
    {
        HttpSession session = request.getSession(false);
        if (session == null && sessionIdOrNull == null)
        {
            // a) The session is expired and b) we do not have openbis session id provided.
            // So a) metadata about datasets are not in the session and b) we cannot get them from
            // openbis.
            return null;
        }
        if (session == null)
        {
            session = request.getSession(true);
            ConfigParameters configParameters = applicationContext.getConfigParameters();
            session.setMaxInactiveInterval(configParameters.getSessionTimeout());
        }
        return session;
    }

    protected final static void printSessionExpired(final HttpServletResponse response)
            throws IOException
    {
        printErrorResponse(response, "Download session expired.");
    }

    protected final static void printErrorResponse(final HttpServletResponse response,
            String errorMessage) throws IOException
    {
        PrintWriter writer = response.getWriter();
        writer.write("<html><body>" + errorMessage + "</body></html>");
        writer.flush();
        writer.close();
    }

    protected final void writeResponseContent(ResponseContentStream responseStream,
            final HttpServletResponse response) throws IOException
    {
        response.setHeader("Content-Disposition", responseStream.getHeaderContentDisposition());
        if (responseStream.getSize() <= Integer.MAX_VALUE)
        {
            response.setContentLength((int) responseStream.getSize());
        } else
        {
            response.addHeader("Content-Length", Long.toString(responseStream.getSize()));
        }
        response.setContentType(responseStream.getContentType());

        ServletOutputStream outputStream = null;
        InputStream content = responseStream.getInputStream();
        try
        {
            outputStream = response.getOutputStream();
            IOUtils.copy(content, outputStream);
        } finally
        {
            IOUtils.closeQuietly(content);
            IOUtils.closeQuietly(outputStream);
        }
    }

    // @Protected
    static String getMimeType(File f, boolean plainTextMode)
    {
        if (plainTextMode)
        {
            return BINARY_CONTENT_TYPE;
        } else
        {
            String extension = FilenameUtils.getExtension(f.getName());
            if (extension.length() == 0)
            {
                return PLAIN_TEXT_CONTENT_TYPE;
            } else if (extension.equalsIgnoreCase("png"))
            {
                return CONTENT_TYPE_PNG;
            } else
            {
                return MIMETYPES.getContentType(f.getName().toLowerCase());
            }
        }
    }

    protected static final BufferedImage createThumbnail(File file, Size thumbnailSize)
    {
        BufferedImage image = ImageUtil.loadImage(file);
        return createThumbnail(image, thumbnailSize);
    }

    protected static final BufferedImage createThumbnail(BufferedImage image, Size thumbnailSize)
    {
        int width = thumbnailSize.getWidth();
        int height = thumbnailSize.getHeight();
        return ImageUtil.createThumbnail(image, width, height);
    }

    protected static final class ResponseContentStream
    {
        private final InputStream inputStream;

        private final long size;

        private final String contentType;

        private final String headerContentDisposition;

        public ResponseContentStream(InputStream inputStream, long size, String contentType,
                String headerContentDisposition)
        {
            this.inputStream = inputStream;
            this.size = size;
            this.contentType = contentType;
            this.headerContentDisposition = headerContentDisposition;
        }

        public InputStream getInputStream()
        {
            return inputStream;
        }

        public long getSize()
        {
            return size;
        }

        public String getContentType()
        {
            return contentType;
        }

        public String getHeaderContentDisposition()
        {
            return headerContentDisposition;
        }
    }

    /**
     * @param image is the content of the response
     * @param fileNameOrNull specified if image was generated from one file
     */
    protected final static ResponseContentStream createResponseContentStream(BufferedImage image,
            String fileNameOrNull) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);

        InputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        String headerContentDisposition = "inline;";
        if (fileNameOrNull != null)
        {
            headerContentDisposition += " filename=" + fileNameOrNull;
        }
        return new ResponseContentStream(inputStream, output.size(), CONTENT_TYPE_PNG,
                headerContentDisposition);
    }

    // if display mode describes a thumbnail return its expected size
    protected static Size tryAsThumbnailDisplayMode(String displayMode)
    {
        if (displayMode.startsWith(THUMBNAIL_MODE_DISPLAY))
        {
            return extractSize(displayMode);
        } else
        {
            return null;
        }
    }

    private static Size extractSize(String displayMode)
    {
        String sizeDescription = displayMode.substring(THUMBNAIL_MODE_DISPLAY.length());
        int indexOfSeparator = sizeDescription.indexOf('x');
        if (indexOfSeparator < 0)
        {
            return DEFAULT_THUMBNAIL_SIZE;
        }
        try
        {
            int width = Integer.parseInt(sizeDescription.substring(0, indexOfSeparator));
            int height = Integer.parseInt(sizeDescription.substring(indexOfSeparator + 1));
            return new Size(width, height);
        } catch (NumberFormatException ex)
        {
            operationLog.warn("Invalid numbers in displayMode '" + displayMode
                    + "'. Default thumbnail size is used.");
            return DEFAULT_THUMBNAIL_SIZE;
        }
    }

    public static final class Size
    {
        private final int width;

        private final int height;

        Size(int width, int height)
        {
            this.width = width;
            this.height = height;
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

    protected final File createDataSetRootDirectory(String dataSetCode, HttpSession session)
    {
        DatabaseInstance databaseInstance = getDatabaseInstance(session);
        File storeDir = applicationContext.getConfigParameters().getStorePath();
        String databaseUuid = databaseInstance.getUuid();

        File dataSetRootDirectory =
                DatasetLocationUtil.getDatasetLocationPath(storeDir, dataSetCode, databaseUuid);
        if (dataSetRootDirectory.exists() == false)
        {
            throw new UserFailureException("Data set '" + dataSetCode + "' not found in the store.");
        }
        return dataSetRootDirectory;
    }

    // ---

    private DatabaseInstance getDatabaseInstance(HttpSession session)
    {
        DatabaseInstance databaseInstance =
                (DatabaseInstance) session.getAttribute(DATABASE_INSTANCE_SESSION_KEY);
        if (databaseInstance == null)
        {
            databaseInstance = applicationContext.getDataSetService().getHomeDatabaseInstance();
            session.setAttribute(DATABASE_INSTANCE_SESSION_KEY, databaseInstance);
        }
        return databaseInstance;
    }

    // ---

    protected final void ensureDatasetAccessible(String dataSetCode, HttpSession session,
            String sessionIdOrNull)
    {
        if (isDatasetAccessible(dataSetCode, sessionIdOrNull, session) == false)
        {
            throw new UserFailureException("Data set '" + dataSetCode + "' is not accessible.");
        }
    }

    private boolean isDatasetAccessible(String dataSetCode, String sessionIdOrNull,
            HttpSession session)
    {
        Boolean access = getDataSetAccess(session).get(dataSetCode);
        if (access == null)
        {
            if (tryToGetCachedDataSet(session, dataSetCode) != null)
            {
                return true; // access already checked and granted
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format(
                        "Check access to the data set '%s' at openBIS server.", dataSetCode));
            }
            IEncapsulatedOpenBISService dataSetService = applicationContext.getDataSetService();
            ensureSessionIdSpecified(sessionIdOrNull);
            try
            {
                dataSetService.checkDataSetAccess(sessionIdOrNull, dataSetCode);
                access = true;
            } catch (UserFailureException ex)
            {
                operationLog.error(String.format(
                        "Error when checking access to the data set '%s' at openBIS server: %s",
                        dataSetCode, ex.getMessage()));
                return false; // do not save this in cache, try to connect to AS next time
            }
            getDataSetAccess(session).put(dataSetCode, access);
        }
        return access;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> getDataSetAccess(HttpSession session)
    {
        Map<String, Boolean> map =
                (Map<String, Boolean>) session.getAttribute(DATA_SET_ACCESS_SESSION_KEY);
        if (map == null)
        {
            map = new HashMap<String, Boolean>();
            session.setAttribute(DATA_SET_ACCESS_SESSION_KEY, map);
        }
        return map;
    }

    protected final ExternalData tryToGetCachedDataSet(HttpSession session, String dataSetCode)
    {
        return getDataSets(session).get(dataSetCode);
    }

    @SuppressWarnings("unchecked")
    protected final Map<String, ExternalData> getDataSets(HttpSession session)
    {
        Map<String, ExternalData> map =
                (Map<String, ExternalData>) session.getAttribute(DATA_SET_SESSION_KEY);
        if (map == null)
        {
            map = new HashMap<String, ExternalData>();
            session.setAttribute(DATA_SET_SESSION_KEY, map);
        }
        return map;
    }

    protected final void ensureSessionIdSpecified(String sessionIdOrNull)
    {
        if (sessionIdOrNull == null)
        {
            throw new EnvironmentFailureException("Session id not specified in the URL");
        }
    }

}
