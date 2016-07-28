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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.NonExistingFtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFolder;

public abstract class V3Resolver
{
    protected FtpPathResolverContext resolverContext;

    protected IApplicationServerApi api;

    protected String sessionToken;

    public V3Resolver(FtpPathResolverContext resolverContext)
    {
        this.resolverContext = resolverContext;
        this.sessionToken = resolverContext.getSessionToken();
        this.api = resolverContext.getV3Api();
    }

    /**
     * Create a ftp file which has specified full path, resolving the local path specified as an array of path items.
     */
    public abstract V3FtpFile resolve(String fullPath, String[] pathItems);

    public interface V3FtpFile extends FtpFile
    {

    }

    public class V3FtpDirectoryResponse extends AbstractFtpFolder implements V3FtpFile
    {
        private final List<FtpFile> files;

        public V3FtpDirectoryResponse(String fullPath)
        {
            super(fullPath);
            this.files = new ArrayList<>();
        }

        @Override
        public List<FtpFile> unsafeListFiles() throws RuntimeException
        {
            return files;
        }

        public void AddDirectory(String directoryName)
        {
            files.add(new AbstractFtpFolder(getAbsolutePath() + "/" + directoryName)
                {
                    @Override
                    public List<FtpFile> unsafeListFiles() throws RuntimeException
                    {
                        throw new IllegalStateException("Don't expect to sak for file listing of scaffolding directory");
                    }
                });
        }

        public void AddFile(String fileName, final IHierarchicalContentNode node)
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
                        return node.getFileLength();
                    }

                    @Override
                    public long getLastModified()
                    {
                        return node.getLastModified();
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

    /**
     * Create a representation for a non-existing {@link FtpFile}, optionally providing an error message.
     */
    public static final FtpFile getNonExistingFile(final String path, final String errorMsgOrNull)
    {
        return new NonExistingFtpFile(path, errorMsgOrNull);
    }
}