/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.ldap;

import java.util.List;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * A {@link IAuthenticationService} for LDAP servers.
 * 
 * @author Bernd Rinn
 */
public class LDAPAuthenticationService implements IAuthenticationService
{
    private static final String DUMMY_TOKEN_STR = "DUMMY-TOKEN";

    private final LDAPPrincipalQuery query;

    public LDAPAuthenticationService(LDAPDirectoryConfiguration config)
    {
        query = new LDAPPrincipalQuery(config);
    }

    @Override
    public String authenticateApplication()
    {
        return DUMMY_TOKEN_STR;
    }

    @Override
    public boolean authenticateUser(String applicationToken, String user, String password)
    {
        return authenticateUser(user, password);
    }

    @Override
    public boolean authenticateUser(String user, String password)
    {
        return query.authenticateUser(user, password);
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String applicationToken, String user,
            String passwordOrNull)
    {
        return tryGetAndAuthenticateUser(user, passwordOrNull);
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String user, String passwordOrNull)
    {
        return query.tryGetAndAuthenticatePrincipal(user, passwordOrNull);
    }

    @Override
    public Principal getPrincipal(String applicationToken, String user)
            throws IllegalArgumentException
    {
        return getPrincipal(user);
    }

    @Override
    public Principal getPrincipal(String user)
            throws IllegalArgumentException
    {
        final Principal principalOrNull = query.tryGetPrincipal(user);
        if (principalOrNull == null)
        {
            throw new IllegalArgumentException("Cannot find user '" + user + "'.");
        }
        return principalOrNull;
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String applicationToken, String emailQuery)
    {
        return listPrincipalsByEmail(emailQuery);
    }
    
    @Override
    public List<Principal> listPrincipalsByEmail(String emailQuery)
    {
        return query.listPrincipalsByEmail(emailQuery);
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String applicationToken, String email,
            String passwordOrNull)
    {
        return tryGetAndAuthenticateUserByEmail(email, passwordOrNull);
    }
    
    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String email,
            String passwordOrNull)
    {
        return query.tryGetAndAuthenticatePrincipalByEmail(email, passwordOrNull);
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String applicationToken, String lastNameQuery)
    {
        return listPrincipalsByLastName(lastNameQuery);
    }
    
    @Override
    public List<Principal> listPrincipalsByLastName(String lastNameQuery)
    {
        return query.listPrincipalsByLastName(lastNameQuery);
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String applicationToken, String userIdQuery)
    {
        return listPrincipalsByUserId(userIdQuery);
    }
    
    @Override
    public List<Principal> listPrincipalsByUserId(String userIdQuery)
    {
        return query.listPrincipalsByUserId(userIdQuery);
    }

    @Override
    public boolean supportsListingByEmail()
    {
        return true;
    }

    @Override
    public boolean supportsListingByLastName()
    {
        return true;
    }

    @Override
    public boolean supportsListingByUserId()
    {
        return true;
    }

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        query.check();
    }

    @Override
    public boolean isRemote()
    {
        return query.isRemote();
    }

}
