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
import java.util.List;

import ch.systemsx.cisd.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.etlserver.IDataSetPathsInfoFeeder;

/**
 * Data set paths info feeder feeding a data base.
 * 
 * @author Franz-Josef Elmer
 */
public class DatabaseBasedDataSetPathsInfoFeeder implements IDataSetPathsInfoFeeder
{
    private final static int BATCH_SIZE = 500;

    private final IPathsInfoDAO dao;

    private final IHierarchicalContentFactory hierarchicalContentFactory;

    private final List<PathEntryDTO> filePaths = new ArrayList<PathEntryDTO>(BATCH_SIZE);

    public DatabaseBasedDataSetPathsInfoFeeder(IPathsInfoDAO dao,
            IHierarchicalContentFactory hierarchicalContentFactory)
    {
        this.dao = dao;
        this.hierarchicalContentFactory = hierarchicalContentFactory;
    }

    public long addPaths(String dataSetCode, String location, File dataSetRoot)
    {
        long dataSetId = dao.createDataSet(dataSetCode, location);
        IHierarchicalContent content =
                hierarchicalContentFactory.asHierarchicalContent(dataSetRoot,
                        IDelegatedAction.DO_NOTHING);
        PathInfo root =
                PathInfo.createPathInfo(hierarchicalContentFactory.asHierarchicalContentNode(
                        content, dataSetRoot));
        addPaths(dataSetId, null, "", root);
        return root.getSizeInBytes();
    }

    private void addPaths(long dataSetId, Long parentId, String pathPrefix, PathInfo pathInfo)
    {
        final boolean directory = pathInfo.isDirectory();
        final String fileName = pathInfo.getFileName();
        String relativePath = (parentId == null) ? "" : pathPrefix + fileName;
        if (directory)
        {
            final long directoryId =
                    dao.createDataSetFile(dataSetId, parentId, relativePath, fileName,
                            pathInfo.getSizeInBytes(), directory, pathInfo.getLastModifiedDate());
            if (relativePath.length() > 0)
            {
                relativePath += '/';
            }
            final List<PathInfo> children = pathInfo.getChildren();
            for (PathInfo child : children)
            {
                addPaths(dataSetId, directoryId, relativePath, child);
            }
        } else
        {
            addFilePathToBatch(new PathEntryDTO(dataSetId, parentId, relativePath, fileName,
                    pathInfo.getSizeInBytes(), pathInfo.getLastModifiedDate()));
        }
    }

    private void addFilePathToBatch(PathEntryDTO filePath)
    {
        filePaths.add(filePath);
        if (filePaths.size() == BATCH_SIZE)
        {
            dao.createDataSetFiles(filePaths);
            filePaths.clear();
        }
    }

    public void commit()
    {
        if (filePaths.isEmpty() == false)
        {
            dao.createDataSetFiles(filePaths);
            filePaths.clear();
        }
        dao.commit();
    }

}
