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

    private final String dataSetRoot;

    private final String listingRoot;

    /**
     * Constructor for FileInfoDssFactory
     * 
     * @param dataSetRoot The root of the directory structure; used to determine the path for
     *            FileInfoDss objects
     */
    public FileInfoDssBuilder(String dataSetRoot, String relativeRoot)
    {
        this.dataSetRoot = dataSetRoot;
        this.listingRoot = relativeRoot;
    }

    /**
     * Append file info for the requested file or file hierarchy. Assumes that the parameters have
     * been verified already.
     * 
     * @param requestedFile A file known to be accessible by the user
     * @param list The list the files infos are appended to
     * @param isRecursive If true, directories will be recursively appended to the list
     */
    public void appendFileInfosForFile(File requestedFile, ArrayList<FileInfoDssDTO> list,
            boolean isRecursive) throws IOException
    {
        // at the top level, we should list the contents of directories, but only recurse if the
        // search is recursive; and we should skip listing the top directory
        appendFileInfosForFile(requestedFile, list, isRecursive ? Integer.MAX_VALUE : 0, true);
    }

    private void appendFileInfosForFile(File requestedFile, ArrayList<FileInfoDssDTO> list,
            int maxDepth, boolean excludeTopLevelIfDirectory) throws IOException
    {
        FileInfoDssDTO fileInfo = fileInfoForFile(requestedFile);
        if (excludeTopLevelIfDirectory && fileInfo.isDirectory())
        {
            // If specified, skip the top level if it is a directory
        } else
        {
            list.add(fileInfo);
        }

        // If this is a file or we have exhausted the depth, return
        if (fileInfo.isDirectory() == false || maxDepth < 0)
        {
            return;
        }

        File[] files = requestedFile.listFiles();
        for (File file : files)
        {
            appendFileInfosForFile(file, list, maxDepth - 1, false);
        }
    }

    private FileInfoDssDTO fileInfoForFile(File file) throws IOException
    {
        FileInfoDssDTO fileInfo = new FileInfoDssDTO();

        fileInfo.setPathInDataSet(pathRelativeToDataSetRoot(file));
        fileInfo.setPathInListing(pathRelativeToListingRoot(file));
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
    private String pathRelativeToDataSetRoot(File file) throws IOException
    {
        String path;
        path = file.getCanonicalPath();
        path = path.substring(dataSetRoot.length());
        return (path.length() > 0) ? path : "/";

    }

    /**
     * Convert the path for file to a path relative to the root of the listing
     */
    private String pathRelativeToListingRoot(File file) throws IOException
    {
        String path;
        path = file.getCanonicalPath();
        path = path.substring(listingRoot.length());
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        return (path.length() > 0) ? path : "";

    }
}
