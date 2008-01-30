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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * Servlet for uploading file into Cifex.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileUploadServlet extends AbstractCIFEXServiceServlet
{
    private static final String MAX_UPLOAD_SIZE = "max-upload-size";

    private static final long serialVersionUID = 1L;

    private final static String RECIPIENTS_FIELD_NAME = "email-addresses";

    /**
     * The maximum allow upload size (in megabytes).
     */
    private long maxUploadSizeInMegabytes;

    private final long getMaxUploadSizeInMegabytes()
    {
        final String value = serviceProperties.get(MAX_UPLOAD_SIZE);
        long longValue = -1;
        if (StringUtils.isNotBlank(value))
        {
            try
            {
                longValue = Long.parseLong(value);
            } catch (final NumberFormatException e)
            {
            }
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Maximum upload size set to %d megabytes (-1 means no limit).", longValue));
        }
        return longValue;
    }

    @Override
    protected final void postInitialization()
    {
        maxUploadSizeInMegabytes = getMaxUploadSizeInMegabytes();
    }

    @Override
    protected final void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        operationLog.info("Uploading ...");
        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart == false)
        {
            operationLog.warn("Request is not a multipart content file upload: " + request.getRequestURI());
            return;
        }
        try
        {
            List<FileDTO> files = new ArrayList<FileDTO>();
            List<String> users = new ArrayList<String>();
            final UserDTO requestUser = extractEmailsAndUploadFiles(request, files, users);
            String url = HttpUtils.getBasicURL(request);
            IFileManager fileManager = domainModel.getFileManager();
            final List<String> invalidEmailAddresses = fileManager.shareFilesWith(url, requestUser, users, files);
            
            operationLog.info("Uploading finished.");
            response.setContentType("text/plain");
            final PrintWriter writer = response.getWriter();
            writer.write("Upload finished.\n");
            if (invalidEmailAddresses.isEmpty() == false)
            {
                writer.write("Invalid email addresses found: ");
                for (String email : invalidEmailAddresses)
                {
                    writer.write(email);
                    writer.write(' ');
                }
            }
            writer.flush();
        } catch (final Exception ex)
        {
            operationLog.error("Could not process multipart content.", ex);
            sendErrorMessage(response, ex);
        }
    }

    private UserDTO extractEmailsAndUploadFiles(final HttpServletRequest request, List<FileDTO> files, List<String> users)
            throws FileUploadException, IOException
    {
        final UserDTO user = getUserDTO(request);
        final ServletFileUpload upload = new ServletFileUpload();
        IFileManager fileManager = domainModel.getFileManager();
        // Sets the maximum allowed size of a complete request in bytes.
        upload.setSizeMax(maxUploadSizeInMegabytes * FileUtils.ONE_MB);
        final FileItemIterator iter = upload.getItemIterator(request);
        while (iter.hasNext())
        {
            final FileItemStream item = iter.next();
            operationLog.info("Handle field '" + item.getFieldName() + "' with file: " + item.getName());
            final InputStream stream = item.openStream();
            if (item.isFormField() == false)
            {
                final String fileName = extractFileName(item);
                // Blank file name are empty file fields.
                if (StringUtils.isNotBlank(fileName))
                {
                    files.add(fileManager.saveFile(user, fileName, item.getContentType(), stream));
                }
            } else
            {
                if (item.getFieldName().equals(RECIPIENTS_FIELD_NAME))
                {
                    StringTokenizer stringTokenizer = new StringTokenizer(Streams.asString(stream));
                    while (stringTokenizer.hasMoreTokens())
                    {
                        users.add(stringTokenizer.nextToken());
                    }
                }
            }
        }
        return user;
    }
    
    private String extractFileName(FileItemStream item)
    {
        String fileName = item.getName().replace('\\', '/');
        int indexOfLastPathSeparator = fileName.lastIndexOf('/');
        if (indexOfLastPathSeparator >= 0)
        {
            fileName = fileName.substring(indexOfLastPathSeparator + 1);
        }
        return fileName;
    }
}
