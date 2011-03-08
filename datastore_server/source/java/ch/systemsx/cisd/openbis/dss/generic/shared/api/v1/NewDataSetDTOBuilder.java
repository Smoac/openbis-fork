/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;

/**
 * A mutable object for building NewDataSetDTO objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class NewDataSetDTOBuilder
{
    private final NewDataSetMetadataDTO dataSetMetadata;

    private DataSetOwner dataSetOwner;

    private String dataSetFolderNameOrNull;

    private final ArrayList<FileInfoDssDTO> fileInfos = new ArrayList<FileInfoDssDTO>();

    public NewDataSetDTOBuilder()
    {
        dataSetMetadata = new NewDataSetMetadataDTO();
    }

    /**
     * The owner may be null during construction but is non-null in a well-formed object.
     */
    public DataSetOwner getDataSetOwner()
    {
        return dataSetOwner;
    }

    public void setDataSetOwner(DataSetOwner dataSetOwner)
    {
        this.dataSetOwner = dataSetOwner;
    }

    /**
     * The optional folder name that the contents of the data set should be put in on the server. If
     * null, a folder name may be generated.
     */
    public String getDataSetFolderNameOrNull()
    {
        return dataSetFolderNameOrNull;
    }

    /**
     * The optional folder name that the contents of the data set should be put in on the server. If
     * null, a folder name may be generated.
     */
    public void setDataSetFolderNameOrNull(String dataSetFolderNameOrNull)
    {
        this.dataSetFolderNameOrNull = dataSetFolderNameOrNull;
    }

    /**
     * The metadata data for the data set, including type and properties.
     */
    public NewDataSetMetadataDTO getDataSetMetadata()
    {
        return dataSetMetadata;
    }

    /**
     * A summary of the contents of the data set.
     */
    public List<FileInfoDssDTO> getFileInfos()
    {
        return fileInfos;
    }

    /**
     * Convert the builder into a NewDataSetDTO object.
     */
    public NewDataSetDTO asNewDataSetDTO()
    {
        return new NewDataSetDTO(dataSetMetadata, dataSetOwner, dataSetFolderNameOrNull, fileInfos);
    }

}
