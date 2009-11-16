/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc;

import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

/**
 * Service interface for file uploading.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICIFEXRPCService
{
    //
    // Protocol versioning
    //

    /** The version of this service interface. */
    public static final int VERSION = 3;

    /** Returns the version of the server side interface. */
    public int getVersion();

    /**
     * Returns the minimal version that the client needs to have in order to be able to talk to this
     * server.
     */
    public int getMinClientVersion();

    //
    // Session
    //

    /**
     * Authenticates given <code>user</code> with given <code>password</code>.
     * <p>
     * If <code>requestAdmin==true</code>, then request an admin login.
     * 
     * @return if the login was successful, a <code>sessionID</code> which can be used in other
     *         methods to upload or download files, <code>null</code> otherwise.
     */
    public String login(final String user, final String password)
            throws AuthorizationFailureException;

    /**
     * Logs the current user out by closing the session identified by <var>sessionID</var>.
     */
    public void logout(String sessionID);

    /**
     * Checks whether the session identified by <var>sessionID</var> is alive.
     * 
     * @throws InvalidSessionException If the session is not alive.
     */
    public void checkSession(String sessionID) throws InvalidSessionException;

    /**
     * Finishes the specified operation (that is the current upload or download). The parameter
     * <var>successful</var> is used for logging purposes and has no consequences otherwise. The
     * session itself is still valid after calling this method and can be used to initiate another
     * operation.
     * 
     * @param successful Flag indicating whether the uploading was successful or not.
     * @throws InvalidSessionException if there is no session with specified session ID.
     */
    public void finish(String sessionID, boolean successful) throws InvalidSessionException;

    //
    // Info
    //

    /**
     * Provides the list of files available for download to the user of this session.
     */
    public FileInfoDTO[] listDownloadFiles(String sessionID) throws InvalidSessionException,
            EnvironmentFailureException;

    //
    // Upload
    //

    /**
     * Starts uploading the given file.
     * 
     * @return The id of the file in the database.
     * @throws InvalidSessionException if there is no session with specified session ID.
     */
    public long startUploading(String sessionID, FilePreregistrationDTO file, String comment)
            throws InvalidSessionException;

    /**
     * Uploads a data block for the specified upload session.
     * 
     * @param filePointer The pointer of the block within the file. Can be either the same one as
     *            the last time or the next one. All other <var>filePointer</var> values are
     *            considered an error.
     * @param runningCrc32Value The value of the CRC32 checksum of all data blocks uploaded up to
     *            now, including the current <var>block</var> ("running CRC32 checksum")
     * @param block Block of data bytes.
     * @throws IllegalStateException if no upload is in progress.
     * @throws InvalidSessionException if there is no session with specified session ID.
     * @throws IOExceptionUnchecked if an I/O error occurred during the upload.
     * @throws FileSizeExceededException if the upload exceed the maximally allowed file size.
     * @throws IllegalStateException if
     *             {@link #startUploading(String, FilePreregistrationDTO, String)} hasn't been
     *             called before.
     */
    public void uploadBlock(String sessionID, long filePointer, int runningCrc32Value, byte[] block)
            throws InvalidSessionException, IOExceptionUnchecked, FileSizeExceededException,
            IllegalStateException;

    //
    // File sharing
    //

    /**
     * Share the given <var>fileIDs</var> with the <var>recipients</var>. It is an error if one of
     * the files specified does not exist on the server.
     */
    public void shareFiles(String sessionID, List<Long> fileIDs, String recipients);

    //
    // Download
    //

    /**
     * Start downloading the file with <var>fileID</var>.
     * 
     * @return The {@link FileInfoDTO} containing the information about the file to download.
     * @throws InvalidSessionException if there is no session with specified session ID.
     * @throws IOExceptionUnchecked if the file with that <var>fileID</var> cannot be found.
     */
    public FileInfoDTO startDownloading(String sessionID, long fileID)
            throws InvalidSessionException, IOExceptionUnchecked;

    /**
     * Download a block of size <var>blockSize</var> from the file currently under download,
     * starting from <var>filePointer</var>. Note that {@link #startDownloading(String, long)} needs
     * to have been called before.
     * 
     * @throws InvalidSessionException if there is no session with specified session ID.
     * @throws IOExceptionUnchecked if an I/O error occurred during the download.
     * @throws IllegalStateException if {@link #startDownloading(String, long)} hasn't been called
     *             before.
     */
    public byte[] downloadBlock(String sessionID, long filePointer, int blockSize)
            throws InvalidSessionException, IOExceptionUnchecked, IllegalStateException;

}
