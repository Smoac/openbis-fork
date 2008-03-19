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

import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * A dummy of a {@link IUserActionLog}, to be used in unit tests.
 * 
 * @author Bernd Rinn
 */
public class DummyUserActionLog implements IUserActionLog
{

    public void logCreateUser(UserDTO user, boolean success)
    {
    }

    public void logDeleteFile(FileDTO file, boolean success)
    {
    }

    public void logDeleteUser(UserDTO user, boolean success)
    {
    }

    public void logDownloadFile(FileDTO file, boolean success)
    {
    }

    public void logExpireFile(FileDTO file, boolean success)
    {
    }

    public void logExpireUser(UserDTO user, boolean success)
    {
    }

    public void logFailedLoginAttempt(String userCode)
    {
    }

    public void logLogout(HttpSession httpSession)
    {
    }

    public void logRenewFile(FileDTO file, boolean success)
    {
    }

    public void logShareFiles(Collection<FileDTO> files, Collection<UserDTO> usersToShareWith,
            Collection<String> emailsOfUsersToShareWith, Collection<String> invalidEmailAddresses,
            boolean success)
    {
    }

    public void logSuccessfulLogin()
    {
    }

    public void logUpdateUser(UserDTO oldUser, UserDTO newUser, boolean success)
    {
    }

    public void logUploadFile(String filename, boolean success)
    {
    }

}
