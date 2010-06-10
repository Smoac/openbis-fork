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

package ch.systemsx.cisd.cina.shared.metadata;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ImageMetadataExtractorTest extends AssertJUnit
{
    @Test
    public void testMetadataExtraction()
    {
        HashMap<String, String> parentMetadata = new HashMap<String, String>();
        File folder =
                new File(
                        "sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/Replica for MRC files/MRC for Thomas/test20090422_BacklashRef.mrc");
        ImageMetadataExtractor metadata = new ImageMetadataExtractor(parentMetadata, folder);
        metadata.prepare();
        Map<String, String> metadataMap = metadata.getMetadataMap();
        assertEquals(16, metadataMap.size());

        assertEquals("", metadataMap.get("annotation"));
        assertEquals("CM10", metadataMap.get("microscope"));
        assertEquals("thomas.braun@unibas.ch", metadataMap.get("operator"));
        assertEquals("test20090422_BacklashRef.mrc", metadataMap.get("filename"));

        assertEquals("1.7691349E-5", metadataMap.get("sizex"));
        assertEquals("1.7691349E-5", metadataMap.get("sizey"));
        assertEquals("0.0", metadataMap.get("sizez"));

        assertEquals("2048", metadataMap.get("dimensionx"));
        assertEquals("2048", metadataMap.get("dimensiony"));
        assertEquals("1", metadataMap.get("dimensionz"));

        assertEquals("false", metadataMap.get("stackflag"));
        assertEquals("false", metadataMap.get("colorflag"));
        assertEquals("16 bit SI", metadataMap.get("data-type"));
        assertEquals("-32417.0", metadataMap.get("min"));
        assertEquals("-29705.0", metadataMap.get("max"));
        assertEquals("2", metadataMap.get("rating"));
    }
}
