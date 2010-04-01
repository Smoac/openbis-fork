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

package ch.systemsx.cisd.openbis.dss.rpc.shared;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDssServiceRpcV1 extends IDssServiceRpc
{
    //
    // Protocol versioning
    //
    /** The version of this service interface. */
    public static final int VERSION = 1;

    /**
     * Get an array of FileInfoDss objects that describe the file-system structure of the data set.
     */
    public FileInfoDss[] listFilesForDataSet(String sessionToken, String dataSetCode,
            String startPath, boolean isRecursive) throws IllegalArgumentException;
}
