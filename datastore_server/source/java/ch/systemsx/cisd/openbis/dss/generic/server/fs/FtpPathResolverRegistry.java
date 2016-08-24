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

package ch.systemsx.cisd.openbis.dss.generic.server.fs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.HierarchicalContentUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.DirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.DirectoryResponse.DirectoryNode;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.DirectoryResponse.FileNode;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.DirectoryResponse.Node;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.FileResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.NonExistingFileResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.plugins.FileSystemPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.resolver.RootLevelResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolverRegistry;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.NonExistingFtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFileWithContent;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;

/**
 * A entry point for the file system resolvers. It delegates resolution to plugin or default resolver.
 * 
 * @author Jakub Straszewski
 */
public class FtpPathResolverRegistry implements IFtpPathResolverRegistry
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FtpPathResolverRegistry.class);

    private List<FileSystemPlugin> plugins = new LinkedList<>();

    @Override
    public void initialize(FtpPathResolverConfig config)
    {
        Properties props = config.getProperties();
        String listOfEnabledFileSystemPlugins = props.getProperty(Constants.DSS_FS_PLUGIN_NAMES);

        if (listOfEnabledFileSystemPlugins != null)
        {
            String[] split = listOfEnabledFileSystemPlugins.split(",");
            for (String pluginName : split)
            {
                pluginName = pluginName.trim();
                String className = props.getProperty(pluginName + ".resolver-class");
                String code = props.getProperty(pluginName + ".code");
                try
                {
                    Class<?> clazz = Class.forName(className);
                    plugins.add(new FileSystemPlugin(pluginName, code, clazz));
                } catch (ClassNotFoundException ex)
                {
                    throw new ConfigurationFailureException("Couldn't load class for file system plugin: " + className);
                } catch (SecurityException ex)
                {
                    throw new ConfigurationFailureException("Couldn't load class for file system plugin");
                } catch (Exception ex)
                {
                    throw new ConfigurationFailureException("Couldn't load class for file system plugin", ex);
                }

            }
        }
    }

    private FtpFile convert(IFileSystemViewResponse response)
    {
        if (response instanceof DirectoryResponse)
        {
            return convert((DirectoryResponse) response);
        }
        if (response instanceof FileResponse)
        {
            return convert((FileResponse) response);
        }
        if (response instanceof NonExistingFileResponse)
        {
            return convert((NonExistingFileResponse) response);
        }
        throw new IllegalArgumentException();
    }

    private FtpFile convert(final DirectoryResponse dir)
    {
        return new AbstractFtpFolder(dir.getFullPath())
            {
                @Override
                public List<FtpFile> unsafeListFiles() throws RuntimeException
                {
                    List<FtpFile> result = new LinkedList<FtpFile>();
                    for (Node entry : dir.getFiles())
                    {
                        if (entry instanceof DirectoryNode)
                        {
                            final DirectoryNode dirEntry = (DirectoryNode) entry;
                            result.add(new AbstractFtpFolder(dirEntry.getFullPath())
                                {
                                    @Override
                                    public List<FtpFile> unsafeListFiles() throws RuntimeException
                                    {
                                        throw new IllegalStateException("Don't expect to ask for file listing of scaffolding directory");
                                    }

                                    @Override
                                    public long getLastModified()
                                    {
                                        return dirEntry.getLastModified();
                                    }
                                });
                        } else if (entry instanceof FileNode)
                        {
                            final FileNode fileNode = (FileNode) entry;
                            AbstractFtpFile file = new AbstractFtpFile(fileNode.getFullPath())
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
                                        return fileNode.getSize();
                                    }

                                    @Override
                                    public long getLastModified()
                                    {
                                        return fileNode.getLastModified();
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
                            result.add(file);
                        }
                    }
                    return result;
                }
            };
    }

    private FtpFile convert(final FileResponse file)
    {
        return new AbstractFtpFileWithContent(file.getFullPath())
            {

                @Override
                public long getSize()
                {
                    return file.getNode().getFileLength();
                }

                @Override
                public InputStream createInputStream(long offset) throws IOException
                {
                    try
                    {
                        InputStream result =
                                HierarchicalContentUtils.getInputStreamAutoClosingContent(file.getNode(), file.getContent());

                        if (offset > 0)
                        {
                            result.skip(offset);
                        }
                        return result;
                    } catch (IOException ioex)
                    {
                        file.getContent().close();
                        throw ioex;
                    } catch (RuntimeException re)
                    {
                        file.getContent().close();
                        throw re;
                    }
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
                public List<FtpFile> unsafeListFiles() throws RuntimeException
                {
                    throw new IllegalStateException("Don't expect to sak for file listing of file");
                }

                @Override
                public IRandomAccessFile getFileContent()
                {
                    return file.getNode().getFileContent();
                }
            };
    }

    private FtpFile convert(NonExistingFileResponse nonExistingFile)
    {
        return new NonExistingFtpFile(nonExistingFile.getFullPath(), nonExistingFile.getErrorMsg());
    }

    @Override
    public FtpFile resolve(String path, FtpPathResolverContext resolverContext)
    {

        String responseCacheKey = resolverContext.getSessionToken() + "$" + path;
        Cache cache = resolverContext.getCache();
        FtpFile response = cache.getResponse(responseCacheKey);
        if (response != null)
        {
            operationLog.debug("Path " + path + " requested (found in cache).");
            return response;
        }

        operationLog.debug("Path " + path + " requested.");

        IFileSystemViewResponse ifsResponse;
        String[] split = path.equals("/") ? new String[] {} : path.substring(1).split("/");
        try
        {
            if (plugins.size() > 0)
            {

                ifsResponse = resolvePlugins(path, split, resolverContext);
            } else
            {
                ifsResponse = resolveDefault(path, resolverContext, split);
            }
        } catch (Exception e)
        {
            operationLog.warn("Resolving " + path + " failed", e);
            ifsResponse = resolverContext.getResolverContext().createNonExistingFileResponse("Error when retrieving path");
        }
        response = convert(ifsResponse);
        cache.putResponse(responseCacheKey, response);
        return response;

    }

    private IFileSystemViewResponse resolveDefault(String path, FtpPathResolverContext resolverContext, String[] split)
    {
        RootLevelResolver resolver = new RootLevelResolver();
        return resolver.resolve(split, resolverContext.getResolverContext());
    }

    private IFileSystemViewResponse resolvePlugins(String path, String[] subPath, FtpPathResolverContext resolverContext)
    {
        if (subPath.length == 0)
        {
            DirectoryResponse response = resolverContext.getResolverContext().createDirectoryResponse();
            response.addDirectory("DEFAULT");
            for (FileSystemPlugin plugin : plugins)
            {
                response.addDirectory(plugin.getPluginCode());
            }
            return response;
        } else
        {
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            if (subPath[0].equals("DEFAULT"))
            {
                return resolveDefault(path, resolverContext, remaining);
            } else
            {
                for (FileSystemPlugin plugin : plugins)
                {
                    if (plugin.getPluginCode().equals(subPath[0]))
                    {
                        IResolver resolver = plugin.getPluginResolver();
                        return resolver.resolve(remaining, resolverContext.getResolverContext());
                    }
                }
                return resolverContext.getResolverContext().createNonExistingFileResponse(null);
            }
        }
    }

}
