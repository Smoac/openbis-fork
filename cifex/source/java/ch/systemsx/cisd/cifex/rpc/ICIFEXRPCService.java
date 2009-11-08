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

    /** The version of this service interface. */
    public static final int VERSION = 2;

    /** Returns the version of the server side interface. */
    public int getVersion();

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
     * Provides the list of files available for download to the user of this session.
     */
    public FileInfoDTO[] listDownloadFiles(String sessionID) throws InvalidSessionException,
            EnvironmentFailureException;

    /**
     * Defines the upload parameters for the specified upload session. The upload status state after
     * invocation will be {@link UploadState#READY_FOR_NEXT_FILE} or {@link UploadState#FINISHED} if
     * <code>files</code> is an empty array.
     * 
     * @param files Absolute file path of files to be uploaded.
     * @param recipients Whitespace of comma separated list of recipients (e-mail address or
     *            <code>id:<em>userID</em></code>).
     * @param comment Comment to be added in recipient notification.
     * @throws IllegalStateException if upload status state isn't {@link UploadState#INITIALIZED}.
     * @throws InvalidSessionException if there is no session with specified session ID.
     */
    public void defineUploadParameters(String sessionID, String[] files, String recipients,
            String comment) throws InvalidSessionException, IllegalStateException;

    /**
     * Returns the status of the specified upload session.
     * 
     * @throws InvalidSessionException if there is no session with specified session ID.
     */
    public UploadStatus getUploadStatus(String sessionID) throws InvalidSessionException;

    /**
     * Starts uploading of the specified upload session. The upload status state after invocation
     * will be {@link UploadState#UPLOADING}.
     * 
     * @throws IllegalStateException if upload status state isn't
     *             {@link UploadState#READY_FOR_NEXT_FILE}.
     * @throws InvalidSessionException if there is no session with specified session ID.
     */
    public void startUploading(String sessionID) throws InvalidSessionException;

    /**
     * Uploads a data block for the specified upload session. The upload status state after
     * invocation will be
     * <ul>
     * <li>{@link UploadState#UPLOADING} if <code>lastBlock == false</code>
     * <li>{@link UploadState#READY_FOR_NEXT_FILE} if <code>lastBlock == true</code> and another
     * file should be uploaded.
     * <li>{@link UploadState#FINISHED} if <code>lastBlock == true</code> and there are no more
     * files to be uploaded.
     * <li>{@link UploadState#ABORTED} if it was already in this state.
     * </ul>
     * 
     * @param filePointer the pointer of the block within the file.
     * @param block Block of data bytes.
     * @param lastBlock <code>true</code> if <code>block</code> is the last block of a file to be
     *            uploaded.
     * @throws IllegalStateException if upload status state isn't {@link UploadState#UPLOADING} or
     *             {@link UploadState#ABORTED}.
     * @throws InvalidSessionException if there is no session with specified session ID.
     * @throws IOExceptionUnchecked if an I/O error occurred during the upload.
     * @throws FileSizeExceededException if the upload exceed the maximally allowed file size.
     * @throws IllegalStateException if {@link #startUploading(String)} hasn't been called before.
     */
    public void uploadBlock(String sessionID, long filePointer, byte[] block, boolean lastBlock)
            throws InvalidSessionException, IOExceptionUnchecked, FileSizeExceededException,
            IllegalStateException;

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

    /**
     * Cancels the specified session.
     * 
     * @throws EnvironmentFailureException if there is no session with specified session ID.
     */
    public void cancel(String sessionID) throws InvalidSessionException,
            EnvironmentFailureException;

    /**
     * Finishes the specified session. The status state after invocation will be
     * {@link UploadState#INITIALIZED} if <code>successful == false</code>.
     * 
     * @param successful Flag indicating whether the uploading was successful or not.
     * @throws InvalidSessionException if there is no session with specified session ID.
     */
    public void finish(String sessionID, boolean successful) throws InvalidSessionException;

}
