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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.imagereaders.IImageReader;
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
        return imageReader.readMetaData(imageFile, null);
    }

    /**
     * NOTE : the speed of this algorithm can be immensely improved by
     * 
     * <pre>
     * 1) Avoiding auto-boxing (we have multiple collections storing primitive types) 
     * 2) Avoid parsing all numbers twice 
     * 3) Use a sorted collection (tree?) + binary search when looking if a value is already present.
     * </pre>
     * 
     * @param tileToMetadataMap mapping from tile number to image metadata
     */
    public static Map<Integer/* tile number */, Location> tryGetTileMapping(
            Map<Integer/* tile number */, Map<String/* name */, Object/* value */>> tileToMetadataMap,
            double epsilon)
    {
        List<Double> xCoords = new ArrayList<Double>();
        List<Double> yCoords = new ArrayList<Double>();

        for (Map<String, Object> metadata : tileToMetadataMap.values())
        {
            addIfNotPresent(xCoords, extractXCoord(metadata), epsilon);
            addIfNotPresent(yCoords, extractYCoord(metadata), epsilon);
        }

        Map<Integer, Location> result = new HashMap<Integer, Location>();
        for (Entry<Integer, Map<String, Object>> entry : tileToMetadataMap.entrySet())
        {
            Integer tileNumber = entry.getKey();
            Location location = extractLocation(entry.getValue(), xCoords, yCoords, epsilon);
            result.put(tileNumber, location);
        }
        return result;
    }

    private static void addIfNotPresent(List<Double> values, Double value, double epsilon)
    {
        int idx = findIdxByEpsilon(values, value, epsilon);
        if (idx < 0)
        {
            values.add(value);
        }
    }

    private static int findIdxByEpsilon(List<Double> values, double toFind, double epsilon)
    {
        for (int idx = 0; idx < values.size(); idx++)
        {
            if (Math.abs(values.get(idx) - toFind) < epsilon)
            {
                return idx;
            }
        }
        return -1;
    }

    private static Location extractLocation(Map<String, Object> metadata, List<Double> xCoords,
            List<Double> yCoords, double epsilon)
    {
        double x = extractXCoord(metadata);
        double y = extractYCoord(metadata);

        int locationX = findIdxByEpsilon(xCoords, x, epsilon);
        int locationY = findIdxByEpsilon(yCoords, y, epsilon);

        Location location = new Location(locationY, locationX);
        return location;
    }

    private static Double extractXCoord(Map<String, Object> metadata)
    {
        return extractNumber(metadata, POSITION_X_PROP);
    }

    private static Double extractYCoord(Map<String, Object> metadata)
    {
        return extractNumber(metadata, POSITION_Y_PROP);
    }

    private static Double extractNumber(Map<String, Object> metadata, String propName)
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
