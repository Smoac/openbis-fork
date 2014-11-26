/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.id;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Base class for ids that identify objects by identifiers. An identifier is a mutable user-defined string. An identifier is assigned to an object
 * during the object creation but can change afterwards. An object's identifier is not guaranteed to be always the same, e.g. a sample identifier
 * changes when the sample is moved to a different space.
 * 
 * @author pkupczyk
 */
@JsonObject("ObjectIdentifier")
public abstract class ObjectIdentifier implements IObjectId
{

    private static final long serialVersionUID = 1L;

    private String identifier;

    public ObjectIdentifier(String identifier)
    {
        setIdentifier(identifier);
    }

    public String getIdentifier()
    {
        return this.identifier;
    }

    //
    // JSON-RPC
    //

    protected ObjectIdentifier()
    {
    }

    private void setIdentifier(String identifier)
    {
        if (identifier == null)
        {
            throw new IllegalArgumentException("Identifier id cannot be null");
        }
        this.identifier = identifier;
    }

    @Override
    public String toString()
    {
        return getIdentifier();
    }

    @Override
    public int hashCode()
    {
        return ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        ObjectIdentifier other = (ObjectIdentifier) obj;
        return getIdentifier() == null ? getIdentifier() == other.getIdentifier() : getIdentifier().equals(other.getIdentifier());
    }

}
