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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.InputStream;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.api.IRpcService;

/**
 * Generic functionality for interacting with the DSS.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDssServiceRpcGeneric extends IRpcService
{
    public String DSS_SERVICE_NAME = "DSS Service";

    /**
     * Get an array of FileInfoDss objects that describe the file-system structure of the data set.
     * 
     * @param sessionToken The session token
     * @param fileOrFolder The file or folder to get information on
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     */
    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, FileReferenceDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Get an array of FileInfoDss objects that describe the file-system structure of the data set.
     * 
     * @param sessionToken The session token
     * @param fileOrFolder The file or folder to retrieve
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     */
    public InputStream getFileForDataSet(String sessionToken, FileReferenceDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Get an array of FileInfoDss objects that describe the file-system structure of the data set.
     * 
     * @param sessionToken The session token
     * @param dataSetCode The data set to retrieve file information about
     * @param path The path within the data set to retrieve file information about
     * @param isRecursive Should the result include information for sub folders?
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     */
    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, String dataSetCode,
            String path, boolean isRecursive) throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Get an array of FileInfoDss objects that describe the file-system structure of the data set.
     * 
     * @param sessionToken The session token
     * @param dataSetCode The data set to retrieve file from
     * @param path The path within the data set to retrieve file information about
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     */
    public InputStream getFileForDataSet(String sessionToken, String dataSetCode, String path)
            throws IOExceptionUnchecked, IllegalArgumentException;

    /**
     * Upload a new data set to the DSS.
     * 
     * @param sessionToken The session token
     * @param newDataset The new data set that should be registered
     * @throws IOExceptionUnchecked Thrown if an IOException occurs when listing the files
     * @throws IllegalArgumentException Thrown if the dataSetCode or startPath are not valid
     */
    public void putDataSet(String sessionToken, NewDatasetDTO newDataset)
            throws IOExceptionUnchecked, IllegalArgumentException;
}
