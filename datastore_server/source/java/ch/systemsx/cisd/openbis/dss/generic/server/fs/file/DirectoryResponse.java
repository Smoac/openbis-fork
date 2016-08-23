/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.file;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

public class DirectoryResponse implements IFileSystemViewResponse
{
    public interface Node
    {

    }

    public class DirectoryNode implements Node
    {
        final String fullPath;

        final long lastModified;

        private DirectoryNode(String fullPath, long lastModified)
        {
            this.fullPath = fullPath;
            this.lastModified = lastModified;
        }

        public String getFullPath()
        {
            return fullPath;
        }

        public long getLastModified()
        {
            return lastModified;
        }

    }

    public class FileNode implements Node
    {
        final String fullPath;

        final long size;

        final long lastModified;

        private FileNode(String fullPath, long size, long lastModified)
        {
            this.fullPath = fullPath;
            this.size = size;
            this.lastModified = lastModified;
        }

        public String getFullPath()
        {
            return fullPath;
        }

        public long getSize()
        {
            return size;
        }

        public long getLastModified()
        {
            return lastModified;
        }
    }

    private final List<Node> files;

    private final String fullPath;

    public DirectoryResponse(String fullPath)
    {
        this.fullPath = fullPath;
        this.files = new ArrayList<>();
    }

    public List<Node> getFiles()
    {
        return files;
    }

    public String getFullPath()
    {
        return fullPath;
    }

    /**
     * Adds a directory entry with current timestamp
     */
    public void addDirectory(String directoryName)
    {
        addDirectory(directoryName, ServiceProvider.DSS_STARTUP_DATE);
    }

    public void addDirectory(String directoryName, Date lastModified)
    {
        addDirectory(directoryName, lastModified.getTime());
    }

    public void addDirectory(String directoryName, final long lastModified)
    {
        files.add(new DirectoryNode(directoryName, lastModified));
    }

    public void addFile(String fileName, final IHierarchicalContentNode node)
    {
        addFile(fileName, node.getFileLength(), node.getLastModified());
    }

    public void addFile(String fileName, final long size, final long lastModified)
    {
        files.add(new FileNode(this.fullPath + "/" + fileName, size, lastModified));
    }
}