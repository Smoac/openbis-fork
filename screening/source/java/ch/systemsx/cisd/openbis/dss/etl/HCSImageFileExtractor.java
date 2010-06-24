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
import java.util.Set;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.openbis.dss.etl.AbstractHCSImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.etl.AcquiredPlateImage;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult.Channel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Generic image extractor implementation. The images names should have an extension present in
 * {@link #IMAGE_EXTENSIONS} constant. Each image name should adhere to the schema:<br>
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
    public static final String[] IMAGE_EXTENSIONS = new String[]
        { "tif", "tiff", "jpg", "jpeg", "gif", "png" };

    private final List<String> channelNames;

    private final List<ColorComponent> channelColorComponentsOrNull;

    private final Geometry wellGeometry;

    public HCSImageFileExtractor(final Properties properties)
    {
        super(properties);
        this.channelNames = extractChannelNames(properties);
        this.channelColorComponentsOrNull = tryGetChannelComponents(properties);
        checkChannelsAndColorComponents();
        this.wellGeometry = getWellGeometry(properties);
    }

    private void checkChannelsAndColorComponents()
    {
        if (channelColorComponentsOrNull != null
                && channelColorComponentsOrNull.size() != channelNames.size())
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
    protected final Location tryGetWellLocation(final String wellLocation)
    {
        try
        {
            int tileNumber = Integer.parseInt(wellLocation);
            Location letterLoc = Location.tryCreateLocationFromPosition(tileNumber, wellGeometry);
            // transpose rows with columns
            return new Location(letterLoc.getY(), letterLoc.getX());
        } catch (final NumberFormatException ex)
        {
            // Nothing to do here. Rest of the code can handle this.
        }
        return null;
    }

    @Override
    protected final List<File> listImageFiles(final File directory)
    {
        return FileOperations.getInstance().listFiles(directory, IMAGE_EXTENSIONS, true);
    }

    @Override
    protected final List<AcquiredPlateImage> getImages(String channelStr, Location plateLocation,
            Location wellLocation, String imageRelativePath)
    {
        List<AcquiredPlateImage> images = new ArrayList<AcquiredPlateImage>();
        checkChannelsAndColorComponents();

        if (channelColorComponentsOrNull != null)
        {
            for (int i = 0; i < channelColorComponentsOrNull.size(); i++)
            {
                ColorComponent colorComponent = channelColorComponentsOrNull.get(i);
                String channelName = channelNames.get(i);
                images.add(createImage(plateLocation, wellLocation, imageRelativePath, channelName,
                        colorComponent));
            }
        } else
        {
            ensureChannelExist(channelStr);
            images
                    .add(createImage(plateLocation, wellLocation, imageRelativePath, channelStr,
                            null));
        }
        return images;
    }

    private void ensureChannelExist(String channelName)
    {
        if (channelNames.indexOf(channelName.toUpperCase()) == -1)
        {
            throw UserFailureException.fromTemplate(
                    "Channel '%s' is not one of: %s. Change the configuration.", channelName,
                    channelNames);
        }
    }

    @Override
    protected Set<Channel> getAllChannels()
    {
        return createChannels(channelNames);
    }
}
