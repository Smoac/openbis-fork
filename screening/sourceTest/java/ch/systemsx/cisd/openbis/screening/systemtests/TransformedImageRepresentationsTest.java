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

import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil.OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor;
import ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDataSetHandler;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups =
    { "slow", "systemtest" })
public class TransformedImageRepresentationsTest extends AbstractScreeningSystemTestCase
{
    private MockHttpServletRequest request;

    private String sessionToken;

    private IScreeningClientService screeningClientService;

    private IScreeningApiServer screeningServer;

    private IScreeningOpenbisServiceFacade screeningFacade;

    @Override
    protected void setUpTestThread()
    {
        setUpTestThread(JythonPlateDataSetHandler.class, PlateStorageProcessor.class,
                getTestDataFolder() + "data-set-handler.py");

        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + "dss-system-test-thread.storage-processor.processor", DefaultStorageProcessor.class.getName());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + "dss-system-test-thread.storage-processor.data-source", "imaging-db");
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
        screeningFacade = ScreeningOpenbisServiceFacade.tryCreateForTest(sessionToken, "http://localhost:" + SYSTEM_TEST_CASE_SERVER_PORT, screeningServer);
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
    public void testTransformedThumbnails() throws Exception
    {
        dropAnExampleDataSet();
        // The components of the plate identifier come from the dropbox code
        // (resource/test-data/TransformedImageRepresentationsTest/data-set-handler.py)
        PlateIdentifier plate = new PlateIdentifier("TRANSFORMED-THUMB-PLATE", "TEST", null);
        List<ImageDatasetReference> imageDataSets = screeningFacade.listRawImageDatasets(Arrays.asList(plate));
        List<DatasetImageRepresentationFormats> representationFormats = screeningFacade.listAvailableImageRepresentationFormats(imageDataSets);
        assertEquals(1, representationFormats.size());
        List<ImageRepresentationFormat> formats = representationFormats.get(0).getImageRepresentationFormats();

        HashSet<Dimension> expectedResolutions = new HashSet<Dimension>();
        expectedResolutions.addAll(Arrays.asList(new Dimension(64, 64), new Dimension(128, 128), new Dimension(256, 256), new Dimension(512, 512)));
        for (ImageRepresentationFormat format : formats)
        {
            Dimension resolution = new Dimension(format.getWidth(), format.getHeight());
            // Make sure the resolution we specified was found
            assertTrue("" + resolution + " was not expected", expectedResolutions.remove(resolution));
        }
        assertEquals(0, expectedResolutions.size());

        System.err.println(representationFormats);
    }

    private void dropAnExampleDataSet() throws IOException, Exception
    {
        File exampleDataSet = createTestDataContents();
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();
    }

    private File createTestDataContents() throws IOException
    {
        File dest = new File(workingDirectory, "test-data");
        dest.mkdirs();
        File src = new File(getTestDataFolder(), "TRANSFORMED-THUMB-PLATE");

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
