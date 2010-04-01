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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Helper Class for creating FileInfoDss objects
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class FileInfoDssBuilder
{

    private final String hierarchyRoot;

    /**
     * Constructor for FileInfoDssFactory
     * 
     * @param hierarchyRoot The root of the directory structure; used to determine the path for
     *            FileInfoDss objects
     */
    public FileInfoDssBuilder(String hierarchyRoot)
    {
        this.hierarchyRoot = hierarchyRoot;
    }

    /**
     * Append file info for the requested file or file hierarchy. Assumes that the parameters have
     * been verified already.
     * 
     * @param requestedFile A file known to be accessible by the user
     * @param list The list the files infos are appended to
     * @param isRecursive If true, directories will be recursively appended to the list
     */
    public void appendFileInfosForFile(File requestedFile, ArrayList<FileInfoDss> list,
            boolean isRecursive) throws IOException
    {
        // at the top level, we should list the contents of directories, but only recurse if the
        // search is recursive
        appendFileInfosForFile(requestedFile, list, isRecursive ? Integer.MAX_VALUE : 0);
    }

    private void appendFileInfosForFile(File requestedFile, ArrayList<FileInfoDss> list,
            int maxDepth) throws IOException
    {
        FileInfoDss fileInfo = fileInfoForFile(requestedFile);
        list.add(fileInfo);

        // If this is a file or we have exhausted the depth, return
        if (requestedFile.isDirectory() == false || maxDepth < 0)
        {
            return;
        }

        File[] files = requestedFile.listFiles();
        for (File file : files)
        {
            appendFileInfosForFile(file, list, maxDepth - 1);
        }
    }

    private FileInfoDss fileInfoForFile(File file) throws IOException
    {
        FileInfoDss fileInfo = new FileInfoDss();

        fileInfo.setPath(pathRelativeToRoot(file));
        fileInfo.setDirectory(file.isDirectory());
        if (fileInfo.isDirectory())
        {
            fileInfo.setFileSize(-1);
        } else
        {
            fileInfo.setFileSize(file.length());
        }

        return fileInfo;
    }

    /**
     * Convert the path for file to a path relative to the root of the data set
     */
    private String pathRelativeToRoot(File file) throws IOException
    {
        String path;
        path = file.getCanonicalPath();
        path = path.substring(hierarchyRoot.length());
        return (path.length() > 0) ? path : "/";

    }
}
