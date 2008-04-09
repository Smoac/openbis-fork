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

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.FileContent;
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
     * @return The file DTO, or <code>null</code>, if no file object exists in the database for
     *         this <var>fileId</var>.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public FileInformation getFileInformation(final long fileId);

    /**
     * Returns <code>true</code>, if the user given by <var>userDTO</var> is allowed access to
     * file <var>fileDTO</var>.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public boolean isAllowedAccess(final UserDTO userDTO, final FileDTO fileDTO);

    /**
     * Returns <code>true</code>, if the user given by <var>userDTO</var> is allowed to delete
     * file <var>fileDTO</var>.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public boolean isAllowedDeletion(final UserDTO userDTO, final FileDTO fileDTO);

    /** Returns all files */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<FileDTO> listFiles();

    /** Lists files for given <var>userId</var>. */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<FileDTO> listDownloadFiles(final long userId) throws UserFailureException;

    /** Lists files uploaded by user with given <var>userId</var>. */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<FileDTO> listUploadedFiles(final long userId) throws UserFailureException;

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
     * Saves the data of the specified input stream which comes from a file with the specified name.
     * 
     * @param user The owner of the file.
     * @param fileName The name of the file. May contain the full path.
     * @param contentType Content type passed by the browser or <code>null</code> if not defined.
     * @param inputStream Input stream of file content.
     * @return file DTO with id.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public FileDTO saveFile(final UserDTO user, final String fileName, final String contentType,
            final InputStream inputStream);

    /**
     * Creates sharing links between the users specified by their e-mail addresses and the specified
     * files. For users not known a temporary account is created. Each user will be informed by an
     * e-mail.
     * 
     * @param url URL for creating the links in the e-mails.
     * @return a list of invalid (non-existent) email addresses. Can only be non-empty for temporary
     *         users.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public List<String> shareFilesWith(String url, UserDTO requestUser,
            Collection<String> emailsOfUsers, Collection<FileDTO> files, String comment);

    /**
     * Deletes file with given <code>fileId</code> from database and file system.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void deleteFile(final FileDTO fileDTO);

    /**
     * @throws UserFailureException indicating that <var>filename</var> does not exist and thus has
     *             not been saved.
     */
    public void throwExceptionOnFileDoesNotExist(final String fileName);

    /** Deletes expired files from database and file system. */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void deleteExpiredFiles();

    /**
     * Update the Expiration Date of the file with the given ID. Only an Admin can set an own
     * ExpirationDate, for all the others, the default expiration Date is used.
     * 
     * @param newExpirationDate The new Expiration date, can only used from an admin.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void updateFileExpiration(final long fileId, final Date newExpirationDate);

}
