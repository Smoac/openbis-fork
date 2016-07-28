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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFile;

public class V3FtpFileResponse extends AbstractFtpFile implements V3FtpFile
{
    private IHierarchicalContentNode node;

    public V3FtpFileResponse(String fullPath, final IHierarchicalContentNode node)
    {
        super(fullPath);
        this.node = node;
    }

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
        return node.getFileLength();
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException
    {
        return node.getInputStream();
    }

    @Override
    public List<FtpFile> unsafeListFiles() throws RuntimeException
    {
        throw new IllegalStateException("Don't expect to sak for file listing of file");
    }
}