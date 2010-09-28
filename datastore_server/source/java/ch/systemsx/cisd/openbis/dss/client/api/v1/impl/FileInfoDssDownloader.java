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

package ch.systemsx.cisd.openbis.dss.client.api.v1.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * Utility class for downloading an array of file infos.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
final class FileInfoDssDownloader
{
    private final AuthenticatedState parent;

    private final DataSetDss dataSetDss;

    private final FileInfoDssDTO[] fileInfos;

    private final File outputDir;

    public FileInfoDssDownloader(AuthenticatedState parent, DataSetDss dataSetDss,
            FileInfoDssDTO[] fileInfos, File outputDir)
    {
        this.parent = parent;
        this.fileInfos = fileInfos;
        this.outputDir = outputDir;
        this.dataSetDss = dataSetDss;
    }

    public final void downloadFiles()
    {
        // Download file in this thread -- could spawn threads for d/l in a future iteration
        for (FileInfoDssDTO fileInfo : fileInfos)
        {
            if (fileInfo.isDirectory())
            {
                File dir = new File(outputDir, fileInfo.getPathInDataSet());
                dir.mkdirs();
            } else
            {
                File file = new File(outputDir, fileInfo.getPathInDataSet());
                // Make sure the parent exists
                file.getParentFile().mkdirs();

                downloadFile(fileInfo, file, dataSetDss);
            }
        }
    }

    private void downloadFile(FileInfoDssDTO fileInfo, File file, DataSetDss dataSet)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            InputStream is = parent.getFile(dataSet, fileInfo.getPathInDataSet());
            IOUtils.copyLarge(is, fos);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }
}
