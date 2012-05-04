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

import net.lemnik.eodsql.ResultColumn;

/**
 * A DTO for path entries which are files.
 * 
 * @author Bernd Rinn
 */
public class PathEntryDTO
{
    @ResultColumn("dase_id")
    private  Long dataSetId;
    @ResultColumn("parent_id")
    private  Long parentId;
    @ResultColumn("relative_path")
    private  String relativePath;
    @ResultColumn("file_name")
    private  String fileName;
    @ResultColumn("size_in_bytes")
    private  Long sizeInBytes;
    @ResultColumn("checksum_crc32")
    private  Long checksumCRC32;
    
    @ResultColumn("is_directory")
    private Boolean directory;
    @ResultColumn("last_modified")
    private  Date lastModifiedDate;

    public PathEntryDTO(long dataSetId, Long parentId, String relativePath, String fileName,
            long sizeInBytes, Long checksumCRC32OrNull, boolean isDirectory, Date lastModifiedDate)
    {
        this.dataSetId = dataSetId;
        this.parentId = parentId;
        this.relativePath = relativePath;
        this.fileName = fileName;
        this.sizeInBytes = sizeInBytes;
        this.checksumCRC32 = checksumCRC32OrNull;
        this.directory = isDirectory;
        this.lastModifiedDate = lastModifiedDate;
    }
    
    private PathEntryDTO() {
        
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
        result = prime * result + (int) (checksumCRC32 == null ? 0 : checksumCRC32); 
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

        if (equals(sizeInBytes, other.sizeInBytes) == false) {
            return false;
        }
        if (equals(checksumCRC32, other.checksumCRC32) == false) {
            return false;
        }
        
        return true;
    }
    
    private boolean equals(Long n1OrNull, Long n2OrNull)
    {
        return n1OrNull == null ? n1OrNull == n2OrNull : n1OrNull.equals(n2OrNull);
    }

    @Override
    public String toString()
    {
        return "PathEntryDTO [dataSetId=" + dataSetId + ", parentId=" + parentId
                + ", relativePath=" + relativePath + ", fileName=" + fileName
                + ", sizeInBytes=" + sizeInBytes + ", lastModifiedDate=" + lastModifiedDate
                + ", isDirectory="+directory
                + "]";
    }

    public long getDataSetId()
    {
        return dataSetId;
    }

    public void setDataSetId(long dataSetId)
    {
        this.dataSetId = dataSetId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public void setRelativePath(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }
    

    public Long getChecksumCRC32()
    {
        return checksumCRC32;
    }

    public void setChecksumCRC32(Long checksumCRC32)
    {
        this.checksumCRC32 = checksumCRC32;
    }

    public Date getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate)
    {
        this.lastModifiedDate = lastModifiedDate;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public void setDirectory(boolean isDirectory)
    {
        this.directory = isDirectory;
    }
}