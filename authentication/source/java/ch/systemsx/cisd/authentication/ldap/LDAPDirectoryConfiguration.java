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

import javax.naming.Context;

import org.apache.commons.lang.StringUtils;

/**
 * The configuration for an LDAP directory server. Example:
 * 
 * <pre>
 *    final LDAPDirectoryConfiguration config = new LDAPDirectoryConfiguration();
 *    config.setServerUrl("ldap://d.ethz.ch:636/DC=d,DC=ethz,DC=ch");
 *    config.setSecurityPrincipalTemplate("CN=%s,OU=EthUsers,DC=d,DC=ethz,DC=ch");
 *    config.setUserId(&lt;user&gt;);
 *    config.setPassword(&lt;password&gt;);
 *    final LDAPPrincipalQuery query = new LDAPPrincipalQuery(config);
 * </pre>
 * 
 * @author Bernd Rinn
 */
public final class LDAPDirectoryConfiguration
{

    private String userIdAttributeName = "uid";

    private String lastNameAttributeName = "sn";

    private String firstNameAttributeName = "givenName";

    private String emailAttributeName = "mail";
    
    private String emailAttributePrefix = "";

    private String securityProtocol = "ssl";

    private String securityAuthenticationMethod = "simple";

    private String referral = "follow";

    private String queryTemplate =
            "(&(objectClass=organizationalPerson)(objectCategory=person)"
                    + "(objectClass=user)(%s))";

    private String securityPrincipalDistinguishedName;

    private String serverUrl;

    private String securityPrincipalPassword;

    /**
     * Default value: <code>uid</code>
     */
    public String getUserIdAttributeName()
    {
        return userIdAttributeName;
    }

    public void setUserIdAttributeName(String userIdAttributeName)
    {
        if (StringUtils.isNotBlank(userIdAttributeName))
        {
            this.userIdAttributeName = userIdAttributeName;
        }
    }

    /**
     * Default value: <code>sn</code>
     */
    public String getLastNameAttributeName()
    {
        return lastNameAttributeName;
    }

    public void setLastNameAttributeName(String lastNameAttributeName)
    {
        if (StringUtils.isNotBlank(lastNameAttributeName))
        {
            this.lastNameAttributeName = lastNameAttributeName;
        }
    }

    /**
     * Default value: <code>givenName</code>
     */
    public String getFirstNameAttributeName()
    {
        return firstNameAttributeName;
    }

    public void setFirstNameAttributeName(String firstNameAttributeName)
    {
        if (StringUtils.isNotBlank(firstNameAttributeName))
        {
            this.firstNameAttributeName = firstNameAttributeName;
        }
    }

    /**
     * Default value: <code>mail</code>
     */
    public String getEmailAttributeName()
    {
        return emailAttributeName;
    }

    public void setEmailAttributeName(String emailAttributeName)
    {
        if (StringUtils.isNotBlank(emailAttributeName))
        {
            this.emailAttributeName = emailAttributeName;
        }
    }

    public String getEmailAttributePrefix()
    {
        return emailAttributePrefix;
    }

    /**
     * The prefix of email values that is used when doing a search.
     * <p>
     * Default value: empty
     */
    public void setEmailAttributePrefix(String emailAttributePrefix)
    {
        this.emailAttributePrefix = emailAttributePrefix;
    }

    /**
     * Default value: <code>ssl</code>
     * 
     * @see Context#SECURITY_PROTOCOL
     */
    public String getSecurityProtocol()
    {
        return securityProtocol;
    }

    /**
     * Use <code>ssl</code> or <code>none</code>.
     * 
     * @see Context#SECURITY_PROTOCOL
     */
    public void setSecurityProtocol(String securityProtocol)
    {
        if (StringUtils.isNotBlank(securityProtocol))
        {
            this.securityProtocol = securityProtocol;
        }
    }

    /**
     * @see Context#SECURITY_PRINCIPAL
     */
    public String getSecurityPrincipalDistinguishedName()
    {
        return securityPrincipalDistinguishedName;
    }

    /**
     * The distinguished name for login of the application to the LDAP server.
     * <p>
     * Example: <code>CN=carl,OU=EthUsers,DC=d,DC=ethz,DC=ch</code>.
     * <p>
     * <strong>Mandatory.</strong>
     * 
     * @see Context#SECURITY_PRINCIPAL
     */
    public void setSecurityPrincipalDistinguishedName(String securityPrincipal)
    {
        this.securityPrincipalDistinguishedName = securityPrincipal;
    }

    public String getSecurityPrincipalPassword()
    {
        return securityPrincipalPassword;
    }

    /**
     * The password for login of the application to the LDAP server.
     * <p>
     * <strong>Mandatory.</strong>
     */
    public void setSecurityPrincipalPassword(String password)
    {
        this.securityPrincipalPassword = password;
    }

    /**
     * Default value: <code>simple</code>
     * 
     * @see Context#SECURITY_AUTHENTICATION
     */
    public String getSecurityAuthenticationMethod()
    {
        return securityAuthenticationMethod;
    }

    /**
     * @see Context#SECURITY_AUTHENTICATION
     */
    public void setSecurityAuthenticationMethod(String securityAuthenticationMethod)
    {
        if (StringUtils.isNotBlank(securityAuthenticationMethod))
        {
            this.securityAuthenticationMethod = securityAuthenticationMethod;
        }
    }

    /**
     * Default value: <code>follow</code>
     * 
     * @see Context#REFERRAL
     */
    public String getReferral()
    {
        return referral;
    }

    /**
     * @see Context#REFERRAL
     */
    public void setReferral(String referral)
    {
        if (StringUtils.isNotBlank(referral))
        {
            this.referral = referral;
        }
    }

    /**
     * Default value:
     * <code>(&(objectClass=organizationalPerson)(objectCategory=person)(objectClass=user)(%s))</code>
     */
    public String getQueryTemplate()
    {
        return queryTemplate;
    }

    /**
     * Needs to contain exactly one '<code>%s</code>' which will be replaced with the filter
     * condition, e.g. '<code>uid=mueller</code>'.
     */
    public void setQueryTemplate(String queryTemplate)
    {
        if (StringUtils.isNotBlank(queryTemplate))
        {
            this.queryTemplate = queryTemplate;
        }
    }

    public String getServerUrl()
    {
        return serverUrl;
    }

    /**
     * Example: <code>ldap://d.ethz.ch:636/DC=d,DC=ethz,DC=ch</code>
     * <p>
     * <strong>Mandatory.</strong>
     */
    public void setServerUrl(String ldapUrl)
    {
        this.serverUrl = ldapUrl;
    }

}
