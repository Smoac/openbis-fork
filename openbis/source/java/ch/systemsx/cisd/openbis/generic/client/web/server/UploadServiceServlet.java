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
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import ch.rinn.restrictions.Private;
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

    ISessionFilesSetter sessionFilesSetter;

    @Private
    UploadServiceServlet(ISessionFilesSetter sessionFilesSetter)
    {
        super(UploadedFilesBean.class);
        setSynchronizeOnSession(true);
        setRequireSession(true);
        this.sessionFilesSetter = sessionFilesSetter;
    }

    public UploadServiceServlet()
    {
        this(new SessionFilesSetter());
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
        if (request instanceof MultipartHttpServletRequest)
        {
            // We must have a session reaching this point. See the constructor where we set
            // 'setRequireSession(true)'.
            final HttpSession session = request.getSession(false);
            assert session != null : "Session must be specified.";
            final MultipartHttpServletRequest multipartRequest =
                    (MultipartHttpServletRequest) request;
            final String sessionKeysNumberParameter = request.getParameter("sessionKeysNumber");
            if (sessionKeysNumberParameter == null
                    || Integer.parseInt(sessionKeysNumberParameter) < 1)
            {
                throw new ServletException(
                        "No form field 'sessionKeysNumber' could be found in the transmitted form.");
            }
            boolean atLeastOneFileUploaded = false;
            for (String sessionKey : extractSessionKeys(request, sessionKeysNumberParameter))
            {
                // 
                boolean fileExtracted =
                        sessionFilesSetter.addFilesToSession(session, multipartRequest, sessionKey);
                atLeastOneFileUploaded = atLeastOneFileUploaded || fileExtracted;
            }
            if (atLeastOneFileUploaded == false)
            {
                throw UserFailureException.fromTemplate("No file has been uploaded or "
                        + "the chosen files have no content.");
            }
            sendResponse(response, null);
        }
        return null;
    }

    private static List<String> extractSessionKeys(final HttpServletRequest request,
            final String sessionKeysNumberParameter) throws ServletException
    {
        List<String> sessionKeys = new ArrayList<String>();
        for (int i = 0; i < Integer.parseInt(sessionKeysNumberParameter); i++)
        {
            String sessionKey = StringUtils.trim(request.getParameter("sessionKey_" + i));
            if (StringUtils.isBlank(sessionKey))
            {
                throw new ServletException("No field 'sessionKey_" + i
                        + "' could be found in the transmitted form.");
            }
            sessionKeys.add(sessionKey);
        }
        return sessionKeys;
    }

    @Private
    interface ISessionFilesSetter
    {
        /**
         * Adds extracted {@link UploadedFilesBean}s to the session.
         * 
         * @return <code>true</code> if at least one file has been found and added
         */
        public boolean addFilesToSession(final HttpSession session,
                final MultipartHttpServletRequest multipartRequest, String sessionKey);
    }

    @Private
    static class SessionFilesSetter implements ISessionFilesSetter
    {
        public boolean addFilesToSession(final HttpSession session,
                final MultipartHttpServletRequest multipartRequest, String sessionKey)
        {
            return addFilesToSessionUsingBean(session, multipartRequest, sessionKey,
                    new UploadedFilesBean());
        }

        @Private
        boolean addFilesToSessionUsingBean(final HttpSession session,
                final MultipartHttpServletRequest multipartRequest, String sessionKey,
                final UploadedFilesBean uploadedFiles)
        {
            assert StringUtils.isBlank(sessionKey) == false;
            boolean fileUploaded = false;
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
                        fileUploaded = true;
                    }
                }
            }
            session.setAttribute(sessionKey, uploadedFiles);
            return fileUploaded;
        }

    }
}
