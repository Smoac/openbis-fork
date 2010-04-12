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

package ch.systemsx.cisd.openbis.dss.component;

import java.io.InputStream;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.rpc.shared.FileInfoDssDTO;

/**
 * The representation of a Data Set managed by a DSS server. It is safe to use instances in multiple
 * threads.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSetDss
{
    /**
     * The code of this data set.
     */
    public String getCode();

    /**
     * List files contained in this data set.
     * 
     * @param startPath The path for the listing. "/" is the root of the hierarchy for this data
     *            set.
     * @param isRecursive If true, the contents of any subdirectories will be listed as well.
     */
    public FileInfoDssDTO[] listFiles(String startPath, boolean isRecursive)
            throws IllegalArgumentException, InvalidSessionException;

    /**
     * Get a file contained in this data set.
     * 
     * @param path The path of the file to retrieve. The path must be absolute with respect to the
     *            data set, such as the path returned by {@link FileInfoDssDTO#getPathInDataSet}.
     */
    public InputStream getFile(String path) throws IllegalArgumentException,
            InvalidSessionException;

}
