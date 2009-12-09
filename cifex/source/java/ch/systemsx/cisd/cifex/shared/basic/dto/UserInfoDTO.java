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

package ch.systemsx.cisd.cifex.shared.basic.dto;

import java.util.Date;

/**
 * A class that describes a user in the presentation layer.
 * 
 * @author Christian Ribeaud
 */
public final class UserInfoDTO extends BasicUserInfoDTO
{
    private static final long serialVersionUID = 1L;

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

    private Date expirationDate;
    
    private Date registrationDate;

    private UserInfoDTO registrator;

    private boolean externallyAuthenticated;

    /**
     * How long (in days) the file registered by this user is going to stay in the system at the
     * max.
     */
    private Integer maxFileRetention;

    private boolean customMaxFileRetention;

    /**
     * How long (in days) a temporary user registered by this user is going to stay in the system at
     * the max.
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

    public final boolean isAdmin()
    {
        return admin;
    }

    public final void setAdmin(final boolean admin)
    {
        this.admin = admin;
    }

    public final boolean isPermanent()
    {
        return expirationDate == null;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public final Date getExpirationDate()
    {
        return expirationDate;
    }

    public final void setExpirationDate(final Date expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public final UserInfoDTO getRegistrator()
    {
        return registrator;
    }

    public final void setRegistrator(UserInfoDTO registrator)
    {
        this.registrator = registrator;
    }

    public final boolean isExternallyAuthenticated()
    {

        return externallyAuthenticated;
    }

    public final void setExternallyAuthenticated(final boolean externallyAuthenticated)
    {
        this.externallyAuthenticated = externallyAuthenticated;
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

    public final Integer getMaxUserRetention()
    {
        return maxUserRetention;
    }

    public final void setMaxUserRetention(Integer userRetention)
    {
        this.maxUserRetention = userRetention;
    }

    public final boolean isCustomMaxUserRetention()
    {
        return customMaxUserRetention;
    }

    public final void setCustomMaxUserRetention(boolean customUserRetention)
    {
        this.customMaxUserRetention = customUserRetention;
    }

    public final long getCurrentFileSize()
    {
        return currentFileSize;
    }

    public final void setCurrentFileSize(long currentFileSize)
    {
        this.currentFileSize = currentFileSize;
    }

    public final int getCurrentFileCount()
    {
        return currentFileCount;
    }

    public final void setCurrentFileCount(int currentFileCount)
    {
        this.currentFileCount = currentFileCount;
    }

    public final Long getMaxFileSizePerQuotaGroupInMB()
    {
        return maxFileSizePerQuotaGroupInMB;
    }

    public final void setMaxFileSizePerQuotaGroupInMB(Long maxFileSizePerQuotaGroupInMB)
    {
        this.maxFileSizePerQuotaGroupInMB = maxFileSizePerQuotaGroupInMB;
    }

    public final boolean isCustomMaxFileSizePerQuotaGroup()
    {
        return customMaxFileSizePerQuotaGroup;
    }

    public final void setCustomMaxFileSizePerQuotaGroup(boolean customMaxFileSizePerQuotaGroup)
    {
        this.customMaxFileSizePerQuotaGroup = customMaxFileSizePerQuotaGroup;
    }

    public final Integer getMaxFileCountPerQuotaGroup()
    {
        return maxFileCountPerQuotaGroup;
    }

    public final void setMaxFileCountPerQuotaGroup(Integer maxFileCountPerQuotaGroup)
    {
        this.maxFileCountPerQuotaGroup = maxFileCountPerQuotaGroup;
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