/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.cifs;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.alfresco.config.ConfigElement;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.AccessDeniedException;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.DSSFileSystemView;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverRegistry;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.NonExistingFtpFile;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetCifsView implements DiskInterface
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DataSetCifsView.class);
    
    private IServiceForDataStoreServer dssService;
    private IGeneralInformationService generalInfoService;
    
    private FtpPathResolverRegistry pathResolverRegistry;
    
    public DataSetCifsView()
    {
        operationLog.info("CIFS view onto the data store created");
    }

    DataSetCifsView(IServiceForDataStoreServer openBisService, IGeneralInformationService generalInfoService)
    {
        this.dssService = openBisService;
        this.generalInfoService = generalInfoService;
    }
    
    @Override
    public DeviceContext createContext(String shareName, ConfigElement args) throws DeviceContextException
    {
        operationLog.info("create context for share " + shareName);
        System.out.println(Utils.render(args));
        FtpPathResolverConfig resolverConfig = new FtpPathResolverConfig(CifsServerConfig.getServerProperties());
        resolverConfig.logStartupInfo("CIFS");
        pathResolverRegistry = new FtpPathResolverRegistry(resolverConfig);
        return new DiskDeviceContext(shareName);
    }

    @Override
    public void treeOpened(SrvSession sess, TreeConnection tree)
    {
        operationLog.info("tree " + tree + " opened");
    }
    
    @Override
    public void treeClosed(SrvSession sess, TreeConnection tree)
    {
        operationLog.info("tree " + tree + " closed");
    }

    @Override
    public boolean isReadOnly(SrvSession sess, DeviceContext ctx) throws IOException
    {
        return true;
    }

    @Override
    public FileInfo getFileInformation(SrvSession sess, TreeConnection tree, String path) throws IOException
    {
        DSSFileSystemView view = createView(sess);
        String normalizedPath = normalizePath(path);
        try
        {
            FtpFile file = view.getFile(normalizedPath);
            FileInfo fileInfo = new FileInfo();
            Utils.populateFileInfo(fileInfo, file);
            operationLog.info("provide file info for virtual file '" + file.getAbsolutePath() + "': " + fileInfo);
            return fileInfo;
        } catch (FtpException ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public SearchContext startSearch(SrvSession sess, TreeConnection tree, String searchPath, int attrib) throws FileNotFoundException
    {
        String normalizedSearchPath = normalizePath(searchPath);
        if (normalizedSearchPath.startsWith("/"))
        {
            return new DSSFileSearchContext(createView(sess), normalizedSearchPath, attrib);
        }
        throw new FileNotFoundException("Unknown file: " + searchPath);
    }

    @Override
    public void closeFile(SrvSession sess, TreeConnection tree, NetworkFile param) throws IOException
    {
        operationLog.debug("Close virtual file '" + param.getFullName() + "'");
        param.close();
    }

    @Override
    public int fileExists(SrvSession sess, TreeConnection tree, String name)
    {
        FtpFile file = getFile(createView(sess), name);
        if (file == null)
        {
            return FileStatus.NotExist;
        } else if (file.isDirectory())
        {
            return FileStatus.DirectoryExists;
        }
        return FileStatus.FileExists;
    }

    @Override
    public void flushFile(SrvSession sess, TreeConnection tree, NetworkFile file) throws IOException
    {
        operationLog.debug("Flush virtual file '" + file.getFullName() + "'");
    }

    @Override
    public NetworkFile openFile(SrvSession sess, TreeConnection tree, FileOpenParams params) throws IOException
    {
        final String fullPath = normalizePath(params.getFullPath());
        String path = normalizePath(params.getPath());
        DSSFileSystemView view = createView(sess);
        FtpFile file = getFile(view, fullPath);
        if (file instanceof NonExistingFtpFile)
        {
            NonExistingFtpFile nonExistingFtpFile = (NonExistingFtpFile) file;
            throw new FileNotFoundException(path + " does not exist. Reason: " + nonExistingFtpFile.getErrorMessage());
        }
        NetworkFile networkFile = new CifsFile(file);
        
        networkFile.setAttributes(FileAttribute.ReadOnly);
        networkFile.setCreationDate(file.getLastModified());
        networkFile.setModifyDate(file.getLastModified());
        networkFile.setFullName(params.getPath());
        networkFile.setFileId(fullPath.hashCode());
        if (file.isDirectory())
        {
            networkFile.setAttributes(FileAttribute.Directory| FileAttribute.ReadOnly);
        } else
        {
            networkFile.setFileSize(file.getSize());
        }
        operationLog.info("Virtual file '" + fullPath + "' opened.");
        return networkFile;
    }

    @Override
    public int readFile(SrvSession sess, TreeConnection tree, NetworkFile file, byte[] buf, int bufPos, 
            int size, long filePos) throws IOException
    {
        if (file.isDirectory())
        {
            throw new AccessDeniedException();
        }
        operationLog.debug("Read from virtual file '" + file.getFullName() + "' at position " + filePos + " "
                + size + " bytes into the buffer of size " + buf.length + " at position " + bufPos + ".");
        int rdlen = file.readFile(buf, size, bufPos, filePos);
        if (rdlen == -1)
        {
            rdlen = 0;
        }
        return rdlen;
    }

    @Override
    public long seekFile(SrvSession sess, TreeConnection tree, NetworkFile file, long pos, int type) throws IOException
    {
        operationLog.debug("Seek in virtual file '" + file.getFullName() + "' to position " + pos + " (seek type: " + type + ").");
        return file.seekFile(pos, type);
    }

    @Override
    public void createDirectory(SrvSession sess, TreeConnection tree, FileOpenParams params) throws IOException
    {
        throw new IOException("Can not create a new directory because the virtual file system is read only.");
    }

    @Override
    public NetworkFile createFile(SrvSession sess, TreeConnection tree, FileOpenParams params) throws IOException
    {
        throw new IOException("Can not create a new file because the virtual file system is read only.");
    }

    @Override
    public void deleteDirectory(SrvSession sess, TreeConnection tree, String dir) throws IOException
    {
        throw new IOException("Can not delete a directory because the virtual file system is read only.");
    }

    @Override
    public void deleteFile(SrvSession sess, TreeConnection tree, String name) throws IOException
    {
        throw new IOException("Can not delete a file because the virtual file system is read only.");
    }

    @Override
    public void renameFile(SrvSession sess, TreeConnection tree, String oldName, String newName) throws IOException
    {
        throw new IOException("Can not rename a file because the virtual file system is read only.");
    }

    @Override
    public void setFileInformation(SrvSession sess, TreeConnection tree, String name, FileInfo info) throws IOException
    {
        throw new IOException("Can not set file information because the virtual file system is read only.");
    }

    @Override
    public void truncateFile(SrvSession sess, TreeConnection tree, NetworkFile file, long siz) throws IOException
    {
        throw new IOException("Can not truncate file because the virtual file system is read only.");
    }

    @Override
    public int writeFile(SrvSession sess, TreeConnection tree, NetworkFile file, byte[] buf, int bufoff, int siz, long fileoff) throws IOException
    {
        throw new IOException("Can not write into a file because the virtual file system is read only.");
    }

    private FtpFile getFile(DSSFileSystemView view, String originalPath)
    {
        try
        {
            return view.getFile(normalizePath(originalPath));
        } catch (FtpException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private DSSFileSystemView createView(SrvSession session)
    {
        try
        {
            String sessionToken = getSessionToken(session);
            return new DSSFileSystemView(sessionToken, getDssService(), getGeneralInfoService(), pathResolverRegistry);
        } catch (FtpException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private String normalizePath(String path)
    {
        return FileName.buildPath(null, path, null, java.io.File.separatorChar);
    }

    private IServiceForDataStoreServer getDssService()
    {
        if (dssService == null)
        {
            dssService = ServiceProvider.getServiceForDSS();
        }
        return dssService;
    }
    
    private IGeneralInformationService getGeneralInfoService()
    {
        if (generalInfoService == null)
        {
            generalInfoService = ServiceProvider.getGeneralInformationService();
        }
        return generalInfoService;
    }
    
    private String getSessionToken(SrvSession sess)
    {
        ClientInfo client = sess.getClientInformation();
        if (client instanceof OpenBisClientInfo)
        {
            return ((OpenBisClientInfo) client).getSessionToken();
        }
        return null;
    }
}
