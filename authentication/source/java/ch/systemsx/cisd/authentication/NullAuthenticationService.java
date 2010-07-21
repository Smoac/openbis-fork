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

package ch.systemsx.cisd.authentication;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * An implementation of {@link IAuthenticationService} which supports as a <code>null</code>
 * object.
 * 
 * @author Franz-Josef Elmer
 */
public class NullAuthenticationService implements IAuthenticationService
{
    public String authenticateApplication()
    {
        throw new UnsupportedOperationException();
    }

    public boolean authenticateUser(String applicationToken, String user, String password)
    {
        throw new UnsupportedOperationException();
    }

    public Principal tryGetAndAuthenticateUser(String applicationToken, String user,
            String passwordOrNull)
    {
        throw new UnsupportedOperationException();
    }

    public Principal getPrincipal(String applicationToken, String user)
    {
        throw new UnsupportedOperationException();
    }

    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
    }

    public boolean isRemote()
    {
        return false;
    }

    public List<Principal> listPrincipalsByEmail(String applicationToken, String emailQuery)
    {
        throw new UnsupportedOperationException();
    }

    public List<Principal> listPrincipalsByLastName(String applicationToken, String lastNameQuery)
    {
        throw new UnsupportedOperationException();
    }

    public List<Principal> listPrincipalsByUserId(String applicationToken, String userIdQuery)
    {
        throw new UnsupportedOperationException();
    }

    public boolean supportsListingByEmail()
    {
        return false;
    }

    public boolean supportsListingByLastName()
    {
        return false;
    }

    public boolean supportsListingByUserId()
    {
        return false;
    }

}
