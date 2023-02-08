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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.id;

/**
 * Base class for ids that identify objects by identifier.
 * 
 * @author pkupczyk
 */
public abstract class ObjectIdentifierId implements IObjectId
{

    private static final long serialVersionUID = 1L;

    private String identifier;

    public ObjectIdentifierId(String identifier)
    {
        setIdentifier(identifier);
    }

    public String getIdentifier()
    {
        return identifier;
    }

    private void setIdentifier(String identifier)
    {
        if (identifier == null)
        {
            throw new IllegalArgumentException("Identifier cannot be null");
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
        return ((identifier == null) ? 0 : identifier.hashCode());
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
        ObjectIdentifierId other = (ObjectIdentifierId) obj;
        if (identifier == null)
        {
            if (other.identifier != null)
            {
                return false;
            }
        } else if (!identifier.equals(other.identifier))
        {
            return false;
        }
        return true;
    }

}
