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

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;

/**
 * DTO which describes which image channels and which overlays should be used to generate an image.
 * 
 * @author Tomasz Pylak
 */
public class ImageGenerationDescription
{
    private final DatasetAcquiredImagesReference imageChannelsOrNull;

    private final List<DatasetAcquiredImagesReference> overlayChannels;

    private final String sessionId;

    // original size if null
    private final Size thumbnailSizeOrNull;

    public ImageGenerationDescription(DatasetAcquiredImagesReference imageChannelsOrNull,
            List<DatasetAcquiredImagesReference> overlayChannels, String sessionId,
            Size thumbnailSizeOrNull)
    {
        this.imageChannelsOrNull = imageChannelsOrNull;
        this.overlayChannels = overlayChannels;
        this.sessionId = sessionId;
        this.thumbnailSizeOrNull = thumbnailSizeOrNull;
    }

    public DatasetAcquiredImagesReference tryGetImageChannels()
    {
        return imageChannelsOrNull;
    }

    public List<DatasetAcquiredImagesReference> getOverlayChannels()
    {
        return overlayChannels;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public Size tryGetThumbnailSize()
    {
        return thumbnailSizeOrNull;
    }

    @Override
    public String toString()
    {
        return "ImageGenerationDescription [image channels="
                + imageChannelsOrNull
                + (overlayChannels == null || overlayChannels.size() == 0 ? ""
                        : ", overlay channels=" + overlayChannels) + ", "
                + (thumbnailSizeOrNull == null ? "original size" : "size=" + thumbnailSizeOrNull)
                + "]";
    }
}
