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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
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
    private static final String DISTINGUISHED_NAME_ATTRIBUTE_NAME = "distinguishedName";

    private static final String UID_NUMBER_ATTRIBUTE_NAME = "uidNumber";

    private static final String LOGIN_DN_MSG_TEMPLATE = "User '%s' <DN='%s'>: authentication %s";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, LDAPPrincipalQuery.class);

    private static final String LDAP_CONTEXT_FACTORY_CLASSNAME = "com.sun.jndi.ldap.LdapCtxFactory";

    private static final String LDAP_CONTEXT_READ_TIMEOUT = "com.sun.jndi.ldap.read.timeout";

    private static final String LDAP_CONTEXT_CONNECT_TIMEOUT = "com.sun.jndi.ldap.connect.timeout";

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
        return tryGetPrincipal(principals, "User '%s' is not unique.", userId);
    }

    public Principal tryGetPrincipalByEmail(String email)
    {
        final List<Principal> principals = listPrincipalsByEmail(email, 1);
        return tryGetPrincipal(principals, "Email '%s' is not unique.", email);
    }

    private Principal tryGetPrincipal(final List<Principal> principals, final String msgTemplate,
            final String user)
    {
        if (principals.size() == 0)
        {
            return null;
        } else if (principals.size() == 1)
        {
            return principals.get(0);
        } else
        {
            // Cannot happen - we have limited the search to 1
            throw new IllegalArgumentException(String.format(msgTemplate, user));
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

    public List<Principal> listPrincipalsByEmail(String email, int limit)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByEmail(%s,%s)", email, limit));
        }
        return listPrincipalsByKeyValue(getEmailAttributeForQueries(),
                getEmailKeyForQueries(email), null, limit);
    }

    private String getEmailAttributeForQueries()
    {
        if (Boolean.parseBoolean(config.getQueryEmailForAliases()))
        {
            return config.getEmailAliasesAttributeName();
        } else
        {
            return config.getEmailAttributeName();
        }
    }

    private String getEmailKeyForQueries(String emailQuery)
    {
        if (Boolean.parseBoolean(config.getQueryEmailForAliases()))
        {
            return config.getEmailAttributePrefix() + emailQuery;
        } else
        {
            return emailQuery;
        }
    }

    public List<Principal> listPrincipalsByEmail(String email)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByEmail(%s)", email));
        }
        return listPrincipalsByKeyValue(getEmailAttributeForQueries(),
                getEmailKeyForQueries(email), null, Integer.MAX_VALUE);
    }

    public List<Principal> listPrincipalsByLastName(String lastName, int limit)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByLastName(%s,%s)", lastName, limit));
        }
        return listPrincipalsByKeyValue(config.getLastNameAttributeName(), lastName, null, limit);
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
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("User '%s' not found in LDAP directory.", userId));
            }
            return null;
        }
        authenticatePrincipal(principal, passwordOrNull);
        return principal;
    }

    public Principal tryGetAndAuthenticatePrincipalByEmail(String email, String passwordOrNull)
    {
        final Principal principal = tryGetPrincipalByEmail(email);
        if (principal == null)
        {
            return null;
        }
        authenticatePrincipal(principal, passwordOrNull);
        return principal;
    }

    private void authenticatePrincipal(final Principal principal, String passwordOrNull)
    {
        final String distinguishedName = principal.getProperty(DISTINGUISHED_NAME_ATTRIBUTE_NAME);
        final boolean authenticated =
                (passwordOrNull == null) ? false : authenticateUserByDistinguishedName(
                        distinguishedName, passwordOrNull);
        principal.setAuthenticated(authenticated);
        if (operationLog.isDebugEnabled() && passwordOrNull != null)
        {
            operationLog.debug(String.format(LOGIN_DN_MSG_TEMPLATE, principal.getUserId(),
                    distinguishedName, getStatus(authenticated)));
        }
    }

    private String getStatus(final boolean status)
    {
        return status ? "OK" : "FAILURE";
    }

    private boolean authenticateUserByDistinguishedName(String dn, String password)
    {
        try
        {
            createContextForDistinguishedName(dn, password, false, true);
            return true;
        } catch (InvalidAuthenticationException ex)
        {
            return false;
        } catch (RuntimeException ex)
        {
            operationLog.error(
                    String.format("Error on creating context to authenticate dn=<%s>", dn), ex);
            throw ex;
        }
    }

    public List<Principal> listPrincipalsByKeyValue(String key, String value)
    {
        return listPrincipalsByKeyValue(key, value, null, Integer.MAX_VALUE);
    }

    /**
     * Returns a list of principals matching a search query given as <var>key</var> and <var>value</var> where value may contain <code>*</code> as a
     * wildcard character.
     * 
     * @param key The key to search for, e.g. <code>uid</code>
     * @param value The value to query for, e.g. <code>may*</code>
     * @param additionalAttributesOrNull If not <code>null</code>, include the attributes with the given attribute names. If
     *            <var>additionalAttributesOrNull</var> is an empty collection, include all properties that the principal has in LDAP
     * @param limit The limit of users to return at most. Note that limiting the search to 1 gives usually a big performance boost.
     * @return The list of principals matching the query
     */
    @SuppressWarnings("null")
    public List<Principal> listPrincipalsByKeyValue(String key, String value,
            Collection<String> additionalAttributesOrNull, int limit)
    {
        RuntimeException firstException = null;
        for (int i = 0; i <= config.getMaxRetries(); ++i)
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
                    if (operationLog.isDebugEnabled() && i < config.getMaxRetries())
                    {
                        operationLog.debug(String.format(
                                "Error listing principle by %s=%s, retrying...", key, value), ex);
                    }
                }
                if (i < config.getMaxRetries())
                {
                    ConcurrencyUtilities.sleep(config.getTimeToWaitAfterFailure());
                }
            }
        }
        operationLog.error(String.format("Error on LDAP query %s=%s", key, value), firstException);
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
            final DirContext context = createContext(false);
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
                final String distinguishedName = result.getNameInNamespace();
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
                        if (additionalAttributesOrNull.isEmpty())
                        {
                            principal.setProperties(getAllAttributes(attributes));
                        } else
                        {
                            for (String attributeName : additionalAttributesOrNull)
                            {
                                final String attributeValue =
                                        tryGetAttribute(attributes, attributeName);
                                if (attributeValue != null)
                                {
                                    principal.getProperties().put(attributeName, attributeValue);
                                }
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

    private DirContext createContext(boolean retry)
    {
        return createContextForDistinguishedName(config.getSecurityPrincipalDistinguishedName(),
                config.getSecurityPrincipalPassword(), true, retry);
    }

    @SuppressWarnings("null")
    private DirContext createContextForDistinguishedName(String dn, String password,
            boolean useThreadContext, boolean retry)
    {
        final DirContext threadContext = useThreadContext ? contextHolder.get() : null;
        if (threadContext != null)
        {
            return threadContext;
        }
        if (password != null && password.isEmpty())
        {
            throw new RuntimeException("Try to login user '" + dn + "' with empty password.");
        }
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY_CLASSNAME);
        env.put(Context.PROVIDER_URL, config.getServerUrl());
        env.put(Context.SECURITY_PROTOCOL, config.getSecurityProtocol());
        env.put(Context.SECURITY_AUTHENTICATION, config.getSecurityAuthenticationMethod());
        env.put(Context.REFERRAL, config.getReferral());
        env.put(Context.SECURITY_PRINCIPAL, dn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(LDAP_CONTEXT_READ_TIMEOUT, Long.toString(config.getTimeout()));
        env.put(LDAP_CONTEXT_CONNECT_TIMEOUT, Long.toString(config.getTimeout()));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Try to login to %s with dn=%s",
                    config.getServerUrl(), dn));
        }
        RuntimeException firstException = null;
        for (int i = 0; i <= config.getMaxRetries(); ++i)
        {
            try
            {
                final InitialDirContext initialDirContext = new InitialDirContext(env);
                if (useThreadContext)
                {
                    contextHolder.set(initialDirContext);
                }
                return initialDirContext;
            } catch (Exception ex)
            {
                if (ex instanceof AuthenticationException)
                {
                    throw new InvalidAuthenticationException("Failed to authenticate dn=<" + dn
                            + ">", ex);
                }
                if (firstException == null)
                {
                    firstException = CheckedExceptionTunnel.wrapIfNecessary(ex);
                    if (operationLog.isDebugEnabled() && retry)
                    {
                        operationLog.debug(
                                "Error connecting to LDAP service: cannot open a context for dn=<"
                                        + dn + ">, retrying...", ex);
                    }
                }
                if (retry == false)
                {
                    break;
                }
                if (i < config.getMaxRetries())
                {
                    ConcurrencyUtilities.sleep(config.getTimeToWaitAfterFailure());
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
        final StringBuilder builder = new StringBuilder();
        while (values.hasMore())
        {
            builder.append(values.next().toString());
            builder.append("::");
        }
        if (builder.length() == 0)
        {
            return defaultValue;
        } else
        {
            builder.setLength(builder.length() - 2);
            return builder.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getAllAttributes(Attributes attributes)
            throws NamingException
    {
        final Map<String, String> result = new HashMap<String, String>();
        final NamingEnumeration<BasicAttribute> attributeEnum =
                (NamingEnumeration<BasicAttribute>) attributes.getAll();
        if (attributeEnum == null)
        {
            return result;
        }
        while (attributeEnum.hasMore())
        {
            final BasicAttribute attribute = attributeEnum.next();
            String attributeName = attribute.getID();
            final NamingEnumeration<?> values = attribute.getAll();
            final StringBuilder builder = new StringBuilder();
            while (values.hasMore())
            {
                builder.append(values.next().toString());
                builder.append("::");
            }
            if (builder.length() > 2)
            {
                builder.setLength(builder.length() - 2);
                result.put(attributeName, builder.toString());
            }
        }
        return result;
    }

    //
    // ISelfTestable
    //

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        try
        {
            createContext(true);
        } catch (InvalidAuthenticationException ex)
        {
            throw ConfigurationFailureException.fromTemplate(ex, AUTHENTICATION_FAILURE_TEMPLATE,
                    config.getServerUrl());
        } catch (RuntimeException ex)
        {
            throw EnvironmentFailureException.fromTemplate(
                    CheckedExceptionTunnel.unwrapIfNecessary(ex), LDAP_ERROR_TEMPLATE, config
                            .getServerUrl());
        }
    }

    @Override
    public boolean isRemote()
    {
        return config.getServerUrl().contains("localhost") == false
                && config.getServerUrl().contains("127.0.0.1") == false;
    }

}