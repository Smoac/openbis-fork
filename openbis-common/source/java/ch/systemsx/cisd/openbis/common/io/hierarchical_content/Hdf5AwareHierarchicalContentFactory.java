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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * HDF5 aware implementation of {@link IHierarchicalContentFactory} using file system as source of information.
 * 
 * @author anttil
 */
public class Hdf5AwareHierarchicalContentFactory implements IHierarchicalContentFactory
{
    private boolean h5arFoldersDefault;

    private boolean h5FoldersDefault;
    
    private Map<String, H5FolderFlags> h5FolderFlagsByTreeRoot = new HashMap<>();

    public Hdf5AwareHierarchicalContentFactory(boolean h5Folders, boolean h5arFolders)
    {
        this.h5FoldersDefault = h5Folders;
        this.h5arFoldersDefault = h5arFolders;
    }
    
    public Hdf5AwareHierarchicalContentFactory(List<H5FolderFlags> h5FolderFlags)
    {
        if (h5FolderFlags.size() == 1)
        {
            h5FoldersDefault = h5FolderFlags.get(0).isH5Folders();
            h5arFoldersDefault = h5FolderFlags.get(0).isH5arFolders();
        }
        for (H5FolderFlags flags : h5FolderFlags)
        {
            h5FolderFlagsByTreeRoot.put(flags.getTreeRoot(), flags);
        }
    }
    
    @Override
    public IHierarchicalContent asVirtualHierarchicalContent(
            List<IHierarchicalContent> components)
    {
        return new VirtualHierarchicalContent(components);
    }

    @Override
    public IHierarchicalContent asHierarchicalContent(File file, IDelegatedAction onCloseAction)
    {
        return new DefaultFileBasedHierarchicalContent(this, file, onCloseAction);
    }

    @Override
    public IHierarchicalContentNode asHierarchicalContentNode(IHierarchicalContent rootContent,
            File file)
    {
        if (handleHdf5AsFolder(rootContent, file))
        {
            try
            {
                HDF5ContainerBasedHierarchicalContentNode node = new HDF5ContainerBasedHierarchicalContentNode(rootContent, file);
                if (node.isFileAbstractionOk())
                {
                    return node;
                }
            } catch (Exception e)
            {
                // Could not open file as HDF5
            }
        }
        return new DefaultFileBasedHierarchicalContentNode(this, rootContent, file);
    }
    
    private boolean handleHdf5AsFolder(IHierarchicalContent rootContent, File file)
    {
        if (FileUtilities.isHDF5ContainerFile(file) == false)
        {
            return false;
        }
        String filename = file.getName();
        if (h5FolderFlagsByTreeRoot.isEmpty() == false)
        {
            IHierarchicalContentNode rootNode = rootContent.getRootNode();
            if (rootNode != null)
            {
                File rootFile = rootNode.tryGetFile();
                if (rootFile != null)
                {
                    String relativeFilePath = FileUtilities.getRelativeFilePath(rootFile, file);
                    if (relativeFilePath != null)
                    {
                        String treeRoot = relativeFilePath.split("/")[0];
                        H5FolderFlags flags = h5FolderFlagsByTreeRoot.get(treeRoot);
                        if (flags != null)
                        {
                            return HierarchicalContentUtils.handleHdf5AsFolder(filename, 
                                    flags.isH5Folders(), flags.isH5arFolders());
                        }
                    }
                }
            }
        }
        return HierarchicalContentUtils.handleHdf5AsFolder(filename, h5FoldersDefault, h5arFoldersDefault);
    }
}
