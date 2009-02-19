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

package ch.systemsx.cisd.cifex.upload;

import ch.systemsx.cisd.cifex.client.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.client.UserFailureException;

/**
 * Service interface for file uploading.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICIFEXRPCService
{
    /**
     * Authenticates given <code>user</code> with given <code>password</code>.
     * <p>
     * If <code>requestAdmin==true</code>, then request an admin login.
     * 
     * @return if the login was successful, a <code>sessionID</code> which can be used in
     *         other methods to upload or download files, <code>null</code> otherwise.
     */
    public String login(final String user, final String password) throws UserFailureException,
            EnvironmentFailureException;

    /**
     * Logout the current user.
     */
    public void logout();

    /**
     * Cancels the specified session.
     * 
     * @throws EnvironmentFailureException if there is no session with specified session ID.
     */
    public void cancel(String sessionID) throws EnvironmentFailureException;

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
     * @throws EnvironmentFailureException if there is no session with specified session ID.
     */
    public void defineUploadParameters(String sessionID, String[] files, String recipients,
            String comment) throws EnvironmentFailureException;

    /**
     * Returns the status of the specified upload session.
     * 
     * @throws EnvironmentFailureException if there is no session with specified session ID.
     */
    public UploadStatus getUploadStatus(String sessionID) throws EnvironmentFailureException;

    /**
     * Starts uploading of the specified upload session. The upload status state after invocation
     * will be {@link UploadState#UPLOADING}.
     * 
     * @throws IllegalStateException if upload status state isn't
     *             {@link UploadState#READY_FOR_NEXT_FILE}.
     * @throws EnvironmentFailureException if there is no session with specified session ID.
     */
    public void startUploading(String sessionID) throws EnvironmentFailureException;

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
     * @param block Block of data bytes.
     * @param blockSize Number of bytes of <code>block</code> which are to be taken.
     * @param lastBlock <code>true</code> if <code>block</code> is the last block of a file to
     *            be uploaded.
     * @throws IllegalStateException if upload status state isn't {@link UploadState#UPLOADING} or
     *             {@link UploadState#ABORTED}.
     * @throws EnvironmentFailureException if there is no session with specified session ID.
     */
    public void uploadBlock(String sessionID, byte[] block, int blockSize, boolean lastBlock)
            throws EnvironmentFailureException;

    /**
     * Finishes the specified session. The status state after invocation will be
     * {@link UploadState#INITIALIZED} if <code>successful == false</code>.
     * 
     * @param successful Flag indicating whether the uploading was successful or not.
     * @throws EnvironmentFailureException if there is no session with specified session ID.
     */
    public void finish(String sessionID, boolean successful)
            throws EnvironmentFailureException;

    /**
     * Closes and removes the specified session.
     */
    public void close(String sessionID);
}
