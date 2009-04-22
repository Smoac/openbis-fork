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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.Message;

/**
 * An {@link AbstractCommandController} extension for uploading files.
 * <p>
 * This can handle multiple files. When uploading is finished and successful, uploaded files are
 * available as session attribute of type {@link UploadedFilesBean}. The key to access this session
 * attribute must be defined in a form field named <code>sessionKey</code>.
 * </p>
 * <p>
 * This service is synchronized on the session object to serialize parallel invocations from the
 * same client. The <i>HTTP</i> response returns an empty string or <code>null</code> if the upload
 * was successful and is finished. Otherwise it returns a {@link Message} as <i>XML</i> string in
 * case of exception.
 * </p>
 * <p>
 * <i>URL</i> mappings are: <code>/upload</code> and <code>/openbis/upload</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
@Controller
@RequestMapping(
    { "/upload", "/openbis/upload" })
public final class UploadServiceServlet extends AbstractCommandController
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, UploadServiceServlet.class);

    public UploadServiceServlet()
    {
        super(UploadedFilesBean.class);
        setSynchronizeOnSession(true);
        setRequireSession(true);
    }

    @SuppressWarnings("unchecked")
    private final static Iterator<String> cast(final Iterator iterator)
    {
        return iterator;
    }

    private final void sendResponse(final HttpServletResponse response, final String textOrNull)
    {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        if (textOrNull != null)
        {
            try
            {
                response.getWriter().write(textOrNull);
            } catch (final IOException ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
        }
    }

    //
    // AbstractCommandController
    //

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
    {
        try
        {
            return super.handleRequestInternal(request, response);
        } catch (final Throwable th)
        {
            operationLog.error("Error handling request.", th);
            if (th instanceof Error)
            {
                throw (Error) th;
            } else
            {
                String msg = th.getMessage();
                if (StringUtils.isBlank(msg))
                {
                    msg = String.format("Error handling request: %s.", th.getClass().getName());
                }
                sendResponse(response, Message.createErrorMessage(msg).toXml());
                return null;
            }
        }
    }

    @Override
    protected final ModelAndView handle(final HttpServletRequest request,
            final HttpServletResponse response, final Object command, final BindException errors)
            throws Exception
    {
        if (request instanceof AbstractMultipartHttpServletRequest)
        {
            // We must have a session reaching this point. See the constructor where we set
            // 'setRequireSession(true)'.
            final HttpSession session = request.getSession(false);
            assert session != null : "Session must be specified.";
            final AbstractMultipartHttpServletRequest multipartRequest =
                    (AbstractMultipartHttpServletRequest) request;
            final String sessionKeysNumberParameter = request.getParameter("sessionKeysNumber");
            if (sessionKeysNumberParameter == null
                    || Integer.parseInt(sessionKeysNumberParameter) < 1)
            {
                throw new ServletException(
                        "No form field 'sessionKeysNumber' could be found in the transmitted form.");
            }
            List<String> sessionKeys = new ArrayList<String>();
            for (int i = 0; i < Integer.parseInt(sessionKeysNumberParameter); i++)
            {
                String sessionKey = request.getParameter("sessionKey_" + i);
                if (sessionKey == null)
                {
                    throw new ServletException("No field 'sessionKey_" + i
                            + "' could be found in the transmitted form.");
                }
                sessionKeys.add(sessionKey);
            }
            boolean atLeastOneFileUploaded = false;
            for (String sessionKey : sessionKeys)
            {
                final UploadedFilesBean uploadedFiles = new UploadedFilesBean();
                for (final Iterator<String> iterator = cast(multipartRequest.getFileNames()); iterator
                        .hasNext(); /**/)
                {
                    final String fileName = iterator.next();
                    if (fileName.startsWith(sessionKey))
                    {
                        final MultipartFile multipartFile = multipartRequest.getFile(fileName);
                        if (multipartFile.isEmpty() == false)
                        {
                            uploadedFiles.addMultipartFile(multipartFile);
                            atLeastOneFileUploaded = true;
                        }
                    }
                }
                if (atLeastOneFileUploaded == false)
                {
                    throw UserFailureException.fromTemplate("No file has been uploaded or "
                            + "the chosen files have no content.");
                }
                session.setAttribute(sessionKey, uploadedFiles);
            }
            sendResponse(response, null);
        }
        return null;
    }
}
