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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment;

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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.entity.experiment.ExperimentUpdate")
public class ExperimentUpdate implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IExperimentId experimentId;

    @JsonProperty
    private Map<String, String> properties = new HashMap<String, String>();

    @JsonProperty
    private FieldUpdateValue<IProjectId> projectId = new FieldUpdateValue<IProjectId>();

    @JsonProperty
    private IdListUpdateValue<ITagId> tagIds = new IdListUpdateValue<ITagId>();

    @JsonProperty
    private AttachmentListUpdateValue attachments = new AttachmentListUpdateValue();

    @JsonIgnore
    public IExperimentId getExperimentId()
    {
        return experimentId;
    }

    @JsonIgnore
    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId = experimentId;
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
    public void setProjectId(IProjectId projectId)
    {
        this.projectId.setValue(projectId);
    }

    @JsonIgnore
    public FieldUpdateValue<IProjectId> getProjectId()
    {
        return projectId;
    }

    @JsonIgnore
    public IdListUpdateValue<ITagId> getTagIds()
    {
        return tagIds;
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
