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

import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

/**
 * RPC service interface for CIFEX.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICIFEXRPCService
{
    //
    // Protocol versioning
    //

    /** The version of this service interface. */
    public static final int VERSION = 5;

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

    //
    // Info
    //

    /**
     * Provides the list of files available for download to the user of this session.
     */
    public FileInfoDTO[] listDownloadFiles(String sessionID) throws InvalidSessionException,
            EnvironmentFailureException;

    /**
     * Provides the list of files owned by the user of this session.
     */
    public FileInfoDTO[] listOwnedFiles(String sessionID) throws InvalidSessionException,
            EnvironmentFailureException;

    /**
     * Returns the information about the file with <var>fileID</var>.
     * 
     * @return The {@link FileInfoDTO} containing the information about the file.
     * @throws InvalidSessionException if there is no session with specified session ID.
     * @throws IOExceptionUnchecked if the file with that <var>fileID</var> cannot be found.
     */
    public FileInfoDTO getFileInfo(String sessionID, long fileID) throws InvalidSessionException,
            IOExceptionUnchecked;

    //
    // Upload
    //

    /**
     * Upload the file described in <var>file></var> and given by <var>contentStream</var>.
     * 
     * @return The id of the file in the database.
     * @throws InvalidSessionException if there is no session with specified session ID.
     */
    public long upload(String sessionID, FilePreregistrationDTO file, String comment,
            InputStream contentStream) throws InvalidSessionException;

    /**
     * Resume the upload the file with given <var>fileId</var> at <var>startPosition</var>. If
     * provided, the new <var>comment</var> will be set in the database.
     * 
     * @throws InvalidSessionException if there is no session with specified session ID.
     */
    public void resumeUpload(String sessionID, long fileId, long startPosition, String comment,
            InputStream contentStream) throws InvalidSessionException;

    /**
     * Returns the candidate for resuming an upload process if any is available on the server, or
     * <code>null</code> otherwise.
     */
    public FileInfoDTO tryGetUploadResumeCandidate(String sessionID,
            FilePreregistrationDTO fileSpecs);

    //
    // File sharing
    //

    /**
     * Share the given <var>fileIDs</var> with the <var>recipients</var>. It is an error if one of
     * the files specified does not exist on the server.
     */
    public void shareFiles(String sessionID, List<Long> fileIDs, String recipients);

    //
    // Deletion
    //

    /**
     * Deletes the file with given <var>fileId</var> on the server.
     */
    public void deleteFile(String sessionID, long fileId) throws InvalidSessionException;

    //
    // Download
    //

    /**
     * Download the file with <var>fileID</var>, starting from position <var>startPosition</var> in
     * file. Read the content of the file from the returned {@link InputStream}.
     * 
     * @return The {@link InputStream} delivering the content of the file.
     * @throws InvalidSessionException if there is no session with specified session ID.
     * @throws IOExceptionUnchecked if the file with that <var>fileID</var> cannot be found.
     */
    public InputStream download(String sessionID, long fileID, long startPosition)
            throws InvalidSessionException, IOExceptionUnchecked;

    //
    // Special service methods
    //

    /**
     * Sets the user that owns this session. All methods called after this method are called with
     * the privileges of the user specified by <var>userCode</code>.
     * <p>
     * This method may only be called by an administrator and only from an explicitly allowed IP
     * address or else it will throw an {@link AuthorizationFailureException}.
     */
    public void setSessionUser(String sessionID, String userCode);
}
