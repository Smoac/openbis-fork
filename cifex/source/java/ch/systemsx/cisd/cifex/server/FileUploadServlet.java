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
import java.util.ArrayList;
import java.util.List;

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

import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.util.FileUploadFeedbackProvider;
import ch.systemsx.cisd.cifex.server.util.FilenameUtilities;
import ch.systemsx.cisd.cifex.shared.basic.dto.Message;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * Servlet for uploading file into Cifex.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileUploadServlet extends AbstractFileUploadServlet
{
    /*
     * Keep in mind that this constant is used by <code>FileUploadWidget</code> to check if upload
     * was successful, so if you change the value of the constant here it should also be changed in
     * the widget.
     */
    private static final long serialVersionUID = 1L;

    private static final String UPLOAD_FINISHED = "Upload finished.\n";

    @Override
    protected final void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, InvalidSessionException
    {
        final UserDTO requestUser = getUserDTO(request); // Throws exception if session is not
        // valid.
        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart == false)
        {
            final String msg =
                    String
                            .format(
                                    "Protocol error : request '%s' is not a multipart content file upload.",
                                    request.getRequestURI());
            operationLog.error(msg);
            throw new UserFailureException(msg);
        }
        final FileUploadFeedbackProvider feedbackProvider =
                (FileUploadFeedbackProvider) request.getSession().getAttribute(
                        CIFEXServiceImpl.UPLOAD_FEEDBACK_QUEUE);
        try
        {
            final String[] filenamesToUpload =
                    (String[]) request.getSession().getAttribute(CIFEXServiceImpl.FILES_TO_UPLOAD);
            if (filenamesToUpload == null)
            {
                final String msg =
                        String.format("Protocol error: no filenames registered for request '%s'.",
                                request.getRequestURI());
                operationLog.warn(msg);
                throw new UserFailureException(msg);
            }
            // Returns the length, in bytes, of the request body and made available by the input
            // stream, or -1 if the
            // length is not known.
            final int contentLength = request.getContentLength();
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Request of user '%s' has a content length of %s.", requestUser.getEmail(),
                        FileUtils.byteCountToDisplaySize(contentLength)));
            }
            long maxUploadSizeInBytes = getMaxUploadSize(requestUser);
            if (contentLength > maxUploadSizeInBytes)
            {
                final String msg =
                        String.format("Request size (%s) exceeds maximum permitted one (%s).",
                                FileUtils.byteCountToDisplaySize(contentLength), FileUtils
                                        .byteCountToDisplaySize(maxUploadSizeInBytes));
                operationLog.error(msg);
                throw new FileUploadBase.SizeLimitExceededException(msg, contentLength,
                        maxUploadSizeInBytes);
            }
            final List<FileDTO> files = new ArrayList<FileDTO>();
            final List<String> userIdentifier = new ArrayList<String>();
            final StringBuffer comment = new StringBuffer();
            extractEmailsAndUploadFilesAndComment(request, requestUser, filenamesToUpload, files,
                    userIdentifier, comment);
            final String url = getURLForEmail(request);
            final IFileManager fileManager = domainModel.getFileManager();
            final List<String> invalidUserIdentifiers =
                    fileManager.shareFilesWith(url, requestUser, userIdentifier, files, comment
                            .toString());
            if (invalidUserIdentifiers.isEmpty() == false)
            {
                final String msg =
                        "Some user identifiers are invalid:<br>"
                                + CollectionUtils.abbreviate(invalidUserIdentifiers, 10);
                feedbackProvider.setMessage(new Message(Message.WARNING, UPLOAD_FINISHED + msg));
            } else
            {
                feedbackProvider.setFileUploadFinished();
            }

        } catch (final Exception ex)
        {
            operationLog.error("Could not process multipart content.", ex);
            final String msg = getErrorMessage(ex);
            feedbackProvider.setMessage(new Message(Message.ERROR, msg));
        }
    }

    private final void extractEmailsAndUploadFilesAndComment(final HttpServletRequest request,
            final UserDTO requestUser, final String[] pathnamesToUpload, final List<FileDTO> files,
            final List<String> userIdentifier, final StringBuffer comment)
            throws FileUploadException, IOException
    {
        final ServletFileUpload upload = new ServletFileUpload();
        upload.setProgressListener(new FileUploadProgressListener(request.getSession(false),
                pathnamesToUpload));
        final IFileManager fileManager = domainModel.getFileManager();
        // Sets the maximum allowed size of a complete request in bytes.
        upload.setSizeMax(getMaxUploadSize(requestUser));
        final FileItemIterator iter = upload.getItemIterator(request);
        for (int fileIndex = 0; iter.hasNext(); fileIndex++)
        {
            final String pathnameToUpload =
                    fileIndex < pathnamesToUpload.length ? pathnamesToUpload[fileIndex] : null;
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
                        throw UserFailureException.fromTemplate("Unexpected file '%s'.",
                                filenameInStream);
                    }
                }
                // Note: this is quite a hack. The first condition can be false when there are
                // special characters in the name, thus we add the check for stream.available().
                if (filenameToUpload.equals(filenameInStream) == false && stream.available() == 0)
                {
                    fileManager.throwExceptionOnFileDoesNotExist(pathnameToUpload);
                }
                // Blank file name are empty file fields.
                if (StringUtils.isNotBlank(filenameToUpload))
                {
                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug(String.format("Handle field '%s' with file '%s'.", item
                                .getFieldName(), item.getName()));
                    }
                    final String fileName =
                            FilenameUtilities.ensureMaximumSize(filenameToUpload,
                                    MAX_FILENAME_LENGTH);
                    final String contentType = item.getContentType();
                    final FileDTO file =
                            fileManager.saveFile(requestUser, fileName, comment.toString(),
                                    contentType, stream);
                    files.add(file);
                } else
                {
                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug(String.format("No file specified in field '%s'.", item
                                .getFieldName()));
                    }
                }
            } else
            {
                if (item.getFieldName().equals(RECIPIENTS_FIELD_NAME))
                {
                    userIdentifier.addAll(StringUtilities.tokenize(Streams.asString(stream)));
                }
                if (item.getFieldName().equals(COMMENT_FIELD_NAME))
                {
                    comment.append(Streams.asString(stream));
                    for (final FileDTO file : files)
                    {
                        file.setComment(comment.toString());
                        fileManager.updateFile(file);
                    }
                }
            }
        }
    }
}
