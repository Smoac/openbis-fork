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
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
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
    
    private IServiceForDataStoreServer openBisService;
    private IGeneralInformationService generalInfoService;
    
    public DataSetCifsView()
    {
    }

    DataSetCifsView(IServiceForDataStoreServer openBisService, IGeneralInformationService generalInfoService)
    {
        this.openBisService = openBisService;
        this.generalInfoService = generalInfoService;
    }
    
    @Override
    public DeviceContext createContext(String shareName, ConfigElement args) throws DeviceContextException
    {
        operationLog.info("create context for share " + shareName);
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
        String normalizedPath = FileName.buildPath(null, path, null, java.io.File.separatorChar);
        System.out.println("DataSetCifsView.getFileInformation("+path+") "+normalizedPath);
        System.out.println(sess.getClientInformation());
        if (normalizedPath.equals("/"))
        {
            System.out.println("ROOT");
        }
        return new FileInfo(normalizedPath, 0, 0);
    }

    @Override
    public SearchContext startSearch(SrvSession sess, TreeConnection tree, String searchPath, int attrib) throws FileNotFoundException
    {
        System.out.println("DataSetCifsView.startSearch() "+searchPath+" "+attrib);
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub

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
        System.out.println("DataSetCifsView.fileExists()");
        // TODO Auto-generated method stub
        return 0;
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
        System.out.println("DataSetCifsView.openFile()");
        // TODO Auto-generated method stub
        return null;
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

    private IServiceForDataStoreServer getServiceForDataStoreServer()
    {
        if (openBisService == null)
        {
            openBisService = ServiceProvider.getServiceForDSS();
        }
        return openBisService;
    }
    
    private IGeneralInformationService getGeneralInfoService()
    {
        if (generalInfoService == null)
        {
            generalInfoService = ServiceProvider.getGeneralInformationService();
        }
        return generalInfoService;
    }

}
