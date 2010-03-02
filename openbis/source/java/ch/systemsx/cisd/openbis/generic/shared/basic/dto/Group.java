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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * The <i>GWT</i> equivalent to GroupPE.
 * 
 * @author Franz-Josef Elmer
 */
public final class Group extends CodeWithRegistration<Group> implements IIdHolder, ISpaceUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    private String description;

    private DatabaseInstance instance;

    private String identifier;

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    public DatabaseInstance getInstance()
    {
        return instance;
    }

    public void setInstance(final DatabaseInstance instance)
    {
        this.instance = instance;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(final String identifier)
    {
        this.identifier = identifier;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    //
    // Object
    //

    // equals and hashCode methods based on simple toString value representation
    // instead of Equals/HashCodeBuilder from apache.commons library like in GroupPE

    @Override
    public String toString()
    {
        return getInstance().getCode() + "/" + getCode();
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Group == false)
        {
            return false;
        }
        final Group that = (Group) obj;
        return this.toString().equals(that.toString());
    }

    @Override
    public final int hashCode()
    {
        return this.toString().hashCode();
    }

}
