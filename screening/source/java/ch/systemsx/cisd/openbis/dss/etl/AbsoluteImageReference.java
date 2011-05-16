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

package ch.systemsx.cisd.openbis.dss.etl;

import java.awt.image.BufferedImage;

import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageTransfomationFactories;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelColor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Reference to the image with an absolute path.
 * 
 * @author Tomasz Pylak
 */
// TODO 2010-12-23, Tomasz Pylak: rename to ImageContent
public class AbsoluteImageReference extends AbstractImageReference
{
    private final IContent content;

    private final String uniqueId;

    private final RequestedImageSize imageSize;

    private final ImageTransfomationFactories imageTransfomationFactories;

    private final ImageLibraryInfo imageLibraryOrNull;

    private BufferedImage image;

    private ImageChannelColor channelColor;

    /**
     * @param content is the original content before choosing the color component and the page
     */
    public AbsoluteImageReference(IContent content, String uniqueId, Integer pageOrNull,
            ColorComponent colorComponentOrNull, RequestedImageSize imageSize,
            ImageChannelColor channelColor,
            ImageTransfomationFactories imageTransfomationFactories,
            ImageLibraryInfo imageLibraryOrNull)
    {
        super(pageOrNull, colorComponentOrNull);
        assert imageSize != null : "image size is null";
        assert imageTransfomationFactories != null : "imageTransfomationFactories is null";

        this.content = content;
        this.uniqueId = uniqueId;
        this.imageSize = imageSize;
        this.channelColor = channelColor;
        this.imageTransfomationFactories = imageTransfomationFactories;
        this.imageLibraryOrNull = imageLibraryOrNull;
    }

    /**
     * Returns id of the content which uniquely identifies the source of it and distinguishes from
     * other sources. Example: for a file-system-based content the absolute path is the correct id.
     */
    public String getUniqueId()
    {
        return uniqueId;
    }

    /**
     * @return unchanged image content if the image does not have to be extracted from the original
     *         content. This method is provided to allow the fastest possible access to original
     *         images.
     */
    public IContent tryGetRawContent()
    {
        if (tryGetColorComponent() == null && tryGetPage() == null
                && getRequestedSize().isThumbnailRequired() == false)
        {
            return content;
        } else
        {
            return null;
        }
    }

    public BufferedImage getImage()
    {
        if (image == null)
        {
            image = loadImage(content, tryGetPage(), imageLibraryOrNull);
        }
        return image;
    }

    static BufferedImage loadImage(IContent content, Integer pageOrNull,
            ImageLibraryInfo imageLibraryOrNull)
    {
        String imageLibraryNameOrNull = null;
        String imageLibraryReaderNameOrNull = null;
        if (imageLibraryOrNull != null)
        {
            imageLibraryNameOrNull = imageLibraryOrNull.getName();
            imageLibraryReaderNameOrNull = imageLibraryOrNull.getReaderName();
        }
        return ImageUtil.loadImage(content, pageOrNull, imageLibraryNameOrNull,
                imageLibraryReaderNameOrNull, null);
    }

    public RequestedImageSize getRequestedSize()
    {
        return imageSize;
    }

    public ImageTransfomationFactories getImageTransfomationFactories()
    {
        return imageTransfomationFactories;
    }

    public ImageChannelColor getChannelColor()
    {
        return channelColor;
    }

    public AbsoluteImageReference createWithoutColorComponent()
    {
        ColorComponent colorComponent = null;
        return new AbsoluteImageReference(content, uniqueId, tryGetPage(), colorComponent,
                imageSize, channelColor, imageTransfomationFactories, imageLibraryOrNull);

    }
}