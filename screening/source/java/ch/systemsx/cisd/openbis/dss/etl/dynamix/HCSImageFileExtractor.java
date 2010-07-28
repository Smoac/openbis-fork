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

package ch.systemsx.cisd.openbis.dss.etl.dynamix;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.openbis.dss.etl.AbstractHCSImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.etl.AcquiredPlateImage;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult.Channel;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Image extractor for DynamiX project - work in progress.
 * 
 * @author Tomasz Pylak
 */
public class HCSImageFileExtractor extends AbstractHCSImageFileExtractor
{
    private final List<String> channelNames;

    public HCSImageFileExtractor(final Properties properties)
    {
        super(properties);
        this.channelNames = extractChannelNames(properties);
    }

    @Override
    protected final Set<Channel> getAllChannels()
    {
        return createChannels(channelNames);
    }

    @Override
    protected final List<AcquiredPlateImage> getImages(String channelToken, Location plateLocation,
            Location wellLocation, Float timepointOrNull, String imageRelativePath)
    {
        String channelName = channelToken.toUpperCase();
        ensureChannelExist(channelNames, channelName);

        List<AcquiredPlateImage> images = new ArrayList<AcquiredPlateImage>();
        images.add(createImage(plateLocation, wellLocation, imageRelativePath, channelName,
                timepointOrNull, null));
        return images;
    }

    @Override
    protected final Location tryGetWellLocation(String wellLocation)
    {
        return new Location(1, 1);
    }

    @Override
    /*
     * Note: the right mapping for DynamiX project should be found, this one is just to fit all
     * images on the 24x48 plate. Odd columns contain right position, even contain left position.
     * @param plateLocation - format left_pos100
     */
    protected final Location tryGetPlateLocation(final String plateLocation)
    {
        final String[] tokens = StringUtils.split(plateLocation, "_");
        boolean isLeft = (tokens[0].equalsIgnoreCase("left"));
        Integer pos = Integer.parseInt(tokens[1].substring(3));
        assert pos > 0 && pos <= 576 : "wrong position: " + pos;

        int sideShift = isLeft ? 1 : 0;
        int singleSidedMaxColumn = 24;
        int row = ((pos - 1) / singleSidedMaxColumn);
        int col = ((pos - 1) % singleSidedMaxColumn) * 2 + sideShift;

        return new Location(col + 1, row + 1);
    }

    @Override
    protected final Float tryGetTimepoint(final String timepointToken)
    {
        return new Float(timepointToken);
    }

    @Override
    protected final ImageFileInfo tryExtractImageInfo(File imageFile, SampleIdentifier datasetSample)
    {
        final String baseName = FilenameUtils.getBaseName(imageFile.getPath());
        final String[] tokens = StringUtils.split(baseName, "_");
        if (tokens == null || tokens.length != 5)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Not enough underscore-separated tokens in %s, exactly 5 expected",
                        imageFile));
            }
            return null;
        }
        // "left_dia_pos100_t20100227_152439.tif"
        ImageFileInfo info = new ImageFileInfo();
        // left_pos100
        info.setPlateLocationToken(tokens[0] + "_" + tokens[2]);
        info.setWellLocationToken(null);
        info.setChannelToken(tokens[1]);

        File[] images = imageFile.getParentFile().listFiles();
        Arrays.sort(images);
        info.setTimepointToken("" + Arrays.asList(images).indexOf(imageFile));
        return info;
    }
}
