/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.io.TransmissionSpeedCalculator;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetUploadClientModel
{
    private final IDssComponent dssComponent;

    private final ITimeProvider timeProvider;

    private final ArrayList<NewDataSetInfo> newDataSetInfos = new ArrayList<NewDataSetInfo>();

    public DataSetUploadClientModel(IDssComponent dssComponent, ITimeProvider timeProvider)
    {
        this.dssComponent = dssComponent;
        this.timeProvider = timeProvider;
    }

    /**
     * NewDataSetInfo is a mixture of NewDataSetDTO, which encapsulates information about new data
     * sets, and upload progress state.
     * <p>
     * Internally, NewDataSetInfo functions as a state machine with the following state transitions:
     * 
     * <pre>
     *                                              /-> FAILED
     * TO_UPLOAD -> QUEUED_FOR_UPLOAD -> UPLOADING <       ->  COMPLETED_UPLOAD
     *                                         /    \-> STALLED -\
     *                                         \-----------------/
     * </pre>
     * 
     * </p>
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class NewDataSetInfo
    {
        static enum Status
        {
            TO_UPLOAD, QUEUED_FOR_UPLOAD, UPLOADING, COMPLETED_UPLOAD, FAILED, STALLED
        }

        private final NewDataSetDTOBuilder newDataSetBuilder;

        private final TransmissionSpeedCalculator transmissionSpeedCalculator;

        private Status status;

        // Computed when the status is set to QUEUED_FOR_UPLOAD
        private long totalFileSize;

        // 0 until status is set to UPLOADING.
        private int percentageUploaded;

        private long numberOfBytesUploaded;

        private NewDataSetInfo(NewDataSetDTOBuilder newDataSetBuilder, ITimeProvider timeProvider)
        {
            this.newDataSetBuilder = newDataSetBuilder;
            transmissionSpeedCalculator = new TransmissionSpeedCalculator(timeProvider);
            percentageUploaded = 0;
            numberOfBytesUploaded = 0;
            setStatus(Status.TO_UPLOAD);
        }

        public NewDataSetDTOBuilder getNewDataSetBuilder()
        {
            return newDataSetBuilder;
        }

        public long getTotalFileSize()
        {
            return totalFileSize;
        }

        public int getPercentageDownloaded()
        {
            return percentageUploaded;
        }

        public long getNumberOfBytesDownloaded()
        {
            return numberOfBytesUploaded;
        }

        public long getEstimatedTimeOfArrival()
        {
            float remainingBytes = (totalFileSize - numberOfBytesUploaded);
            float bytesPerMillisecond =
                    transmissionSpeedCalculator.getEstimatedBytesPerMillisecond();
            if (bytesPerMillisecond < 0.001)
            {
                return -1;
            }
            return (long) (remainingBytes / bytesPerMillisecond);
        }

        void updateProgress(int percent, long numberOfBytes)
        {
            setStatus(Status.UPLOADING);
            int transmittedSinceLastUpdate = (int) (numberOfBytes - numberOfBytesUploaded);
            percentageUploaded = percent;
            numberOfBytesUploaded = numberOfBytes;
            transmissionSpeedCalculator
                    .noteTransmittedBytesSinceLastUpdate(transmittedSinceLastUpdate);
        }

        public void setStatus(Status status)
        {
            this.status = status;
            if (Status.QUEUED_FOR_UPLOAD == status)
            {
                long size = 0;
                for (FileInfoDssDTO fileInfo : newDataSetBuilder.getFileInfos())
                {
                    if (fileInfo.getFileSize() > 0)
                    {
                        size += fileInfo.getFileSize();
                    }
                }
                // Initialize some variables
                totalFileSize = size;

                percentageUploaded = 0;
                numberOfBytesUploaded = 0;
            }
        }

        public Status getStatus()
        {
            return status;
        }
    }

    /**
     * A list of data set info object managed by this model.
     */
    public List<NewDataSetInfo> getNewDataSetInfos()
    {
        return Collections.unmodifiableList(newDataSetInfos);
    }

    /**
     * Add a new data set info to the list of data set info objectss and return it.
     */
    public NewDataSetInfo addNewDataSetInfo()
    {
        NewDataSetDTOBuilder newDataSetBuilder = new NewDataSetDTOBuilder();
        NewDataSetInfo newDataSetInfo = new NewDataSetInfo(newDataSetBuilder, timeProvider);
        newDataSetInfos.add(newDataSetInfo);
        return newDataSetInfo;
    }

    /**
     * Remove a data set info.
     */
    public void removeNewDataSetInfo(NewDataSetInfo dataSetInfoToRemove)
    {
        newDataSetInfos.remove(dataSetInfoToRemove);
    }

    public IDssComponent getDssComponent()
    {
        return dssComponent;
    }

    /**
     * Get the data set types that are shown here.
     */
    public List<String> getDataSetTypes()
    {
        String[] dataSetTypes =
            { "Data Set Type 1", "Data Set Type 2", "Data Set Type 3" };
        return Arrays.asList(dataSetTypes);
    }

    public int getIndexOfDataSetType(String dataSetType)
    {
        if (null == dataSetType)
        {
            return 0;
        }
        int index = getDataSetTypes().indexOf(dataSetType);
        if (index < 0)
        {
            return 0;
        }
        return index;
    }

}
