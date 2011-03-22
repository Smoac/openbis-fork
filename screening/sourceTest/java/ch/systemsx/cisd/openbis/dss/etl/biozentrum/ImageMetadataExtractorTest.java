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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Location;

/**
 * @author Kaloyan Enimanev
 */
public class ImageMetadataExtractorTest extends AssertJUnit
{

    public final String FILE_NAME =
            "./sourceTest/java/ch/systemsx/cisd/openbis/dss/etl/biozentrum/demo.tif";

    @Test
    public void testMetaDataExtraction() throws Exception
    {
        File imageFile = new File(FILE_NAME);
        Map<String, Object> metaData = ImageMetadataExtractor.extractMetadata(imageFile);
        List<String> sortedKeys = new ArrayList<String>(metaData.keySet());
        Collections.sort(sortedKeys);

        StringBuilder output = new StringBuilder();
        for (String key : sortedKeys)
        {
            Object value = metaData.get(key);
            output.append(key + ": " + value);
            output.append("\n");
        }

        assertEquals(
                "<prop id=\"Description\" type=\"string\" value=\"Experiment base name:ME20101116METADATA03&amp;#13;&amp;#10;Experiment set:ME20101116METADATA03&amp;#13;&amp;#10;Plate Screen&amp;#13;&amp;#10;Exposure: 10 ms&amp;#13;&amp;#10;Binning: 1 x 1&amp;#13;&amp;#10;Region: 1392 x 1040, offset at (0, 0)&amp;#13;&amp;#10;Acquired from Photometrics&amp;#13;&amp;#10;Subtract: Off&amp;#13;&amp;#10;Shading: Off&amp;#13;&amp;#10;Digitizer: 20 MHz&amp;#13;&amp;#10;Gain: Gain 1 (1x)&amp;#13;&amp;#10;Camera Shutter: Always Open&amp;#13;&amp;#10;Clear Count: 2&amp;#13;&amp;#10;Clear Mode: CLEAR PRE SEQUENCE&amp;#13;&amp;#10;Frames to Average: 1&amp;#13;&amp;#10;Trigger Mode: Normal (TIMED)&amp;#13;&amp;#10;Temperature: -29.95&amp;#13;&amp;#10;\">\n"
                        + "<prop id=\"stage-label\" type=\"string\" value=\"F09: Site 1\">\n"
                        + "ApplicationName: MetaMorph\n"
                        + "ApplicationVersion: 3.1.0.79\n"
                        + "Binning: 1 x 1\n"
                        + "BitsPerSample: 16\n"
                        + "Camera Bit Depth: 12\n"
                        + "Camera Shutter: Always Open\n"
                        + "Clear Count: 2\n"
                        + "Clear Mode: CLEAR PRE SEQUENCE\n"
                        + "Compression: Uncompressed\n"
                        + "DateTime: 20101121 16:52:52.986\n"
                        + "Digitizer: 20 MHz\n"
                        + "Experiment base name: ME20101116METADATA03\n"
                        + "Experiment set: ME20101116METADATA03\n"
                        + "Exposure: 10 ms\n"
                        + "Frames to Average: 1\n"
                        + "Gain: Gain 1 (1x)\n"
                        + "ImageLength: 1040\n"
                        + "ImageWidth: 1392\n"
                        + "ImageXpress Micro Filter Cube: Texas Red\n"
                        + "ImageXpress Micro Objective: 10X S Fluor\n"
                        + "ImageXpress Micro Shutter: Closed\n"
                        + "Laser focus score: 36.4006\n"
                        + "MetaDataPhotometricInterpretation: Monochrome\n"
                        + "MetaDataVersion: 1\n"
                        + "NewSubfileType: 2\n"
                        + "NumberOfChannels: 1\n"
                        + "PhotometricInterpretation: BlackIsZero\n"
                        + "Region: 1392 x 1040, offset at (0, 0)\n"
                        + "SamplesPerPixel: 1\n"
                        + "Shading: Off\n"
                        + "Software: MetaSeries\n"
                        + "Subtract: Off\n"
                        + "Temperature: -29.95\n"
                        + "Trigger Mode: Normal (TIMED)\n"
                        + "X position for position #1: 73296.5\n"
                        + "Y position for position #1: 25554.2\n"
                        + "_IllumSetting_: Cy3\n"
                        + "_MagNA_: 0.5\n"
                        + "_MagRI_: 1\n"
                        + "_MagSetting_: 10X S Fluor\n"
                        + "acquisition-time-local: 20101121 16:52:52.986\n"
                        + "autoscale-max-percent: 0\n"
                        + "autoscale-min-percent: 0\n"
                        + "autoscale-state: off\n"
                        + "bits-per-pixel: 16\n"
                        + "camera-binning-x: 1\n"
                        + "camera-binning-y: 1\n"
                        + "camera-chip-offset-x: 0\n"
                        + "camera-chip-offset-y: 0\n"
                        + "gamma: 1\n"
                        + "gray-calibration-curve-fit-algorithm: 4\n"
                        + "gray-calibration-max: -1\n"
                        + "gray-calibration-min: -1\n"
                        + "gray-calibration-units: \n"
                        + "gray-calibration-values: \n"
                        + "image-name: Cy3\n"
                        + "look-up-table-name: Set By Wavelength\n"
                        + "look-up-table-type: by-wavelength\n"
                        + "modification-time-local: 20101121 16:52:53.50\n"
                        + "number-of-planes: 1\n"
                        + "photonegative-mode: off\n"
                        + "pixel-size-x: 1392\n"
                        + "pixel-size-y: 1040\n"
                        + "plane-guid: {70EAFB69-A568-464E-9EE2-4479767F96EF}\n"
                        + "plane-type: plane\n"
                        + "scale-max: 1058\n"
                        + "scale-min: 96\n"
                        + "spatial-calibration-state: on\n"
                        + "spatial-calibration-units: um\n"
                        + "spatial-calibration-x: 0.645\n"
                        + "spatial-calibration-y: 0.645\n"
                        + "stage-label: F09 : Site 1\n"
                        + "stage-position-x: 73296.5\n"
                        + "stage-position-y: 25554.2\n"
                        + "threshold-color: 4080ff\n"
                        + "threshold-high: 65535\n"
                        + "threshold-low: 0\n"
                        + "threshold-state: ThresholdOff\n"
                        + "timestamp 0: 2010-11-21T16:52:52\n"
                        + "wavelength: 624\n" + "z-position: 11145.7\n" + "zoom-percent: 50\n",
                output.toString());
    }

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
