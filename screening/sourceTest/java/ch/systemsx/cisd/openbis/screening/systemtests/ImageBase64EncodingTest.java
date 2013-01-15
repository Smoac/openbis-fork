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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.googlecode.jsonrpc4j.Base64;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

import ch.systemsx.cisd.common.collection.IModifiable;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

@Test(groups =
    { "slow", "systemtest" })
public class ImageBase64EncodingTest extends AbstractScreeningSystemTestCase
{
    private static String IMAGE1_FILENAME = getTestDataFolder()
            + "TRANSFORMED-THUMB-PLATE/bPLATE_wA1_s1_cRGB.png";

    private static String IMAGE2_FILENAME = getTestDataFolder()
            + "TRANSFORMED-THUMB-PLATE/bPLATE_wA2_s1_cRGB.png";

    private MockHttpServletRequest request;

    private String sessionToken;

    private IScreeningClientService screeningClientService;

    private IScreeningApiServer screeningServer;

    private IScreeningOpenbisServiceFacade screeningFacade;

    private IDssServiceRpcScreening screeningJsonApi;

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

        JsonRpcHttpClient client =
                new JsonRpcHttpClient(new URL(TestInstanceHostUtils.getDSSUrl()
                        + "/rmi-datastore-server-screening-api-v1.json/"));
        screeningJsonApi =
                ProxyUtil.createProxy(this.getClass().getClassLoader(),
                        IDssServiceRpcScreening.class, client);

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
    public void base64EncodedImagesContainTheOriginalData() throws Exception
    {
        byte[] originalImage1 = getBytes(IMAGE1_FILENAME);
        byte[] originalImage2 = getBytes(IMAGE2_FILENAME);

        List<Plate> p = screeningFacade.listPlates();
        List<Plate> plates = new ArrayList<Plate>();
        for (Plate plate : p)
        {
            if (plate.getAugmentedCode().equals("/TEST/BASE64PLATE"))
            {
                plates.add(plate);
                break;
            }
        }

        List<ImageDatasetReference> imageDatasets = screeningFacade.listRawImageDatasets(plates);

        System.out.println(plates);
        System.out.println(imageDatasets);

        List<PlateImageReference> plateImages = new ArrayList<PlateImageReference>();

        plateImages.add(new PlateImageReference(0, "MERGED CHANNELS", new WellPosition(1, 1),
                imageDatasets.get(0)));
        plateImages.add(new PlateImageReference(0, "MERGED CHANNELS", new WellPosition(1, 2),
                imageDatasets.get(0)));

        List<String> results =
                screeningJsonApi.loadImagesBase64(sessionToken, new PlateImageReferenceList(
                        plateImages), false);

        byte[] image1 = Base64.decode(results.get(0));
        byte[] image2 = Base64.decode(results.get(1));

        assertThat(image1, is(equalTo(originalImage1)));
        assertThat(image2, is(equalTo(originalImage2)));
        assertThat(results.size(), is(2));
    }

    private static byte[] getBytes(String filename) throws FileNotFoundException, IOException
    {
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fis.read(bytes);
        fis.close();
        return bytes;
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

    private static String getTestDataFolder()
    {
        return "../screening/resource/test-data/ImageBase64EncodingTest/";
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 6000;
    }

    @Override
    protected boolean checkLogContentForFinishedDataSetRegistration(String logContent)
    {
        return checkOnFinishedPostRegistration(logContent);
    }

    private static class PlateImageReferenceList extends ArrayList<PlateImageReference> implements
            IModifiable
    {

        private static final long serialVersionUID = 1845499753563383092L;

        public PlateImageReferenceList(Collection<? extends PlateImageReference> c)
        {
            super(c);
        }
    }
}
