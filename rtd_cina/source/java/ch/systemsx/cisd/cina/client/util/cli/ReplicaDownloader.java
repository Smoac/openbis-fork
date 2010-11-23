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

package ch.systemsx.cisd.cina.client.util.cli;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.cina.shared.constants.BundleStructureConstants;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.openbis.dss.client.api.v1.FileInfoDssDownloader;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;

/**
 * Utility class for downloading replicas.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ReplicaDownloader
{
    private final ICinaUtilities component;

    private final String replicaCode;

    private final File outputDir;

    ReplicaDownloader(ICinaUtilities component, String code, File outputDir)
    {
        this.component = component;
        this.replicaCode = code;
        this.outputDir = outputDir;
    }

    private static class DownloaderListener implements
            FileInfoDssDownloader.FileInfoDssDownloaderListener
    {
        public void willDownload(FileInfoDssDTO fileInfo)
        {
            System.out.println("downloading " + fileInfo.getPathInDataSet());
        }

        public void willCreateDirectory(FileInfoDssDTO fileInfo)
        {
            System.out.println("mkdir " + fileInfo.getPathInDataSet());
        }

        public void didFinish()
        {
            System.out.println("Finished.");
        }
    }

    protected void download()
    {
        // Find all datasets connected to this sample
        List<DataSet> dataSets = component.listDataSetsForSampleCode(replicaCode);

        DataSet mostRecentMetadata = null;

        for (DataSet dataSet : dataSets)
        {
            String typeCode = dataSet.getDataSetTypeCode();
            if (typeCode.equals(CinaConstants.RAW_IMAGES_DATA_SET_TYPE_CODE))
            {
                // Download the raw images
                downloadDataSet(dataSet, BundleStructureConstants.RAW_IMAGES_FOLDER_NAME);
            }

            if (typeCode.equals(CinaConstants.METADATA_DATA_SET_TYPE_CODE))
            {
                if (null == mostRecentMetadata)
                {
                    mostRecentMetadata = dataSet;
                } else if (mostRecentMetadata.getRegistrationDate().compareTo(
                        dataSet.getRegistrationDate()) < 0)
                {
                    // This element is newer than the current value
                    mostRecentMetadata = dataSet;
                }
            }
        }

        // Download the most recent metadata data set
        if (null != mostRecentMetadata)
        {
            downloadDataSet(mostRecentMetadata, BundleStructureConstants.METADATA_FOLDER_NAME);
        }
    }

    private void downloadDataSet(DataSet dataSet, String subfolderName)
    {
        IDataSetDss dataSetDss = component.getDataSet(dataSet.getCode());
        FileInfoDssDTO[] fileInfos = dataSetDss.listFiles("/original/", true);

        FileInfoDssDownloader downloader =
                new FileInfoDssDownloader(dataSetDss, fileInfos,
                        new File(outputDir, subfolderName), new DownloaderListener());
        downloader.downloadFiles();
    }
}
