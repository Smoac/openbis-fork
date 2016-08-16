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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.FtpDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.IFtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.FtpNonExistingFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolverRegistry;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;

/**
 * A registry of ftp resolvers. It keeps the style of old-style resolver regisrty, but actually only calls itself root resolver.
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
                    throw new ConfigurationFailureException("Couldn't load class for file system plugin");
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

    @Override
    public FtpFile resolve(String path, FtpPathResolverContext resolverContext)
    {

        String responseCacheKey = resolverContext.getSessionToken() + "$" + path;
        Cache cache = resolverContext.getCache();
        IFtpFile response = cache.getResponse(responseCacheKey);
        if (response != null)
        {
            operationLog.debug("Path " + path + " requested (found in cache).");
            return response;
        }

        operationLog.debug("Path " + path + " requested.");

        String[] split = path.equals("/") ? new String[] {} : path.substring(1).split("/");
        try
        {
            if (plugins.size() > 0)
            {

                response = resolvePlugins(path, split, resolverContext);
            } else
            {
                response = resolveDefault(path, resolverContext, split);
            }
        } catch (Exception e)
        {
            operationLog.warn("Resolving " + path + " failed", e);
            response = new FtpNonExistingFile(path, "Error when retrieving path");
        }

        cache.putResponse(responseCacheKey, response);
        return response;

    }

    private IFtpFile resolveDefault(String path, FtpPathResolverContext resolverContext, String[] split)
    {
        RootLevelResolver resolver = new RootLevelResolver();
        return resolver.resolve(path, split, resolverContext);
    }

    private IFtpFile resolvePlugins(String path, String[] subPath, FtpPathResolverContext resolverContext)
    {
        if (subPath.length == 0)
        {
            FtpDirectoryResponse response = new FtpDirectoryResponse(path);
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
                        return resolver.resolve(path, remaining, resolverContext);
                    }
                }
                return new FtpNonExistingFile(path, null);
            }
        }
    }

}
