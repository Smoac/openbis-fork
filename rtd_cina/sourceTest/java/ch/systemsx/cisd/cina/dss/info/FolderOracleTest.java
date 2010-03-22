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

package ch.systemsx.cisd.cina.dss.info;

import java.io.File;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cina.dss.info.FolderOracle.FolderMetadata;
import ch.systemsx.cisd.cina.dss.info.FolderOracle.FolderType;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class FolderOracleTest extends AssertJUnit
{

    @Test
    public void testExperimentFolder()
    {
        FolderOracle folderOracle = new FolderOracle();
        File experimentFolder =
                new File("sourceTest/java/ch/systemsx/cisd/cina/dss/info/experiment-data-folder");
        FolderMetadata metadata = folderOracle.getFolderMetadataForFolder(experimentFolder);
        assertTrue(metadata.getType() == FolderType.EXPERIMENT);
        assertTrue("experiment.properties".equals(metadata.tryGetMarkerFile().getName()));
    }

    @Test
    public void testSampleFolder()
    {
        FolderOracle folderOracle = new FolderOracle();
        File sampleFolder =
                new File("sourceTest/java/ch/systemsx/cisd/cina/dss/info/sample-data-folder");
        FolderMetadata metadata = folderOracle.getFolderMetadataForFolder(sampleFolder);
        assertTrue(metadata.getType() == FolderType.SAMPLE);
        assertTrue("sample.properties".equals(metadata.tryGetMarkerFile().getName()));
    }

    @Test
    public void testDataSetFolder()
    {
        FolderOracle folderOracle = new FolderOracle();
        File datasetFolder =
                new File("sourceTest/java/ch/systemsx/cisd/cina/dss/info/dataset-data-folder");
        FolderMetadata metadata = folderOracle.getFolderMetadataForFolder(datasetFolder);
        assertTrue(metadata.getType() == FolderType.DATA_SET);
        assertTrue("dataset.properties".equals(metadata.tryGetMarkerFile().getName()));
    }

    @Test
    public void testAmbiguousFolder()
    {
        FolderOracle folderOracle = new FolderOracle();
        File ambiguousFolder =
                new File("sourceTest/java/ch/systemsx/cisd/cina/dss/info/ambiguous-data-folder");
        try
        {
            folderOracle.getFolderMetadataForFolder(ambiguousFolder);
            fail("The method getMetadataForFolder should throw an error on folder ambiguous-data-folder.");
        } catch (UserFailureException ex)
        {
            // This should happen
        }
    }
}
