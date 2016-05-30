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
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
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
    
    private IEncapsulatedOpenBISService openBISService;

    private FtpPathResolverRegistry pathResolverRegistry;
    
    public DataSetCifsView()
    {
        System.err.println("create a new CIFS view instance");
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
        pathResolverRegistry = new FtpPathResolverRegistry(resolverConfig);
        return new DiskDeviceContext(shareName);
    }

    @Override
    public void treeOpened(SrvSession sess, TreeConnection tree)
    {
        operationLog.info("DataSetCifsView.treeOpened()");
    }
    

    @Override
    public boolean isReadOnly(SrvSession sess, DeviceContext ctx) throws IOException
    {
        System.out.println("DataSetCifsView.isReadOnly() " + ctx);
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
            System.out.println("fileInfo:" + fileInfo+" file:"+file.getAbsolutePath());
            return fileInfo;
        } catch (FtpException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    public SearchContext startSearch(SrvSession sess, TreeConnection tree, String searchPath, int attrib) throws FileNotFoundException
    {
        return new DSSFileSearchContext(createView(sess), normalizePath(searchPath), attrib);
    }

    @Override
    public void treeClosed(SrvSession sess, TreeConnection tree)
    {
        operationLog.info("DataSetCifsView.treeClosed()");
    }

    @Override
    public void closeFile(SrvSession sess, TreeConnection tree, NetworkFile param) throws IOException
    {
        System.out.println("DataSetCifsView.closeFile()");
    }

    @Override
    public void createDirectory(SrvSession sess, TreeConnection tree, FileOpenParams params) throws IOException
    {
        System.out.println("DataSetCifsView.createDirectory()");
        // TODO Auto-generated method stub

    }

    @Override
    public NetworkFile createFile(SrvSession sess, TreeConnection tree, FileOpenParams params) throws IOException
    {
        System.out.println("DataSetCifsView.createFile()");
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteDirectory(SrvSession sess, TreeConnection tree, String dir) throws IOException
    {
        System.out.println("DataSetCifsView.deleteDirectory()");
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteFile(SrvSession sess, TreeConnection tree, String name) throws IOException
    {
        System.out.println("DataSetCifsView.deleteFile()");
        // TODO Auto-generated method stub

    }

    @Override
    public int fileExists(SrvSession sess, TreeConnection tree, String name)
    {
        FtpFile file;
        try
        {
            file = createView(sess).getFile(normalizePath(name));
            if (file == null)
            {
                return FileStatus.NotExist;
            } else if (file.isDirectory())
            {
                return FileStatus.DirectoryExists;
            }
            return FileStatus.FileExists;
        } catch (FtpException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void flushFile(SrvSession sess, TreeConnection tree, NetworkFile file) throws IOException
    {
        System.out.println("DataSetCifsView.flushFile()");
        // TODO Auto-generated method stub

    }

    @Override
    public NetworkFile openFile(SrvSession sess, TreeConnection tree, FileOpenParams params) throws IOException
    {
        final String fullPath = normalizePath(params.getFullPath());
        String path = normalizePath(params.getPath());
        System.out.println("DataSetCifsView.openFile() "+params+" path:"+path+" fullPath:"+fullPath);
        NetworkFile networkFile = new NetworkFile(path)
            {
                
                @Override
                public void writeFile(byte[] buf, int len, int pos, long fileOff) throws IOException
                {
                    System.out.println("DataSetCifsView..openFile("+fullPath+").new NetworkFile() {...}.writeFile()");
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void truncateFile(long siz) throws IOException
                {
                    System.out.println("DataSetCifsView..openFile("+fullPath+").new NetworkFile() {...}.truncateFile()");
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public long seekFile(long pos, int typ) throws IOException
                {
                    System.out.println("DataSetCifsView..openFile("+fullPath+").new NetworkFile() {...}.seekFile()");
                    // TODO Auto-generated method stub
                    return 0;
                }
                
                @Override
                public int readFile(byte[] buf, int len, int pos, long fileOff) throws IOException
                {
                    System.out.println("DataSetCifsView..openFile("+fullPath+").new NetworkFile() {...}.readFile()");
                    // TODO Auto-generated method stub
                    return 0;
                }
                
                @Override
                public void openFile(boolean createFlag) throws IOException
                {
                    System.out.println("DataSetCifsView..openFile("+fullPath+").new NetworkFile() {...}.openFile()");
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void flushFile() throws IOException
                {
                    System.out.println("DataSetCifsView..openFile("+fullPath+").new NetworkFile() {...}.flushFile()");
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void closeFile() throws IOException
                {
                    System.out.println("DataSetCifsView..openFile("+fullPath+").new NetworkFile() {...}.closeFile()");
                    // TODO Auto-generated method stub
                    
                }

                @Override
                public String getName()
                {
                    System.out.println("DataSetCifsView..openFile("+fullPath+").new NetworkFile() {...}.getName()");
                    return super.getName();
                }

                @Override
                public boolean hasModifyDate()
                {
                    System.out.println("DataSetCifsView..openFile("+fullPath+").new NetworkFile() {...}.hasModifyDate()");
                    return true;
                }

                @Override
                public void close() throws IOException
                {
                    System.out.println("DataSetCifsView..openFile("+fullPath+").new NetworkFile() {...}.close()");
                    // TODO Auto-generated method stub
                    super.close();
                }
                
            };
        networkFile.setAttributes(FileAttribute.Directory | FileAttribute.ReadOnly);
        return networkFile;
    }

    @Override
    public int readFile(SrvSession sess, TreeConnection tree, NetworkFile file, byte[] buf, int bufPos, int siz, long filePos) throws IOException
    {
        System.out.println("DataSetCifsView.readFile()");
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void renameFile(SrvSession sess, TreeConnection tree, String oldName, String newName) throws IOException
    {
        System.out.println("DataSetCifsView.renameFile()");
        // TODO Auto-generated method stub

    }

    @Override
    public long seekFile(SrvSession sess, TreeConnection tree, NetworkFile file, long pos, int typ) throws IOException
    {
        System.out.println("DataSetCifsView.seekFile()");
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setFileInformation(SrvSession sess, TreeConnection tree, String name, FileInfo info) throws IOException
    {
        System.out.println("DataSetCifsView.setFileInformation()");
        // TODO Auto-generated method stub

    }

    @Override
    public void truncateFile(SrvSession sess, TreeConnection tree, NetworkFile file, long siz) throws IOException
    {
        System.out.println("DataSetCifsView.truncateFile()");
        // TODO Auto-generated method stub

    }

    @Override
    public int writeFile(SrvSession sess, TreeConnection tree, NetworkFile file, byte[] buf, int bufoff, int siz, long fileoff) throws IOException
    {
        System.out.println("DataSetCifsView.writeFile()");
        // TODO Auto-generated method stub
        return 0;
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
