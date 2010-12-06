/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult.Channel;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabelUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Generic image extractor implementation. The images names should have an extension present in
 * {@link ImageFileExtractorUtils#IMAGE_EXTENSIONS} constant. Each image name should adhere to the
 * schema:<br>
 * 
 * <pre>
 * &lt;any-text&gt;_&lt;plate-code&gt;_&lt;well-code&gt;_&lt;tile-code&gt;_&lt;channel-name&gt;.&lt;allowed-image-extension&gt;
 * </pre>
 * 
 * If 'extract-single-image-channels' property is specified for storage processor then the channels
 * are extracted from the color components and the token &lt;channel-name&gt; from the image file
 * name is ignored.
 * 
 * @author Tomasz Pylak
 */
public class HCSImageFileExtractor extends AbstractHCSImageFileExtractor
{
    private static final String TILE_MAPPING = "tile_mapping";

    // boolean property, if true the names of the plate in file name and directory name have to
    // match.
    // True by default.
    private static final String CHECK_PLATE_NAME_FLAG_PROPERTY_NAME = "validate-plate-name";

    private final boolean shouldValidatePlateName;

    private final TileMapper tileMapperOrNull;

    private final List<ChannelDescription> channelDescriptions;

    private final List<ColorComponent> channelColorComponentsOrNull;

    protected final Geometry wellGeometry;

    public HCSImageFileExtractor(final Properties properties)
    {
        super(properties);
        this.channelDescriptions = tryExtractChannelDescriptions(properties);
        this.channelColorComponentsOrNull = tryGetChannelComponents(properties);
        checkChannelsAndColorComponents();
        this.wellGeometry = getWellGeometry(properties);
        this.tileMapperOrNull =
                TileMapper.tryCreate(properties.getProperty(TILE_MAPPING), wellGeometry);
        this.shouldValidatePlateName =
                PropertyUtils.getBoolean(properties, CHECK_PLATE_NAME_FLAG_PROPERTY_NAME, true);
    }

    private void checkChannelsAndColorComponents()
    {
        if (channelColorComponentsOrNull != null
                && channelColorComponentsOrNull.size() != channelDescriptions.size())
        {
            throw ConfigurationFailureException.fromTemplate(
                    "There should be exactly one color component for each channel name."
                            + " Correct the list of values for '%s' property.",
                    AbstractHCSImageFileExtractor.EXTRACT_SINGLE_IMAGE_CHANNELS_PROPERTY);
        }
    }

    /**
     * Extracts the well location from given token. Returns <code>null</code> if the operation
     * fails.<br>
     * Can be overwritten in the subclasses if they use
     * {@link #tryExtractImageInfo(UnparsedImageFileInfo)} internally.
     */
    protected Location tryGetWellLocation(final String wellLocation)
    {
        try
        {
            int tileNumber = Integer.parseInt(wellLocation);

            if (tileMapperOrNull != null)
            {
                return tileMapperOrNull.tryGetLocation(tileNumber);
            } else
            {
                return Location.tryCreateLocationFromRowwisePosition(tileNumber, wellGeometry);
            }
        } catch (final NumberFormatException ex)
        {
            // Nothing to do here. Rest of the code can handle this.
        }
        return null;
    }

    @Override
    protected List<AcquiredPlateImage> getImages(ImageFileInfo imageInfo)
    {
        checkChannelsAndColorComponents();

        if (channelColorComponentsOrNull != null)
        {
            List<AcquiredPlateImage> images = new ArrayList<AcquiredPlateImage>();
            for (int i = 0; i < channelColorComponentsOrNull.size(); i++)
            {
                ColorComponent colorComponent = channelColorComponentsOrNull.get(i);
                ChannelDescription channelDescription = channelDescriptions.get(i);
                imageInfo.setChannelCode(channelDescription.getCode());
                images.add(createImage(imageInfo, colorComponent));
            }
            return images;
        } else
        {
            ensureChannelExist(channelDescriptions, imageInfo.getChannelCode());
            return getDefaultImages(imageInfo);
        }

    }

    @Override
    protected List<Channel> getAllChannels()
    {
        return createChannels(channelDescriptions);
    }

    @Override
    /** Default implementation, can be overwritten in subclasses. */
    protected ImageFileInfo tryExtractImageInfo(File imageFile, File incomingDataSetDirectory,
            SampleIdentifier datasetSample)
    {
        UnparsedImageFileInfo unparsedInfo =
                tryExtractDefaultImageInfo(imageFile, incomingDataSetDirectory, datasetSample,
                        shouldValidatePlateName);
        if (unparsedInfo == null)
        {
            return null;
        }
        return tryExtractImageInfo(unparsedInfo);
    }

    protected final ImageFileInfo tryExtractImageInfo(UnparsedImageFileInfo unparsedInfo)
    {
        assert unparsedInfo != null;

        Location plateLocation = tryGetPlateLocation(unparsedInfo.getPlateLocationToken());
        if (plateLocation == null)
        {
            operationLog.info("Cannot extract plate location from token "
                    + unparsedInfo.getPlateLocationToken());
            return null;
        }
        Location wellLocation = tryGetWellLocation(unparsedInfo.getWellLocationToken());
        if (wellLocation == null)
        {
            operationLog.info("Cannot extract well location (a.k.a. tile/field/side) from token "
                    + unparsedInfo.getWellLocationToken());
            return null;
        }
        String channelCode = CodeAndLabelUtil.normalize(unparsedInfo.getChannelToken());

        Float timepointOrNull = tryAsFloat(unparsedInfo.getTimepointToken());
        Float depthOrNull = tryAsFloat(unparsedInfo.getDepthToken());

        return new ImageFileInfo(plateLocation, channelCode, wellLocation,
                unparsedInfo.getImageRelativePath(), timepointOrNull, depthOrNull);
    }

    private static Float tryAsFloat(String valueOrNull)
    {
        if (valueOrNull == null)
        {
            return null;
        }
        try
        {
            return Float.parseFloat(valueOrNull);
        } catch (NumberFormatException e)
        {
            return null;
        }
    }
}
