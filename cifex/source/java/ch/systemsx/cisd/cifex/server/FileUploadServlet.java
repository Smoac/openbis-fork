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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.client.dto.Message;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.CollectionUtils;

/**
 * Servlet for uploading file into Cifex.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileUploadServlet extends AbstractCIFEXServiceServlet
{
    /*
     * Keep in mind that this constant is used by <code>FileUploadWidget</code> to check if upload was successful, so
     * if you change the value of the constant here it should also be changed in the widget.
     */
    private static final String UPLOAD_FINISHED = "Upload finished.\n";

    private static final String MAX_UPLOAD_SIZE = "max-upload-size";

    private static final long serialVersionUID = 1L;

    private final static String RECIPIENTS_FIELD_NAME = "email-addresses";
    
    private final static String COMMENT_FIELD_NAME = "upload-comment";

    /**
     * The maximum allow upload size (in bytes).
     */
    private long maxUploadSizeInBytes;

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
        maxUploadSizeInBytes = getMaxUploadSizeInMegabytes() * FileUtils.ONE_MB;
    }

    @Override
    protected final void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, InvalidSessionException
    {
        final UserDTO requestUser = getUserDTO(request); // Throws exception if session is not valid.
        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart == false)
        {
            final String msg =
                    String.format("Protocol error : request '%s' is not a multipart content file upload.", request
                            .getRequestURI());
            operationLog.error(msg);
            throw new UserFailureException(msg);
        }
        final BlockingQueue<Message> uploadMsgQueue =
                (BlockingQueue<Message>) request.getSession().getAttribute(CIFEXServiceImpl.UPLOAD_MSG_QUEUE);
        try
        {
            final BlockingQueue<String[]> uploadQueue =
                    (BlockingQueue<String[]>) request.getSession().getAttribute(CIFEXServiceImpl.UPLOAD_QUEUE);
            final String[] filenamesToUpload = uploadQueue.poll(1, TimeUnit.SECONDS);
            if (filenamesToUpload == null)
            {
                final String msg =
                        String.format("Protocol error : no filenames registered for request '%s'.", request
                                .getRequestURI());
                operationLog.warn(msg);
                throw new UserFailureException(msg);
            }
            // Returns the length, in bytes, of the request body and made available by the input stream, or -1 if the
            // length is not known.
            final int contentLength = request.getContentLength();
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Request of user '%s' has a content length of %s.", requestUser
                        .getEmail(), FileUtils.byteCountToDisplaySize(contentLength)));
            }
            if (contentLength > maxUploadSizeInBytes)
            {
                final String msg =
                        String.format("Request size (%s) exceeds maximum permitted one (%s).", FileUtils
                                .byteCountToDisplaySize(contentLength), FileUtils
                                .byteCountToDisplaySize(maxUploadSizeInBytes));
                operationLog.error(msg);
                throw new FileUploadBase.SizeLimitExceededException(msg, contentLength, maxUploadSizeInBytes);
            }
            final List<FileDTO> files = new ArrayList<FileDTO>();
            final List<String> userEmails = new ArrayList<String>();
            final StringBuffer comment = new StringBuffer();
            extractEmailsAndUploadFilesAndComment(request, requestUser, filenamesToUpload, files, userEmails, comment);
            String url = HttpUtils.getBasicURL(request);
            IFileManager fileManager = domainModel.getFileManager();
            final List<String> invalidEmailAddresses = fileManager.shareFilesWith(url, requestUser, userEmails, files, comment.toString());
            response.setContentType("text/plain");
            final PrintWriter writer = response.getWriter();
            writer.write(UPLOAD_FINISHED);
            writer.flush();
            writer.close();
            if (invalidEmailAddresses.isEmpty() == false)
            {
                final String msg =
                    "Some email addresses are invalid: " + CollectionUtils.abbreviate(invalidEmailAddresses, 10);
                uploadMsgQueue.add(new Message(Message.WARNING, UPLOAD_FINISHED + msg));
            } else
            {
                uploadMsgQueue.add(new Message(Message.INFO, UPLOAD_FINISHED));
            }

        } catch (final Exception ex)
        {
            operationLog.error("Could not process multipart content.", ex);
            final String msg = getErrorMessage(ex);
            sendErrorMessage(response, msg);
            uploadMsgQueue.add(new Message(Message.ERROR, msg));
        }
    }

    private void extractEmailsAndUploadFilesAndComment(final HttpServletRequest request, UserDTO requestUser,
            String[] pathnamesToUpload, List<FileDTO> files, List<String> userEmails, StringBuffer comment) throws FileUploadException,
            IOException
    {
        final ServletFileUpload upload = new ServletFileUpload();
        IFileManager fileManager = domainModel.getFileManager();
        // Sets the maximum allowed size of a complete request in bytes.
        upload.setSizeMax(maxUploadSizeInBytes);
        final FileItemIterator iter = upload.getItemIterator(request);
        int fileIndex = 0;
        while (iter.hasNext())
        {
            final String pathnameToUpload =
                    (fileIndex < pathnamesToUpload.length) ? pathnamesToUpload[fileIndex] : null;
            ++fileIndex;
            final String filenameToUpload = FilenameUtils.getName(pathnameToUpload);
            final FileItemStream item = iter.next();
            final InputStream stream = item.openStream();
            if (item.isFormField() == false)
            {
                final String filenameInStream = FilenameUtils.getName(item.getName());
                if (StringUtils.isBlank(filenameToUpload))
                {
                    if (StringUtils.isBlank(filenameInStream))
                    {
                        continue;
                    } else
                    {
                        throw UserFailureException.fromTemplate("Unexpected file '%s'.", filenameInStream);
                    }
                }
                if (filenameToUpload.equals(filenameInStream) == false)
                {
                    fileManager.throwExceptionOnFileDoesNotExist(pathnameToUpload);
                }
                // Blank file name are empty file fields.
                if (StringUtils.isNotBlank(filenameInStream))
                {
                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug(String.format("Handle field '%s' with file '%s'.", item.getFieldName(), item
                                .getName()));
                    }
                    final FileDTO file =
                            fileManager.saveFile(requestUser, filenameInStream, item.getContentType(), stream);
                    files.add(file);
                } else
                {
                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug(String.format("No file specified in field '%s'.", item.getFieldName()));
                    }
                }
            } else
            {
                if (item.getFieldName().equals(RECIPIENTS_FIELD_NAME))
                {
                    final StringTokenizer stringTokenizer = new StringTokenizer(Streams.asString(stream));
                    while (stringTokenizer.hasMoreTokens())
                    {
                        userEmails.add(stringTokenizer.nextToken());
                    }
                }
                if(item.getFieldName().equals(COMMENT_FIELD_NAME)){
                    comment.append(Streams.asString(stream));
                }
            }
        }
    }
}
