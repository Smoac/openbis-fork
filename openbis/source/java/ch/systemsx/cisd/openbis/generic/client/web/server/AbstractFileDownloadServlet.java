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
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Superclass for servlets supporting file download.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractFileDownloadServlet extends AbstractController
{
    abstract protected FileContent getFileContent(final HttpServletRequest request)
            throws Exception;

    protected String getSessionToken(final HttpServletRequest request)
    {
        return ((Session) request.getSession(false).getAttribute(
                SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY)).getSessionToken();
    }

    protected void writeResponse(final HttpServletResponse response, final String value)
            throws IOException
    {
        final PrintWriter writer = response.getWriter();
        writer.write(value);
        writer.flush();
        writer.close();
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
    {
        try
        {
            FileContent fileContent = getFileContent(request);
            if (fileContent != null)
            {
                response.setContentLength(fileContent.getContent().length);
                response.setHeader("Content-Disposition", "attachment; filename="
                        + fileContent.getFileName());
                final ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(fileContent.getContent());
                outputStream.flush();
                outputStream.close();
            }
        } catch (final UserFailureException ex)
        {
            writeResponse(response, ex.getMessage());
        }
        return null;
    }

    protected static class FileContent
    {
        private final byte[] content;

        private final String fileName;

        public FileContent(byte[] content, String fileName)
        {
            this.content = content;
            this.fileName = fileName;
        }

        public byte[] getContent()
        {
            return content;
        }

        public String getFileName()
        {
            return fileName;
        }
    }
}
