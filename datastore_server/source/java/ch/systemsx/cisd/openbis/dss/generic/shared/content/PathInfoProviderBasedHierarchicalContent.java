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
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.io.AbstractHierarchicalContentNode;
import ch.systemsx.cisd.common.io.HDF5ContainerBasedHierarchicalContentNode;
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
        return new PathInfoNode(pathInfo);
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

    class PathInfoNode extends AbstractHierarchicalContentNode
    {
        private final DataSetPathInfo pathInfo;

        private IFileContentProvider fileContentProviderOrNull;

        PathInfoNode(DataSetPathInfo pathInfo)
        {
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
            List<DataSetPathInfo> pathInfos =
                    dataSetPathInfoProvider.listChildrenPathInfos(pathInfo);
            List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
            for (DataSetPathInfo child : pathInfos)
            {
                result.add(asNode(child));
            }
            return result;
        }

        @Override
        protected long doGetFileLength()
        {
            return pathInfo.getSizeInBytes();
        }

        public File getFile() throws UnsupportedOperationException
        {
            File result = doGetFile();
            if (result.exists())
            {
                return result;
            } else
            {
                throw new UnsupportedOperationException("This is not a normal file/directory node.");
            }
        }

        @Override
        protected IRandomAccessFile doGetFileContent()
        {
            return getContentProvider().getReadOnlyRandomAccessFile();
        }

        @Override
        protected InputStream doGetInputStream()
        {
            return getContentProvider().getInputStream();
        }

        private IFileContentProvider getContentProvider()
        {
            if (fileContentProviderOrNull == null)
            {
                File file = doGetFile();
                fileContentProviderOrNull = getFileContentProvider(file);
            }
            return fileContentProviderOrNull;
        }

        /**
         * Returns a file object with given node's relative path. The file doesn't have to exist on
         * the file system.
         */
        private File doGetFile()
        {
            if (StringUtils.isBlank(getRelativePath()))
            {
                return root;
            } else
            {
                return new File(root, getRelativePath());
            }
        }
    }

    private IFileContentProvider getFileContentProvider(File file)
    {
        if (file.exists())
        {
            return asFileContentProvider(file);
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
                    new HDF5ContainerBasedHierarchicalContentNode(this, existingFile);
            String relativePath = FileUtilities.getRelativeFile(existingFile, file);
            IHierarchicalContentNode node = containerNode.getChildNode(relativePath);
            return asFileContentProvider(node);
        }
        throw new IllegalArgumentException("Resource '" + FileUtilities.getRelativeFile(root, file)
                + "' is currently not available. It might be in an archive.");
    }

    private interface IFileContentProvider
    {
        public IRandomAccessFile getReadOnlyRandomAccessFile();

        public InputStream getInputStream();
    }

    private static IFileContentProvider asFileContentProvider(final File existingFile)
    {
        assert existingFile.exists();
        return new IFileContentProvider()
            {

                public IRandomAccessFile getReadOnlyRandomAccessFile()
                {
                    return new RandomAccessFileImpl(existingFile, "r");
                }

                public InputStream getInputStream()
                {
                    try
                    {
                        return new FileInputStream(existingFile);
                    } catch (FileNotFoundException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }

            };
    }

    private static IFileContentProvider asFileContentProvider(final IHierarchicalContentNode node)
    {
        return new IFileContentProvider()
            {

                public IRandomAccessFile getReadOnlyRandomAccessFile()
                {
                    return node.getFileContent();
                }

                public InputStream getInputStream()
                {
                    return node.getInputStream();
                }

            };
    }

}
