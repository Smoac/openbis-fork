/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.path;

import java.util.Date;

/**
 * A DTO for path entries which are files.
 * 
 * @author Bernd Rinn
 */
class PathEntryDTO
{
    public final long dataSetId;

    public final Long parentId;

    public final String relativePath;

    public final String fileName;

    public final long sizeInBytes;

    public final Date lastModifiedDate;

    PathEntryDTO(long dataSetId, Long parentId, String relativePath, String fileName,
            long sizeInBytes, Date lastModifiedDate)
    {
        this.dataSetId = dataSetId;
        this.parentId = parentId;
        this.relativePath = relativePath;
        this.fileName = fileName;
        this.sizeInBytes = sizeInBytes;
        this.lastModifiedDate = lastModifiedDate;
    }
    
    //
    // For unit tests.
    //

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (dataSetId ^ (dataSetId >>> 32));
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result =
                prime * result + ((lastModifiedDate == null) ? 0 : lastModifiedDate.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
        result = prime * result + (int) (sizeInBytes ^ (sizeInBytes >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PathEntryDTO other = (PathEntryDTO) obj;
        if (dataSetId != other.dataSetId)
            return false;
        if (fileName == null)
        {
            if (other.fileName != null)
                return false;
        } else if (!fileName.equals(other.fileName))
            return false;
        if (lastModifiedDate == null)
        {
            if (other.lastModifiedDate != null)
                return false;
        } else if (!lastModifiedDate.equals(other.lastModifiedDate))
            return false;
        if (parentId == null)
        {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
            return false;
        if (relativePath == null)
        {
            if (other.relativePath != null)
                return false;
        } else if (!relativePath.equals(other.relativePath))
            return false;
        if (sizeInBytes != other.sizeInBytes)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "PathEntryDTO [dataSetId=" + dataSetId + ", parentId=" + parentId
                + ", relativePath=" + relativePath + ", fileName=" + fileName
                + ", sizeInBytes=" + sizeInBytes + ", lastModifiedDate=" + lastModifiedDate
                + "]";
    }
}