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
import java.util.Collection;
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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * A class for querying an LDAP server for principals.
 * 
 * @author Bernd Rinn
 */
public final class LDAPPrincipalQuery implements ISelfTestable
{
    private static final int MAX_RETRIES = 10;

    private static final String DISTINGUISHED_NAME_ATTRIBUTE_NAME = "distinguishedName";

    private static final String UID_NUMBER_ATTRIBUTE_NAME = "uidNumber";

    private static final String LOGIN_DN_MSG_TEMPLATE = "User '%s' <DN='%s'>: authentication %s";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, LDAPPrincipalQuery.class);

    private static final String LDAP_CONTEXT_FACTORY_CLASSNAME = "com.sun.jndi.ldap.LdapCtxFactory";

    private static final String AUTHENTICATION_FAILURE_TEMPLATE =
            "Authentication failure connecting to LDAP server '%s'.";

    private static final String LDAP_ERROR_TEMPLATE = "Error connecting to LDAP server '%s'.";

    private final LDAPDirectoryConfiguration config;

    private final ThreadLocal<DirContext> contextHolder;

    public LDAPPrincipalQuery(LDAPDirectoryConfiguration config)
    {
        this.config = config;
        this.contextHolder = new ThreadLocal<DirContext>();
    }

    public Principal tryGetPrincipal(String userId) throws IllegalArgumentException
    {
        final List<Principal> principals = listPrincipalsByUserId(userId, 1);
        if (principals.size() == 0)
        {
            return null;
        } else if (principals.size() == 1)
        {
            return principals.get(0);
        } else
        {
            // Cannot happen - we have limited the search to 1
            throw new IllegalArgumentException("User '" + userId + "' is not unique.");
        }
    }

    public List<Principal> listPrincipalsByUserId(String userId)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByUserId(%s)", userId));
        }
        return listPrincipalsByKeyValue(config.getUserIdAttributeName(), userId, null,
                Integer.MAX_VALUE);
    }

    private List<Principal> listPrincipalsByUserId(String userId, int limit)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByUserId(%s,%s)", userId, limit));
        }
        return listPrincipalsByKeyValue(config.getUserIdAttributeName(), userId, null, limit);
    }

    public List<Principal> listPrincipalsByEmail(String email)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByEmail(%s)", email));
        }
        return listPrincipalsByKeyValue(config.getEmailAttributeName(), email, null,
                Integer.MAX_VALUE);
    }

    public List<Principal> listPrincipalsByLastName(String lastName)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByLastName(%s)", lastName));
        }
        return listPrincipalsByKeyValue(config.getLastNameAttributeName(), lastName, null,
                Integer.MAX_VALUE);
    }

    public boolean authenticateUser(String userId, String password)
    {
        final Principal principal = tryGetAndAuthenticatePrincipal(userId, password);
        return (principal == null) ? false : principal.isAuthenticated();
    }

    public Principal tryGetAndAuthenticatePrincipal(String userId, String passwordOrNull)
    {
        final Principal principal = tryGetPrincipal(userId);
        if (principal == null)
        {
            return null;
        }
        final String distinguishedName = principal.getProperty(DISTINGUISHED_NAME_ATTRIBUTE_NAME);
        final boolean authenticated =
                (passwordOrNull == null) ? false : authenticateUserByDistinguishedName(
                        distinguishedName, passwordOrNull);
        principal.setAuthenticated(authenticated);
        if (operationLog.isDebugEnabled() && passwordOrNull != null)
        {
            operationLog.debug(String.format(LOGIN_DN_MSG_TEMPLATE, userId, distinguishedName,
                    getStatus(authenticated)));
        }
        return principal;
    }

    private String getStatus(final boolean status)
    {
        return status ? "OK" : "FAILURE";
    }

    private boolean authenticateUserByDistinguishedName(String dn, String password)
    {
        try
        {
            createContextForDistinguishedName(dn, password, false);
            return true;
        } catch (AuthenticationException ex)
        {
            return false;
        } catch (NamingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public List<Principal> listPrincipalsByKeyValue(String key, String value)
    {
        return listPrincipalsByKeyValue(key, value, null, Integer.MAX_VALUE);
    }

    @SuppressWarnings("null")
    public List<Principal> listPrincipalsByKeyValue(String key, String value,
            Collection<String> additionalAttributesOrNull, int limit)
    {
        RuntimeException firstException = null;
        // See bug 
        // http://bugs.sun.com/bugdatabase/view_bug.do;jsessionid=b399a5ff102b13d178b4c703df19?bug_id=6924489
        // on Solaris with SSL connections
        for (int i = 0; i < MAX_RETRIES; ++i)
        {
            try
            {
                return primListPrincipalsByKeyValue(key, value, additionalAttributesOrNull, limit);
            } catch (RuntimeException ex)
            {
                contextHolder.set(null);
                if (firstException == null)
                {
                    firstException = ex;
                }
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Exception in SSL protocol, retrying.");
                }
            }
        }
        throw firstException;
    }
    
    private List<Principal> primListPrincipalsByKeyValue(String key, String value,
            Collection<String> additionalAttributesOrNull, int limit)
    {
        final List<Principal> principals = new ArrayList<Principal>();
        final String filter = String.format("%s=%s", key, value);
        final String query = String.format(config.getQueryTemplate(), filter);
        try
        {
            final DirContext context = createContext();
            final SearchControls ctrl = new SearchControls();
            ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            final NamingEnumeration<SearchResult> enumeration = context.search("", query, ctrl);
            int count = 0;
            while (count++ < limit && enumeration.hasMore())
            {
                final SearchResult result = enumeration.next();
                final Attributes attributes = result.getAttributes();
                final String userId = tryGetAttribute(attributes, config.getUserIdAttributeName());
                final String email = tryGetAttribute(attributes, config.getEmailAttributeName());
                final String distinguishedName =
                        tryGetAttribute(attributes, DISTINGUISHED_NAME_ATTRIBUTE_NAME);
                if (userId != null && email != null && distinguishedName != null)
                {
                    final String firstName =
                            tryGetAttribute(attributes, config.getFirstNameAttributeName(), "?");
                    final String lastName =
                            tryGetAttribute(attributes, config.getLastNameAttributeName(), "?");
                    final String uidNumber = tryGetAttribute(attributes, UID_NUMBER_ATTRIBUTE_NAME);
                    final Principal principal =
                            new Principal(userId, firstName, lastName, email, false);
                    principal.getProperties().put(DISTINGUISHED_NAME_ATTRIBUTE_NAME,
                            distinguishedName);
                    if (uidNumber != null)
                    {
                        principal.getProperties().put(UID_NUMBER_ATTRIBUTE_NAME, uidNumber);
                    }
                    if (additionalAttributesOrNull != null)
                    {
                        for (String attributeName : additionalAttributesOrNull)
                        {
                            final String attributeValue = tryGetAttribute(attributes, attributeName);
                            if (attributeValue != null)
                            {
                                principal.getProperties().put(attributeName, attributeValue);
                            }
                        }
                    }
                    principals.add(principal);
                }
            }
            return principals;
        } catch (AuthenticationException ex)
        {
            throw ConfigurationFailureException.fromTemplate(ex, AUTHENTICATION_FAILURE_TEMPLATE,
                    config.getServerUrl());
        } catch (NamingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private DirContext createContext() throws NamingException
    {
        return createContextForDistinguishedName(config.getSecurityPrincipalDistinguishedName(),
                config.getSecurityPrincipalPassword(), true);
    }

    @SuppressWarnings("null")
    private DirContext createContextForDistinguishedName(String dn, String password,
            boolean useThreadContext) throws NamingException
    {
        final DirContext threadContext = useThreadContext ? contextHolder.get() : null;
        if (threadContext != null)
        {
            return threadContext;
        }
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY_CLASSNAME);
        env.put(Context.PROVIDER_URL, config.getServerUrl());
        env.put(Context.SECURITY_PROTOCOL, config.getSecurityProtocol());
        env.put(Context.SECURITY_AUTHENTICATION, config.getSecurityAuthenticationMethod());
        env.put(Context.REFERRAL, config.getReferral());
        env.put(Context.SECURITY_PRINCIPAL, dn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Try to login to %s with dn=%s",
                    config.getServerUrl(), dn));
        }
        RuntimeException firstException = null;
        // See bug 
        // http://bugs.sun.com/bugdatabase/view_bug.do;jsessionid=b399a5ff102b13d178b4c703df19?bug_id=6924489
        // on Solaris with SSL connections
        for (int i = 0; i < MAX_RETRIES; ++i)
        {
            try
            {
                final InitialDirContext initialDirContext = new InitialDirContext(env);
                if (useThreadContext)
                {
                    contextHolder.set(initialDirContext);
                }
                return initialDirContext;
            } catch (RuntimeException ex)
            {
                if (firstException == null)
                {
                    firstException = ex;
                }
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Exception in SSL protocol, retrying.");
                }
            }
        }
        throw firstException;
    }

    private static String tryGetAttribute(Attributes attributes, String attributeName)
            throws NamingException
    {
        return tryGetAttribute(attributes, attributeName, null);
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
            createContext();
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