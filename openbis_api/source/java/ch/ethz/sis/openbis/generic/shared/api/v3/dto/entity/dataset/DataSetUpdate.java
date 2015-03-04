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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.entity.dataset.DataSetUpdate")
public class DataSetUpdate implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IDataSetId dataSetId;

    @JsonProperty
    private FieldUpdateValue<IExperimentId> experimentId = new FieldUpdateValue<IExperimentId>();

    @JsonProperty
    private FieldUpdateValue<ISampleId> sampleId = new FieldUpdateValue<ISampleId>();

    @JsonProperty
    private FieldUpdateValue<ExternalDataUpdate> externalData = new FieldUpdateValue<ExternalDataUpdate>();

    @JsonProperty
    private IdListUpdateValue<ITagId> tagIds = new IdListUpdateValue<ITagId>();

    @JsonProperty
    private Map<String, String> properties = new HashMap<String, String>();

    @JsonProperty
    private IdListUpdateValue<IDataSetId> containerIds = new IdListUpdateValue<IDataSetId>();

    @JsonProperty
    private IdListUpdateValue<IDataSetId> containedIds = new IdListUpdateValue<IDataSetId>();

    @JsonProperty
    private IdListUpdateValue<IDataSetId> parentIds = new IdListUpdateValue<IDataSetId>();

    @JsonProperty
    private IdListUpdateValue<IDataSetId> childIds = new IdListUpdateValue<IDataSetId>();

    @JsonIgnore
    public IDataSetId getDataSetId()
    {
        return dataSetId;
    }

    @JsonIgnore
    public void setDataSetId(IDataSetId dataSetId)
    {
        this.dataSetId = dataSetId;
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
    public FieldUpdateValue<ISampleId> getSampleId()
    {
        return sampleId;
    }

    @JsonIgnore
    public void setSampleId(ISampleId sampleId)
    {
        this.sampleId.setValue(sampleId);
    }

    public FieldUpdateValue<ExternalDataUpdate> getExternalData()
    {
        return externalData;
    }

    public void setExternalData(ExternalDataUpdate externalData)
    {
        this.externalData.setValue(externalData);
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
    public IdListUpdateValue<IDataSetId> getContainerIds()
    {
        return containerIds;
    }

    @JsonIgnore
    public void setContainerActions(List<ListUpdateAction<IDataSetId>> actions)
    {
        containerIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<IDataSetId> getContainedIds()
    {
        return containedIds;
    }

    @JsonIgnore
    public void setContainedActions(List<ListUpdateAction<IDataSetId>> actions)
    {
        containedIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<IDataSetId> getParentIds()
    {
        return parentIds;
    }

    @JsonIgnore
    public void setParentActions(List<ListUpdateAction<IDataSetId>> actions)
    {
        parentIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<IDataSetId> getChildIds()
    {
        return childIds;
    }

    @JsonIgnore
    public void setChildActions(List<ListUpdateAction<IDataSetId>> actions)
    {
        childIds.setActions(actions);
    }

}
