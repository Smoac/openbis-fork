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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.rpc.shared.FileInfoDss;
import ch.systemsx.cisd.openbis.dss.rpc.shared.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDssServiceRpcV1;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcV1 extends AbstractDssServiceRpc implements IDssServiceRpcV1
{
    public DssServiceRpcV1(IEncapsulatedOpenBISService openBISService)
    {
        super(openBISService);
        operationLog.info("Started RPC V1 service.");
    }

    public FileInfoDss[] listFilesForDataSet(String sessionToken, String dataSetCode,
            String startPath, boolean isRecursive)
    {
        FileInfoDss[] defaultReturnValue = new FileInfoDss[0];
        if (isDatasetAccessible(sessionToken, dataSetCode) == false)
            return defaultReturnValue;

        File dataSetRootDirectory = getRootDirectoryForDataSet(dataSetCode);
        if (dataSetRootDirectory.exists() == false)
        {
            return defaultReturnValue;
        }

        try
        {
            String dataSetRootPath = dataSetRootDirectory.getCanonicalPath();
            File requestedFile = new File(dataSetRootDirectory, startPath);
            // Make sure the requested file is under the root of the data set
            if (requestedFile.getCanonicalPath().startsWith(dataSetRootPath) == false)
            {
                return defaultReturnValue;
            }

            ArrayList<FileInfoDss> list = new ArrayList<FileInfoDss>();
            appendFileInfosForFile(requestedFile, dataSetRootPath, list, isRecursive);
            return (FileInfoDss[]) list.toArray();

        } catch (IOException ex)
        {
            operationLog.info("listFiles: " + startPath + " caused an exception", ex);
            return defaultReturnValue;
        }
    }

    public int getMinClientVersion()
    {
        return 1;
    }

    public int getVersion()
    {
        return 1;
    }

    /**
     * Append file info for the requested file or file hierarchy. Assumes that the parameters have
     * been verified already.
     * 
     * @param requestedFile A file known to be accessible by the user
     * @param hierarchyRoot The root of the file hierarchy; used to determine the path of the file
     * @param list The list the files infos are appended to
     * @param isRecursive If true, directories will be recursively appended to the list
     */
    private void appendFileInfosForFile(File requestedFile, String hierarchyRoot,
            ArrayList<FileInfoDss> list, boolean isRecursive)
    {
        FileInfoDssBuilder factory = new FileInfoDssBuilder(hierarchyRoot);
        factory.appendFileInfosForFile(requestedFile, list, isRecursive);
    }
}
