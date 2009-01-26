/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.upload.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.cisd.cifex.server.AbstractFileUploadServlet;
import ch.systemsx.cisd.cifex.server.HttpUtils;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.upload.IUploadService;
import ch.systemsx.cisd.cifex.upload.UploadStatus;
import ch.systemsx.cisd.cifex.upload.client.FileUploadClient;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.utilities.Template;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class File2GBUploadServlet extends AbstractFileUploadServlet implements IUploadService
{
    private static final String UPLOAD_SESSION_MANAGER = "upload-session-manager";

    private static final long serialVersionUID = 1L;
    
    private static final Template JNLP_TEMPLATE = new Template("<?xml version='1.0' encoding='utf-8'?>\n" + 
            "<jnlp spec='1.0+' codebase='${base-URL}'>\n" + 
            "  <information>\n" + 
            "    <title>CIFEX File Uploader</title>\n" + 
            "    <vendor>Center for Information Science and Databases</vendor>\n" + 
            "    <description>CIFEX File Uploader</description>\n" + 
            "  </information>\n" + 
            "  <security>\n" + 
            "    <all-permissions/>\n" + 
            "  </security>\n" + 
            "  <resources>\n" + 
            "    <j2se version='1.5+'/>\n" + 
            "    <jar href='cifex.jar'/>\n" + 
            "    <jar href='spring-context.jar'/>\n" + 
            "    <jar href='spring-beans.jar'/>\n" + 
            "    <jar href='spring-core.jar'/>\n" + 
            "    <jar href='commons-logging.jar'/>\n" + 
            "  </resources>\n" + 
            "  <application-desc main-class='${main-class}'>\n" +
            "    <argument>${upload-session-id}</argument>\n" +
            "  </application-desc>\n" +
            "</jnlp>\n");
    
    
    public UploadStatus getUploadStatus(String uploadSessionID)
    {
        return null;
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, InvalidSessionException
    {
        final UserDTO requestUser = getUserDTO(request); // Throws exception if session is invalid
        System.out.println(requestUser);
        UploadSession session = getUploadSessionManager(request).createSession();
        UploadStatus uploadStatus = new UploadStatus(request.getParameterValues("files"));
        session.setUploadStatus(uploadStatus);
        
        response.setContentType("application/x-java-jnlp-file");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
        Template template = JNLP_TEMPLATE.createFreshCopy();
        template.attemptToBind("base-URL", createBaseURL(request));
        template.attemptToBind("main-class", FileUploadClient.class.getName());
        template.attemptToBind("upload-session-id", session.getSessionID());
        writer.print(template.createText());
        writer.close();
    }
    
    private UploadSessionManager getUploadSessionManager(HttpServletRequest request)
    {
        UploadSessionManager manager = (UploadSessionManager) request.getAttribute(UPLOAD_SESSION_MANAGER);
        if (manager == null)
        {
            manager = new UploadSessionManager();
            request.setAttribute(UPLOAD_SESSION_MANAGER, manager);
        }
        return manager;
    }

    private String createBaseURL(final HttpServletRequest request)
    {
        String url = HttpUtils.getBasicURL(request);
        if (url.indexOf("localhost") > 0)
        {
            url = url + "/ch.systemsx.cisd.cifex.Cifex/";
        } else
        {
            url = url + "/cifex/";
        }
        return url;
    }
    
    private String createFileList(String[] files)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < files.length; i++)
        {
            builder.append(files[i]);
            if (i < files.length - 1)
            {
                builder.append(',');
            }
        }
        return builder.toString();
    }
}
