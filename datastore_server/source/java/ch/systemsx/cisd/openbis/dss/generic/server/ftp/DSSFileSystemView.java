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

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * A central class that manages the movement of a user up and down the exposed hierarchical
 * structure.
 * 
 * @author Kaloyan Enimanev
 */
public class DSSFileSystemView implements FileSystemView
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DSSFileSystemView.class);

    private final String sessionToken;

    private final IETLLIMSService service;

    private final IGeneralInformationService generalInfoService;
    
    private FtpFile workingDirectory;

    private final IFtpPathResolverRegistry pathResolverRegistry;

    DSSFileSystemView(String sessionToken, IETLLIMSService service,
            IGeneralInformationService generalInfoService,
            IFtpPathResolverRegistry pathResolverRegistry) throws FtpException
    {
        this.sessionToken = sessionToken;
        this.service = service;
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
        String result = path.trim();
        if (result.startsWith(".."))
        {
            String currentPath = workingDirectory.getAbsolutePath();
            int idx = currentPath.lastIndexOf(FtpConstants.FILE_SEPARATOR);
            result = currentPath.substring(0, idx) + result.substring(2);
        } else if (result.startsWith("."))
        {
            result = workingDirectory.getAbsolutePath() + result.substring(1);
        } else if (false == result.startsWith(FtpConstants.ROOT_DIRECTORY))
        {
            result = workingDirectory.getAbsolutePath() + FtpConstants.FILE_SEPARATOR + result;
        }
        // remove '.' at the end of a path
        result = result.replaceAll("/\\.$", "/");
        // remove trailing slashes
        result = result.replaceAll("/*$", "");
        // replace multiple adjacent slashes with a single slash
        result = result.replaceAll("/+", "/");

        if (StringUtils.isBlank(result))
        {
            return FtpConstants.ROOT_DIRECTORY;
        }

        return result;
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
