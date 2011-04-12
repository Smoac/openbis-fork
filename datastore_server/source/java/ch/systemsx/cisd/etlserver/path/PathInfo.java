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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Immutable class with informations about a path in a file system.
 * 
 *
 * @author Franz-Josef Elmer
 */
final class PathInfo
{
    private static final List<PathInfo> NO_CHILDREN = Collections.emptyList();
    
    /**
     * Creates path info for specified file and recursively of all its children.
     */
    static PathInfo createPathInfo(File file)
    {
        if (file.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        PathInfo pathInfo = new PathInfo();
        pathInfo.fileName = file.getName();
        pathInfo.directory = file.isDirectory();
        if (pathInfo.directory)
        {
            pathInfo.children = createPathInfos(file);
            long sum = 0;
            for (PathInfo childInfo : pathInfo.children)
            {
                childInfo.parent = pathInfo;
                sum += childInfo.sizeInBytes;
            }
            pathInfo.sizeInBytes = sum;
        } else
        {
            pathInfo.sizeInBytes = file.length();
        }
        return pathInfo;
    }

    private static List<PathInfo> createPathInfos(File file)
    {
        if (file.isDirectory() == false)
        {
            throw new IllegalArgumentException("Not a folder: " + file);
        }
        File[] files = file.listFiles();
        List<PathInfo> childInfos = new ArrayList<PathInfo>();
        for (File child : files)
        {
            childInfos.add(createPathInfo(child));
        }
        Collections.sort(childInfos, new Comparator<PathInfo>()
            {
                public int compare(PathInfo p1, PathInfo p2)
                {
                    return p1.getFileName().compareTo(p2.getFileName());
                }
            });
        return childInfos;
    }
    
    private String fileName;
    
    private long sizeInBytes;
    
    private PathInfo parent;
    
    private boolean directory;
    
    private List<PathInfo> children;

    public String getFileName()
    {
        return fileName;
    }

    public long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public PathInfo getParent()
    {
        return parent;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public List<PathInfo> getChildren()
    {
        return children == null ? NO_CHILDREN : Collections.unmodifiableList(children);
    }
    
    
}