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

package ch.systemsx.cisd.authentication.file;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * An implementation of {@link IAuthenticationService} that gets the authentication information from
 * a password store (which is usually backed by a file).
 * <p>
 * The file contains:
 * <ul>
 * <li><code>user_id</code></li>
 * <li><code>email</code></li>
 * <li><code>first_name</code></li>
 * <li><code>last_name</code></li>
 * <li><code>password</code></li>
 * </ul>
 * 
 * @author Bernd Rinn
 */
public class FileAuthenticationService implements IAuthenticationService
{

    private static final String TOKEN_FAILURE_MSG_TEMPLATE =
            "Wrong application token provided, expected '%s', got '%s'";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileAuthenticationService.class);

    private final IUserStore userStore;

    private static IUserStore createUserStore(final String passwordFileName)
    {
        final ILineStore lineStore =
                new FileBasedLineStore(new File(passwordFileName), "Password file");
        return new LineBasedUserStore(lineStore);
    }

    public FileAuthenticationService(final String passwordFileName)
    {
        this(createUserStore(passwordFileName));
    }

    public FileAuthenticationService(IUserStore userStore)
    {
        this.userStore = userStore;
    }

    private String getToken()
    {
        return userStore.getId();
    }

    /**
     * Returns the id of the password store, which we consider to be the token.
     */
    public String authenticateApplication()
    {
        return getToken();
    }

    public boolean authenticateUser(String applicationToken, String user, String password)
    {
        final String token = getToken();
        if (token.equals(applicationToken) == false)
        {
            operationLog.warn(String.format(TOKEN_FAILURE_MSG_TEMPLATE, token, applicationToken));
            return false;
        }
        return userStore.isPasswordCorrect(user, password);
    }

    public Principal getPrincipal(String applicationToken, String user)
    {
        final String token = getToken();
        if (token.equals(applicationToken) == false)
        {
            operationLog.warn(String.format(TOKEN_FAILURE_MSG_TEMPLATE, token, applicationToken));
            return null;
        }
        final UserEntry userOrNull = userStore.tryGetUser(user);
        if (userOrNull == null)
        {
            throw new IllegalArgumentException("Cannot find user '" + user + "'.");
        }
        return userOrNull.asPrincipal();
    }

    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        userStore.check();
    }

    public boolean isRemote()
    {
        return false;
    }

}
