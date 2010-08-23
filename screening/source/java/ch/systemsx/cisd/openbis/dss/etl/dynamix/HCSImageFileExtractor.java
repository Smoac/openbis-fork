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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.etl.AbstractHCSImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.etl.AcquiredPlateImage;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dynamix.WellLocationMappingUtils.DynamixWellPosition;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Image extractor for DynamiX project - work in progress.
 * 
 * @author Tomasz Pylak
 */
public class HCSImageFileExtractor extends AbstractHCSImageFileExtractor
{
    private static final String DYNAMIX_TOKEN_SEPARATOR = "_";

    private static final String POSITION_MAPPING_FILE_NAME = "pos2loc.tsv";

    private final List<ChannelDescription> channelDescriptions;

    private final Map<File/* mapping file */, Map<DynamixWellPosition, WellLocation>> wellLocationMapCache;

    public HCSImageFileExtractor(final Properties properties)
    {
        super(properties);
        this.channelDescriptions = tryExtractChannelDescriptions(properties);
        this.wellLocationMapCache = new HashMap<File, Map<DynamixWellPosition, WellLocation>>();
    }

    @Override
    protected final Set<Channel> getAllChannels()
    {
        return createChannels(channelDescriptions);
    }

    @Override
    protected final List<AcquiredPlateImage> getImages(String channelToken, Location plateLocation,
            Location wellLocation, Float timepointOrNull, String imageRelativePath)
    {
        String channelCode = channelToken.toUpperCase();
        ensureChannelExist(channelDescriptions, channelCode);

        List<AcquiredPlateImage> images = new ArrayList<AcquiredPlateImage>();
        images.add(createImage(plateLocation, wellLocation, imageRelativePath, channelCode,
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
     * @param plateLocation - format row_column
     */
    protected final Location tryGetPlateLocation(final String plateLocation)
    {
        final String[] tokens = StringUtils.split(plateLocation, DYNAMIX_TOKEN_SEPARATOR);
        Integer row = new Integer(tokens[0]);
        Integer column = new Integer(tokens[1]);
        return Location.tryCreateLocationFromRowAndColumn(row, column);
    }

    @Override
    protected final Float tryGetTimepoint(final String timepointToken)
    {
        return new Float(timepointToken);
    }

    @Override
    protected final ImageFileInfo tryExtractImageInfo(File imageFile, SampleIdentifier datasetSample)
    {
        String baseName = FilenameUtils.getBaseName(imageFile.getPath());
        String[] tokens = StringUtils.split(baseName, DYNAMIX_TOKEN_SEPARATOR);
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
        WellLocation wellLocation = getWellLocation(imageFile, tokens);

        // "left_dia_pos100_t20100227_152439.tif"
        ImageFileInfo info = new ImageFileInfo();
        // row_column - will be parsed later. It's unnecessary and should be refactored.
        info.setPlateLocationToken(wellLocation.getRow() + DYNAMIX_TOKEN_SEPARATOR
                + wellLocation.getColumn());
        info.setWellLocationToken(null);
        info.setChannelToken(tokens[1]);

        long timepoint = getSecondsFromFirstMeasurement(imageFile, tokens);
        info.setTimepointToken("" + timepoint);
        return info;
    }

    private static long getSecondsFromFirstMeasurement(File imageFile, String[] tokens)
    {
        File[] images = imageFile.getParentFile().listFiles();
        Arrays.sort(images);
        String firstMeasurementFilePath = images[0].getPath();

        String firstMeasurementFileBaseName = FilenameUtils.getBaseName(firstMeasurementFilePath);
        String[] firstMeasurementTokens =
                StringUtils.split(firstMeasurementFileBaseName, DYNAMIX_TOKEN_SEPARATOR);

        return getSecondsFromFirstMeasurement(tokens, firstMeasurementTokens);
    }

    @Private
    static long getSecondsFromFirstMeasurement(String[] tokens, String[] firstMeasurementTokens)
    {
        Date firstMeasurementDate = parseDate(firstMeasurementTokens);
        Date thisMeasurementDate = parseDate(tokens);
        return (thisMeasurementDate.getTime() - firstMeasurementDate.getTime()) / 1000;
    }

    private static Date parseDate(String[] tokens)
    {
        // t20100227_152439 -> 20100227152439
        String dateToken = tokens[3].substring(1) + tokens[4];
        try
        {
            return new SimpleDateFormat("yyyymmddhhmmss").parse(dateToken);
        } catch (ParseException ex)
        {
            throw new EnvironmentFailureException("Cannot parse the data in the file name: " + ex);
        }
    }

    private WellLocation getWellLocation(File imageFile, final String[] tokens)
    {
        Map<DynamixWellPosition, WellLocation> map = getWellLocationMapping(imageFile);
        String posToken = tokens[2].substring("pos".length());
        DynamixWellPosition wellPos =
                WellLocationMappingUtils.parseWellPosition(tokens[0], posToken);
        return map.get(wellPos);
    }

    private Map<DynamixWellPosition, WellLocation> getWellLocationMapping(File imageFile)
    {
        File mappingFile = getMappingFile(imageFile);
        Map<DynamixWellPosition, WellLocation> map = wellLocationMapCache.get(mappingFile);
        if (map == null)
        {
            map = WellLocationMappingUtils.parseWellLocationMap(mappingFile);
            wellLocationMapCache.put(mappingFile, map);
        }
        return map;
    }

    private static File getMappingFile(File imageFile)
    {
        File mappingDir = imageFile.getParentFile().getParentFile().getParentFile();
        return new File(mappingDir, POSITION_MAPPING_FILE_NAME);
    }
}
