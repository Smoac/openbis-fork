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
import java.util.List;

import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.FileOutput;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogAnnotation;
import ch.systemsx.cisd.common.logging.LogCategory;

/**
 * A manager that proxies and handles access to {@link FileDTO}.
 * 
 * @author Christian Ribeaud
 */
public interface IFileManager
{

    /**
     * Tries to find a file with given <var>fileId</var>.
     * 
     * @return never returns <code>null</code> but prefers to throw an exception.
     * @throws UserFailureException if given <var>fileId</var> could not be found in the database or if current user
     *             does not have access to the file.
     */
    public FileOutput getFile(final UserDTO userDTO, final long fileId) throws UserFailureException;

    /**
     * Saves the data of the specified input stream which comes from a file with the specified name.
     * 
     * @param user The owner of the file.
     * @param fileName The name of the file. May contain the full path.
     * @param contentType Content type passed by the browser or <code>null</code> if not defined.
     * @param inputStream Input stream of file content.
     * @return file DTO with id.
     */
    @LogAnnotation(logCategory = LogCategory.TRACKING)
    public FileDTO saveFile(final UserDTO user, final String fileName, final String contentType,
            final InputStream inputStream);

    /**
     * Creates sharing links between the users specified by their e-mail addresses and the specified files. For users
     * not known a temporary account is created. Each user will be informed by an e-mail.
     * 
     * @param url URL for creating the links in the e-mails.
     * @return a list of invalid (non-existent) email addresses. Can only be non-empty for temporary users.
     */
    @LogAnnotation(logCategory = LogCategory.TRACKING)
    public List<String> shareFilesWith(String url, UserDTO requestUser, Collection<String> emailsOfUsers,
            Collection<FileDTO> files);

    /** Lists files for given <var>userId</var>. */
    public List<FileDTO> listDownloadFiles(final long userId) throws UserFailureException;

    /** Lists files uploaded by user with given <var>userId</var>. */
    public List<FileDTO> listUploadedFiles(final long userId) throws UserFailureException;

    /**
     * @throws UserFailureException indicating that <var>filename</var> does not exist and thus has not been saved.
     */
    public void throwExceptionOnFileDoesNotExist(final String fileName);

    //
    // Helper classes
    //

    /** Deletes expired files from database and filesystem */
    @LogAnnotation(logCategory = LogCategory.TRACKING)
    public void deleteExpiredFiles();

    /** Returns all files */
    public List<FileDTO> listFiles();

    /** Deletes file with given <code>fileId</code> */
    public void deleteFile(long fileId);

}
