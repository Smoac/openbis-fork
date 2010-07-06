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

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;

/**
 * Abstract super class of all Data Transfer Objects (DTOs) with a unique ID.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class ID implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long id;

    protected static Long getAndCheckID(final ID objectWithID, final Long id)
    {
        if (objectWithID == null)
        {
            return id;
        }
        assert id == null || id.equals(objectWithID.getID());
        return objectWithID.getID();
    }

    /**
     * Returns ID.
     * 
     * @return <code>null</code> when undefined.
     */
    public final Long getID()
    {
        return id;
    }

    /**
     * Sets ID.
     * 
     * @param id New value. Can be <code>null</code>.
     */
    public final void setID(final Long id)
    {
        this.id = id;
    }
    
    //
    // Object
    //

    @Override
    public int hashCode()
    {
        return ((id == null) ? 0 : id.hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof ID == false)
        {
            return false;
        }
        final ID other = (ID) obj;
        if (id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        } else if (id.equals(other.id) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }
}
