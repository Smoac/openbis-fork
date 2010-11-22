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
import java.io.InputStream;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.IDssServiceRpcGenericInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcGenericLogger extends AbstractServerLogger implements
        IDssServiceRpcGenericInternal
{
    DssServiceRpcGenericLogger(IInvocationLoggerContext context)
    {
        super(null, context);
    }

    public int getMajorVersion()
    {
        return 0;
    }

    public int getMinorVersion()
    {
        return 0;
    }

    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "list_files_for_data_set", "DATA_SET(%s)", fileOrFolder);
        return null;
    }

    public InputStream getFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "get_file_for_data_set", "DATA_SET(%s)", fileOrFolder);
        return null;
    }

    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, String dataSetCode,
            String path, boolean isRecursive) throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "list_files_for_data_set", "DATA_SET(%s) PATH(%s) RECURSIVE(%s)",
                dataSetCode, path, isRecursive);
        return null;
    }

    public InputStream getFileForDataSet(String sessionToken, String dataSetCode, String path)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logAccess(sessionToken, "get_file_for_data_set", "DATA_SET(%s) PATH(%s)", dataSetCode, path);
        return null;
    }

    public String putDataSet(String sessionToken, NewDataSetDTO newDataset, InputStream inputStream)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        logTracking(sessionToken, "put_data_set", "DATA_SET(%s)", newDataset);
        return null;
    }

    public String getPathToDataSet(String sessionToken, String dataSetCode,
            String overrideStoreRootPathOrNull) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        logAccess(sessionToken, "get_path_to_data_set", "DATA_SET(%s) STORE_ROOT_PATH(%s)",
                dataSetCode, overrideStoreRootPathOrNull);
        return null;
    }

    public boolean isDatasetAccessible(String sessionToken, String dataSetCode)
    {
        // server already logs
        return false;
    }

    public boolean isSpaceWriteable(String sessionToken, SpaceIdentifier spaceId)
    {
        // server already logs
        return false;
    }
    
    public void setStoreDirectory(File aFile)
    {
    }
    
    public void setIncomingDirectory(File aFile)
    {
    }

}
