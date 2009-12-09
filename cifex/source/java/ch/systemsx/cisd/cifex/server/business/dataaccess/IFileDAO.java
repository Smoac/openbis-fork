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

package ch.systemsx.cisd.cifex.server.business.dataaccess;

import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;

/**
 * <i>Data Access Object</i> for files.
 * 
 * @author Izabela Adamczyk
 */
public interface IFileDAO
{
    /**
     * Inserts given <code>File</code> into the database.
     * <p>
     * As side effect the <i>unique identifier</i> returned by the database is set to given
     * <code>File</code> object using {@link FileDTO#setID(Long)}.
     * </p>
     * 
     * @param file <code>File</code> object to be inserted into the database. Can not be
     *            <code>null</code>.
     */
    public void createFile(final FileDTO file) throws DataAccessException;

    /**
     * Updates all fields (except the registration timestamp) from <var>file</var> in the database.
     */
    public void updateFile(final FileDTO file) throws DataAccessException;

    /**
     * Updates the <var>file</var> in the database with the current upload progress.
     */
    public void updateFileUploadProgress(final long id, final long size, final int crc32,
            final Date expirationDate) throws DataAccessException;

    /**
     * Removes <code>File</code> with given id from database.
     * 
     * @param fileId Id of file which should be removed from database.
     * @return <code>true</code> if the file was deleted, <code>false</code> if there was no file
     *         found with that id.
     */
    public boolean deleteFile(final long fileId) throws DataAccessException;

    /**
     * Returns a list of all files existing in database.
     */
    public List<FileDTO> listFiles() throws DataAccessException;

    /**
     * Returns detailed information about file, including registrator data and list of users the
     * file is shared with.
     */
    public FileDTO tryGetFile(final long fileId) throws DataAccessException;

    /**
     * Returns the registration date of the file with given <var>fileId</var>.
     */
    public Date getFileRegistrationDate(final long fileId) throws DataAccessException;
    
    /**
     * Adds a link between the specified file and user.
     */
    public void createSharingLink(long fileID, long userID) throws DataAccessException;

    /**
     * Deletes a link between the specified file and user.
     */
    public boolean deleteSharingLink(long fileID, String userCode) throws DataAccessException;

    /** Returns a list of expired files */
    public List<FileDTO> getExpiredFiles();

    /**
     * Returns a list of all files (including expired and not yet deleted) given <var>userId</var>
     * has access to.
     */
    public List<FileDTO> listDownloadFiles(final long userId) throws DataAccessException;

    /**
     * Returns a list of all files owned by the user with given <var>userId</var> or by a user that
     * this user has registered.
     */
    public List<FileDTO> listDirectlyAndIndirectlyOwnedFiles(final long userId)
            throws DataAccessException;

    /**
     * Returns a partially uploaded file that is a candidate for resuming upload. Candidates are
     * defined by being uploaded by the same user, having the same file name and the same complete
     * size. If there are multiple files which are candidates, a random one is provided. If there is
     * no candidate, <code>null</code> is returned.
     */
    public FileDTO tryGetResumeCandidate(final long userId, final String fileName,
            final long completeSize);
}
