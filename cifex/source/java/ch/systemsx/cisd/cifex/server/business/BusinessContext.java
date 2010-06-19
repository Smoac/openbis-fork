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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PasswordGenerator;

/**
 * Bean implementing {@link IBusinessContext}.
 * 
 * @author Franz-Josef Elmer
 */
class BusinessContext implements IBusinessContext
{
    /** The session timeout (in minutes). */
    private int sessionTimeoutMin;
    
    /** The root location where uploaded files are going to be stored. */
    private File fileStore;

    /** How long (in days) an uploaded file is going to stay in the system by default? */
    private int fileRetention;

    /** How long (in days) a temporary user is going to stay in the system by default? */
    private int userRetention;

    /** How long (in days) an uploaded file is going to stay in the system at the max? */
    private int maxFileRetention;

    /** How long (in days) a temporary user is going to stay in the system at the max? */
    private int maxUserRetention;

    /**
     * Maximum size of all uploaded files allowed per quota group in MB, <code>null</code> for 'no
     * limit'.
     */
    private Long maxFileSizePerQuotaGroupInMB;

    /**
     * Maximum number of all uploaded files allowed per quota group. <code>null</code> for 'no
     * limit'
     */
    private Integer maxFileCountPerQuotaGroup;

    /** Whether new externally authenticated users should start active or not. */
    private boolean newExternallyAuthenticatedUserStartActive;

    private IMailClient mailClient;

    private PasswordGenerator passwordGenerator;

    private UserHttpSessionHolder userHttpSessionHolder;

    /** The logger object for user actions. */
    private IUserActionLog userActionLogHttp;

    /** The logger object for user actions. */
    private IUserActionLog userActionLogRpc;

    /**
     * The URL to use for emails in override mode.
     */
    private String overrideURL;

    private String systemVersion;

    private int triggerPermits;

    private final Set<String> allowedIPsForSetSessionUser = new HashSet<String>();
    
    public int getSessionTimeoutMin()
    {
        return sessionTimeoutMin;
    }

    public void setSessionTimeoutMin(int sessionTimeoutMin)
    {
        this.sessionTimeoutMin = sessionTimeoutMin;
    }
    
    public int getTriggerPermits()
    {
        return triggerPermits;
    }

    public void setTriggerPermits(int triggerPermits)
    {
        this.triggerPermits = triggerPermits;
    }

    public final IUserSessionInvalidator getUserSessionInvalidator()
    {
        return userHttpSessionHolder;
    }

    public final void setUserHttpSessionHolder(final UserHttpSessionHolder userHttpSessionHolder)
    {
        this.userHttpSessionHolder = userHttpSessionHolder;
    }

    /**
     * Returns the logger of user behavior for pure HTTP sessions.
     */
    public IUserActionLog getUserActionLogHttp()
    {
        return userActionLogHttp;
    }

    /**
     * Sets the logger of user behavior for pure HTTP sessions.
     */
    public void setUserActionLogHttp(IUserActionLog userActionLog)
    {
        this.userActionLogHttp = userActionLog;
    }

    /**
     * Returns the logger of user behavior for RPC sessions.
     */
    public IUserActionLog getUserActionLogRpc()
    {
        return userActionLogRpc;
    }

    /**
     * Sets the logger of user behavior for RPC sessions.
     */
    public void setUserActionLogRpc(IUserActionLog userActionLog)
    {
        this.userActionLogRpc = userActionLog;
    }

    public final int getFileRetention()
    {
        return fileRetention;
    }

    public final void setFileRetention(final int fileRetention)
    {
        this.fileRetention = fileRetention;
    }

    public void setMaxFileRetention(int maxFileRetention)
    {
        this.maxFileRetention = maxFileRetention;
    }

    public int getMaxFileRetention()
    {
        return maxFileRetention;
    }

    public final File getFileStore()
    {
        return fileStore;
    }

    public final void setFileStore(final File fileStore)
    {
        this.fileStore = fileStore;
    }

    public final int getUserRetention()
    {
        return userRetention;
    }

    public final void setUserRetention(final int userRetention)
    {
        this.userRetention = userRetention;
    }

    public void setMaxUserRetention(int maxUserRetention)
    {
        this.maxUserRetention = maxUserRetention;
    }

    public int getMaxUserRetention()
    {
        return maxUserRetention;
    }

    public void setMaxFileSizePerQuotaGroupInMB(Long maxFileSizePerQuotaGroupInMB)
    {
        this.maxFileSizePerQuotaGroupInMB = maxFileSizePerQuotaGroupInMB;
    }

    public Long getMaxFileSizePerQuotaGroupInMB()
    {
        return maxFileSizePerQuotaGroupInMB;
    }

    public void setMaxFileCountPerQuotaGroup(Integer maxFileCountPerQuotaGroup)
    {
        this.maxFileCountPerQuotaGroup = maxFileCountPerQuotaGroup;
    }

    public Integer getMaxFileCountPerQuotaGroup()
    {
        return maxFileCountPerQuotaGroup;
    }

    public boolean isNewExternallyAuthenticatedUserStartActive()
    {
        return newExternallyAuthenticatedUserStartActive;
    }

    public void setNewExternallyAuthenticatedUserStartActive(
            boolean newExternallyAuthenticateUserStartActive)
    {
        this.newExternallyAuthenticatedUserStartActive = newExternallyAuthenticateUserStartActive;
    }

    public final IMailClient getMailClient()
    {
        return mailClient;
    }

    public final void setMailClient(final IMailClient mailClient)
    {
        this.mailClient = mailClient;
    }

    public final PasswordGenerator getPasswordGenerator()
    {
        return passwordGenerator;
    }

    public final void setPasswordGenerator(final PasswordGenerator passwordGenerator)
    {
        this.passwordGenerator = passwordGenerator;
    }

    public final String getSystemVersion()
    {
        return systemVersion;
    }

    public final void setOverrideURL(String overrideURL)
    {
        this.overrideURL = overrideURL;
    }

    public final String getOverrideURL()
    {
        return overrideURL;
    }

    public final void setSystemVersion(final String systemVersion)
    {
        this.systemVersion = systemVersion;
    }

    public final Set<String> getAllowedIPsForSetSessionUser()
    {
        return allowedIPsForSetSessionUser;
    }
    
    public final void setAllowedIPsForSetSessionUser(String allowedIPs)
    {
        allowedIPsForSetSessionUser.clear();
        for (String ip : StringUtils.split(allowedIPs, ','))
        {
            allowedIPsForSetSessionUser.add(ip.trim());
        }
    }

}
