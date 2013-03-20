/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.io.IOUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.HierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.DssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.IDssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.PathInfoDBOnlyHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetAndPathInfoDBConsistencyChecker
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetAndPathInfoDBConsistencyChecker.class);

    private static final Comparator<Map.Entry<IDatasetLocation, List<Difference>>> DATA_SET_COMPARATOR =
            new Comparator<Map.Entry<IDatasetLocation, List<Difference>>>()
                {
                    @Override
                    public int compare(Entry<IDatasetLocation, List<Difference>> e1,
                            Entry<IDatasetLocation, List<Difference>> e2)
                    {
                        return e1.getKey().getDataSetCode().compareTo(e2.getKey().getDataSetCode());
                    }
                };

    private IHierarchicalContentProvider fileProvider;
    private IHierarchicalContentProvider pathInfoProvider;
    private IDssServiceRpcGenericFactory serviceFactory;

    private Map<IDatasetLocation, List<Difference>> differences;

    private ProcessingStatus status;

    private List<? extends IDatasetLocation> dataSets;

    public DataSetAndPathInfoDBConsistencyChecker(
            IHierarchicalContentProvider fileProvider, IHierarchicalContentProvider pathInfoProvider)
    {
        this.fileProvider = fileProvider;
        this.pathInfoProvider = pathInfoProvider;
    }
    
    public void check(List<? extends IDatasetLocation> datasets)
    {
        dataSets = datasets;
        differences = new HashMap<IDatasetLocation, List<Difference>>();
        status = new ProcessingStatus();

        for (IDatasetLocation dataset : datasets)
        {
            IHierarchicalContent fileContent = null;
            IHierarchicalContent pathInfoContent = null;

            try
            {
                fileContent = tryGetContent(getFileProvider(), dataset.getDataSetCode());
                pathInfoContent = tryGetContent(getPathInfoProvider(), dataset.getDataSetCode());

                List<Difference> datasetDifferences = new ArrayList<Difference>();

                compare(fileContent, pathInfoContent, datasetDifferences);

                if (datasetDifferences.isEmpty() == false)
                {
                    differences.put(dataset, datasetDifferences);
                }
                status.addDatasetStatus(dataset.getDataSetCode(), Status.OK);

            } catch (Exception e)
            {
                operationLog.error(
                        "Couldn't check consistency of the file system and the path info database for a data set: "
                                + dataset.getDataSetCode(), e);
                status.addDatasetStatus(
                        dataset.getDataSetCode(),
                        Status.createError("Couldn't check consistency of the file system and the path info database for a data set: "
                                + dataset.getDataSetCode()
                                + " because of the following exception: " + e.getMessage()));
            } finally
            {
                if (null != fileContent)
                {
                    fileContent.close();
                }
                if (null != pathInfoContent)
                {
                    pathInfoContent.close();
                }
            }
        }
    }
    
    public ProcessingStatus getStatus()
    {
        if (status == null)
        {
            throw new IllegalStateException("Undefined status before check() has been executed.");
        }
        return status;
    }
    
    public boolean noErrorAndInconsitencyFound()
    {
        return status.getErrorStatuses().isEmpty() && differences.isEmpty();
    }
    
    public String createReport()
    {
        return status.getErrorStatuses().isEmpty() ? createNormalReport() : createErrorReport();
    }
    
    private String createNormalReport()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Data sets checked:\n\n");

        Iterator<? extends IDatasetLocation> datasetsIterator = dataSets.iterator();
        while (datasetsIterator.hasNext())
        {
            builder.append(datasetsIterator.next().getDataSetCode());
            if (datasetsIterator.hasNext())
            {
                builder.append(", ");
            }
        }

        builder.append("\n\n");
        builder.append("Differences found:\n\n");

        if (differences.isEmpty())
        {
            builder.append("None");
        } else
        {
            List<Map.Entry<IDatasetLocation, List<Difference>>> entries =
                    new ArrayList<Map.Entry<IDatasetLocation, List<Difference>>>(
                            differences.entrySet());
            Collections.sort(entries, DATA_SET_COMPARATOR);
            for (Map.Entry<IDatasetLocation, List<Difference>> differencesEntry : entries)
            {
                IDatasetLocation dataset = differencesEntry.getKey();
                List<Difference> datasetDifferences = differencesEntry.getValue();

                Collections.sort(datasetDifferences);

                builder.append("Data set " + dataset.getDataSetCode() + ":\n");
                for (Difference datasetDifference : datasetDifferences)
                {
                    builder.append("- " + datasetDifference.getDescription() + "\n");
                }
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    private String createErrorReport()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("Error when checking datasets:\n\n");

        for (Status s : status.getErrorStatuses())
        {
            builder.append(s.toString());
            builder.append("\n");
        }

        return builder.toString();

    }

    private void compare(IHierarchicalContent fileContent, IHierarchicalContent pathInfoContent,
            List<Difference> diffs)
    {
        IHierarchicalContentNode fileRoot = tryGetRoot(fileContent);
        IHierarchicalContentNode pathInfoRoot = tryGetRoot(pathInfoContent);

        if (fileRoot != null && pathInfoRoot != null)
        {
            compare(fileRoot, pathInfoRoot, diffs);
        } else if (fileRoot == null ^ pathInfoRoot == null)
        {
            diffs.add(new RootExistenceDifference(fileRoot != null));
        } else
        {
            throw new IllegalArgumentException(
                    "Data set does not exist on the file system nor in the path info database");
        }
    }

    @SuppressWarnings("null")
    private void compare(IHierarchicalContentNode fileNode, IHierarchicalContentNode pathInfoNode,
            List<Difference> diffs)
    {
        boolean fileNodeExists = fileNode != null && fileNode.exists();
        boolean pathInfoNodeExists = pathInfoNode != null && pathInfoNode.exists();

        // report a difference if one node is null and the other not
        if (fileNodeExists == false || pathInfoNodeExists == false)
        {
            if (fileNodeExists && pathInfoNodeExists == false)
            {
                diffs.add(new NodeExistenceDifference(fileNode.getRelativePath(), true));
            }
            if (pathInfoNodeExists && fileNodeExists == false)
            {
                diffs.add(new NodeExistenceDifference(pathInfoNode.getRelativePath(), false));
            }
            return;
        }

        // report a difference if node paths do not match
        if (fileNode.getRelativePath().equals(pathInfoNode.getRelativePath()) == false)
        {
            diffs.add(new NodeChildrenDifference(fileNode.getRelativePath(), true));
            diffs.add(new NodeChildrenDifference(pathInfoNode.getRelativePath(), false));
        }

        if (fileNode.isDirectory() && pathInfoNode.isDirectory())
        {
            Children children = new Children(fileNode, pathInfoNode);

            // compare children nodes that exist both in the file system and the path info database
            for (String commonPath : children.getCommonPaths())
            {
                compare(children.getFileNode(commonPath), children.getPathInfoNode(commonPath),
                        diffs);
            }

            // report differences for nodes that exist only in one place
            for (IHierarchicalContentNode uncommonNode : children.getFileUncommonNodes())
            {
                diffs.add(new NodeChildrenDifference(uncommonNode.getRelativePath(), true));
            }
            for (IHierarchicalContentNode uncommonNode : children.getPathInfoUncommonNodes())
            {
                diffs.add(new NodeChildrenDifference(uncommonNode.getRelativePath(), false));
            }

        } else if (fileNode.isDirectory() == false && pathInfoNode.isDirectory() == false)
        {
            // check file lengths
            if (fileNode.getFileLength() != pathInfoNode.getFileLength())
            {
                diffs.add(new SizeDifference(fileNode.getRelativePath(), fileNode
                        .getFileLength(), pathInfoNode.getFileLength()));
            }
            // check checksums if stored in path Info db
            if (pathInfoNode.isChecksumCRC32Precalculated()
                    && (fileNode.getChecksumCRC32() != pathInfoNode.getChecksumCRC32()))
            {
                diffs.add(new ChecksumDifference(fileNode.getRelativePath(), fileNode
                        .getChecksumCRC32(), pathInfoNode.getChecksumCRC32()));
            }
        } else
        {
            // report a difference if one node is a directory and the other is a file
            diffs.add(new DirectoryDifference(fileNode.getRelativePath(), fileNode
                    .isDirectory()));
        }
    }

    private abstract class Difference implements Comparable<Difference>
    {

        private String path;

        public Difference(String path)
        {
            this.path = path;
        }

        public String getPath()
        {
            return path;
        }

        public abstract String getDescription();

        @Override
        public int compareTo(Difference o)
        {
            return getPath().compareTo(o.getPath());
        }

    }

    private class DirectoryDifference extends Difference
    {

        private boolean dirInFS;

        public DirectoryDifference(String path, boolean dirInFS)
        {
            super(path);
            this.dirInFS = dirInFS;
        }

        @Override
        public String getDescription()
        {
            if (dirInFS)
            {
                return "'"
                        + getPath()
                        + "' is a directory in the file system but a file in the path info database";
            } else
            {
                return "'"
                        + getPath()
                        + "' is a directory in the path info database but a file in the file system";
            }
        }

    }

    private class RootExistenceDifference extends Difference
    {

        private boolean existsInFS;

        public RootExistenceDifference(boolean existsInFS)
        {
            super(null);
            this.existsInFS = existsInFS;
        }

        @Override
        public String getDescription()
        {
            if (existsInFS)
            {
                return "exists in the file system but does not exist in the path info database";
            } else
            {
                return "exists in the path info database but does not exist in the file system";
            }
        }

    }

    private class NodeExistenceDifference extends Difference
    {

        private boolean existsInFS;

        public NodeExistenceDifference(String path, boolean existsInFS)
        {
            super(path);
            this.existsInFS = existsInFS;
        }

        @Override
        public String getDescription()
        {
            if (existsInFS)
            {
                return "'"
                        + getPath()
                        + "' exists on the file system but does not exist in the path info database";
            } else
            {
                return "'"
                        + getPath()
                        + "' exists in the path info database but does not exist on the file system";
            }
        }

    }

    private class NodeChildrenDifference extends Difference
    {

        private boolean existsInFS;

        public NodeChildrenDifference(String path, boolean existsInFS)
        {
            super(path);
            this.existsInFS = existsInFS;
        }

        @Override
        public String getDescription()
        {
            if (existsInFS)
            {
                return "'" + getPath()
                        + "' is on the file system but is not referenced in the path info database";
            } else
            {
                return "'"
                        + getPath()
                        + "' is referenced in the path info database but does not exist on the file system";
            }
        }

    }

    private class SizeDifference extends Difference
    {

        private long sizeInFS;

        private long sizeInDB;

        public SizeDifference(String path, long sizeInFS, long sizeInDB)
        {
            super(path);
            this.sizeInFS = sizeInFS;
            this.sizeInDB = sizeInDB;
        }

        @Override
        public String getDescription()
        {
            return "'" + getPath() + "' size in the file system = " + sizeInFS
                    + " bytes but in the path info database = " + sizeInDB + " bytes.";
        }

    }

    private class ChecksumDifference extends Difference
    {

        private int checksumInFS;

        private int checksumInDB;

        public ChecksumDifference(String path, int checksumInFS, int checksumInDB)
        {
            super(path);
            this.checksumInFS = checksumInFS;
            this.checksumInDB = checksumInDB;
        }

        @Override
        public String getDescription()
        {
            return "'" + getPath() + "' CRC32 checksum in the file system = "
                    + IOUtilities.crc32ToString(checksumInFS) + " but in the path info database = "
                    + IOUtilities.crc32ToString(checksumInDB);
        }

    }

    private class Children
    {

        private Map<String, IHierarchicalContentNode> fileChildrenMap;

        private Map<String, IHierarchicalContentNode> pathInfoChildrenMap;

        public Children(IHierarchicalContentNode fileNode, IHierarchicalContentNode pathInfoNode)
        {
            this.fileChildrenMap = getMap(fileNode);
            this.pathInfoChildrenMap = getMap(pathInfoNode);
        }

        public IHierarchicalContentNode getFileNode(String path)
        {
            return fileChildrenMap.get(path);
        }

        public IHierarchicalContentNode getPathInfoNode(String path)
        {
            return pathInfoChildrenMap.get(path);
        }

        public Set<IHierarchicalContentNode> getFileUncommonNodes()
        {
            return getUncommonNodes(fileChildrenMap);
        }

        public Set<IHierarchicalContentNode> getPathInfoUncommonNodes()
        {
            return getUncommonNodes(pathInfoChildrenMap);
        }

        public Set<String> getCommonPaths()
        {
            Set<String> commonPaths = new HashSet<String>();
            for (String path : fileChildrenMap.keySet())
            {
                if (pathInfoChildrenMap.containsKey(path))
                {
                    commonPaths.add(path);
                }
            }
            return commonPaths;
        }

        private Map<String, IHierarchicalContentNode> getMap(IHierarchicalContentNode node)
        {
            Map<String, IHierarchicalContentNode> map =
                    new TreeMap<String, IHierarchicalContentNode>();
            for (IHierarchicalContentNode child : node.getChildNodes())
            {
                map.put(child.getRelativePath(), child);
            }
            return map;
        }

        public Set<IHierarchicalContentNode> getUncommonNodes(
                Map<String, IHierarchicalContentNode> childrenMap)
        {
            Set<String> commonNames = getCommonPaths();
            Set<IHierarchicalContentNode> uncommonNodes = new HashSet<IHierarchicalContentNode>();

            for (Map.Entry<String, IHierarchicalContentNode> child : childrenMap.entrySet())
            {
                if (commonNames.contains(child.getValue().getRelativePath()) == false)
                {
                    uncommonNodes.add(child.getValue());
                }
            }
            return uncommonNodes;
        }

    }

    private IHierarchicalContent tryGetContent(IHierarchicalContentProvider contentProvider,
            String datasetCode)
    {
        try
        {
            return contentProvider.asContent(datasetCode);
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    private IHierarchicalContentNode tryGetRoot(IHierarchicalContent content)
    {
        try
        {
            if (content == null)
            {
                return null;
            } else
            {
                return content.getRootNode();
            }
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    private IHierarchicalContentProvider getFileProvider()
    {
        if (fileProvider == null)
        {
            fileProvider =
                    new HierarchicalContentProvider(ServiceProvider.getOpenBISService(),
                            ServiceProvider.getShareIdManager(),
                            ServiceProvider.getConfigProvider(), ServiceProvider.getContentCache(),
                            new DefaultFileBasedHierarchicalContentFactory(), getServiceFactory(),
                            null, null);
        }
        return fileProvider;
    }

    private IHierarchicalContentProvider getPathInfoProvider()
    {
        if (pathInfoProvider == null)
        {
            IHierarchicalContentFactory pathInfoDBFactory =
                    PathInfoDBOnlyHierarchicalContentFactory.create();

            if (pathInfoDBFactory == null)
            {
                throw new IllegalArgumentException("Path info database is not configured.");
            } else
            {
                pathInfoProvider =
                        new HierarchicalContentProvider(ServiceProvider.getOpenBISService(),
                                ServiceProvider.getShareIdManager(),
                                ServiceProvider.getConfigProvider(),
                                ServiceProvider.getContentCache(), pathInfoDBFactory,
                                getServiceFactory(), null, null);
            }
        }
        return pathInfoProvider;
    }

    private IDssServiceRpcGenericFactory getServiceFactory()
    {
        if (serviceFactory == null)
        {
            serviceFactory = new DssServiceRpcGenericFactory();
        }
        return serviceFactory;
    }
}
