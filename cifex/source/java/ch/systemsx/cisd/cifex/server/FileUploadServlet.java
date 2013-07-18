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
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.rpc.QuotaExceededException;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.util.FileUploadFeedbackProvider;
import ch.systemsx.cisd.cifex.server.util.FilenameUtilities;
import ch.systemsx.cisd.cifex.shared.basic.dto.Message;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Servlet for uploading file into Cifex.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileUploadServlet extends AbstractFileUploadDownloadServlet
{
    /*
     * Keep in mind that this constant is used by <code>FileUploadWidget</code> to check if upload
     * was successful, so if you change the value of the constant here it should also be changed in
     * the widget.
     */
    private static final long serialVersionUID = 1L;

    private static final String UPLOAD_FINISHED = "Upload finished.\n";

    @Override
    // @Protected
    public void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, InvalidSessionException
    {
        // Do nothing. This servlet uses POST
    }

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
                        AbstractCIFEXService.UPLOAD_FEEDBACK_QUEUE);
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
            checkQuota(requestUser, filenamesToUpload.length, contentLength);
            final List<FileDTO> files = new ArrayList<FileDTO>();
            final List<String> userIdentifiers = new ArrayList<String>();
            final StringBuffer comment = new StringBuffer();
            new FormDataExtractor(request, requestUser, filenamesToUpload, files, userIdentifiers,
                    comment).execute();
            final String url = getURLForEmail(request);
            final IFileManager fileManager = domainModel.getFileManager();
            final List<String> invalidUserIdentifiers =
                    fileManager.shareFilesWith(url, requestUser, userIdentifiers, files, comment
                            .toString(), domainModel.getBusinessContext().getUserActionLogHttp());
            if (invalidUserIdentifiers.isEmpty() == false)
            {
                final String msg =
                        "Some user identifiers are invalid:<br>"
                                + CollectionUtils.abbreviate(invalidUserIdentifiers, 10);
                feedbackProvider
                        .setMessage(new Message(Message.Type.WARNING, UPLOAD_FINISHED + msg));
            } else
            {
                feedbackProvider.setFileUploadFinished();
            }

        } catch (final Exception ex)
        {
            operationLog.error("Could not process multipart content.", ex);
            String msg = getErrorMessage(ex);
            if (msg.contains("value too long"))
            {
                msg = "Cannot upload file(s): length constraint in database exceeded.";
            }
            feedbackProvider.setMessage(new Message(Message.Type.ERROR, msg));
        }
    }

    private void checkQuota(final UserDTO requestUser, int count, long fileSize)
    {
        domainModel.getUserManager().refreshQuotaInformation(requestUser);
        final boolean countOK =
                (requestUser.getMaxFileCountPerQuotaGroup() == null)
                        || (requestUser.getCurrentFileCount() + count <= requestUser
                                .getMaxFileCountPerQuotaGroup());
        final boolean sizeOK =
                (requestUser.getMaxFileSizePerQuotaGroupInMB() == null)
                        || (requestUser.getCurrentFileSize() + fileSize <= requestUser
                                .getMaxFileSizePerQuotaGroupInMB()
                                * MB);
        if ((countOK && sizeOK) == false)
        {
            final double currentFileSizeInMB = ((double) requestUser.getCurrentFileSize()) / MB;
            final QuotaExceededException exception =
                    new QuotaExceededException(requestUser.getMaxFileCountPerQuotaGroup(),
                            requestUser.getMaxFileSizePerQuotaGroupInMB(), requestUser
                                    .getCurrentFileCount(), currentFileSizeInMB);
            operationLog.error(exception.getMessage());
            throw exception;
        }
    }

    /**
     * Encapsulation of form data extraction.
     */
    private final class FormDataExtractor
    {

        private final HttpServletRequest request;

        private final UserDTO requestUser;

        private final String[] pathnamesToUpload;

        private final List<FileDTO> files;

        private final List<String> userIdentifier;

        private final StringBuffer comment;

        private final ServletFileUpload upload;

        private final List<String> formIndexedPathnamesAndNulls;

        private final FileUploadProgressListener progressListener;

        public FormDataExtractor(final HttpServletRequest request, final UserDTO requestUser,
                final String[] pathnamesToUpload, final List<FileDTO> files,
                final List<String> userIdentifier, final StringBuffer comment)
        {
            this.request = request;
            this.requestUser = requestUser;
            this.pathnamesToUpload = pathnamesToUpload;
            this.files = files;
            this.userIdentifier = userIdentifier;
            this.comment = comment;

            upload = new ServletFileUpload();

            formIndexedPathnamesAndNulls = new ArrayList<String>();
            // The progress listener needs a reference to the pathnames and nulls -- it uses the
            // list to figure out the names of files being updated.
            progressListener =
                    new FileUploadProgressListener(request.getSession(false),
                            formIndexedPathnamesAndNulls);

            upload.setProgressListener(progressListener);
        }

        /**
         * Loop over the POST parameters and extract the email addresses, the comment, and the
         * files. Cannot use getParameterValue because the data was encoded as "multipart/form-data"
         * to support file upload.
         */
        public void execute() throws FileUploadException, IOException
        {
            // A list that stores the pathnames specified in the form at the same index it appears
            // in the form. If the form parameter at a given index is not a pathname, store a null.
            final IFileManager fileManager = domainModel.getFileManager();
            final FileItemIterator iter = upload.getItemIterator(request);

            // Iterate over the form fields
            for (int fileIndex = 0; iter.hasNext();)
            {
                final FileItemStream item = iter.next();
                final InputStream stream = item.openStream();
                if (item.isFormField())
                {
                    // This is a simple form field, not a file
                    extractSimpleFieldData(item, stream);
                } else
                {
                    // This is a file
                    extractFileFieldData(item, stream, fileManager, fileIndex);
                    fileIndex++;
                }
            }

            // After processing the form data, set the comments on the files
            for (final FileDTO file : files)
            {
                file.setComment(comment.toString());
                fileManager.updateFile(file, requestUser);
            }
        }

        private void extractSimpleFieldData(final FileItemStream item, final InputStream stream)
                throws IOException
        {
            // Need to update the list of pathnames before doing anything else because the progress
            // listener needs the up-to-date list of parameters.
            formIndexedPathnamesAndNulls.add(null);
            String recipientsAsString = Streams.asString(stream);
            if (item.getFieldName().equals(RECIPIENTS_FIELD_NAME))
            {
                userIdentifier.addAll(Arrays.asList(recipientsAsString.split(",")));
            }
            if (item.getFieldName().equals(COMMENT_FIELD_NAME))
            {
                comment.append(recipientsAsString);
            }
        }

        private void extractFileFieldData(FileItemStream item, InputStream stream,
                IFileManager fileManager, int fileIndex) throws IOException
        {
            // Need to update the list of pathnames before doing anything else because the progress
            // listener needs the up-to-date list of parameters.
            final String pathnameToUpload =
                    fileIndex < pathnamesToUpload.length ? pathnamesToUpload[fileIndex] : null;
            final String filenameToUpload = FilenameUtils.getName(pathnameToUpload);
            formIndexedPathnamesAndNulls.add(filenameToUpload);

            final String filenameInStream = FilenameUtils.getName(item.getName());
            if (StringUtils.isBlank(filenameToUpload))
            {
                if (StringUtils.isBlank(filenameInStream))
                {
                    return;
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
                        FilenameUtilities.ensureMaximumSize(filenameToUpload, MAX_FILENAME_LENGTH);
                final String contentType = FilenameUtilities.getMimeType(item.getName());
                domainModel.getBusinessContext().getUserActionLogHttp().logUploadFileStart(
                        fileName, null, 0L);
                boolean success = false;
                FileDTO file = null;
                try
                {
                    file =
                            fileManager.saveFile(requestUser, fileName, comment.toString(),
                                    contentType, stream);
                    success = true;
                    files.add(file);
                } finally
                {
                    domainModel.getBusinessContext().getUserActionLogHttp().logUploadFileFinished(
                            fileName, file, success);
                }
            } else
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format("No file specified in field '%s'.", item
                            .getFieldName()));
                }
            }
        }

    }

    @Override
    protected String getMainClassName()
    {
        // Doesn't apply
        return null;
    }

    @Override
    protected String getOperationName()
    {
        // Doesn't apply
        return null;
    }

    @Override
    protected String getTitle()
    {
        // Doesn't apply
        return null;
    }

}
