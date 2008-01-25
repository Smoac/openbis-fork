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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * Servlet for uploading file into Cifex.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileUploadServlet extends AbstractCIFEXServiceServlet
{
    private static final long serialVersionUID = 1L;

    //
    // AbstractCIFEXServiceServlet
    //

    @Override
    protected final void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart == false)
        {
            operationLog.warn("Request is not a multipart content file upload: " + request.getRequestURI());
            return;
        }
        try
        {
            final UserDTO user = getUserDTO(request);
            final FileItemFactory factory = new DiskFileItemFactory();
            final ServletFileUpload upload = new ServletFileUpload(factory);
            final List<FileItem> items = upload.parseRequest(request);
            for (final FileItem item : items)
            {
                final InputStream stream = item.getInputStream();
                final String fileName = item.getName();
                if (item.isFormField() == false)
                {
                    if (StringUtils.isNotBlank(fileName))
                    {
                        domainModel.getFileManager().saveFile(user, fileName, stream);
                    }
                } else
                {
                    registerTemporaryUsers(item.getString());
                }
            }
        } catch (final Exception ex)
        {
            sendErrorMessage(response, ex);
        }
    }

    private void registerTemporaryUsers(final String temporaryUserList)
    {
        System.out.println(temporaryUserList + "**********************");
        // domainModel.getUserManager().tryToFindUser(email);
    }
}
