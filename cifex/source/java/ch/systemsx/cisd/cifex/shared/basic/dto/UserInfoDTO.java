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
 * A small class that describes an user.
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
     * Whether this user is <i>permanent</i> or not.
     * <p>
     * A non-permanent user has a non-<code>null</code> {@link #expirationDate} value.
     * </p>
     */
    private boolean permanent;
    
    /**
     * Whether this user is currently active (set to false to deactivate a user).
     */
    private boolean active = true;

    private Date expirationDate;

    private UserInfoDTO registrator;

    private boolean externallyAuthenticated;

    private Integer fileRetention;

    private Long maxUploadRequestSizeInMB;
    
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
        return permanent;
    }

    public final void setPermanent(final boolean permanent)
    {
        this.permanent = permanent;
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

    public final Integer getFileRetention()
    {
        return fileRetention;
    }

    public final void setFileRetention(Integer fileRetention)
    {
        this.fileRetention = fileRetention;
    }

    public final Long getMaxUploadRequestSizeInMB()
    {
        return maxUploadRequestSizeInMB;
    }

    public final void setMaxUploadRequestSizeInMB(Long maxUploadRequestSizeInMB)
    {
        this.maxUploadRequestSizeInMB = maxUploadRequestSizeInMB;
    }

}