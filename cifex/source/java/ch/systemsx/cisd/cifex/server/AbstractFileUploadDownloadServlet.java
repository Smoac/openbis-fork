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

package ch.systemsx.cisd.cifex.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.systemsx.cisd.cifex.rpc.server.IExtendedCIFEXRPCService;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.string.Template;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractFileUploadDownloadServlet extends AbstractCIFEXServiceServlet
{
    private static final long serialVersionUID = 1L;

    /*
     * Keep in mind that this constant is used by <code>FileUploadWidget</code> to check if upload
     * was successful, so if you change the value of the constant here it should also be changed in
     * the widget.
     */
    public static final int MAX_FILENAME_LENGTH = 250;

    public static final Template JNLP_TEMPLATE =
            new Template(
                    "<?xml version='1.0' encoding='utf-8'?>\n"
                            + "<jnlp spec='1.0+' codebase='${base-URL}'>\n"
                            + "  <information>\n"
                            + "    <title>${title}</title>\n"
                            + "    <vendor>Center for Information Science and Databases</vendor>\n"
                            + "    <description>${description}</description>\n"
                            + "  </information>\n"
                            + "  <security>\n"
                            + "    <all-permissions/>\n"
                            + "  </security>\n"
                            + "  <resources>\n"
                            + "    <j2se version='1.5+'/>\n"
                            + "    <jar href='cifex.jar'/>\n"
                            + "    <jar href='cisd-base.jar'/>\n"
                            + "    <jar href='spring-web.jar'/>\n"
                            + "    <jar href='spring-context.jar'/>\n"
                            + "    <jar href='spring-beans.jar'/>\n"
                            + "    <jar href='spring-aop.jar'/>\n"
                            + "    <jar href='spring-core.jar'/>\n"
                            + "    <jar href='aopalliance.jar'/>\n"
                            + "    <jar href='stream-supporting-httpinvoker.jar'/>\n"
                            + "    <jar href='commons-codec.jar'/>\n"
                            + "    <jar href='commons-httpclient.jar'/>\n"
                            + "    <jar href='commons-io.jar'/>\n"
                            + "    <jar href='commons-lang.jar'/>\n"
                            + "    <jar href='commons-logging.jar'/>\n"
                            + "    <extension name='Bouncy Castle Crypto Provider' href='cifex/bouncycastle.jnlp'/>\n"
                            + "  </resources>\n"
                            + "  <application-desc main-class='${main-class}'>\n"
                            + "    <argument>${service-URL}</argument>\n"
                            + "    <argument>${session-id}</argument>\n"
                            + "  </application-desc>\n" + "</jnlp>\n");

    protected final static String RECIPIENTS_FIELD_NAME = "email-addresses";

    protected final static String COMMENT_FIELD_NAME = "upload-comment";

    protected static final long MB = 1024 * 1024;

    protected IExtendedCIFEXRPCService service;

    protected String getURLForEmail(final HttpServletRequest request)
    {
        return HttpUtils.getURLForEmail(request, domainModel.getBusinessContext());
    }

    protected String createBaseURL(final HttpServletRequest request)
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

    protected String createServiceURL(final HttpServletRequest request)
    {
        String baseURL = HttpUtils.getBasicURL(request);
        return baseURL + "/cifex/rpc-service";
    }

    protected AbstractFileUploadDownloadServlet()
    {
    }

    // For unit tests
    protected AbstractFileUploadDownloadServlet(IExtendedCIFEXRPCService service,
            IDomainModel domainModel)
    {
        this.service = service;
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
            service = (IExtendedCIFEXRPCService) context.getBean("rpc-service");
        } catch (final Exception ex)
        {
            notificationLog.fatal("Failure during file service servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }

    //
    // HttpServlet
    //

    @Override
    // @Protected
    public void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, InvalidSessionException
    {
        UserDTO user = getUserDTO(request); // Throws exception if session is invalid
        String url = getURLForEmail(request);
        String sessionID = service.createSession(user, url);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Start file " + getOperationName() + " session with ID " + sessionID);
        }

        response.setContentType("application/x-java-jnlp-file");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
        Template template = JNLP_TEMPLATE.createFreshCopy();
        template.attemptToBind("title", getTitle());
        template.attemptToBind("description", getDescription());
        template.attemptToBind("base-URL", createBaseURL(request));
        template.attemptToBind("main-class", getMainClassName());
        template.attemptToBind("service-URL", createServiceURL(request));
        template.attemptToBind("session-id", sessionID);
        writer.print(template.createText());
        writer.close();
    }

    abstract protected String getMainClassName();
    
    abstract protected String getOperationName();

    abstract protected String getTitle();

    protected String getDescription()
    {
        return getTitle();
    }

}
