/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a new data set that the DSS should register.
 * <p>
 * The information required to register a new data set are the owner of the data set, the name of
 * the container of the data set (folder or file name), and file info about the files in the data
 * set.
 * <p>
 * Optionally, a data set type and properties may be specified. The type and properties will
 * override those inferred by the server.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class NewDataSetDTO implements Serializable
{
    private static String DEFAULT_DATA_SET_FOLDER_NAME = "original";

    /**
     * The different types of owners of data sets; there are two: experiment and sample.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static enum DataSetOwnerType
    {
        EXPERIMENT, SAMPLE
    }

    /**
     * The identifier of the owner of the new data set. This could either be an experiment
     * identifier or a sample identifier.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class DataSetOwner implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final DataSetOwnerType type;

        private final String identifier;

        public DataSetOwner(DataSetOwnerType type, String identifier)
        {
            this.type = type;
            this.identifier = identifier;
        }

        public DataSetOwnerType getType()
        {
            return type;
        }

        public String getIdentifier()
        {
            return identifier;
        }

        @Override
        public String toString()
        {
            ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
            sb.append(getType());
            sb.append(getIdentifier());
            return sb.toString();
        }
    }

    private static final long serialVersionUID = 1L;

    private final DataSetOwner dataSetOwner;

    private final String dataSetFolderName;

    private final List<FileInfoDssDTO> fileInfos;

    private final HashMap<String, String> properties;

    private String dataSetTypeOrNull;

    /**
     * Constructor
     * 
     * @param dataSetOwner The owner of the new data set
     * @param dataSetFolderNameOrNull The name of the folder the data is stored in. If the data set
     *            is just a single file and the folder name is null, a folder will be created.
     * @param fileInfos FileInfoDssDTO objects for each of the files in the data set.
     */
    public NewDataSetDTO(DataSetOwner dataSetOwner, String dataSetFolderNameOrNull,
            List<FileInfoDssDTO> fileInfos)
    {
        this.dataSetOwner = dataSetOwner;
        this.dataSetFolderName =
                (null == dataSetFolderNameOrNull) ? DEFAULT_DATA_SET_FOLDER_NAME
                        : dataSetFolderNameOrNull;
        this.fileInfos = fileInfos;
        properties = new HashMap<String, String>();
    }

    /**
     * Constructor
     * 
     * @param dataSetType The type of the new data set
     * @param dataSetOwner The owner of the new data set
     * @param dataSetFolderNameOrNull The name of the folder the data is stored in. If the data set
     *            is just a single file and the folder name is null, a folder will be created.
     * @param fileInfos FileInfoDssDTO objects for each of the files in the data set.
     */
    public NewDataSetDTO(String dataSetType, DataSetOwner dataSetOwner,
            String dataSetFolderNameOrNull, List<FileInfoDssDTO> fileInfos)
    {
        this(dataSetOwner, dataSetFolderNameOrNull, fileInfos);
        this.dataSetTypeOrNull = dataSetType;
    }

    /**
     * The direct owner of the data set, either an experiment or a sample.
     */
    public DataSetOwner getDataSetOwner()
    {
        return dataSetOwner;
    }

    /**
     * The name of the folder containing the files in the data set.
     */
    public String getDataSetFolderName()
    {
        return dataSetFolderName;
    }

    /**
     * {@link FileInfoDssDTO} objects describing the files within the data set.
     */
    public List<FileInfoDssDTO> getFileInfos()
    {
        return fileInfos;
    }

    /**
     * The code for the type of the new data set. May be null.
     */
    public String tryDataSetType()
    {
        return dataSetTypeOrNull;
    }

    public void setDataSetTypeOrNull(String dataSetTypeOrNull)
    {
        // Capitalize the data set type
        this.dataSetTypeOrNull =
                (null != dataSetTypeOrNull) ? dataSetTypeOrNull.toUpperCase() : dataSetTypeOrNull;
    }

    /**
     * The properties for the new data set. Key is the property code, value is the value (as a
     * string).
     */
    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, String> props)
    {
        properties.clear();
        properties.putAll(props);
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        String myDataSetTypeOrNull = tryDataSetType();
        if (null != myDataSetTypeOrNull)
        {
            sb.append(myDataSetTypeOrNull);
        }
        sb.append(getDataSetOwner());
        sb.append(getFileInfos());
        return sb.toString();
    }
}
