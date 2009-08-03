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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicDataSetUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Piotr Buczek
 */
public class DataSetUpdates extends BasicDataSetUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String sampleIdentifier;

    private String parentDatasetCode;

    private FileFormatType fileFormatType;

    public DataSetUpdates()
    {
    }

    public DataSetUpdates(TechId sampleId, List<IEntityProperty> properties, Date version,
            String sampleIdentifier, String parentDatasetCode)
    {
        super(sampleId, properties, version);
        this.sampleIdentifier = sampleIdentifier;
        this.parentDatasetCode = parentDatasetCode;
    }

    public String getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleIdentifier(String sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    public String getParentDatasetCode()
    {
        return parentDatasetCode;
    }

    public void setParentDatasetCode(String parentDatasetCode)
    {
        this.parentDatasetCode = parentDatasetCode;
    }

    public FileFormatType getFileFormatType()
    {
        return fileFormatType;
    }

    public void setFileFormatType(FileFormatType fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }
}