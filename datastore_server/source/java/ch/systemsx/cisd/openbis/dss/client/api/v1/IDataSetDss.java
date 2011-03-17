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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import java.io.File;
import java.io.InputStream;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

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

    /**
     * Returns a {@link File}, if possible, that directly references the contents of a data set in
     * the data store server. This is only possible if the file system used by the DSS is also
     * mounted locally.
     * 
     * @param overrideStoreRootPathOrNull A path, in the context of the local file system mounts, to
     *            the DSS' store root. If null, paths are returned in the context of the DSS' file
     *            system mounts.
     * @return Returns null if the operation is not possible, a File that references the contents of
     *         the data set otherwise.
     * @since 1.1
     */
    public File tryLinkToContents(String overrideStoreRootPathOrNull)
            throws IllegalArgumentException, InvalidSessionException;

    /**
     * Returns a {@link File}, if possible, that directly references the contents of a data set in
     * the data store server. If not possible, downloads the data set contents and returns a File in
     * the downloadDir containing the contents of the data set.
     * 
     * @param overrideStoreRootPathOrNull A path, in the context of the local file system mounts, to
     *            the DSS' store root. If null, paths are returned in the context of the DSS' file
     *            system mounts.
     * @param downloadDir The directory in which to place the contents of the data set if they must
     *            be downloaded.
     * @return A File containing the contents of the data set.
     * @since 1.1
     */
    public File getLinkOrCopyOfContents(String overrideStoreRootPathOrNull, File downloadDir)
            throws IllegalArgumentException, InvalidSessionException;

}
