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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * A class for querying an LDAP server for principals.
 * 
 * @author Bernd Rinn
 */
public final class LDAPPrincipalQuery implements ISelfTestable
{
    private static final String LDAP_CONTEXT_FACTORY_CLASSNAME = "com.sun.jndi.ldap.LdapCtxFactory";

    private static final String QUERY_TEMPLATE = "%s=%s";

    private static final String AUTHENTICATION_FAILURE_TEMPLATE =
            "Authentication failure connecting to LDAP server '%s'.";

    private static final String LDAP_ERROR_TEMPLATE = "Error connecting to LDAP server '%s'.";

    private final LDAPDirectoryConfiguration config;

    public LDAPPrincipalQuery(LDAPDirectoryConfiguration config)
    {
        this.config = config;
    }

    public Principal tryGetPrincipalByUserId(String userId) throws IllegalArgumentException
    {
        final List<Principal> principals = listPrincipalsByUserId(userId);
        if (principals.size() == 0)
        {
            return null;
        } else if (principals.size() == 1)
        {
            return principals.get(0);
        } else
        {
            throw new IllegalArgumentException("User '" + userId + "' is not unique.");
        }
    }

    public List<Principal> listPrincipalsByUserId(String userId)
    {
        return listPrincipalsKeyValue(config.getUserIdAttributeName(), userId);
    }

    public List<Principal> listPrincipalsByEmail(String email)
    {
        return listPrincipalsKeyValue(config.getEmailAttributeName(), email);
    }

    public List<Principal> listPrincipalsByLastName(String lastName)
    {
        return listPrincipalsKeyValue(config.getLastNameAttributeName(), lastName);
    }

    public boolean authenticateUser(String userId, String password)
    {
        try
        {
            final DirContext context = createContext(userId, password);
            context.close();
            return true;
        } catch (AuthenticationException ex)
        {
            return false;
        } catch (NamingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private List<Principal> listPrincipalsKeyValue(String key, String value)
    {
        return listPrincipalsParameterized(QUERY_TEMPLATE, key, value);
    }
    
    private List<Principal> listPrincipalsParameterized(String filterTemplate, Object... params)
    {
        final List<Principal> principals = new ArrayList<Principal>();
        final String query =
                String.format(config.getQueryTemplate(), String.format(filterTemplate, params));
        DirContext context = null;
        try
        {
            context = createContext(config.getUserId(), config.getPassword());
            try
            {
                final SearchControls ctrl = new SearchControls();
                ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
                final NamingEnumeration<SearchResult> enumeration = context.search("", query, ctrl);
                while (enumeration.hasMore())
                {
                    final SearchResult result = enumeration.next();
                    final Attributes attributes = result.getAttributes();
                    final String userId =
                            tryGetAttribute(attributes, config.getUserIdAttributeName(), null);
                    final String email =
                            tryGetAttribute(attributes, config.getEmailAttributeName(), null);
                    final String firstName =
                            tryGetAttribute(attributes, config.getFirstNameAttributeName(), "?");
                    final String lastName =
                            tryGetAttribute(attributes, config.getLastNameAttributeName(), "?");
                    if (userId != null && email != null)
                    {
                        principals.add(new Principal(userId, firstName, lastName, email));
                    }
                }
                return principals;
            } finally
            {
                if (context != null)
                {
                    context.close();
                }
            }
        } catch (AuthenticationException ex)
        {
            throw ConfigurationFailureException.fromTemplate(ex, AUTHENTICATION_FAILURE_TEMPLATE,
                    config.getServerUrl());
        } catch (NamingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private DirContext createContext(String userId, String password) throws NamingException
    {
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY_CLASSNAME);
        env.put(Context.PROVIDER_URL, config.getServerUrl());
        env.put(Context.SECURITY_PROTOCOL, config.getSecurityProtocol());
        env.put(Context.SECURITY_AUTHENTICATION, config.getSecurityAuthenticationMethod());
        env.put(Context.REFERRAL, config.getReferral());
        env.put(Context.SECURITY_PRINCIPAL, String.format(config.getSecurityPrincipalTemplate(),
                userId));
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialDirContext(env);
    }

    private static String tryGetAttribute(Attributes attributes, String attributeName,
            String defaultValue) throws NamingException
    {
        final BasicAttribute basicAttribute = (BasicAttribute) attributes.get(attributeName);
        if (basicAttribute == null)
        {
            return defaultValue;
        }
        final NamingEnumeration<?> values = basicAttribute.getAll();
        while (values.hasMore())
        {
            return values.next().toString();
        }
        return defaultValue;
    }

    //
    // ISelfTestable
    //

    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        try
        {
            createContext(config.getUserId(), config.getPassword());
        } catch (AuthenticationException ex)
        {
            throw ConfigurationFailureException.fromTemplate(ex, AUTHENTICATION_FAILURE_TEMPLATE,
                    config.getServerUrl());
        } catch (NamingException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, LDAP_ERROR_TEMPLATE, config
                    .getServerUrl());
        }
    }

    public boolean isRemote()
    {
        return config.getServerUrl().contains("localhost") == false
                && config.getServerUrl().contains("127.0.0.1") == false;
    }

}