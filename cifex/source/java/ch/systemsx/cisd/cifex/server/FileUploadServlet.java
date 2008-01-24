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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FileUploadServlet extends HttpServlet implements Controller
{
    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, FileUploadServlet.class);
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, FileUploadServlet.class);

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
        ServletFileUpload upload = new ServletFileUpload();
        try
        {
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext())
            {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField())
                {
                    System.out.println("Form field " + name + " with value " + Streams.asString(stream) + " detected.");
                } else
                {
                    System.out.println("File field " + name + " with file name " + item.getName() + " detected.");
                    // Process the input stream
                }
            }
        } catch (FileUploadException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

}
