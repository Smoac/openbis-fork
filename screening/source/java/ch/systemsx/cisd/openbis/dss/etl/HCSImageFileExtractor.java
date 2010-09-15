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

    private final Geometry wellGeometry;

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
     * Extracts the well location from given <var>value</var>, following the convention adopted
     * here.<br>
     * Here is a numbering example for a 3x3 plate:<br>
     * 1 4 7<br>
     * 2 5 8<br>
     * 3 6 9<br>
     * <p>
     * Returns <code>null</code> if the operation fails.
     * </p>
     */
    @Override
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
                Location letterLoc =
                        Location.tryCreateLocationFromPosition(tileNumber, wellGeometry);
                if (letterLoc == null)
                {
                    return null;
                }
                // transpose rows with columns
                return new Location(letterLoc.getY(), letterLoc.getX());
            }
        } catch (final NumberFormatException ex)
        {
            // Nothing to do here. Rest of the code can handle this.
        }
        return null;
    }

    @Override
    protected final List<AcquiredPlateImage> getImages(String channelCode, Location plateLocation,
            Location wellLocation, Float timepointOrNull, String imageRelativePath)
    {
        List<AcquiredPlateImage> images = new ArrayList<AcquiredPlateImage>();
        checkChannelsAndColorComponents();

        if (channelColorComponentsOrNull != null)
        {
            for (int i = 0; i < channelColorComponentsOrNull.size(); i++)
            {
                ColorComponent colorComponent = channelColorComponentsOrNull.get(i);
                ChannelDescription channelDescription = channelDescriptions.get(i);
                images.add(createImage(plateLocation, wellLocation, imageRelativePath,
                        channelDescription.getCode(), timepointOrNull, colorComponent));
            }
        } else
        {
            ensureChannelExist(channelDescriptions, channelCode);
            images.add(createImage(plateLocation, wellLocation, imageRelativePath, channelCode,
                    timepointOrNull, null));
        }
        return images;
    }

    @Override
    protected List<Channel> getAllChannels()
    {
        return createChannels(channelDescriptions);
    }

    @Override
    protected ImageFileInfo tryExtractImageInfo(File imageFile, SampleIdentifier datasetSample)
    {
        return tryExtractDefaultImageInfo(imageFile, datasetSample, shouldValidatePlateName);
    }
}
