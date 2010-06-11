/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.client;

import java.io.File;

/**
 * Interface for downloads in CIFEX.
 * 
 * @author Bernd Rinn
 */
public interface ICIFEXDownloader extends ICIFEXOperation
{

    /**
     * Downloads the file identified by <var>fileID</var> to the local <var>file</var>. If a file
     * with the given destination name already exists, CIFEX will assume that this is the same file
     * and will resume the download.
     * 
     * @param fileID The id of the file in CIFEX.
     * @param directoryToDownloadOrNull The directory to download the file to, or <code>null</code>,
     *            if the file should be downloaded to the current working directory.
     * @param fileNameOrNull The file name to save the file to, or <code>null</code>, if the name
     *            stored in CIFEX should be used.
     * @return The (local) file downloaded.
     */
    public File download(long fileID, File directoryToDownloadOrNull, String fileNameOrNull);

}