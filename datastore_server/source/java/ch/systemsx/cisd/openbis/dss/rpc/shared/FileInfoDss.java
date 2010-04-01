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

package ch.systemsx.cisd.openbis.dss.rpc.shared;

import java.io.Serializable;

/**
 * Represents information about a file stored in DSS.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class FileInfoDss implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String pathInDataSet;

    private boolean isDirectory;

    private long fileSize;

    FileInfoDss()
    {

    }

    /**
     * The the path of this file in the data set (i.e., the root of the data set has a path of "/").
     */
    public String getPath()
    {
        return pathInDataSet;
    }

    /**
     * Return true if this FileInfo represents a folder.
     */
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

    /**
     * Package-visible method for configuring instances.
     */
    void setPath(String pathInDataSet)
    {
        this.pathInDataSet = pathInDataSet;
    }

    /**
     * Package-visible method for configuring instances.
     */
    void setDirectory(boolean isDirectory)
    {
        this.isDirectory = isDirectory;
    }

    /**
     * Package-visible method for configuring instances.
     */
    public void setFileSize(long fileSize)
    {
        this.fileSize = fileSize;
    }

}
