/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Jakub Straszewski
 */
@JsonObject("dto.entity.sample.MaterialUpdate")
public class MaterialUpdate implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IMaterialId materialId;

    @JsonProperty
    private IdListUpdateValue<ITagId> tagIds = new IdListUpdateValue<ITagId>();

    @JsonProperty
    private Map<String, String> properties = new HashMap<String, String>();

    @JsonIgnore
    public IMaterialId getMaterialId()
    {
        return materialId;
    }

    @JsonIgnore
    public void setMaterialId(IMaterialId materialId)
    {
        this.materialId = materialId;
    }

    @JsonIgnore
    public void setProperty(String key, String value)
    {
        properties.put(key, value);
    }

    @JsonIgnore
    public Map<String, String> getProperties()
    {
        return properties;
    }

    @JsonIgnore
    public IdListUpdateValue<ITagId> getTagIds()
    {
        return tagIds;
    }

    @JsonIgnore
    public void setTagActions(List<ListUpdateAction<ITagId>> actions)
    {
        tagIds.setActions(actions);
    }
}
