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

package ch.systemsx.cisd.cifex.rpc.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.cifex.rpc.client.gui.FileUploadClient;
import ch.systemsx.cisd.cifex.server.AbstractFileUploadServlet;
import ch.systemsx.cisd.cifex.server.HttpUtils;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.utilities.Template;

/**
 * Servlet which are triggered by the Web client in order to start the Java upload client via
 * Java Web Start.
 *
 * @author Franz-Josef Elmer
 */
public class File2GBUploadServlet extends AbstractFileUploadServlet
{
    private static final long serialVersionUID = 1L;
    
    @Private static final Template JNLP_TEMPLATE = new Template("<?xml version='1.0' encoding='utf-8'?>\n" + 
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
            "    <jar href='cisd-base.jar'/>\n" + 
            "    <jar href='spring-web.jar'/>\n" + 
            "    <jar href='spring-context.jar'/>\n" + 
            "    <jar href='spring-beans.jar'/>\n" + 
            "    <jar href='spring-aop.jar'/>\n" + 
            "    <jar href='spring-core.jar'/>\n" + 
            "    <jar href='aopalliance.jar'/>\n" + 
            "    <jar href='commons-codec.jar'/>\n" + 
            "    <jar href='commons-httpclient.jar'/>\n" + 
            "    <jar href='commons-io.jar'/>\n" + 
            "    <jar href='commons-lang.jar'/>\n" + 
            "    <jar href='commons-logging.jar'/>\n" + 
            "  </resources>\n" + 
            "  <application-desc main-class='${main-class}'>\n" +
            "    <argument>${service-URL}</argument>\n" +
            "    <argument>${upload-session-id}</argument>\n" +
            "    <argument>${maxUploadSizeInMB}</argument>\n" +
            "  </application-desc>\n" +
            "</jnlp>\n");

    private IExtendedCIFEXRPCService uploadService;
    
    public File2GBUploadServlet()
    {
    }
    
    @Private File2GBUploadServlet(IExtendedCIFEXRPCService uploadService, IDomainModel domainModel)
    {
        this.uploadService = uploadService;
        this.domainModel = domainModel;
    }

    @Override
    public void init() throws ServletException
    {
        super.init();
        try
        {
            ServletContext servletContext = getServletContext();
            final BeanFactory context =
                    WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
            uploadService = (IExtendedCIFEXRPCService) context.getBean("rpc-service");
        } catch (final Exception ex)
        {
            notificationLog.fatal("Failure during file upload service servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, InvalidSessionException
    {
        UserDTO user = getUserDTO(request); // Throws exception if session is invalid
        String url = getURLForEmail(request);
        String uploadSessionID = uploadService.createSession(user, url);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Start file upload session with ID " + uploadSessionID);
        }
        
        response.setContentType("application/x-java-jnlp-file");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
        Template template = JNLP_TEMPLATE.createFreshCopy();
        template.attemptToBind("base-URL", createBaseURL(request));
        template.attemptToBind("main-class", FileUploadClient.class.getName());
        template.attemptToBind("service-URL", createServiceURL(request));
        template.attemptToBind("upload-session-id", uploadSessionID);
        long maxUploadSizeInMB = getMaxUploadSize(user) / MB;
        template.attemptToBind("maxUploadSizeInMB", Long.toString(maxUploadSizeInMB));
        writer.print(template.createText());
        writer.close();
    }

    private String createBaseURL(final HttpServletRequest request)
    {
        String url = HttpUtils.getBasicURL(request);
        if (url.indexOf("localhost:8888") > 0)
        {
            url = url + "/ch.systemsx.cisd.cifex.Cifex/";
        } else
        {
            url = url + "/";
        }
        return url;
    }
    
    private String createServiceURL(final HttpServletRequest request)
    {
        String baseURL = HttpUtils.getBasicURL(request);
        return baseURL + "/cifex/rpc-service";
    }
    
}
