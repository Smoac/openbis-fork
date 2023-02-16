/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
 * Base class for ids that identify objects by tech id.
 * 
 * @author pkupczyk
 */
public abstract class ObjectTechIdId implements IObjectId
{

    private static final long serialVersionUID = 1L;

    private Long techId;

    public ObjectTechIdId(Long techId)
    {
        setTechId(techId);
    }

    public Long getTechId()
    {
        return techId;
    }

    private void setTechId(Long techId)
    {
        if (techId == null)
        {
            throw new IllegalArgumentException("TechId cannot be null");
        }
        this.techId = techId;
    }

    @Override
    public String toString()
    {
        return getTechId().toString();
    }

    @Override
    public int hashCode()
    {
        return ((techId == null) ? 0 : techId.hashCode());
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
        ObjectTechIdId other = (ObjectTechIdId) obj;
        if (techId == null)
        {
            if (other.techId != null)
            {
                return false;
            }
        } else if (!techId.equals(other.techId))
        {
            return false;
        }
        return true;
    }

}
