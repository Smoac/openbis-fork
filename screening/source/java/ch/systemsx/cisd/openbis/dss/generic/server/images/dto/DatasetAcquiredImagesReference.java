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

package ch.systemsx.cisd.openbis.dss.generic.server.images.dto;

import java.util.List;

/**
 * DTO which points to acquired images of one dataset in chosen channels.
 * 
 * @author Tomasz Pylak
 */
public class DatasetAcquiredImagesReference
{
    private final String datasetCode;

    private final ImageChannelStackReference channelStackReference;

    private final List<String> channelCodesOrNull;

    /** @param channelCodesOrNull null if channels should be merged */
    public DatasetAcquiredImagesReference(String datasetCode,
            ImageChannelStackReference channelStackReference, List<String> channelCodesOrNull)
    {
        assert datasetCode != null;
        assert channelStackReference != null;

        this.datasetCode = datasetCode;
        this.channelStackReference = channelStackReference;
        this.channelCodesOrNull = channelCodesOrNull;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public ImageChannelStackReference getChannelStackReference()
    {
        return channelStackReference;
    }

    public boolean isMergeAllChannels()
    {
        return channelCodesOrNull == null;
    }

    public List<String> getChannelCodes()
    {
        return channelCodesOrNull;
    }

    @Override
    public String toString()
    {
        String channelsDesc =
                channelCodesOrNull == null ? "all channels merged" : "channels="
                        + channelCodesOrNull;
        return "[datasetCode=" + datasetCode + ", channelStackReference=" + channelStackReference
                + ", " + channelsDesc + "]";
    }

}
