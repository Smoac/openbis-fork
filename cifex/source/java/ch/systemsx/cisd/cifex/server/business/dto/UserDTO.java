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

package ch.systemsx.cisd.cifex.server.business.dto;

import java.util.Date;

import ch.systemsx.cisd.cifex.server.common.Password;

/**
 * An <code>ID</code> extension which describes an user in the database.
 * 
 * @author Franz-Josef Elmer
 */
public class UserDTO extends ID
{
    private static final long serialVersionUID = 1L;

    private String userFullName;

    /**
     * Email Address of the user. Is not Unique!
     */
    private String email;

    private Password password;
    
    private String passwordHash;

    private UserDTO registrator;

    /**
     * The database id of the quota group that this user is in.
     */
    private Long quotaGroupId;
    
    /**
     * Whether this user is an administrator or not.
     * <p>
     * Note that an administrator is a <i>permanent</i> user as well.
     * </p>
     */
    private boolean admin;

    /**
     * Whether this user is currently active (set to false to deactivate a user).
     */
    private boolean active = true;

    private boolean externallyAuthenticated;

    private Date registrationDate;

    private Date expirationDate;

    /** 
     * How long (in days) the file registered by this user is going to stay in the system.
     */
    private Integer maxFileRetention;
    
    private boolean customMaxFileRetention;

    /** 
     * How long (in days) a temporary user registered by this user is going to stay in the system.
     */
    private Integer maxUserRetention;

    private boolean customMaxUserRetention;

    /**
     * Current total size of files uploaded by the user's quota group (in bytes).
     */
    private long currentFileSize;
    
    /**
     * Current total number of files uploaded by the user's quota group.
     */
    private int currentFileCount;
    
    /**
     * Maximum size of files allowed to be uploaded by the user's quota group (in MB)  
     */
    private Long maxFileSizePerQuotaGroupInMB;
    
    private boolean customMaxFileSizePerQuotaGroup;

    /**
     * Maximum total number of files allowed to be uploaded by the user's quota group.
     */
    private Integer maxFileCountPerQuotaGroup;

    private boolean customMaxFileCountPerQuotaGroup;

    /**
     * The unique userCode of the <code>UserDTO</code>.
     */
    private String userCode;

    public final boolean isExternallyAuthenticated()
    {
        return externallyAuthenticated;
    }

    public final void setExternallyAuthenticated(final boolean externallyAuthenticated)
    {
        this.externallyAuthenticated = externallyAuthenticated;
    }

    public final boolean isAdmin()
    {
        return admin;
    }

    public final void setAdmin(final boolean admin)
    {
        this.admin = admin;
    }

    public final String getEmail()
    {
        return email;
    }

    public final void setEmail(final String email)
    {
        this.email = (email == null) ? null : email.toLowerCase().trim();
    }

    public final Password getPassword()
    {
        return password;
    }

    public final void setPassword(final Password password)
    {
        this.password = password;
    }

    public String getPasswordHash()
    {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash)
    {
        this.passwordHash = passwordHash;
    }

    public final boolean isPermanent()
    {
        return (expirationDate == null);
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public final String getUserFullName()
    {
        return userFullName;
    }

    public final void setUserFullName(final String fullName)
    {
        this.userFullName = fullName;
    }

    public final Date getExpirationDate()
    {
        return expirationDate;
    }

    public final void setExpirationDate(final Date expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public final UserDTO getRegistrator()
    {
        return registrator;
    }

    public final void setRegistrator(UserDTO registrator)
    {
        this.registrator = registrator;
    }

    public final Long getQuotaGroupId()
    {
        return quotaGroupId;
    }

    public final void setQuotaGroupId(Long quotaGroupId)
    {
        this.quotaGroupId = quotaGroupId;
    }

    public String getUserCode()
    {
        return userCode;
    }

    public void setUserCode(String userCode)
    {
        this.userCode = userCode;
    }

    public final Integer getMaxFileRetention()
    {
        return maxFileRetention;
    }

    public final void setMaxFileRetention(Integer fileRetention)
    {
        this.maxFileRetention = fileRetention;
    }

    public final boolean isCustomMaxFileRetention()
    {
        return customMaxFileRetention;
    }

    public final void setCustomMaxFileRetention(boolean customFileRetention)
    {
        this.customMaxFileRetention = customFileRetention;
    }

    public void setMaxUserRetention(Integer userRetention)
    {
        this.maxUserRetention = userRetention;
    }

    public Integer getMaxUserRetention()
    {
        return maxUserRetention;
    }

    public final boolean isCustomMaxUserRetention()
    {
        return customMaxUserRetention;
    }

    public final void setCustomMaxUserRetention(boolean customUserRetention)
    {
        this.customMaxUserRetention = customUserRetention;
    }

    public void setCurrentFileSize(long currentFileSize)
    {
        this.currentFileSize = currentFileSize;
    }

    public long getCurrentFileSize()
    {
        return currentFileSize;
    }

    public void setCurrentFileCount(int currentFileCountInMB)
    {
        this.currentFileCount = currentFileCountInMB;
    }

    public int getCurrentFileCount()
    {
        return currentFileCount;
    }

    public void setMaxFileSizePerQuotaGroupInMB(Long maxFileSizePerQuotaGroupInMB)
    {
        this.maxFileSizePerQuotaGroupInMB = maxFileSizePerQuotaGroupInMB;
    }

    public Long getMaxFileSizePerQuotaGroupInMB()
    {
        return maxFileSizePerQuotaGroupInMB;
    }

    public final boolean isCustomMaxFileSizePerQuotaGroup()
    {
        return customMaxFileSizePerQuotaGroup;
    }

    public final void setCustomMaxFileSizePerQuotaGroup(boolean customMaxFileSizePerQuotaGroup)
    {
        this.customMaxFileSizePerQuotaGroup = customMaxFileSizePerQuotaGroup;
    }

    public void setMaxFileCountPerQuotaGroup(Integer maxFileCountPerQuotaGroup)
    {
        this.maxFileCountPerQuotaGroup = maxFileCountPerQuotaGroup;
    }

    public Integer getMaxFileCountPerQuotaGroup()
    {
        return maxFileCountPerQuotaGroup;
    }

    public final boolean isCustomMaxFileCountPerQuotaGroup()
    {
        return customMaxFileCountPerQuotaGroup;
    }

    public final void setCustomMaxFileCountPerQuotaGroup(boolean customMaxFileCountPerQuotaGroup)
    {
        this.customMaxFileCountPerQuotaGroup = customMaxFileCountPerQuotaGroup;
    }

}
