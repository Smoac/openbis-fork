/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.List;

public class UserGroup
{
    private String name;

    private String key;
    
    private boolean enabled = true;

    private List<String> ldapGroupKeys;
    
    private List<String> users;

    private List<String> admins;

    private List<String> shareIds;

    public String getName()
    {
        return name;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public List<String> getAdmins()
    {
        return admins;
    }

    public void setAdmins(List<String> admins)
    {
        this.admins = admins;
    }

    public List<String> getLdapGroupKeys()
    {
        return ldapGroupKeys;
    }

    public List<String> getUsers()
    {
        return users;
    }

    public List<String> getShareIds()
    {
        return shareIds;
    }

    public void setShareIds(List<String> shareIds)
    {
        this.shareIds = shareIds;
    }

}