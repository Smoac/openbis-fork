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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * Channels of the basic images and overlay images.
 * 
 * @author Tomasz Pylak
 */
public class LogicalImageChannelsReference
{
    public static LogicalImageChannelsReference createWithoutOverlays(
            LogicalImageReference basicImage, String channel)
    {
        return new LogicalImageChannelsReference(basicImage, channel,
                new HashSet<ImageDatasetChannel>());
    }

    // ----
    
    private final LogicalImageReference basicImage;

    private final String channel;

    private final Set<ImageDatasetChannel> overlayChannels;

    public LogicalImageChannelsReference(LogicalImageReference basicImage, String channel,
            Set<ImageDatasetChannel> overlayChannels)
    {
        this.basicImage = basicImage;
        this.channel = channel;
        this.overlayChannels = overlayChannels;
    }

    public LogicalImageReference getBasicImage()
    {
        return basicImage;
    }

    public String getBasicImageChannelCode()
    {
        return channel;
    }

    public Set<ImageDatasetChannel> getOverlayChannels()
    {
        return overlayChannels;
    }
}
