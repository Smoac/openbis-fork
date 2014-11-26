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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.CreationId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("SampleCreation")
public class SampleCreation implements Serializable
{
    private static final long serialVersionUID = 1L;

    private IEntityTypeId typeId;

    private IExperimentId experimentId;

    private ISpaceId spaceId;

    private String code;

    private List<? extends ITagId> tagIds;

    private Map<String, String> properties = new HashMap<String, String>();

    private ISampleId containerId;

    private List<? extends ISampleId> containedIds;

    private List<? extends ISampleId> parentIds;

    private List<? extends ISampleId> childIds;

    private List<AttachmentCreation> attachments;

    private CreationId creationId;

    public IEntityTypeId getTypeId()
    {
        return this.typeId;
    }

    public void setTypeId(IEntityTypeId typeId)
    {
        this.typeId = typeId;
    }

    public IExperimentId getExperimentId()
    {
        return this.experimentId;
    }

    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId = experimentId;
    }

    public ISpaceId getSpaceId()
    {
        return this.spaceId;
    }

    public void setSpaceId(ISpaceId spaceId)
    {
        this.spaceId = spaceId;
    }

    public String getCode()
    {
        return this.code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public List<? extends ITagId> getTagIds()
    {
        return this.tagIds;
    }

    public void setTagIds(List<? extends ITagId> tagIds)
    {
        this.tagIds = tagIds;
    }

    public ISampleId getContainerId()
    {
        return this.containerId;
    }

    public void setContainerId(ISampleId containerId)
    {
        this.containerId = containerId;
    }

    public List<? extends ISampleId> getContainedIds()
    {
        return this.containedIds;
    }

    public void setContainedIds(List<? extends ISampleId> containedIds)
    {
        this.containedIds = containedIds;
    }

    public List<? extends ISampleId> getParentIds()
    {
        return this.parentIds;
    }

    public void setParentIds(List<? extends ISampleId> parentIds)
    {
        this.parentIds = parentIds;
    }

    public List<? extends ISampleId> getChildIds()
    {
        return this.childIds;
    }

    public void setChildIds(List<? extends ISampleId> childIds)
    {
        this.childIds = childIds;
    }

    public List<AttachmentCreation> getAttachments()
    {
        return this.attachments;
    }

    public void setAttachments(List<AttachmentCreation> attachments)
    {
        this.attachments = attachments;
    }

    // NOTE: does this construct work well with JSON?
    public void setProperty(String key, String value)
    {
        this.properties.put(key, value);
    }

    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    public CreationId getCreationId()
    {
        return this.creationId;
    }

    public void setCreationId(CreationId creationId)
    {
        this.creationId = creationId;
    }

}
