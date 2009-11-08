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

package ch.systemsx.cisd.cifex.rpc.client;

import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

/**
 * The CIFEX component API. 
 *
 * @author Bernd Rinn
 */
public interface ICIFEXComponent
{
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
     * Creates a new file downloader for the given session.
     */
    public Downloader createDownloader(String sessionID);
    
    /**
     * Creates a new file uploader for the given session.
     */
    public Uploader createUploader(String sessionID);
    
}
