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

import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Location;

/**
 * @author Kaloyan Enimanev
 */
public class ImageMetadataExtractorTest extends AssertJUnit
{
    @Test
    public void testTileMapping() throws Exception
    {
        Map<Integer, Map<String, Object>> tileMetadata =
                new HashMap<Integer, Map<String, Object>>();

        tileMetadata.put(1, createTileMetadata(1, 1));
        tileMetadata.put(2, createTileMetadata(2, 2));
        tileMetadata.put(3, createTileMetadata(3, 3));
        tileMetadata.put(4, createTileMetadata(1, 2));
        tileMetadata.put(5, createTileMetadata(3, 2));

        Map<Integer, Location> locations = ImageMetadataExtractor.tryGetTileMapping(tileMetadata);

        assertLocation(0, 0, 1, locations);
        assertLocation(1, 1, 2, locations);
        assertLocation(2, 2, 3, locations);
        assertLocation(0, 1, 4, locations);
        assertLocation(2, 1, 5, locations);

    }

    private void assertLocation(int column, int row, int tileNumber, Map<Integer, Location> locations)
    {
        Location location = locations.get(tileNumber);
        assertEquals(column, location.getColumn());
        assertEquals(row, location.getRow());
    }

    private Map<String, Object> createTileMetadata(double x, double y)
    {
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(ImageMetadataExtractor.POSITION_X_PROP, String.valueOf(x));
        metadata.put(ImageMetadataExtractor.POSITION_Y_PROP, String.valueOf(y));
        return metadata;
    }

}
