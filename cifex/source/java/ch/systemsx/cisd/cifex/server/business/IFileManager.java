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

package ch.systemsx.cisd.cifex.server.business;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.cifex.rpc.FilePreregistrationDTO;
import ch.systemsx.cisd.cifex.server.business.dto.FileContent;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogAnnotation;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * A manager that proxies and handles access to {@link FileDTO}.
 * 
 * @author Christian Ribeaud
 */
public interface IFileManager
{

    /**
     * Returns the file DTO for given <var>fileId</var>.
     * 
     * @return The file DTO, or an information object containing an error message if no file object
     *         exists in the database or in the file store for this <var>fileId</var>.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public FileInformation getFileInformation(final long fileId);

    /**
     * Returns the file DTO for given <var>fileId</var> even if the file does not exist in the file
     * store.
     * 
     * @return The file DTO, or an information object containing an error message if no file object
     *         exists in the database or in the file store for this <var>fileId</var>.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public FileInformation getFileInformationFilestoreUnimportant(final long fileId);

    /**
     * Returns <code>true</code>, if the user given by <var>userDTO</var> is allowed access to file
     * <var>fileDTO</var>.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public boolean isAllowedAccess(final UserDTO userDTO, final FileDTO fileDTO);

    /**
     * Returns <code>true</code>, if the user given by <var>userDTO</var> is in control of file
     * <var>fileDTO</var>.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public boolean isControlling(final UserDTO userDTO, final FileDTO fileDTO);

    /** Returns all files */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<FileDTO> listFiles();

    /** Lists files for given <var>userId</var>. */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<FileDTO> listDownloadFiles(final long userId);

    /** Lists files directly or indirectly owned by user with given <var>userId</var>. */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<FileDTO> listOwnedFiles(final long userId);

    /**
     * Returns the content for the given <var>fileDTO</var>.
     * 
     * @return The file content.
     * @throws IllegalStateException If the given <var>fileDTO</var> does not exist on the file
     *             system.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public FileContent getFileContent(final FileDTO fileDTO) throws IllegalStateException;

    /**
     * Saves the data in the specified input stream to the file system and create a link in the
     * database with the given <var>fileName</var>.
     * <p>
     * This method will either succeed completely or not save anything.
     * 
     * @param user The owner of the file.
     * @param fileName The name of the file. May contain the full path.
     * @param comment The comment that the uploader has provided.
     * @param contentType Content type passed by the browser or <code>null</code> if not defined.
     * @param inputStream Input stream of file content.
     * @return file DTO with id.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public FileDTO saveFile(final UserDTO user, final String fileName, String comment,
            final String contentType, final InputStream inputStream);

    /**
     * Saves the data in the specified input stream to the file system and create a link in the
     * database with the given <var>fileName</var> and <var>fileSize</var>.
     * <p>
     * This method can succeed partially, that is: leave a file in the data base and data store that
     * can be used for resuming the save process.
     * 
     * @param user The owner of the file.
     * @param fileName The name of the file. May contain the full path.
     * @param comment The comment that the uploader has provided.
     * @param contentType Content type passed by the browser or <code>null</code> if not defined.
     * @param size The size of the content of the stream.
     * @param inputStream Input stream of file content.
     * @return file DTO with id.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public FileDTO saveFile(final UserDTO user, final String fileName, String comment,
            final String contentType, final long size, final InputStream inputStream);

    /**
     * Resumes saving the data in the specified input stream to the file system and create a link in
     * the database with the given <var>fileName</var> and <var>fileSize</var>.
     * <p>
     * This method can succeed partially, that is it can save data and then be interrupted, in which
     * case it can be called subsequently until the whole file has been uploaded.
     * 
     * @param user The owner of the file.
     * @param fileDTO The link of the file in the database.
     * @param file The file in the data store.
     * @param comment The comment that the uploader has provided.
     * @param startPos The start position of the resume. Must be smaller than
     *            <code>fileDTO.getSize()</code> or else an {@link IllegalArgumentException} will be
     *            thrown.
     * @param inputStream Input stream of file content.
     */
    public void resumeSaveFile(final UserDTO user, final FileDTO fileDTO, final File file,
            final String comment, final long startPos, final InputStream inputStream)
            throws IllegalArgumentException;

    /**
     * Registers specified file for the specified user and sends an e-mail to all specified
     * recipients.
     * 
     * @return a list of invalid (non-existent) email addresses. Can only be non-empty for temporary
     *         users.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public List<String> registerFileLinkAndInformRecipients(final UserDTO user,
            final String fileName, final String comment, final String contentType, final File file,
            int crc32Value, String[] recipients, String url, final IUserActionLog logOrNull);

    /**
     * Updates the file in the database specified by <var>fileDTO.getID()</var> regarding size
     * (<var>fileDTO.getSize()</var>) and CRC32 checksum (<var>fileDTO.getCrc32Value()</var>).
     * 
     * @param requestUser The user requesting the upload of the upload progress.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void updateUploadProgress(FileDTO fileDTO, UserDTO requestUser);

    /**
     * Returns the candidate for upload resuming for user <var>userId</var> of given
     * <var>fileName</var> with <var>completeSize</var>, if one is available, or <code>null</code>,
     * otherwise. If multiple candidates are available, the one with the largest already uploaded
     * size is returned.
     */
    public FileDTO tryGetUploadResumeCandidate(final long userId, final String fileName,
            final long completeSize);

    /**
     * Creates sharing links between the users specified by their e-mail addresses or user id (with
     * the prefix 'id:') and the specified files. For users specified with an Email address not
     * known a temporary account is created. Each user will be informed by an e-mail.
     * 
     * @param url URL for creating the links in the e-mails.
     * @param userIdentifiers An Identifier for the user, either a email address (which can be
     *            ambiguous) or the user code with the prefix 'id:'.
     * @return a list of invalid (non-existent) email addresses. Can only be non-empty for temporary
     *         users.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public List<String> shareFilesWith(String url, UserDTO requestUser,
            Collection<String> userIdentifiers, Collection<FileDTO> files, String comment,
            final IUserActionLog logOrNull) throws UserFailureException;

    /**
     * Deletes file with given <code>fileId</code> from database and file system.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void deleteFile(final FileDTO fileDTO);

    /**
     * Creates a file in the file store for <code>user</code> with basic name <var>fileName</var>.
     * This method will always return a new (not yet existing) file, if necessary, appending a
     * suffix.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public File createFile(final UserDTO user, final String fileName);

    /**
     * Creates a file in the file store for <code>user</code> with basic name <var>fileName</var>
     * and a link to it in the database. This method will always return a new (not yet existing)
     * file, if necessary, appending a suffix.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public PreCreatedFileDTO createFile(final UserDTO user, final FilePreregistrationDTO fileDTO,
            final String comment);

    /**
     * @throws UserFailureException indicating that <var>filename</var> does not exist and thus has
     *             not been saved.
     */
    public void throwExceptionOnFileDoesNotExist(final String fileName);

    /** Deletes expired files from database and file system. */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void deleteExpiredFiles(final IUserActionLog logOrNull);

    /**
     * Update the user data of the <var>fileId</var>.
     * 
     * @param fileId The id of the file to update.
     * @param name The new name of the file.
     * @param commentOrNull The new comment of the file.
     * @param expirationDate The new (requested) expiration date.
     * @param requestUser The user requesting the update of the file
     * @return The new expiration date.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public Date updateFileUserData(final long fileId, final String name,
            final String commentOrNull, final Date expirationDate, final UserDTO requestUser);

    /**
     * Update the file DTO.
     * 
     * @param file The file DTO to update.
     * @param requestUser The user requesting the update of the file
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void updateFile(final FileDTO file, final UserDTO requestUser);

    /**
     * Removes sharing link between file with given fileId and user with given userCode
     */
    @LogAnnotation(logCategory = LogCategory.TRACKING)
    public void deleteSharingLink(long fileId, String userCode);

    /**
     * Returns the file identified by <var>fileId</var>.
     * 
     * @throws IllegalArgumentException If a file with that id doesn't exist.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public FileDTO getFile(final long fileId) throws IllegalArgumentException;

    /**
     * Returns the file on the file system for the given <var>fileDTO</var>.
     */
    public File getRealFile(final FileDTO fileDTO);

}
