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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("SampleUpdate")
public class SampleUpdate implements Serializable
{
    private static final long serialVersionUID = 1L;

    private ISampleId sampleId;

    private FieldUpdateValue<IExperimentId> experimentId = new FieldUpdateValue<IExperimentId>();

    private FieldUpdateValue<ISpaceId> spaceId = new FieldUpdateValue<ISpaceId>();

    private ListUpdateValue<ITagId> tagIds = new ListUpdateValue<ITagId>();

    private Map<String, String> properties = new HashMap<String, String>();

    private FieldUpdateValue<ISampleId> containerId = new FieldUpdateValue<ISampleId>();

    private ListUpdateValue<ISampleId> containedIds = new ListUpdateValue<ISampleId>();

    private ListUpdateValue<ISampleId> parentIds = new ListUpdateValue<ISampleId>();

    private ListUpdateValue<ISampleId> childIds = new ListUpdateValue<ISampleId>();

    public ISampleId getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(ISampleId sampleId)
    {
        this.sampleId = sampleId;
    }

    public FieldUpdateValue<IExperimentId> getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId.setValue(experimentId);
    }

    public FieldUpdateValue<ISpaceId> getSpaceId()
    {
        return spaceId;
    }

    public void setSpaceId(ISpaceId spaceId)
    {
        this.spaceId.setValue(spaceId);
    }

    public FieldUpdateValue<ISampleId> getContainerId()
    {
        return containerId;
    }

    public void setContainerId(ISampleId containerId)
    {
        this.containerId.setValue(containerId);
    }

    public void setProperty(String key, String value)
    {
        properties.put(key, value);
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public ListUpdateValue<ITagId> getTagIds()
    {
        return tagIds;
    }

    public void setTagActions(List<ListUpdateAction<ITagId>> actions)
    {
        tagIds.setActions(actions);
    }

    public ListUpdateValue<ISampleId> getContainedIds()
    {
        return containedIds;
    }

    public void setContainedActions(List<ListUpdateAction<ISampleId>> actions)
    {
        containedIds.setActions(actions);
    }

    public ListUpdateValue<ISampleId> getParentIds()
    {
        return parentIds;
    }

    public void setParentActions(List<ListUpdateAction<ISampleId>> actions)
    {
        parentIds.setActions(actions);
    }

    public ListUpdateValue<ISampleId> getChildIds()
    {
        return childIds;
    }

    public void setChildActions(List<ListUpdateAction<ISampleId>> actions)
    {
        childIds.setActions(actions);
    }

}
