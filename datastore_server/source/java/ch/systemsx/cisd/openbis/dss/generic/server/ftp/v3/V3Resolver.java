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

    public abstract FtpFile resolve(String fullPath, String[] subPath);

    public static FtpFile createDirectoryScaffolding(String parentPath, String name)
    {
        return new AbstractFtpFolder(parentPath + "/" + name)
            {
                @Override
                public List<FtpFile> unsafeListFiles() throws RuntimeException
                {
                    throw new IllegalStateException("Don't expect to sak for file listing of scaffolding directory");
                }
            };
    }

    public static FtpFile createDirectoryWithContent(String name, final List<FtpFile> files)
    {
        return new AbstractFtpFolder(name)
            {

                @Override
                public List<FtpFile> unsafeListFiles() throws RuntimeException
                {
                    return files;
                }

            };
    }

    protected FtpFile createFileWithContent(String name, final IHierarchicalContentNode node)
    {
        return new AbstractFtpFile(name)
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
                    throw new IllegalStateException("Size is not required for content node");
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
            };
    }

    protected FtpFile createFileScaffolding(String parentPath, String name, final IHierarchicalContentNode node)
    {
        return new AbstractFtpFile(parentPath + "/" + name)
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
    }

    /**
     * Create a representation for a non-existing {@link FtpFile}, optionally providing an error message.
     */
    public static final FtpFile getNonExistingFile(final String path, final String errorMsgOrNull)
    {
        return new NonExistingFtpFile(path, errorMsgOrNull);
    }
}