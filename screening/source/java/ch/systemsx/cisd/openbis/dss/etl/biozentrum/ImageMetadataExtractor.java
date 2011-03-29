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

package ch.systemsx.cisd.openbis.dss.etl.biozentrum;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IMetaDataAwareImageReader;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Location;

/**
 * @author Kaloyan Enimanev
 */
public class ImageMetadataExtractor
{

    static final String POSITION_X_PROP = "stage-position-x";

    static final String POSITION_Y_PROP = "stage-position-y";

    public static Map<String, Object> extractMetadata(File imageFile)
    {
        IImageReader imageReader =
                ImageReaderFactory.tryGetImageReaderForFile(
                        ImageReaderConstants.BIOFORMATS_LIBRARY,
                        imageFile.getAbsolutePath());

        IMetaDataAwareImageReader metaDataReader = (IMetaDataAwareImageReader) imageReader;
        return metaDataReader.readMetaData(imageFile, null);
    }

    /**
     * @param tileToMetadataMap mapping from tile number to image metadata
     */
    public static Map<Integer/* tile number */, Location> tryGetTileMapping(
            Map<Integer/* tile number */, Map<String/* name */, Object/* value */>> tileToMetadataMap)
    {
        Set<Number> xCoords = new TreeSet<Number>();
        Set<Number> yCoords = new TreeSet<Number>();
        for (Map<String, Object> metadata : tileToMetadataMap.values())
        {
            xCoords.add(extractXCoord(metadata));
            yCoords.add(extractYCoord(metadata));
        }

        List<Number> sortedXCoords = new ArrayList<Number>(xCoords);
        List<Number> sortedYCoords = new ArrayList<Number>(yCoords);

        Map<Integer, Location> result = new HashMap<Integer, Location>();
        for (Entry<Integer, Map<String, Object>> entry : tileToMetadataMap.entrySet())
        {
            Integer tileNumber = entry.getKey();
            Location location = extractLocation(entry.getValue(), sortedXCoords, sortedYCoords);
            result.put(tileNumber, location);
        }
        return result;
    }

    private static Location extractLocation(Map<String, Object> metadata, List<Number> xCoords,
            List<Number> yCoords)
    {
        Number x = extractXCoord(metadata);
        Number y = extractYCoord(metadata);

        int locationX = xCoords.indexOf(x);
        int locationY = yCoords.indexOf(y);

        Location location = new Location(locationY, locationX);
        return location;
    }

    private static Number extractXCoord(Map<String, Object> metadata)
    {
        return extractNumber(metadata, POSITION_X_PROP);
    }

    private static Number extractYCoord(Map<String, Object> metadata)
    {
        return extractNumber(metadata, POSITION_Y_PROP);
    }

    private static Number extractNumber(Map<String, Object> metadata, String propName)
    {
        String numberAsString = (String) metadata.get(propName);
        try
        {
            return Double.valueOf(numberAsString);
        } catch (NumberFormatException nfe)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot parse number for property %s with value %s", propName, numberAsString);
        }
    }

}
