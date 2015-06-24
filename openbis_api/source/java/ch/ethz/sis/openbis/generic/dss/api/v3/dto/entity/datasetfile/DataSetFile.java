/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.dss.api.v3.dto.entity.datasetfile;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Jakub Straszewski
 */
@JsonObject("dto.entity.datasetfile.DataSetFile")
public class DataSetFile implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String fileName;

    @JsonProperty
    private DataSetPermId dataSetPermId;

    @JsonIgnore
    public DataSetPermId getPermId()
    {
        return dataSetPermId;
    }

    public void setPermId(DataSetPermId permId)
    {
        this.dataSetPermId = permId;
    }

    @JsonIgnore
    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
}
