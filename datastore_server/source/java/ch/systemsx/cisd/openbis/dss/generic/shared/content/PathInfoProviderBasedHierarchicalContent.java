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

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.common.io.AbstractHierarchicalContentNode;
import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * {@link IHierarchicalContent} implementation using {@link ISingleDataSetPathInfoProvider} to
 * retrieve file metadata.
 * 
 * @author Piotr Buczek
 */
class PathInfoProviderBasedHierarchicalContent implements IHierarchicalContent
{

    private final File root;

    private final IDelegatedAction onCloseAction;

    private IHierarchicalContentNode rootNode;

    private final ISingleDataSetPathInfoProvider dataSetPathInfoProvider;

    PathInfoProviderBasedHierarchicalContent(
            ISingleDataSetPathInfoProvider dataSetPathInfoProvider, File dataSetDir,
            IDelegatedAction onCloseAction)
    {
        assert dataSetPathInfoProvider != null;
        this.dataSetPathInfoProvider = dataSetPathInfoProvider;
        if (dataSetDir.exists() == false)
        {
            throw new IllegalArgumentException(dataSetDir.getAbsolutePath() + " doesn't exist");
        }
        if (dataSetDir.isDirectory() == false)
        {
            throw new IllegalArgumentException(dataSetDir.getAbsolutePath() + " is not a directory");
        }
        this.onCloseAction = onCloseAction;
        this.root = dataSetDir;
    }

    public IHierarchicalContentNode getRootNode()
    {
        if (rootNode == null)
        {
            DataSetPathInfo pathInfo = dataSetPathInfoProvider.getRootPathInfo();
            rootNode = asNode(pathInfo);
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
            DataSetPathInfo pathInfo = findPathInfo(relativePath);
            return asNode(pathInfo);
        }
    }

    private IHierarchicalContentNode asNode(DataSetPathInfo pathInfo)
    {
        return new PathInfoNode(root, pathInfo);
    }

    private DataSetPathInfo findPathInfo(String relativePath) throws IllegalArgumentException
    {
        DataSetPathInfo result = dataSetPathInfoProvider.tryGetPathInfoByRelativePath(relativePath);
        if (result != null)
        {
            return result;
        } else
        {
            throw new IllegalArgumentException("Resource '" + relativePath + "' does not exist.");
        }
    }

    public List<IHierarchicalContentNode> listMatchingNodes(final String relativePathPattern)
    {
        List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
        List<DataSetPathInfo> matchingPathInfos =
                dataSetPathInfoProvider.listMatchingPathInfos(relativePathPattern);
        for (DataSetPathInfo pathInfo : matchingPathInfos)
        {
            result.add(asNode(pathInfo));
        }
        return result;
    }

    public List<IHierarchicalContentNode> listMatchingNodes(final String startingPath,
            final String fileNamePattern)
    {
        List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
        List<DataSetPathInfo> matchingPathInfos =
                dataSetPathInfoProvider.listMatchingPathInfos(startingPath, fileNamePattern);
        for (DataSetPathInfo pathInfo : matchingPathInfos)
        {
            result.add(asNode(pathInfo));
        }
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
        return "PathInfoProviderBasedHierarchicalContent [root=" + root + "]";
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
        if (!(obj instanceof PathInfoProviderBasedHierarchicalContent))
        {
            return false;
        }
        PathInfoProviderBasedHierarchicalContent other =
                (PathInfoProviderBasedHierarchicalContent) obj;
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

    static class PathInfoNode extends AbstractHierarchicalContentNode
    {

        private final DataSetPathInfo pathInfo;

        private final File root;

        PathInfoNode(File root, DataSetPathInfo pathInfo)
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
                result.add(new PathInfoNode(root, child));
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
