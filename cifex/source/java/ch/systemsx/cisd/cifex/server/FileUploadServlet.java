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

package ch.systemsx.cisd.cifex.server;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Servlet for uploading file into Cifex.
 *
 * @author Franz-Josef Elmer
 */
public class FileUploadServlet extends HttpServlet implements Controller, InitializingBean, ServletConfigAware
{
    private static final long serialVersionUID = 1L;
    
    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, FileUploadServlet.class);
    
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, FileUploadServlet.class);
    
    private ServletConfig servletConfig;

    private String beanName;

    private IDomainModel domainModel;

    //
    // InitializingBean
    //

    /**
     * Note that {@link #setServletConfig(ServletConfig)} gets called before this method.
     */
    public final void afterPropertiesSet() throws Exception
    {
        LogInitializer.init();
        if (operationLog.isTraceEnabled())
        {
            final String message =
                    "All the properties have been set for bean '" + beanName + "'. Time to initialize this servlet.";
            operationLog.trace(message);
        }
        init(servletConfig);
    }

    //
    // ServletConfigAware
    //

    public final void setServletConfig(final ServletConfig servletConfig)
    {
        assert servletConfig != null;
        if (operationLog.isTraceEnabled())
        {
            final String message = "Setting servlet config for class '" + getClass().getSimpleName() + "'.";
            operationLog.trace(message);
        }
        this.servletConfig = servletConfig;
    }

    @Override
    public final void init(final ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            ServletContext servletContext = config.getServletContext();
            final BeanFactory context = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
            domainModel = (IDomainModel) context.getBean("domain-model");
            operationLog.info("File upload servlet successfully initialized.");
        } catch (final Exception ex)
        {
            notificationLog.fatal("Failure during file upload servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        doPost(request, response);
        return null;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart == false)
        {
            operationLog.warn("Request is not a multipart content file upload: " + request.getRequestURI());
            return;
        }
        UserDTO user = (UserDTO) request.getSession(false).getAttribute(CIFEXServiceImpl.SESSION_NAME);
        ServletFileUpload upload = new ServletFileUpload();
        try
        {
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext())
            {
                FileItemStream item = iter.next();
                InputStream stream = item.openStream();
                if (item.isFormField() == false)
                {
                    domainModel.getFileManager().saveFile(user, item.getName(), stream);
                } else
                {
//                    System.out.println("Form field " + item.getFieldName() + " with value " + Streams.asString(stream) + " detected.");
                }
            }
        } catch (FileUploadException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

}
