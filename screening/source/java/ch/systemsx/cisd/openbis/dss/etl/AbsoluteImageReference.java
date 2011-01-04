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

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
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

    private final Size thumbnailSizeOrNull;

    private IImageTransformerFactory transformerFactory;

    private IImageTransformerFactory transformerFactoryForMergedChannels;

    // This is an artificial value which helps to keep coloring channels constant. Starts with 0.
    // Unique for a given experiment or dataset (if channels are per dataset).
    private int channelIndex;

    /**
     * @param content the content before choosing the color component and the page
     */
    public AbsoluteImageReference(IContent content, String uniqueId, Integer pageOrNull,
            ColorComponent colorComponentOrNull, Size thumbnailSizeOrNull, int channelIndex)
    {
        super(pageOrNull, colorComponentOrNull);
        this.content = content;
        this.uniqueId = uniqueId;
        this.thumbnailSizeOrNull = thumbnailSizeOrNull;
        this.channelIndex = channelIndex;
    }

    /**
     * Returns id of the content which uniquely identifies the source of it and distinguishes from
     * other sources. Example: for a file-system-based content the absolute path is the correct id.
     */
    public String getUniqueId()
    {
        return uniqueId;
    }

    public IContent getContent()
    {
        return content;
    }

    public Size tryGetSize()
    {
        return thumbnailSizeOrNull;
    }

    public final IImageTransformerFactory getTransformerFactory()
    {
        return transformerFactory;
    }

    public final IImageTransformerFactory getTransformerFactoryForMergedChannels()
    {
        return transformerFactoryForMergedChannels;
    }

    public int getChannelIndex()
    {
        return channelIndex;
    }

    public final void setTransformerFactoryForMergedChannels(
            IImageTransformerFactory transformerFactoryForMergedChannels)
    {
        this.transformerFactoryForMergedChannels = transformerFactoryForMergedChannels;
    }

    public final void setTransformerFactory(IImageTransformerFactory transformerFactory)
    {
        this.transformerFactory = transformerFactory;
    }

    public AbsoluteImageReference createWithoutColorComponent()
    {
        ColorComponent colorComponent = null;
        AbsoluteImageReference ref =
                new AbsoluteImageReference(content, uniqueId, tryGetPage(), colorComponent,
                        thumbnailSizeOrNull, channelIndex);
        ref.setTransformerFactory(transformerFactory);
        ref.setTransformerFactoryForMergedChannels(transformerFactoryForMergedChannels);
        return ref;

    }
}