/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

public class FtpDirectoryResponse extends AbstractFtpFolder implements IFtpFile
{
    private final List<FtpFile> files;

    public FtpDirectoryResponse(String fullPath)
    {
        super(fullPath);
        this.files = new ArrayList<>();
    }

    @Override
    public List<FtpFile> unsafeListFiles() throws RuntimeException
    {
        return files;
    }

    /**
     * Adds a directory entry with current timestamp
     */
    public void addDirectory(String directoryName)
    {
        addDirectory(directoryName, ServiceProvider.DSS_STARTUP_DATE);
    }

    public void addDirectory(String directoryName, Date lastModified)
    {
        addDirectory(directoryName, lastModified.getTime());
    }

    public void addDirectory(String directoryName, final long lastModified)
    {
        files.add(new AbstractFtpFolder(getAbsolutePath() + "/" + directoryName)
            {
                @Override
                public List<FtpFile> unsafeListFiles() throws RuntimeException
                {
                    throw new IllegalStateException("Don't expect to sak for file listing of scaffolding directory");
                }

                @Override
                public long getLastModified()
                {
                    return lastModified;
                }
            });
    }

    public void addFile(String fileName, final IHierarchicalContentNode node)
    {
        addFile(fileName, node.getFileLength(), node.getLastModified());
    }

    public void addFile(String fileName, final long size, final long lastModified)
    {
        AbstractFtpFile file = new AbstractFtpFile(this.absolutePath + "/" + fileName)
            {

                @Override
                public boolean isFile()
                {
                    return true;
                }

                @Override
                public boolean isDirectory()
                {
                    return false;
                }

                @Override
                public long getSize()
                {
                    return size;
                }

                @Override
                public long getLastModified()
                {
                    return lastModified;
                }

                @Override
                public InputStream createInputStream(long offset) throws IOException
                {
                    throw new IllegalStateException("Don't expect to ask for input stream of scaffolding file");
                }

                @Override
                public List<FtpFile> unsafeListFiles() throws RuntimeException
                {
                    throw new IllegalStateException("Don't expect to ask for file listing of scaffolding file");
                }
            };
        files.add(file);
    }
}