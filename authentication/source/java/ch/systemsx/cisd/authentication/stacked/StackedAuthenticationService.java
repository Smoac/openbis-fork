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

package ch.systemsx.cisd.authentication.stacked;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * An authentication service that uses a list of delegate authentication services to authenticate a
 * user. The first authentication service that can authenticate a user wins.
 * 
 * @author Bernd Rinn
 */
public class StackedAuthenticationService implements IAuthenticationService
{
    private static final String DUMMY_TOKEN_STR = "DUMMY-TOKEN";
    
    private final List<IAuthenticationService> delegates;
    
    private final List<String> tokens;
    
    private final boolean remote;
    
    private final boolean supportsListingByUserId;

    private final boolean supportsListingByEmail;
    
    private final boolean supportsListingByLastName;
    
    public StackedAuthenticationService(List<IAuthenticationService> authenticationServices)
    {
        this.delegates = authenticationServices;
        this.tokens = new ArrayList<String>(delegates.size());
        boolean foundRemote = false;
        boolean foundSupportsListingByUserId = false;
        boolean foundSupportsListingByEmail = false;
        boolean foundSupportsListingByLastName = false;
        for (IAuthenticationService service : delegates)
        {
            foundRemote |= service.isRemote();
            foundSupportsListingByUserId |= service.supportsListingByUserId();
            foundSupportsListingByEmail |= service.supportsListingByEmail();
            foundSupportsListingByLastName |= service.supportsListingByLastName();
        }
        this.remote = foundRemote;
        this.supportsListingByUserId = foundSupportsListingByUserId;
        this.supportsListingByEmail = foundSupportsListingByEmail;
        this.supportsListingByLastName = foundSupportsListingByLastName;
    }

    public String authenticateApplication()
    {
        tokens.clear();
        for (IAuthenticationService service : delegates)
        {
            final String token = service.authenticateApplication();
            if (token == null)
            {
                tokens.clear();
                return null;
            }
            tokens.add(token);
        }
        return DUMMY_TOKEN_STR;
    }

    public boolean authenticateUser(String applicationToken, String user, String password)
    {
        checkAuthenticatedApplication();
        int i = 0;
        for (IAuthenticationService service : delegates)
        {
            final String token = tokens.get(i);
            final boolean authenticated = service.authenticateUser(token, user, password);
            if (authenticated)
            {
                return true;
            }
            ++i;
        }
        return false;
    }

    public Principal getPrincipal(String applicationToken, String user)
            throws IllegalArgumentException
    {
        checkAuthenticatedApplication();
        int i = 0;
        for (IAuthenticationService service : delegates)
        {
            final String token = tokens.get(i);
            try
            {
                final Principal principal = service.getPrincipal(token, user);
                if (principal != null)
                {
                    return principal;
                }
            } catch (IllegalArgumentException ex)
            {
                // Ignore - we try the next one.
            }
            ++i;
        }
        throw new IllegalArgumentException("User '" + user + "' does not exist.");
    }

    public List<Principal> listPrincipalsByEmail(String applicationToken, String emailQuery)
    {
        if (supportsListingByEmail == false)
        {
            throw new UnsupportedOperationException();
        }
        checkAuthenticatedApplication();
        final List<Principal> principals = new ArrayList<Principal>();
        int i = 0;
        for (IAuthenticationService service : delegates)
        {
            final String token = tokens.get(i);
            if (service.supportsListingByEmail())
            {
                principals.addAll(service.listPrincipalsByEmail(token, emailQuery));
            }
            ++i;
        }
        return principals;
    }

    public List<Principal> listPrincipalsByLastName(String applicationToken, String lastNameQuery)
    {
        if (supportsListingByLastName == false)
        {
            throw new UnsupportedOperationException();
        }
        checkAuthenticatedApplication();
        final List<Principal> principals = new ArrayList<Principal>();
        int i = 0;
        for (IAuthenticationService service : delegates)
        {
            final String token = tokens.get(i);
            if (service.supportsListingByLastName())
            {
                principals.addAll(service.listPrincipalsByLastName(token, lastNameQuery));
            }
            ++i;
        }
        return principals;
    }

    public List<Principal> listPrincipalsByUserId(String applicationToken, String userIdQuery)
    {
        if (supportsListingByUserId == false)
        {
            throw new UnsupportedOperationException();
        }
        checkAuthenticatedApplication();
        final List<Principal> principals = new ArrayList<Principal>();
        int i = 0;
        for (IAuthenticationService service : delegates)
        {
            final String token = tokens.get(i);
            if (service.supportsListingByUserId())
            {
                principals.addAll(service.listPrincipalsByUserId(token, userIdQuery));
            }
            ++i;
        }
        return principals;
    }

    public boolean supportsListingByEmail()
    {
        return supportsListingByEmail;
    }

    public boolean supportsListingByLastName()
    {
        return supportsListingByLastName;
    }

    public boolean supportsListingByUserId()
    {
        return supportsListingByUserId;
    }

    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        for (IAuthenticationService service : delegates)
        {
            service.check();
        }
    }

    public boolean isRemote()
    {
        return remote;
    }
    
    private void checkAuthenticatedApplication()
    {
        if (tokens.isEmpty())
        {
            throw new IllegalArgumentException("Application not authenticated.");
        }
    }

}
