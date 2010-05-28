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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a reference to a file/folder within a data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetFileDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String dataSetCode;

    private final String path;

    private final boolean isRecursive;

    public DataSetFileDTO(String dataSetCode, String path, boolean isRecursive)
    {
        this.dataSetCode = dataSetCode;
        this.path = path;
        this.isRecursive = isRecursive;
    }

    /** The code of the data set that owns this file */
    public String getDataSetCode()
    {
        return dataSetCode;
    }

    /** The path within the data set of the file */
    public String getPath()
    {
        return path;
    }

    /** If true, this object includes sub folders */
    public boolean isRecursive()
    {
        return isRecursive;
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        sb.append(getDataSetCode());
        sb.append(getPath());
        if (isRecursive())
        {
            sb.append("recursive");
        }
        return sb.toString();
    }
}
