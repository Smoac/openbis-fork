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

package ch.systemsx.cisd.common.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;

/**
 * {@link IHierarchicalContent} implementation for normal {@link java.io.File} directory.
 * <p>
 * NOTE: The directory can contain HDF5 containers inside and they will handled in a special way.
 * 
 * @author Piotr Buczek
 */
class DefaultFileBasedHierarchicalContent implements IHierarchicalContent
{
    private final IHierarchicalContentFactory hierarchicalContentFactory;

    private final File root;

    private final IDelegatedAction onCloseAction;

    private IHierarchicalContentNode rootNode;

    DefaultFileBasedHierarchicalContent(IHierarchicalContentFactory hierarchicalContentFactory,
            File file, IDelegatedAction onCloseAction)
    {
        assert hierarchicalContentFactory != null;
        if (file.exists() == false)
        {
            throw new IllegalArgumentException(file.getAbsolutePath() + " doesn't exist");
        }
        if (file.isDirectory() == false)
        {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a directory");
        }
        this.hierarchicalContentFactory = hierarchicalContentFactory;
        this.onCloseAction = onCloseAction;
        this.root = file;
    }

    public IHierarchicalContentNode getRootNode()
    {
        if (rootNode == null)
        {
            rootNode = createFileNode(root);
        }
        return rootNode;
    }

    public IHierarchicalContentNode getNode(String relativePath)
    {
        if (StringUtils.isBlank(relativePath))
        {
            return getRootNode();
        } else
        {
            return asNode(new File(root, relativePath));
        }
    }

    private IHierarchicalContentNode asNode(File file)
    {
        if (file.exists())
        {
            return createFileNode(file);
        }
        // The file doesn't exist in file system but it could be inside a HDF5 container.
        // Go up in file hierarchy until existing file is found.
        File existingFile = file;
        while (existingFile != null && existingFile.exists() == false)
        {
            existingFile = existingFile.getParentFile();
        }
        if (existingFile != null && FileUtilities.isHDF5ContainerFile(existingFile))
        {
            HDF5ContainerBasedHierarchicalContentNode containerNode =
                    new HDF5ContainerBasedHierarchicalContentNode(hierarchicalContentFactory, this,
                            existingFile);
            String relativePath = FileUtilities.getRelativeFile(existingFile, file);
            return containerNode.getChildNode(relativePath);
        }
        throw new IllegalArgumentException("Resource '" + FileUtilities.getRelativeFile(root, file)
                + "' does not exist.");
    }

    private IHierarchicalContentNode createFileNode(File file)
    {
        return hierarchicalContentFactory.asHierarchicalContentNode(this, file);
    }

    public List<IHierarchicalContentNode> listMatchingNodes(final String relativePathPattern)
    {
        final IHierarchicalContentNode startingNode = getRootNode();
        final Pattern compiledPattern = Pattern.compile(relativePathPattern);
        final IHierarchicalContentNodeFilter relativePathFilter =
                new IHierarchicalContentNodeFilter()
                    {
                        public boolean accept(IHierarchicalContentNode node)
                        {
                            return compiledPattern.matcher(node.getRelativePath()).matches();
                        }
                    };

        List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
        findMatchingNodes(startingNode, relativePathFilter, result);
        return result;
    }

    public List<IHierarchicalContentNode> listMatchingNodes(final String startingPath,
            final String fileNamePattern)
    {
        final IHierarchicalContentNode startingNode = getNode(startingPath);
        final Pattern compiledPattern = Pattern.compile(fileNamePattern);
        final IHierarchicalContentNodeFilter fileNameFilter = new IHierarchicalContentNodeFilter()
            {
                public boolean accept(IHierarchicalContentNode node)
                {
                    return compiledPattern.matcher(node.getName()).matches();
                }
            };

        List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
        findMatchingNodes(startingNode, fileNameFilter, result);
        return result;
    }

    public void close()
    {
        onCloseAction.execute();
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "DefaultFileBasedHierarchicalContent [root=" + root + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((root == null) ? 0 : root.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof DefaultFileBasedHierarchicalContent))
        {
            return false;
        }
        DefaultFileBasedHierarchicalContent other = (DefaultFileBasedHierarchicalContent) obj;
        if (root == null)
        {
            if (other.root != null)
            {
                return false;
            }
        } else if (!root.equals(other.root))
        {
            return false;
        }
        return true;
    }

    /**
     * Recursively browses hierarchical content looking for nodes accepted by given
     * <code>filter</code> and adding them to <code>result</code> list.
     */
    private static void findMatchingNodes(IHierarchicalContentNode dirNode,
            IHierarchicalContentNodeFilter filter, List<IHierarchicalContentNode> result)
    {
        assert dirNode.isDirectory() : "expected a directory node, got: " + dirNode;
        for (IHierarchicalContentNode childNode : dirNode.getChildNodes())
        {
            if (childNode.isDirectory())
            {
                findMatchingNodes(childNode, filter, result);
            } else
            {
                if (filter.accept(childNode))
                {
                    result.add(childNode);
                }
            }
        }
    }

}
