/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.tar.Untar;
import ch.systemsx.cisd.common.io.MonitoredIOStreamCopier;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * An implementations of {@link ISingleDataSetPathInfoProvider} which retrieves and calculates path info 
 * from a tar file.
 *
 * @author Franz-Josef Elmer
 */
class TarBasedPathInfoProvider implements ISingleDataSetPathInfoProvider
{
    private final File packageFile;
    private final int bufferSize;
    private final ISimpleLogger ioSpeedLogger;
    
    private Map<String, DataSetPathInfo> pathInfos;
    private Map<String, List<DataSetPathInfo>> pathInfoChildren;

    TarBasedPathInfoProvider(File packageFile, int bufferSize, ISimpleLogger ioSpeedLogger)
    {
        this.packageFile = packageFile;
        this.bufferSize = bufferSize;
        this.ioSpeedLogger = ioSpeedLogger;
    }

    @Override
    public DataSetPathInfo getRootPathInfo()
    {
        return tryGetPathInfoByRelativePath("");
    }

    @Override
    public DataSetPathInfo tryGetPathInfoByRelativePath(String relativePath)
    {
        return getPathInfos().get(relativePath);
    }

    @Override
    public List<DataSetPathInfo> listChildrenPathInfos(DataSetPathInfo parent)
    {
        return pathInfoChildren.get(parent.getRelativePath());
    }

    @Override
    public List<DataSetPathInfo> listMatchingPathInfos(String relativePathPattern)
    {
        List<DataSetPathInfo> result = new ArrayList<DataSetPathInfo>();
        Pattern pattern = Pattern.compile(relativePathPattern);
        for (DataSetPathInfo pathInfo : pathInfos.values())
        {
            if (pattern.matcher(pathInfo.getRelativePath()).matches())
            {
                result.add(pathInfo);
            }
        }
        return result;
    }

    @Override
    public List<DataSetPathInfo> listMatchingPathInfos(String startingPath, String fileNamePattern)
    {
        List<DataSetPathInfo> result = new ArrayList<DataSetPathInfo>();
        Pattern pattern = Pattern.compile(fileNamePattern);
        for (DataSetPathInfo pathInfo : pathInfos.values())
        {
            if (pathInfo.getRelativePath().startsWith(startingPath) 
                    && pattern.matcher(pathInfo.getFileName()).matches())
            {
                result.add(pathInfo);
            }
        }
        return result;
    }
    

    private Map<String, DataSetPathInfo> getPathInfos()
    {
        if (pathInfos == null)
        {
            MonitoredIOStreamCopier copier = new MonitoredIOStreamCopier(bufferSize);
            copier.setLogger(ioSpeedLogger);
            UntarMetaData untar = null;
            try
            {
                pathInfos = new TreeMap<String, DataSetPathInfo>();
                untar = new UntarMetaData(packageFile, copier, pathInfos);
                untar.extract((File) null);
                createPathInfoLinks();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                if (untar != null)
                {
                    try
                    {
                        untar.close();
                    } catch (IOException e)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(e);
                    }
                }
            }
        }
        return pathInfos;
    }
    
    private void createPathInfoLinks()
    {
        pathInfoChildren = new HashMap<String, List<DataSetPathInfo>>();
        for (DataSetPathInfo pathInfo : pathInfos.values())
        {
            String relativePath = pathInfo.getRelativePath();
            String parentRelativePath = FileUtilities.getParentRelativePath(relativePath);
            DataSetPathInfo parentPathInfo = pathInfos.get(parentRelativePath);
            pathInfo.setParent(parentPathInfo);
            List<DataSetPathInfo> children = pathInfoChildren.get(parentRelativePath);
            if (children == null)
            {
                children = new ArrayList<DataSetPathInfo>();
                pathInfoChildren.put(parentRelativePath, children);
            }
            children.add(pathInfo);
        }
    }
    
    private static final void addDirectory(String path, Map<String, DataSetPathInfo> pathInfos)
    {
        if (StringUtils.isNotBlank(path) && pathInfos.containsKey(path) == false)
        {
            DataSetPathInfo pathInfo = new DataSetPathInfo();
            pathInfo.setDirectory(true);
            pathInfo.setFileName(FileUtilities.getFileNameFromRelativePath(path));
            pathInfo.setRelativePath(path);
            pathInfos.put(path, pathInfo);
            addDirectory(FileUtilities.getParentRelativePath(path), pathInfos);
        }

    }
    
    private static final class UntarMetaData extends Untar
    {
        private Map<String, DataSetPathInfo> pathInfos;

        public UntarMetaData(File tarFile, MonitoredIOStreamCopier copier, Map<String, DataSetPathInfo> pathInfos) throws FileNotFoundException
        {
            super(tarFile, copier);
            this.pathInfos = pathInfos;
        }

        @Override
        protected OutputStream createOutputStream(File entryFile, TarArchiveEntry entry)
                throws IOException
        {
            return new PathInfoOutputStream(entry, pathInfos);
        }
        
        @Override
        protected void createDirectory(File directory)
        {
            addDirectory(directory.getPath(), pathInfos);
        }

        @Override
        protected void createParentDirectories(File file)
        {
        }

        @Override
        protected void createSymbolicLink(File file, TarArchiveEntry entry)
        {
        }

        @Override
        protected void createHardLink(File file, TarArchiveEntry entry)
        {
        }

        @Override
        protected void setFileMetadata(File entryFile, TarArchiveEntry entry)
        {
        }
    }
    
    private static final class PathInfoOutputStream extends FilterOutputStream
    {
        private final TarArchiveEntry entry;
        private final Map<String, DataSetPathInfo> pathInfos;
        private final Checksum checksum;
        private long fileSize;

        public PathInfoOutputStream(TarArchiveEntry entry, Map<String, DataSetPathInfo> pathInfos)
        {
            super(new NullOutputStream());
            this.entry = entry;
            this.pathInfos = pathInfos;
            checksum = new CRC32();
        }

        @Override
        public void write(int b) throws IOException
        {
            out.write(b);
            checksum.update(b);
            fileSize++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            out.write(b, off, len);
            checksum.update(b, off, len);
            fileSize += len;
        }

        @Override
        public void close() throws IOException
        {
            super.close();
            String name = entry.getName();
            DataSetPathInfo pathInfo = new DataSetPathInfo();
            pathInfo.setChecksumCRC32((int) checksum.getValue());
            pathInfo.setDirectory(false);
            pathInfo.setFileName(FileUtilities.getFileNameFromRelativePath(name));
            pathInfo.setLastModified(entry.getLastModifiedDate());
            pathInfo.setRelativePath(name);
            pathInfo.setSizeInBytes(fileSize);
            pathInfos.put(name, pathInfo);
            addDirectory(FileUtilities.getParentRelativePath(name), pathInfos);
        }
        
    }
}
