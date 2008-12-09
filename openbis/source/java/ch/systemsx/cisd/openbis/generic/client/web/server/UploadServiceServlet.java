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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * An {@link AbstractCommandController} extension for uploading files.
 * <p>
 * This can handle multiple files. When uploading is finished and successful, uploaded files are
 * available as session attribute of type {@link UploadedFilesBean}. The key to access this session
 * attribute must be defined in a form field named <code>sessionKey</code>.
 * </p>
 * <p>
 * This service is synchronized on the session object to serialize parallel invocations from the
 * same client. The <i>HTTP</i> response returns an empty string or <code>null</code> if the
 * upload was successful and is finished.
 * </p>
 * <p>
 * <i>URL</i> mappings are: <code>/upload</code> and <code>/genericopenbis/upload</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
@Controller
@RequestMapping(
    { "/upload", "/genericopenbis/upload" })
public final class UploadServiceServlet extends AbstractCommandController
{
    public UploadServiceServlet()
    {
        super(UploadedFilesBean.class);
        setSynchronizeOnSession(true);
    }

    @SuppressWarnings("unchecked")
    private final static Iterator<String> cast(final Iterator iterator)
    {
        return iterator;
    }

    //
    // AbstractCommandController
    //

    // TODO 2008-12-09, Christian Ribeaud: Exception handling. See HandlerExceptionResolver and/or
    // HandlerInterceptor.
    @Override
    protected final ModelAndView handle(final HttpServletRequest request,
            final HttpServletResponse response, final Object command, final BindException errors)
            throws Exception
    {
        assert request instanceof DefaultMultipartHttpServletRequest : "HttpServletRequest not an instance "
                + "of DefaultMultipartHttpServletRequest.";
        final DefaultMultipartHttpServletRequest multipartRequest =
                (DefaultMultipartHttpServletRequest) request;
        final UploadedFilesBean uploadedFiles = (UploadedFilesBean) command;
        final String sessionKey = uploadedFiles.getSessionKey();
        if (sessionKey == null)
        {
            throw new ServletException(
                    "No form field 'sessionKey' could be found in the transmitted form.");
        }
        request.getSession(false).setAttribute(sessionKey, uploadedFiles);
        for (final Iterator<String> iterator = cast(multipartRequest.getFileNames()); iterator
                .hasNext(); /**/)
        {
            final String fileName = iterator.next();
            final MultipartFile multipartFile = multipartRequest.getFile(fileName);
            if (multipartFile.isEmpty() == false)
            {
                uploadedFiles.addMultipartFile(multipartFile);
            }
        }
        if (uploadedFiles.size() == 0)
        {
            throw UserFailureException.fromTemplate("No file has been uploaded, that is, "
                    + "the chosen file(s) has no content.");
        }
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        return null;
    }
}
