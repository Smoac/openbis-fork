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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.common.io.AbstractHierarchicalContentNode;
import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.common.io.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * {@link IHierarchicalContent} implementation using PathInfoDB for file metadata.
 * 
 * @author Piotr Buczek
 */
class SimplePathInfoBasedHierarchicalContent implements IHierarchicalContent
{

    private final File root;

    private final IDelegatedAction onCloseAction;

    private IHierarchicalContentNode rootNode;

    private final DataSetPathInfo rootPathInfo;

    SimplePathInfoBasedHierarchicalContent(DataSetPathInfo rootPathInfo, File file,
            IDelegatedAction onCloseAction)
    {
        assert rootPathInfo != null;
        this.rootPathInfo = rootPathInfo;
        if (file.exists() == false)
        {
            throw new IllegalArgumentException(file.getAbsolutePath() + " doesn't exist");
        }
        if (file.isDirectory() == false)
        {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a directory");
        }
        this.onCloseAction = onCloseAction;
        this.root = file;
    }

    public IHierarchicalContentNode getRootNode()
    {
        if (rootNode == null)
        {
            rootNode = new SimplePathInfoNode(root, rootPathInfo);
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
            return createNode(relativePath);
        }
    }

    private IHierarchicalContentNode createNode(String relativePath)
    {
        DataSetPathInfo pathInfo = findPathInfo(rootPathInfo, relativePath);
        return new SimplePathInfoNode(root, pathInfo);
    }

    /**
     * Recursively finds path info with given <var>relativePath</var> starting from
     * <var>current</var>.
     * <p>
     * <b>Invariant:</b> <var>current</var> path info has relative path which is a prefix of
     * <var>relativePath</var>.
     * 
     * @throws IllegalArgumentException if path info with given relative path doesn't exist
     */
    private DataSetPathInfo findPathInfo(DataSetPathInfo current, String relativePath)
            throws IllegalArgumentException
    {
        assert relativePath.startsWith(current.getRelativePath());

        if (current.getRelativePath().equals(relativePath))
        {
            return current;
        }

        if (current.isDirectory())
        {
            for (DataSetPathInfo child : current.getChildren())
            {
                if (relativePath.startsWith(child.getRelativePath()))
                {
                    return findPathInfo(child, relativePath);
                }
            }
        }

        throw new IllegalArgumentException("Resource '" + relativePath + "' does not exist.");
    }

    // TODO 2011-04-19, Piotr Buczek: remove repetition
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
        return "SimplePathInfoBasedHierarchicalContent [root=" + root + "]";
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
        if (!(obj instanceof SimplePathInfoBasedHierarchicalContent))
        {
            return false;
        }
        SimplePathInfoBasedHierarchicalContent other = (SimplePathInfoBasedHierarchicalContent) obj;
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

    static class SimplePathInfoNode extends AbstractHierarchicalContentNode
    {

        private final DataSetPathInfo pathInfo;

        private final File root;

        SimplePathInfoNode(File root, DataSetPathInfo pathInfo)
        {
            this.root = root;
            this.pathInfo = pathInfo;
        }

        public String getName()
        {
            return pathInfo.getFileName();
        }

        public boolean exists()
        {
            return true;
        }

        public boolean isDirectory()
        {
            return pathInfo.isDirectory();
        }

        @Override
        protected String doGetRelativePath()
        {
            return pathInfo.getRelativePath();
        }

        @Override
        protected List<IHierarchicalContentNode> doGetChildNodes()
        {
            List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
            for (DataSetPathInfo child : pathInfo.getChildren())
            {
                result.add(new SimplePathInfoNode(root, child));
            }
            return result;
        }

        @Override
        protected long doGetFileLength()
        {
            return pathInfo.getSizeInBytes();
        }

        // TODO 2011-04-19, Piotr Buczek: use abstraction to get file content

        public File getFile() throws UnsupportedOperationException
        {
            if (StringUtils.isBlank(getRelativePath()))
            {
                return root;
            } else
            {
                return new File(root, getRelativePath());
            }
        }

        @Override
        protected IRandomAccessFile doGetFileContent()
        {
            return new RandomAccessFileImpl(getFile(), "r");
        }

        @Override
        protected InputStream doGetInputStream()
        {
            try
            {
                return new FileInputStream(getFile());
            } catch (FileNotFoundException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

    }

}
