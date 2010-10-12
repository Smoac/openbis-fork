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
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ReplicaMetadataExtractorTest extends AssertJUnit
{
    @Test
    public void testMetadataExtraction()
    {
        File folder =
                new File(
                        "sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/ReplicTest/");
        ReplicaMetadataExtractor metadata = new ReplicaMetadataExtractor(folder);
        metadata.prepare();

        Map<String, String> metadataMap = metadata.getMetadataMap();
        assertEquals(7, metadataMap.size());

        assertEquals("thomas.braun@bsse.ethz.ch", metadataMap.get("creator name (e-mail)"));
        assertEquals("This replica is a test for imported MRC files",
                metadataMap.get("description"));
        assertEquals("602516637", metadataMap.get("id nummer"));
        assertEquals("REPLICA-CODE", metadata.tryReplicaSampleCode());

        List<ImageMetadataExtractor> extractors = metadata.getImageMetadataExtractors();
        assertEquals(1, extractors.size());
    }
}
