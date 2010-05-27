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

package ch.systemsx.cisd.cifex.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The part of the configuration of CIFEX that the client cares about.
 * <p>
 * The information in this object is supposed to be unchanged during the whole session.
 * 
 * @author Bernd Rinn
 */
public class Configuration implements IsSerializable
{

    private int fileRetention;

    private int userRetention;

    private int maxFileRetention;

    private int maxUserRetention;

    private Long maxFileSizePerQuotaGroupInMB;

    private Integer maxFileCountPerQuotaGroup;

    private String systemVersion;

    private Boolean systemHasExternalAuthentication;

    public final int getFileRetention()
    {
        return fileRetention;
    }

    public final void setFileRetention(int fileRetention)
    {
        this.fileRetention = fileRetention;
    }

    public int getMaxFileRetention()
    {
        return maxFileRetention;
    }

    public void setMaxFileRetention(int maxFileRetention)
    {
        this.maxFileRetention = maxFileRetention;
    }

    public final int getUserRetention()
    {
        return userRetention;
    }

    public final void setUserRetention(int userRetention)
    {
        this.userRetention = userRetention;
    }

    public int getMaxUserRetention()
    {
        return maxUserRetention;
    }

    public void setMaxUserRetention(int maxUserRetention)
    {
        this.maxUserRetention = maxUserRetention;
    }

    public final Long getMaxFileSizePerQuotaGroupInMB()
    {
        return maxFileSizePerQuotaGroupInMB;
    }

    public final void setMaxFileSizePerQuotaGroupInMB(Long maxFileSizePerQuotaGroupInMB)
    {
        this.maxFileSizePerQuotaGroupInMB = maxFileSizePerQuotaGroupInMB;
    }

    public final Integer getMaxFileCountPerQuotaGroup()
    {
        return maxFileCountPerQuotaGroup;
    }

    public final void setMaxFileCountPerQuotaGroup(Integer maxFileCountPerQuotaGroup)
    {
        this.maxFileCountPerQuotaGroup = maxFileCountPerQuotaGroup;
    }

    public final String getSystemVersion()
    {
        return systemVersion;
    }

    public final void setSystemVersion(final String systemVersion)
    {
        this.systemVersion = systemVersion;
    }

    public Boolean getSystemHasExternalAuthentication()
    {
        return systemHasExternalAuthentication;
    }

    public void setSystemHasExternalAuthentication(Boolean systemHasExternalAuthentication)
    {
        this.systemHasExternalAuthentication = systemHasExternalAuthentication;
    }

}
