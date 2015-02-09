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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.entity.experiment.ExperimentCreation")
public class ExperimentCreation implements Serializable
{
    private static final long serialVersionUID = 1L;

    private IEntityTypeId typeId;

    private IProjectId projectId;

    private String code;

    private List<? extends ITagId> tagIds;

    private Map<String, String> properties = new HashMap<String, String>();

    private List<AttachmentCreation> attachments;

    public void setTypeId(IEntityTypeId typeId)
    {
        this.typeId = typeId;
    }

    public void setProjectId(IProjectId projectId)
    {
        this.projectId = projectId;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public IEntityTypeId getTypeId()
    {
        return typeId;
    }

    public IProjectId getProjectId()
    {
        return projectId;
    }

    public String getCode()
    {
        return code;
    }

    public List<? extends ITagId> getTagIds()
    {
        return tagIds;
    }

    public void setTagIds(List<? extends ITagId> tagIds)
    {
        this.tagIds = tagIds;
    }

    // NOTE: does this construct work well with JSON?
    public void setProperty(String key, String value)
    {
        this.properties.put(key, value);
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public List<AttachmentCreation> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<AttachmentCreation> attachments)
    {
        this.attachments = attachments;
    }

}
