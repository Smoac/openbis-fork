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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;

/**
 * The <i>GWT</i> equivalent to {@link GroupPE}.
 * 
 * @author Franz-Josef Elmer
 */
public final class Group extends CodeWithRegistration
{
    private String description;

    private Person leader;

    private Group parent;

    private DatabaseInstance instance;

    private DataStore dataStore;

    private String identifier;

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    public Person getLeader()
    {
        return leader;
    }

    public void setLeader(final Person leader)
    {
        this.leader = leader;
    }

    public Group getParent()
    {
        return parent;
    }

    public void setParent(final Group parent)
    {
        this.parent = parent;
    }

    public DatabaseInstance getInstance()
    {
        return instance;
    }

    public void setInstance(final DatabaseInstance instance)
    {
        this.instance = instance;
    }

    public DataStore getDataStore()
    {
        return dataStore;
    }

    public void setDataStore(final DataStore dataStore)
    {
        this.dataStore = dataStore;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(final String identifier)
    {
        this.identifier = identifier;
    }
}
