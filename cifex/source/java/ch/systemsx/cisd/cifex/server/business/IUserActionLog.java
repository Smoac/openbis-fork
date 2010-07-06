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

import java.util.Collection;
import java.util.Date;

import ch.systemsx.cisd.cifex.rpc.server.Session;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.servlet.IActionLog;

/**
 * This role provides methods that are required to log the actions of CIFEX users.
 * 
 * @author Bernd Rinn
 */
public interface IUserActionLog extends IActionLog
{
    public static final String USER_ACTION_LOG_BEAN_NAME = "user-action-log-http";

    //
    // Sessions
    //

    /**
     * Returns a short description which contains user code, client host, and session id.
     */
    public String getUserHostSessionDescription();

    /**
     * Logs a logout.
     * 
     * @param session Session object that is terminated.
     * @param reason The reason for the logout.
     */
    public void logLogout(Session session, LogoutReason reason);

    //
    // Users
    //

    public void logCreateUser(UserDTO user, boolean success);

    public void logUpdateUser(UserDTO oldUser, UserDTO newUser, boolean success);

    public void logDeleteUser(UserDTO user, boolean success);

    public void logExpireUser(UserDTO user, boolean success);

    //
    // Files
    //

    public void logUploadFileStart(String filename, FileDTO fileOrNull, long startPosition);

    public void logUploadFileFinished(String filename, FileDTO fileOrNull, boolean success);

    public void logShareFilesAuthorizationFailure(Collection<FileDTO> files,
            Collection<String> recipientsToShareWith);

    public void logShareFiles(Collection<FileDTO> files, Collection<UserDTO> usersToShareWith,
            Collection<String> emailsOfUsersToShareWith, Collection<String> invalidEmailAddresses,
            boolean success);

    public void logDeleteFile(FileDTO file, boolean success);

    public void logExpireFile(FileDTO file, boolean success);

    public void logEditFile(long fileId, String newName, Date fileExpirationDateOrNull,
            boolean success);

    public void logDownloadFileFailedNotFound(FileDTO file);

    public void logDownloadFileFailedNotAuthorized(FileDTO file);

    public void logDownloadFileStart(FileDTO file, long startPosition);

    public void logDownloadFileFinished(FileDTO file, boolean success);

    public void logDeleteSharingLink(long fileId, String userCode, boolean success);

    public void logChangeUserCode(String before, String after, boolean success);

    public void logSwitchToExternalAuthentication(String userCode, boolean success);

}