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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Points to one dataset.
 * 
 * @author Tomasz Pylak
 */
public class DatasetReference implements ISerializable, IEntityInformationHolderWithPermId
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private long id;

    private String datasetCode;

    private String typeCode;

    private String fileTypeCode;

    private String datastoreCode;

    private String datastoreHostUrl;

    private Date registrationDate;

    private String experimentPermId;

    // GWT only
    @SuppressWarnings("unused")
    private DatasetReference()
    {
    }

    public DatasetReference(long id, String code, String typeCode, Date registrationDate,
            String fileTypeCode, String datastoreCode, String datastoreHostUrl,
            String experimentPermId)
    {
        this.id = id;
        this.datasetCode = code;
        this.typeCode = typeCode;
        this.registrationDate = registrationDate;
        this.fileTypeCode = fileTypeCode;
        this.datastoreCode = datastoreCode;
        this.datastoreHostUrl = datastoreHostUrl;
        this.experimentPermId = experimentPermId;
    }

    public String getCode()
    {
        return datasetCode;
    }

    public String getDatastoreCode()
    {
        return datastoreCode;
    }

    public String getDatastoreHostUrl()
    {
        return datastoreHostUrl;
    }

    public EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    public BasicEntityType getEntityType()
    {
        return new BasicEntityType(typeCode);
    }

    public Long getId()
    {
        return id;
    }

    public String getPermId()
    {
        return datasetCode;
    }

    public String getFileTypeCode()
    {
        return fileTypeCode;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public String getExperimentPermId()
    {
        return experimentPermId;
    }
}
