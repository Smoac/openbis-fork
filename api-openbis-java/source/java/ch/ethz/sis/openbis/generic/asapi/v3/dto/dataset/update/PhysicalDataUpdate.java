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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IFileFormatTypeId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.update.PhysicalDataUpdate")
public class PhysicalDataUpdate implements IUpdate
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    @Deprecated
    private FieldUpdateValue<IFileFormatTypeId> fileFormatTypeId = new FieldUpdateValue<IFileFormatTypeId>();

    @JsonProperty
    private FieldUpdateValue<Boolean> archivingRequested = new FieldUpdateValue<Boolean>();

    @JsonProperty
    private FieldUpdateValue<String> shareId = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<Long> size = new FieldUpdateValue<Long>();

    @JsonIgnore
    @Deprecated
    public FieldUpdateValue<IFileFormatTypeId> getFileFormatTypeId()
    {
        return fileFormatTypeId;
    }

    @JsonIgnore
    @Deprecated
    public void setFileFormatTypeId(IFileFormatTypeId fileFormatTypeId)
    {
        this.fileFormatTypeId.setValue(fileFormatTypeId);
    }

    @JsonIgnore
    public FieldUpdateValue<Boolean> isArchivingRequested()
    {
        return archivingRequested;
    }

    @JsonIgnore
    public void setArchivingRequested(boolean archivingRequested)
    {
        this.archivingRequested.setValue(archivingRequested);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getShareId()
    {
        return shareId;
    }

    @JsonIgnore
    public void setShareId(String shareId)
    {
        this.shareId.setValue(shareId);
    }

    @JsonIgnore
    public FieldUpdateValue<Long> getSize()
    {
        return size;
    }

    @JsonIgnore
    public void setSize(Long size)
    {
        this.size.setValue(size);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).toString();
    }

}
