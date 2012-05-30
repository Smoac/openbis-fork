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
import org.codehaus.jackson.annotate.JsonProperty;

import ch.systemsx.cisd.common.json.JsonObject;



/**
 * Represents information about a file stored in DSS.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("FileInfoDssDTO")
public class FileInfoDssDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String pathInDataSet;

    private String pathInListing;

    private boolean isDirectory;

    private long fileSize;

    public FileInfoDssDTO(String pathInDataSet, String pathInListing, boolean isDirectory,
            long fileSize)
    {
        this.pathInDataSet = pathInDataSet;
        this.pathInListing = pathInListing;
        this.isDirectory = isDirectory;
        this.fileSize = fileSize;
    }

    /**
     * The path of this file in the data set (i.e., the root of the data set has a path of "").
     */
    public String getPathInDataSet()
    {
        return pathInDataSet;
    }

    /**
     * The path of this file relative to the path of the request that produced this FileInfoDss
     */
    public String getPathInListing()
    {
        return pathInListing;
    }

    /**
     * Return true if this FileInfo represents a folder.
     */
    @JsonProperty(value = "isDirectory")
    public boolean isDirectory()
    {
        return isDirectory;
    }

    /**
     * Return the file size if this FileInfo represents a file. If this FileInfo represents a
     * folder, the return value is negative.
     */
    public long getFileSize()
    {
        return fileSize;
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        sb.append(getPathInDataSet());
        sb.append(getPathInListing());
        sb.append(getFileSize());
        return sb.toString();
    }

    //
    // JSON-RPC
    //
    private FileInfoDssDTO()
    {

    }

    private void setPathInDataSet(String pathInDataSet)
    {
        this.pathInDataSet = pathInDataSet;
    }

    private void setPathInListing(String pathInListing)
    {
        this.pathInListing = pathInListing;
    }

    private void setIsDirectory(boolean isDirectory)
    {
        this.isDirectory = isDirectory;
    }

    private void setFileSize(long fileSize)
    {
        this.fileSize = fileSize;
    }
}
