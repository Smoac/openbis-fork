/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.shared.api.v3.util.JsonPropertyUtil;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.id.ObjectTechId")
public class ObjectTechId implements IObjectId
{

    private static final long serialVersionUID = 1L;

    private Long techId;

    public ObjectTechId(Long techId)
    {
        setTechId(techId);
    }

    @JsonIgnore
    public Long getTechId()
    {
        return techId;
    }

    //
    // JSON-RPC
    //

    protected ObjectTechId()
    {
    }

    @JsonIgnore
    private void setTechId(Long techId)
    {
        if (techId == null)
        {
            throw new IllegalArgumentException("TechId cannot be null");
        }
        this.techId = techId;
    }

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(techId);
    }

    @SuppressWarnings("unused")
    private void setIdAsString(String id)
    {
        this.techId = JsonPropertyUtil.toLongOrNull(id);
    }

    @Override
    public String toString()
    {
        return getTechId().toString();
    }

    @Override
    public int hashCode()
    {
        return ((getTechId() == null) ? 0 : getTechId().hashCode());
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
        ObjectTechId other = (ObjectTechId) obj;
        if (getTechId() == null)
        {
            if (other.getTechId() != null)
            {
                return false;
            }
        } else if (!getTechId().equals(other.getTechId()))
        {
            return false;
        }
        return true;
    }

}
