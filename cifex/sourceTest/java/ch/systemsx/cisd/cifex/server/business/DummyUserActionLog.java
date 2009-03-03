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

import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.cifex.rpc.server.Session;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * A dummy of a {@link IUserActionLog}, to be used in unit tests.
 * 
 * @author Bernd Rinn
 */
public class DummyUserActionLog implements IUserActionLog
{

    public void logCreateUser(final UserDTO user, final boolean success)
    {
    }

    public void logDeleteFile(final FileDTO file, final boolean success)
    {
    }

    public void logDeleteUser(final UserDTO user, final boolean success)
    {
    }

    public void logDownloadFile(final FileDTO file, final boolean success)
    {
    }

    public void logExpireFile(final FileDTO file, final boolean success)
    {
    }

    public void logExpireUser(final UserDTO user, final boolean success)
    {
    }

    public void logFailedLoginAttempt(final String userCode)
    {
    }

    public void logLogout(final HttpSession httpSession)
    {
    }

    public void logRenewFile(final FileDTO file, final boolean success)
    {
    }

    public void logShareFiles(final Collection<FileDTO> files,
            final Collection<UserDTO> usersToShareWith,
            final Collection<String> emailsOfUsersToShareWith,
            final Collection<String> invalidEmailAddresses, final boolean success)
    {
    }

    public void logSuccessfulLogin()
    {
    }

    public void logUpdateUser(final UserDTO oldUser, final UserDTO newUser, final boolean success)
    {
    }

    public void logUploadFile(final String filename, final boolean success)
    {
    }

    public void logDeleteSharingLink(final long fileId, final String userCode, final boolean success)
    {
    }

    public void logChangeUserCodeUser(final String before, final String after, final boolean success)
    {
    }

    public void logSwitchToExternalAuthentication(final String userCode, final boolean success)
    {
    }

    public void logLogout(Session session, LogoutReason reason)
    {
    }

    public void logSuccessfulLogin(Session session)
    {
    }

    public void logDownloadFileFinished(FileDTO file, boolean success)
    {
    }

    public void logDownloadFileStart(FileDTO file, boolean success)
    {
    }

    public void logUploadFileFinished(String filename, boolean success)
    {
    }

    public void logUploadFileStart(String filename, boolean success)
    {
    }
}
