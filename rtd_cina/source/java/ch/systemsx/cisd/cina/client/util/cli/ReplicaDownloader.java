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
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.cina.shared.constants.BundleStructureConstants;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.FileInfoDssDownloader;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Utility class for downloading replicas.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ReplicaDownloader
{
    private final ICinaUtilities component;

    private final SampleIdentifier replicaIdentifier;

    private final SampleIdentifier gridIdentifier;

    private final File outputDir;

    ReplicaDownloader(ICinaUtilities component, String bundleIdentifier, String replicaIdentifier,
            File outputDir)
    {
        this.component = component;
        this.replicaIdentifier = SampleIdentifierFactory.parse(replicaIdentifier);
        this.gridIdentifier = SampleIdentifierFactory.parse(bundleIdentifier);
        this.outputDir = outputDir;
    }

    private static class DownloaderListener implements
            FileInfoDssDownloader.FileInfoDssDownloaderListener
    {
        private final File targetDir;

        DownloaderListener(File targetDir)
        {
            this.targetDir = targetDir;
        }

        public void willDownload(FileInfoDssDTO fileInfo)
        {
            System.out.println("downloading " + getPathForFileInfo(fileInfo));
        }

        public void willCreateDirectory(FileInfoDssDTO fileInfo)
        {
            System.out.println("mkdir " + getPathForFileInfo(fileInfo));
        }

        public void didFinish()
        {
            System.out.println("Finished.");
        }

        private String getPathForFileInfo(FileInfoDssDTO fileInfo)
        {
            return targetDir.getPath() + "/" + fileInfo.getPathInListing();
        }
    }

    /**
     * Retrieve the raw data and current metadata for the replica sample as well as the current
     * metadata for the parent grid of the replica.
     */
    protected void download()
    {
        // Get the replica and parent grid samples.
        Sample replicaSample = searchForSample(replicaIdentifier);
        downloadReplica(replicaSample);

        Sample gridSample = searchForSample(gridIdentifier);
        downloadGrid(gridSample);
    }

    private void downloadReplica(Sample replicaSample)
    {
        // Find all datasets connected to the replica sample
        List<DataSet> dataSets = component.listDataSets(Collections.singletonList(replicaSample));

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
                mostRecentMetadata = compareReturningMoreRecent(mostRecentMetadata, dataSet);
            }
        }

        // Download the most recent metadata data set
        if (null != mostRecentMetadata)
        {
            downloadDataSet(mostRecentMetadata, BundleStructureConstants.METADATA_FOLDER_NAME);
        }
    }

    private void downloadGrid(Sample parentGridSample)
    {
        // Find all datasets connected to the grid sample
        List<DataSet> dataSets =
                component.listDataSets(Collections.singletonList(parentGridSample));

        DataSet mostRecentMetadata = null;

        for (DataSet dataSet : dataSets)
        {
            String typeCode = dataSet.getDataSetTypeCode();
            if (typeCode.equals(CinaConstants.METADATA_DATA_SET_TYPE_CODE))
            {
                mostRecentMetadata = compareReturningMoreRecent(mostRecentMetadata, dataSet);
            }
        }

        // Download the most recent metadata data set
        if (null != mostRecentMetadata)
        {
            downloadDataSet(mostRecentMetadata, null);
        }
    }

    private DataSet compareReturningMoreRecent(DataSet mostRecentReplicaMetadata, DataSet dataSet)
    {
        if (null == mostRecentReplicaMetadata)
        {
            return dataSet;
        } else if (mostRecentReplicaMetadata.getRegistrationDate().compareTo(
                dataSet.getRegistrationDate()) < 0)
        {
            // This element is newer than the current value
            return dataSet;
        }
        return mostRecentReplicaMetadata;
    }

    private Sample searchForSample(SampleIdentifier identifier)
    {
        // Get the sample
        // Find the sample that matches the given code (there should only be 1)
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                identifier.getSampleCode()));
        List<Sample> samples = component.searchForSamples(searchCriteria);
        if (samples.size() < 1)
        {
            throw new UserFailureException("No sample with specified code.");
        }

        // There should only be 1
        if (samples.size() > 1)
        {
            throw new EnvironmentFailureException(
                    "Found multiple matching samples -- this should not happen. Please contact administrator to resolve this problem.");
        }
        return samples.get(0);
    }

    private void downloadDataSet(DataSet dataSet, String subfolderNameOrNull)
    {
        IDataSetDss dataSetDss = component.getDataSet(dataSet.getCode());
        FileInfoDssDTO[] fileInfos = dataSetDss.listFiles("/original/", true);

        File targetDir =
                (subfolderNameOrNull != null) ? new File(outputDir, subfolderNameOrNull)
                        : outputDir;
        FileInfoDssDownloader downloader =
                new FileInfoDssDownloader(dataSetDss, fileInfos, targetDir, new DownloaderListener(
                        targetDir));
        downloader.downloadFiles();
    }
}
