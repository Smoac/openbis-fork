/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.ApplicationServerApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractServlet;


/**
 * Servlet for uploading and downloading files.
 *
 * @author Franz-Josef Elmer
 */
@Controller
@RequestMapping({ "/" + FileServiceServlet.FILE_SERVICE_PATH_MAPPING,
        "/openbis/" + FileServiceServlet.FILE_SERVICE_PATH_MAPPING,
        "/openbis/openbis/" + FileServiceServlet.FILE_SERVICE_PATH_MAPPING})
public class FileServiceServlet extends AbstractServlet
{
    public static final String FILE_SERVICE_PATH = "file-service";
    public static final String FILE_SERVICE_PATH_MAPPING = FILE_SERVICE_PATH + "/**/*";

    private static final String APP_PREFIX = "/" + FILE_SERVICE_PATH + "/";
    private static final String REPO_PATH_KEY = "file-server.repository-path";
    private static final String DEFAULT_REPO_PATH = "../../data/file-server";
    
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, FileServiceServlet.class);
    
    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Resource(name = ApplicationServerApi.INTERNAL_SERVICE_NAME)
    private IApplicationServerApi service;

    @Override
    protected void respondToRequest(HttpServletRequest request, HttpServletResponse response) 
            throws Exception, IOException
    {
        String fullPath = request.getPathInfo();
        if (fullPath.startsWith(APP_PREFIX) == false)
        {
            return;
        }
        PathInfo pathInfo = new PathInfo(fullPath.substring(APP_PREFIX.length()));
        File filesRepository = new File(configurer.getResolvedProps().getProperty(REPO_PATH_KEY, DEFAULT_REPO_PATH));
        operationLog.info(fullPath);
        if (request instanceof MultipartHttpServletRequest)
        {
            handleUpload((MultipartHttpServletRequest) request, response, filesRepository, pathInfo);
        } else
        {
            handleDownload(response, filesRepository, pathInfo);
        }
        
    }

    private void handleDownload(HttpServletResponse response, File filesRepository, 
            PathInfo pathInfo) throws IOException
    {
        File file = new File(filesRepository, pathInfo.getRealmAndPath());
        if (file.isFile())
        {
            response.setContentLength((int) file.length());
            String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file.getName());
            response.setContentType(contentType);
            ServletOutputStream outputStream = null;
            FileInputStream inputStream = null;
            try
            {
                outputStream = response.getOutputStream();
                inputStream = new FileInputStream(file);
                IOUtils.copy(inputStream, outputStream);
                operationLog.info(file.length() + " bytes of file '" + pathInfo + "' have been deliverd.");
            } catch (IOException ex)
            {
                operationLog.error("Delivering file '" + pathInfo + "' failed.", ex);
                writeError(response, ex.getMessage());
            } finally
            {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        } else
        {
            operationLog.warn("Unknown file: " + pathInfo);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleUpload(MultipartHttpServletRequest multipartRequest, HttpServletResponse response, 
            File filesRepository, PathInfo pathInfo) throws IOException
    {
        String sessionToken = getSessionToken(multipartRequest);
        if (service.isSessionActive(sessionToken) == false)
        {
            writeError(response, "Session time out");
            operationLog.warn("Session time out for session " + sessionToken);
            return;
        }
        Iterator<String> fileNamesIterator = multipartRequest.getFileNames();
        if (fileNamesIterator.hasNext() == false)
        {
            writeError(response, "No file.");
        }
        String name = fileNamesIterator.next();
        MultipartFile multipartFile = multipartRequest.getFile(name);
        String uuid = UUID.randomUUID().toString();
        String originalFilename = multipartFile.getOriginalFilename();
        String filePath = pathInfo.getRealm() + "/" + uuid.substring(0, 2) + "/" + uuid.substring(2, 4) 
                + "/" + uuid.substring(4, 6) + "/" + uuid + "/" + originalFilename; 
        File file = new File(filesRepository, filePath);
        file.getParentFile().mkdirs();
        multipartFile.transferTo(file);
        operationLog.info(multipartFile.getSize() + " bytes have been uploaded for file '" 
                + originalFilename + "' and stored in '" + filePath + "'.");
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        writeResponse(response, "{\n" + 
                "    \"uploaded\": 1,\n" + 
                "    \"fileName\": \"foo.jpg\",\n" + 
                "    \"url\": \"/openbis" + APP_PREFIX + filePath + "\"\n" + 
                "}");
    }
    
    private void writeError(HttpServletResponse response, String message) throws IOException
    {
        writeResponse(response, "{'uploaded': 0, 'error': {'message': '" + message + "'}}");
    }
    
    private static final class PathInfo
    {
        private final String realmAndPath;
        private final String realm;

        PathInfo(String realmAndPath)
        {
            this.realmAndPath = realmAndPath;
            String[] splittedString = realmAndPath.split("/", 2);
            realm = splittedString[0];
        }

        public String getRealmAndPath()
        {
            return realmAndPath;
        }

        public String getRealm()
        {
            return realm;
        }

        @Override
        public String toString()
        {
            return realmAndPath;
        }
    }
}
