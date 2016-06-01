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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IFileFormatTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ILocatorTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IStorageFormatId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.create.PhysicalDataCreation")
public class PhysicalDataCreation implements ICreation
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String shareId;

    @JsonProperty
    private String location;

    @JsonProperty
    private Long size;

    @JsonProperty
    private IStorageFormatId storageFormatId;

    @JsonProperty
    private IFileFormatTypeId fileFormatTypeId;

    @JsonProperty
    private ILocatorTypeId locatorTypeId;

    @JsonProperty
    private Complete complete;

    @JsonProperty
    private Integer speedHint;

    public String getShareId()
    {
        return shareId;
    }

    public void setShareId(String shareId)
    {
        this.shareId = shareId;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public Long getSize()
    {
        return size;
    }

    public void setSize(Long size)
    {
        this.size = size;
    }

    public IStorageFormatId getStorageFormatId()
    {
        return storageFormatId;
    }

    public void setStorageFormatId(IStorageFormatId storageFormatId)
    {
        this.storageFormatId = storageFormatId;
    }

    public IFileFormatTypeId getFileFormatTypeId()
    {
        return fileFormatTypeId;
    }

    public void setFileFormatTypeId(IFileFormatTypeId fileFormatTypeId)
    {
        this.fileFormatTypeId = fileFormatTypeId;
    }

    public ILocatorTypeId getLocatorTypeId()
    {
        return locatorTypeId;
    }

    public void setLocatorTypeId(ILocatorTypeId locatorTypeId)
    {
        this.locatorTypeId = locatorTypeId;
    }

    public Complete getComplete()
    {
        return complete;
    }

    public void setComplete(Complete complete)
    {
        this.complete = complete;
    }

    public Integer getSpeedHint()
    {
        return speedHint;
    }

    public void setSpeedHint(Integer speedHint)
    {
        this.speedHint = speedHint;
    }

}
