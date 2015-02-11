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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.AttachmentListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.entity.sample.SampleUpdate")
public class SampleUpdate implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ISampleId sampleId;

    @JsonProperty
    private FieldUpdateValue<IExperimentId> experimentId = new FieldUpdateValue<IExperimentId>();

    @JsonProperty
    private FieldUpdateValue<ISpaceId> spaceId = new FieldUpdateValue<ISpaceId>();

    @JsonProperty
    private IdListUpdateValue<ITagId> tagIds = new IdListUpdateValue<ITagId>();

    @JsonProperty
    private Map<String, String> properties = new HashMap<String, String>();

    @JsonProperty
    private FieldUpdateValue<ISampleId> containerId = new FieldUpdateValue<ISampleId>();

    @JsonProperty
    private IdListUpdateValue<ISampleId> containedIds = new IdListUpdateValue<ISampleId>();

    @JsonProperty
    private IdListUpdateValue<ISampleId> parentIds = new IdListUpdateValue<ISampleId>();

    @JsonProperty
    private IdListUpdateValue<ISampleId> childIds = new IdListUpdateValue<ISampleId>();

    @JsonProperty
    private AttachmentListUpdateValue attachments = new AttachmentListUpdateValue();

    @JsonIgnore
    public ISampleId getSampleId()
    {
        return sampleId;
    }

    @JsonIgnore
    public void setSampleId(ISampleId sampleId)
    {
        this.sampleId = sampleId;
    }

    @JsonIgnore
    public FieldUpdateValue<IExperimentId> getExperimentId()
    {
        return experimentId;
    }

    @JsonIgnore
    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId.setValue(experimentId);
    }

    @JsonIgnore
    public FieldUpdateValue<ISpaceId> getSpaceId()
    {
        return spaceId;
    }

    @JsonIgnore
    public void setSpaceId(ISpaceId spaceId)
    {
        this.spaceId.setValue(spaceId);
    }

    @JsonIgnore
    public FieldUpdateValue<ISampleId> getContainerId()
    {
        return containerId;
    }

    @JsonIgnore
    public void setContainerId(ISampleId containerId)
    {
        this.containerId.setValue(containerId);
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

    @JsonIgnore
    public IdListUpdateValue<ISampleId> getContainedIds()
    {
        return containedIds;
    }

    @JsonIgnore
    public void setContainedActions(List<ListUpdateAction<ISampleId>> actions)
    {
        containedIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<ISampleId> getParentIds()
    {
        return parentIds;
    }

    @JsonIgnore
    public void setParentActions(List<ListUpdateAction<ISampleId>> actions)
    {
        parentIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<ISampleId> getChildIds()
    {
        return childIds;
    }

    @JsonIgnore
    public void setChildActions(List<ListUpdateAction<ISampleId>> actions)
    {
        childIds.setActions(actions);
    }

    @JsonIgnore
    public AttachmentListUpdateValue getAttachments()
    {
        return attachments;
    }

    @JsonIgnore
    public void setAttachmentsActions(List<ListUpdateAction<Object>> actions)
    {
        attachments.setActions(actions);
    }

}
