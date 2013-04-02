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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups =
    { "slow", "systemtest" })
public class FeatureVectorsDropboxTest extends AbstractScreeningSystemTestCase
{
    private MockHttpServletRequest request;

    private String sessionToken;

    private IScreeningClientService screeningClientService;

    private IScreeningApiServer screeningServer;

    private IScreeningOpenbisServiceFacade screeningFacade;

    @BeforeTest
    public void dropAnExampleDataSet() throws IOException, Exception
    {
        File exampleDataSet = createTestDataContents();
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();
    }

    @Override
    protected boolean checkLogContentForFinishedDataSetRegistration(String logContent)
    {
        return checkOnFinishedPostRegistration(logContent);
    }

    @BeforeMethod
    public void setUp() throws Exception
    {
        screeningClientService =
                (IScreeningClientService) applicationContext
                        .getBean(ResourceNames.SCREENING_PLUGIN_SERVICE);
        request = new MockHttpServletRequest();
        ((SpringRequestContextProvider) applicationContext.getBean("request-context-provider"))
                .setRequest(request);
        Object bean = applicationContext.getBean(ResourceNames.SCREENING_PLUGIN_SERVER);
        screeningServer = (IScreeningApiServer) bean;
        sessionToken = screeningClientService.tryToLogin("admin", "a").getSessionID();
        screeningFacade =
                ScreeningOpenbisServiceFacade.tryCreateForTest(sessionToken,
                        TestInstanceHostUtils.getOpenBISUrl(), screeningServer);
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
    public void testFeatureVectors() throws Exception
    {
        List<Plate> plates = screeningFacade.listPlates();

        List<FeatureVectorDatasetReference> features =
                screeningFacade.listFeatureVectorDatasets(filterPlates(plates));

        // exactly one feature vector data set should be created in this test
        assertEquals(1, features.size());

        FeatureVectorDatasetReference feature = features.get(0);
        assertEquals("HCS_ANALYSIS_CONTAINER_WELL_FEATURES", feature.getDataSetType());

        List<FeatureInformation> availableFeatures =
                screeningFacade.listAvailableFeatures(features);

        List<String> availableFeatureCodes = new LinkedList<String>();
        for (FeatureInformation availableFeature : availableFeatures)
        {
            availableFeatureCodes.add(availableFeature.getCode());
        }
        assertEquals(
                "[ROW_NUMBER, COLUMN_NUMBER, TPU, STATE, Z_VERY_LONG_FEATURE_LONGER_THAN_80_CHARS_123456789012345678901234567890123456789012345678901234567890]",
                availableFeatureCodes.toString());

        List<FeatureVectorDataset> featureVectorDatasets =
                screeningFacade.loadFeatures(features, Collections.singletonList("TPU"));

        assertEquals(1, featureVectorDatasets.size());

        FeatureVectorDataset featureVectorDataset = featureVectorDatasets.get(0);

        int nonNanPositions = 0;

        for (FeatureVector v : featureVectorDataset.getFeatureVectors())
        {
            if (false == Double.isNaN(v.getValues()[0]))
            {
                nonNanPositions++;
            }
        }

        assertEquals(
                "There are not enough values in the feature. Maybe the feature was not loaded correctly",
                8 * 12, nonNanPositions);

        List<String> featureList = screeningFacade.getFeatureList(feature, "shortListOfFeatures");
        assertEquals("[feature1, feature2]", featureList.toString());

    }

    private List<Plate> filterPlates(List<Plate> plates)
    {
        List<Plate> filteredPlates = new ArrayList<Plate>();
        for (Plate plate : plates)
        {
            if (plate.getPlateCode().equals("PLATE-FEATURE-VECTOR-TEST"))
            {
                filteredPlates.add(plate);
            }
        }
        return filteredPlates;
    }

    private File createTestDataContents() throws IOException
    {
        File dest = new File(workingDirectory, "test-data");
        dest.mkdirs();

        File featureSrc = getFeatureVectorsTestData();
        FileUtils.copyFileToDirectory(featureSrc, dest);
        // Copy the test data set to the location for processing

        return dest;
    }

    private File getFeatureVectorsTestData()
    {
        return new File("../screening/resource/test-data/FeatureVectorsDropboxTest/",
                "features.csv");
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 60;
    }

}
