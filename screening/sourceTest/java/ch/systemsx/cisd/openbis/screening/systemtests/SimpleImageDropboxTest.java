/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SimpleImageDropboxTest extends AbstractScreeningSystemTestCase
{

    @BeforeTest
    public void dropAnExampleDataSet() throws Exception
    {
        File exampleDataSet = createTestDataContents();
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported(FINISHED_POST_REGISTRATION_CONDITION);
    }

    private File createTestDataContents() throws IOException
    {
        File destination = new File(workingDirectory, "test-data");
        destination.mkdirs();
        FileUtils.copyDirectory(new File(getTestDataFolder(), "PLATE1"), destination);
        return destination;
    }

    private String getTestDataFolder()
    {
        return "../screening/resource/test-data/" + getClass().getSimpleName();
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 60;
    }
    
    @Test
    public void test() throws Exception
    {
        ExperimentIdentifier identifier = ExperimentIdentifierFactory.parse("/TEST/TEST-PROJECT/DEMO-EXP-HCS");
        Experiment experiment = commonServer.getExperimentInfo(sessionToken, identifier);
        ListSampleCriteria sampleCriteria = ListSampleCriteria.createForExperiment(TechId.create(experiment));
        List<Sample> samples = commonServer.listSamples(sessionToken, sampleCriteria);
        Sample plate = samples.get(0);
        assertEquals("/TEST/PLATE1", plate.getIdentifier());
        assertEquals(1, samples.size());
        List<AbstractExternalData> dataSets2 
                = commonServer.listRelatedDataSets(sessionToken, new DataSetRelatedEntities(samples), false);
        AbstractExternalData dataSet = dataSets2.get(0);
        assertEquals(1, dataSets2.size());
        ImageChecker imageChecker = new ImageChecker();
        imageChecker.check(new File(getTestDataFolder(), "1_1_Merged_Default.png"), 
                new ImageLoader(dataSet, sessionToken));
        imageChecker.check(new File(getTestDataFolder(), "1_1_DAPI_Default.png"), 
                new ImageLoader(dataSet, sessionToken).channel("DAPI"));
        imageChecker.check(new File(getTestDataFolder(), "1_3_DAPI_CY3_256x191.png"), 
                new ImageLoader(dataSet, sessionToken).tileColumn(3).channel("DAPI").channel("CY3").mode("thumbnail256x191"));
        imageChecker.assertNoFailures();
    }
    
}
