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

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile;

import java.io.Serializable;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Jakub Straszewski
 */
@JsonObject("dss.dto.entity.datasetfile.DataSetFile")
public class DataSetFile implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataSetFilePermId permId;

    @JsonProperty
    private DataSetPermId dataSetPermId;

    @JsonProperty
    private String path;

    @JsonProperty
    private boolean isDirectory;

    @JsonProperty
    private long fileLength;

    @JsonProperty
    private int checksumCRC32;

    @JsonIgnore
    public DataSetFilePermId getPermId()
    {
        return permId;
    }

    public void setPermId(DataSetFilePermId permId)
    {
        this.permId = permId;
    }

    @JsonIgnore
    public DataSetPermId getDataSetPermId()
    {
        return dataSetPermId;
    }

    public void setDataSetPermId(DataSetPermId dataSetPermId)
    {
        this.dataSetPermId = dataSetPermId;
    }

    @JsonIgnore
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    @JsonIgnore
    public boolean isDirectory()
    {
        return isDirectory;
    }

    public void setDirectory(boolean isDirectory)
    {
        this.isDirectory = isDirectory;
    }

    @JsonIgnore
    public long getFileLength()
    {
        return fileLength;
    }

    public void setFileLength(long fileLength)
    {
        this.fileLength = fileLength;
    }

    @Override
    public String toString()
    {
        return "DataSetFile: " + path + ", " + dataSetPermId;
    }

    @JsonIgnore
    public int getChecksumCRC32()
    {
        return checksumCRC32;
    }

    public void setChecksumCRC32(int checksumCRC32)
    {
        this.checksumCRC32 = checksumCRC32;
    }
}
