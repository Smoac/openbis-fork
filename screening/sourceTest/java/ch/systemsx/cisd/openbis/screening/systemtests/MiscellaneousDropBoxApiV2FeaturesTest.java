/*
 * Copyright 2012 ETH Zuerich, CISD
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
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test(groups =
    { "slow", "systemtest" })
public class MiscellaneousDropBoxApiV2FeaturesTest extends AbstractScreeningSystemTestCase
{
    private MockHttpServletRequest request;
    private String sessionToken;

    private ICommonServer commonServer;
    private IScreeningClientService screeningClientService;
    private IScreeningServer screeningServer;

    @BeforeTest
    public void dropAnExampleDataSet() throws IOException, Exception
    {
        File exampleDataSet = createTestDataContents();
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();
    }

    @BeforeMethod
    public void setUp() throws Exception
    {
        commonServer =
                (ICommonServer) applicationContext
                        .getBean(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER);
        screeningClientService =
                (IScreeningClientService) applicationContext
                        .getBean(ResourceNames.SCREENING_PLUGIN_SERVICE);
        request = new MockHttpServletRequest();
        ((SpringRequestContextProvider) applicationContext.getBean("request-context-provider"))
                .setRequest(request);
        Object bean = applicationContext.getBean(ResourceNames.SCREENING_PLUGIN_SERVER);
        screeningServer = (IScreeningServer) bean;
        sessionToken = screeningClientService.tryToLogin("admin", "a").getSessionID();
    }
    
    @AfterMethod
    public void tearDown()
    {
        File[] files = getIncomingDirectory().listFiles();
        for (File file : files)
        {
            FileUtilities.deleteRecursively(file);
        }
    }
    
    @Test
    public void testSettingPlateGeometryByFigureGeometry()
    {
        Experiment experiment =
                commonServer.getExperimentInfo(sessionToken,
                        ExperimentIdentifierFactory.parse("/TEST/TEST-PROJECT/TEST-EXP-HCS"));
        List<Sample> samples =
                commonServer.listSamples(sessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(experiment.getId())));

        Sample sample = samples.get(0);
        assertEquals("/TEST/PLATE1", sample.getIdentifier());
        assertEquals("[$PLATE_GEOMETRY: 24_WELLS_4X6]", sample.getProperties().toString());
        assertEquals(1, samples.size());
    }
    
    @Test
    public void testImageResolutions()
    {
        Experiment experiment =
                commonServer.getExperimentInfo(sessionToken,
                        ExperimentIdentifierFactory.parse("/TEST/TEST-PROJECT/TEST-EXP-HCS"));
        List<Sample> samples =
                commonServer.listSamples(sessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(experiment.getId())));
        Sample sample = samples.get(0);
        
        PlateContent plateContent = screeningServer.getPlateContent(sessionToken, new TechId(sample.getId()));
        
        PlateMetadata plateMetadata = plateContent.getPlateMetadata();
        assertEquals(4, plateMetadata.getRowsNum());
        assertEquals(6, plateMetadata.getColsNum());
        assertEquals(0, plateMetadata.getWells().size());
        List<ImageDatasetEnrichedReference> imageDatasets = plateContent.getImageDatasets();
        DatasetImagesReference imageDataset = imageDatasets.get(0).getImageDataset();
        assertEquals("/TEST/TEST-PROJECT/TEST-EXP-HCS", imageDataset.getDatasetReference().getExperimentIdentifier());
        assertEquals(1, imageDatasets.size());
        List<ImageResolution> imageDatasetResolutions =
                screeningServer.getImageDatasetResolutions(sessionToken,
                        imageDataset.getDatasetCode(), imageDataset.getDatastoreCode());
        Collections.sort(imageDatasetResolutions);
        assertEquals("[256x191, 1392x1040]", imageDatasetResolutions.toString());
    }

    private File createTestDataContents() throws IOException
    {
        File dest = new File(workingDirectory, "TEST-PLATE1");
        dest.mkdirs();
        File src = new File(getTestDataFolder(), "PLATE1");

        // Copy the test data set to the location for processing
        FileUtils.copyDirectory(src, dest);
        return dest;
    }

    private String getTestDataFolder()
    {
        return "../screening/resource/test-data/" + getClass().getSimpleName() + "/";
    }
    
    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 60;
    }
}
