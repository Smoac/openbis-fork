/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.logicalimage;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LazyImageSeriesFrame.ImagesDownloadListener;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LogicalImageClickHandler;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LogicalImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

/**
 * @author pkupczyk
 */
public class LogicalImageSeriesGridInitializer
{

    private String sessionId;

    private List<ImageChannelStack> channelStackImages;

    private LogicalImageChannelsReference channelReferences;

    private LogicalImageSize imageSize;

    private LogicalImageClickHandler imageClickHandler;

    private ImagesDownloadListener imageDownloadListener;

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public List<ImageChannelStack> getChannelStackImages()
    {
        return channelStackImages;
    }

    public void setChannelStackImages(List<ImageChannelStack> channelStackImages)
    {
        this.channelStackImages = channelStackImages;
    }

    public LogicalImageChannelsReference getChannelReferences()
    {
        return channelReferences;
    }

    public void setChannelReferences(LogicalImageChannelsReference channelReferences)
    {
        this.channelReferences = channelReferences;
    }

    public LogicalImageSize getImageSize()
    {
        return imageSize;
    }

    public void setImageSize(LogicalImageSize imageSize)
    {
        this.imageSize = imageSize;
    }

    public LogicalImageClickHandler getImageClickHandler()
    {
        return imageClickHandler;
    }

    public void setImageClickHandler(LogicalImageClickHandler imageClickHandler)
    {
        this.imageClickHandler = imageClickHandler;
    }

    public ImagesDownloadListener getImageDownloadListener()
    {
        return imageDownloadListener;
    }

    public void setImageDownloadListener(ImagesDownloadListener imageDownloadListener)
    {
        this.imageDownloadListener = imageDownloadListener;
    }

}
