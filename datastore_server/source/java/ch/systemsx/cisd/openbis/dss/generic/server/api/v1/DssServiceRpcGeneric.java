/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.IDssServiceRpcGenericInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;

/**
 * Implementation of the generic RPC interface.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcGeneric extends AbstractDssServiceRpc implements
        IDssServiceRpcGenericInternal
{
    private final PutDataSetService putService;

    /**
     * The designated constructor.
     * 
     * @param openBISService
     */
    public DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService)
    {
        super(openBISService);
        putService = new PutDataSetService(openBISService, operationLog);
        operationLog.info("[rpc] Started DSS API V1 service.");
    }

    /**
     * A constructor for testing.
     * 
     * @param openBISService
     * @param service
     */
    public DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService,
            PutDataSetService service)
    {
        super(openBISService);
        putService = service;
        operationLog.info("[rpc] Started DSS API V1 service.");
    }

    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, String dataSetCode,
            String startPath, boolean isRecursive) throws IllegalArgumentException
    {
        File dataSetRootDirectory = checkAccessAndGetRootDirectory(sessionToken, dataSetCode);

        try
        {
            String dataSetRootPath = dataSetRootDirectory.getCanonicalPath();
            File requestedFile = new File(dataSetRootDirectory, startPath);
            // Make sure the requested file is under the root of the data set
            if (requestedFile.getCanonicalPath().startsWith(dataSetRootPath) == false)
            {
                throw new IllegalArgumentException("Path does not exist.");
            }

            String listingRoot =
                    (requestedFile.isDirectory()) ? requestedFile.getCanonicalPath()
                            : requestedFile.getParentFile().getCanonicalPath();
            ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
            appendFileInfosForFile(requestedFile, dataSetRootPath, listingRoot, list, isRecursive);
            FileInfoDssDTO[] fileInfos = new FileInfoDssDTO[list.size()];
            return list.toArray(fileInfos);

        } catch (IOException ex)
        {
            operationLog.info("listFiles: " + startPath + " caused an exception", ex);
            throw new IOExceptionUnchecked(ex);
        }
    }

    public InputStream getFileForDataSet(String sessionToken, String dataSetCode, String path)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        try
        {
            File requestedFile = checkAccessAndGetFile(sessionToken, dataSetCode, path);
            return new FileInputStream(requestedFile);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    public String putDataSet(String sessionToken, NewDataSetDTO newDataSet, InputStream inputStream)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return putService.putDataSet(sessionToken, newDataSet, inputStream);
    }

    public int getMajorVersion()
    {
        return 1;
    }

    public int getMinorVersion()
    {
        return 1;
    }

    /**
     * Append file info for the requested file or file hierarchy. Assumes that the parameters have
     * been verified already.
     * 
     * @param requestedFile A file known to be accessible by the user
     * @param dataSetRoot The root of the file hierarchy; used to determine the absolute path of the
     *            file
     * @param listingRoot The root of the list hierarchy; used to determine the relative path of the
     *            file
     * @param list The list the files infos are appended to
     * @param isRecursive If true, directories will be recursively appended to the list
     */
    private void appendFileInfosForFile(File requestedFile, String dataSetRoot, String listingRoot,
            ArrayList<FileInfoDssDTO> list, boolean isRecursive) throws IOException
    {
        FileInfoDssBuilder factory = new FileInfoDssBuilder(dataSetRoot, listingRoot);
        factory.appendFileInfosForFile(requestedFile, list, isRecursive);
    }

    public InputStream getFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return this.getFileForDataSet(sessionToken, fileOrFolder.getDataSetCode(),
                fileOrFolder.getPath());
    }

    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return this.listFilesForDataSet(sessionToken, fileOrFolder.getDataSetCode(),
                fileOrFolder.getPath(), fileOrFolder.isRecursive());
    }

    @Override
    public void setStoreDirectory(File aFile)
    {
        super.setStoreDirectory(aFile);
        putService.setStoreDirectory(aFile);
    }

    @Override
    public void setIncomingDirectory(File aFile)
    {
        putService.setIncomingDir(aFile);
    }

    public String getPathToDataSet(String sessionToken, String dataSetCode,
            String overrideStoreRootPathOrNull) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        File rootDir = checkAccessAndGetRootDirectory(sessionToken, dataSetCode);
        return convertPath(getStoreDirectory(), rootDir, overrideStoreRootPathOrNull);
    }

    public static String convertPath(File storeRoot, File dataSetRoot,
            String overrideStoreRootPathOrNull)
    {
        String dataStoreRootPath = storeRoot.getAbsolutePath();
        String dataSetPath = dataSetRoot.getAbsolutePath();

        // No override specified; give the user the path as we understand it.
        if (null == overrideStoreRootPathOrNull
                || false == dataSetPath.startsWith(dataStoreRootPath))
        {
            return dataSetPath;
        }

        // Make the path begin with the user's store root override
        File usersPath =
                new File(overrideStoreRootPathOrNull, dataSetPath.substring(dataStoreRootPath
                        .length()));
        return usersPath.getPath();
    }

}
