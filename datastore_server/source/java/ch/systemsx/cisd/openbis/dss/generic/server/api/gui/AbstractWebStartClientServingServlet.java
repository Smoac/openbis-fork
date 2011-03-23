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

package ch.systemsx.cisd.openbis.dss.generic.server.api.gui;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.cisd.cifex.rpc.server.IExtendedCIFEXRPCService;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet;
import ch.systemsx.cisd.openbis.dss.generic.server.Utils;

/**
 * A servlet the generates the JNLP document for serving a web-start client.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractWebStartClientServingServlet extends AbstractDatasetDownloadServlet
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

    /**
     * A utility class for dealing with the parameters required for initializing the web start
     * client
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class RequestParams
    {
        private final String sessionId;

        public RequestParams(HttpServletRequest request)
        {
            sessionId = getParam(request, Utils.SESSION_ID_PARAM);
        }

        private static String getParam(final HttpServletRequest request, String paramName)
        {
            String value = request.getParameter(paramName);
            if (value == null)
            {
                throw new UserFailureException("no value for the parameter " + paramName
                        + " found in the URL");
            }
            return value;
        }

        public String getSessionId()
        {
            return sessionId;
        }

    }

    protected String createBaseURL(final HttpServletRequest request)
    {
        StringBuffer url = request.getRequestURL();
        if (url.indexOf("localhost:8888") > 0)
        {
            url = url.append("/");
        } else
        {
            url = url.append("/");
        }
        return url.toString();
    }

    protected String createServiceURL(final HttpServletRequest request)
    {
        StringBuffer url = request.getRequestURL();
        if (url.indexOf("localhost:8888") > 0)
        {
            url = url.append("/");
        } else
        {
            url = url.append("/");
        }
        return url.toString();
    }

    protected AbstractWebStartClientServingServlet()
    {
    }

    // For unit tests
    protected AbstractWebStartClientServingServlet(IExtendedCIFEXRPCService service,
            IDomainModel domainModel)
    {
    }

    //
    // HttpServlet
    //
    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, InvalidSessionException
    {
        RequestParams params = new RequestParams(request);
        String sessionID = params.getSessionId();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Serving request for web start client " + getClientName()
                    + " session with ID " + sessionID);
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

    abstract protected String getClientName();

    abstract protected String getTitle();

    protected String getDescription()
    {
        return getTitle();
    }

}
