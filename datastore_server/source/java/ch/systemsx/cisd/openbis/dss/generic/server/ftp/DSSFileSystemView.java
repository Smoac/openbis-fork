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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.util.Key;

/**
 * A central class that manages the movement of a user up and down the exposed hierarchical
 * structure.
 * 
 * @author Kaloyan Enimanev
 */
public class DSSFileSystemView implements FileSystemView
{
    private static final Set<String> METHOD_NAMES = new HashSet<String>(Arrays.asList(
            "tryToGetExperiment", "listDataSetsByExperimentID"));

    private final class ServiceInvocationHandler implements InvocationHandler
    {
        private final Map<Key, Object> cache = new HashMap<Key, Object>();
        private final IETLLIMSService openbisService;

        private ServiceInvocationHandler(IETLLIMSService service)
        {
            this.openbisService = service;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
        {
            if (METHOD_NAMES.contains(method.getName()))
            {
                Key key = new Key(args);
                Object result = cache.get(key);
                if (result == null)
                {
                    result = invoke(method, args);
                    cache.put(key, result);
                }
                return result;
            } else
            {
                return invoke(method, args);
            }
        }

        private Object invoke(Method method, Object[] args) throws IllegalAccessException,
                InvocationTargetException
        {
            return method.invoke(openbisService, args);
        }
    }

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DSSFileSystemView.class);

    private final String sessionToken;

    private final IETLLIMSService service;

    private final IGeneralInformationService generalInfoService;
    
    private FtpFile workingDirectory;

    private final IFtpPathResolverRegistry pathResolverRegistry;

    DSSFileSystemView(String sessionToken, final IETLLIMSService service,
            IGeneralInformationService generalInfoService,
            IFtpPathResolverRegistry pathResolverRegistry) throws FtpException
    {
        this.sessionToken = sessionToken;
        this.service =
                (IETLLIMSService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]
                    { IETLLIMSService.class }, new ServiceInvocationHandler(service));
        this.generalInfoService = generalInfoService;
        this.pathResolverRegistry = pathResolverRegistry;
        this.workingDirectory = getHomeDirectory();
    }

    public boolean changeWorkingDirectory(String path) throws FtpException
    {
        FtpFile ftpFile = getFile(path);
        if (ftpFile != null && ftpFile.isDirectory())
        {
            workingDirectory = ftpFile;
            return true;
        }
        return false;
    }

    public void dispose()
    {
    }

    public FtpFile getFile(String path) throws FtpException
    {
        String normalizedPath = normalizePath(path);

        // this check speeds directory listings in the LFTP console client
        if (workingDirectory != null && workingDirectory.getAbsolutePath().equals(normalizedPath))
        {
            return workingDirectory;
        }

        try
        {
            FtpPathResolverContext context =
                    new FtpPathResolverContext(sessionToken, service, generalInfoService,
                            pathResolverRegistry);
            return pathResolverRegistry.tryResolve(normalizedPath, context);
        } catch (RuntimeException rex)
        {
            String message =
                    String.format("Error while resolving FTP path '%s' : %s", path,
                            rex.getMessage());
            operationLog.error(message);
            throw new FtpException(message, rex);
        }
    }

    private String normalizePath(String path) throws FtpException
    {
        String fullPath = path.trim();
        if (fullPath.startsWith("./"))
        {
            fullPath = fullPath.substring(2);
        }
        if (fullPath.startsWith(FtpConstants.FILE_SEPARATOR) == false)
        {
            String absolutePath = workingDirectory.getAbsolutePath();
            if (absolutePath.endsWith(FtpConstants.FILE_SEPARATOR) == false)
            {
                absolutePath += FtpConstants.FILE_SEPARATOR;
            }
            fullPath = absolutePath + fullPath;
        }

        try
        {
            URI uri = new URI(fullPath);
            String normalizedPath = uri.normalize().toString();
            // remove trailing slashes
            normalizedPath = normalizedPath.replaceAll("/*$", "");
            // replace multiple adjacent slashes with a single slash
            normalizedPath = normalizedPath.replaceAll("/+", "/");
            return StringUtils.isBlank(normalizedPath) ? FtpConstants.ROOT_DIRECTORY : normalizedPath;
        } catch (URISyntaxException ex)
        {
            throw new FtpException("Cannot parse path " + fullPath, ex);
        }
    }

    public FtpFile getHomeDirectory() throws FtpException
    {
        return getFile(FtpConstants.ROOT_DIRECTORY);
    }

    public FtpFile getWorkingDirectory() throws FtpException
    {
        return workingDirectory;
    }

    public boolean isRandomAccessible() throws FtpException
    {
        return true;
    }

}
