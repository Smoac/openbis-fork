/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author Piotr Buczek
 */
public class BasicDataSetUpdates implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private TechId datasetId;

    private Date version;

    // ----- the data which should be changed:

    private String[] modifiedParentDatasetCodesOrNull;

    private String fileFormatTypeCode;

    // new set of properties for the data set, they will replace the old set
    private List<IEntityProperty> properties;

    public BasicDataSetUpdates()
    {
    }

    public BasicDataSetUpdates(TechId datasetId, List<IEntityProperty> properties, Date version)
    {
        this.datasetId = datasetId;
        this.version = version;
        this.properties = properties;
    }

    public TechId getDatasetId()
    {
        return datasetId;
    }

    public void setDatasetId(TechId datasetId)
    {
        this.datasetId = datasetId;
    }

    public Date getVersion()
    {
        return version;
    }

    public void setVersion(Date version)
    {
        this.version = version;
    }

    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<IEntityProperty> properties)
    {
        this.properties = properties;
    }

    public String[] getModifiedParentDatasetCodesOrNull()
    {
        return modifiedParentDatasetCodesOrNull;
    }

    public void setModifiedParentDatasetCodesOrNull(String[] modifiedParentDatasetCodesOrNull)
    {
        this.modifiedParentDatasetCodesOrNull = modifiedParentDatasetCodesOrNull;
    }

    public String getFileFormatTypeCode()
    {
        return fileFormatTypeCode;
    }

    public void setFileFormatTypeCode(String fileFormatTypeCode)
    {
        this.fileFormatTypeCode = fileFormatTypeCode;
    }

}
