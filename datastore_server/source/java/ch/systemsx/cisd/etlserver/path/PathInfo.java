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

package ch.systemsx.cisd.etlserver.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Immutable class with path informations about a {@link IHierarchicalContentNode} and its descendants.
 * 
 *
 * @author Franz-Josef Elmer
 */
final class PathInfo
{
    private static final List<PathInfo> NO_CHILDREN = Collections.emptyList();
    
    static PathInfo createPathInfo(IHierarchicalContentNode node, boolean computeChecksum)
    {
        if (node.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: " + node.getRelativePath());
        }
        PathInfo pathInfo = new PathInfo();
        pathInfo.fileName = node.getName();
        pathInfo.lastModifiedDate = new Date(node.getLastModified());
        pathInfo.directory = node.isDirectory();
        if (pathInfo.directory)
        {
            pathInfo.children = createPathInfos(node, computeChecksum);
            long sum = 0;
            for (PathInfo childInfo : pathInfo.children)
            {
                childInfo.parent = pathInfo;
                sum += childInfo.sizeInBytes;
            }
            pathInfo.sizeInBytes = sum;
        } else
        {
            pathInfo.sizeInBytes = node.getFileLength();
            pathInfo.checksumCRC32 = computeChecksum ? node.getChecksumCRC32() : null;
        }
        return pathInfo;
    }
    
    private static List<PathInfo> createPathInfos(IHierarchicalContentNode node, boolean computeChecksum)
    {
        if (node.isDirectory() == false)
        {
            throw new IllegalArgumentException("Not a folder: " + node.getRelativePath());
        }
        List<PathInfo> childInfos = new ArrayList<PathInfo>();
        List<IHierarchicalContentNode> childNodes = node.getChildNodes();
        for (IHierarchicalContentNode child : childNodes)
        {
            childInfos.add(createPathInfo(child, computeChecksum));
        }
        Collections.sort(childInfos, new Comparator<PathInfo>()
            {
                @Override
                public int compare(PathInfo p1, PathInfo p2)
                {
                    return p1.getFileName().compareTo(p2.getFileName());
                }
            });
        return childInfos;
    }
    
    private String fileName;
    
    private long sizeInBytes;
    
    private Integer checksumCRC32;
    
    private PathInfo parent;
    
    private boolean directory;
    
    private List<PathInfo> children;
    
    private Date lastModifiedDate;

    public String getFileName()
    {
        return fileName;
    }

    public long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public Integer getChecksumCRC32()
    {
        return checksumCRC32;
    }

    public PathInfo getParent()
    {
        return parent;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public Date getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    public List<PathInfo> getChildren()
    {
        return children == null ? NO_CHILDREN : Collections.unmodifiableList(children);
    }
    
    
}