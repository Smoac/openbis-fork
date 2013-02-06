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

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.json.ScreeningObjectMapper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

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
                new JsonRpcHttpClient(new ScreeningObjectMapper(), new URL(
                        TestInstanceHostUtils.getDSSUrl()
                                + "/rmi-datastore-server-screening-api-v1.json/"),
                        new HashMap<String, String>());
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

    /**
     * data to compare whether the fetched thumbnail images are exactly as required
     */
    HashMap<Integer, String> expectedThumbnails()
    {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        map.put(128,
                "/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCACAAIADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDiaSioZpgg6182lc/a5zUVdjpJQgqD7TngGs+e6LNgGoTeLF3y3pXTGi7HiVczSlvZGykhxkmneevY5rB+3M3JOcUHUFQYJzT+rshZzTS3OgEoPcCnhsjrXNf2s/Rdq/QZqJ9XuO0j49OKPqk2D4gw8VrdnV7wOpppnQd649tVlP8Ay1kBpo1WbPL5HvVLAyMJcUUOiOua6Xsaja6HPNcsNWbPzA1INSDd6r6m0Z/6xU57M6E3Oe9WIpww5rmheg96t215yBmonh2kb4fN4ue50QORRVSCcMBk1bByK45RaZ9HSqxqRuiGVwi1k3U5Zjg9Kv3H3TmsO9k2qcHmuqhC7PBzXEOMX2Kd3eMrbVIzVQynA55qFmJYmm54r1o00kfAVsXOpJtsnMpb+LApplA+7z7moc0marlRzutJkhdz3pOT/EPzplFVYhzb3HYP94H8aXkdqZRRYXMLRmkzRmgVx24joasW1wyuOarU5DhgalpNG1OpKEk0zpbO5PGa2reUMMVzVkcgVu2w6V5OIgrn6DlGIm0h90owa5zUW2g1092PkNcpqZ+cD3q8JqzDiH3IMyjSVLINuB361Ea9VanwMlZ2EoopaZmJRS0ZoASiilzQAlFLSUAKKUcGkFLSKRs6cdwFdHaqMCuY0lstt966y1XCg15WL0Z+gcPe/TTHXAzGa5DURm8212TjKkVxt7/yE5PY0sFuy+Jl+7h5sz5TumOPWoj1pwOWY/WmV6yPz6o7u4UtJRTMwooooAKKKKAF7UlFFACjrStxSUrdvpSKWxo6Of8ASwvqK7SAYjFcPpJ/0+H3bFd4owoFeVj9JI/QOEvew8n2Y1zgE1xV+wXU5B/tV2c5xGa4rVxi8LeopYH4mPiptUYtdGUB1b6GmU8HnNN716yPz+WwlBoopkBRRRQAUUUUAFFLSUALSt2+lIOtK3NIpbF3Sf8AkIQ/71d4pyoNcNo6/wClg+grtoTmMV5OP+NH6Bwlph5LuyK6bCEVyWrLlw1dTdNwea5rUhuBp4PRhxF79Nox6Q0p60hr1T8/YlLSUUyRaSiigBaKSigBaSiigBRS0gpR1pFI1NJXDlq661b5MVy2mjaBXSWrcDmvJxmrP0Dh33KaQy5BIJrBvRkGummQEZrGvbUnJWpw80mdGb4aUoto5duGIpKs3Fsyv0qAow6ivWTTR+e1KcoSaaGUYpcYoqjGwmKKXdijd7A/hQFkJRS7vYflRuzQFkJijFLRigLBSrywFG0noKsW9uzOOKltJGtOnKckkjTshgCt22OMVmWdseM1t28QUZrycRNNn6Fk+HmoomPNV5osg8cVYpOD1rlTsfQVIKaszBu7LnIFUzZA9q6SSEEdMiovsobpXVDENI8CvlEZzukc6dODdqjbSSfukiunFrjjFSrar3AqvrbRz/6u057o486PcdgDUZ0m7/54Mfoa7gQIO1SBVHQCn9fmugf6pYeW8mjg/wCybv8A54N+dPGj3HcAV3JUHqKYYUPal9fn2D/VLDraTZxq6Sw+8TUo00L2rqWtV7ComtRzxT+uNh/q7ThsjnhZAdqtW1n82cVpm2x2qxFbhRzUTxDaN8Pk8VPYZb24UAkVbAwKAMUVxyk2z6KlSjTjZH//2Q==");
        map.put(256,
                "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");
        map.put(64,
                "/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCABAAEADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDhmYIuScVnSagN5y21f503Urgop54FctNdPJISScdhXj4fD86uz9GzfOfq0uSJ1Q1WMDA6epNB1u3QcuDx2rkDMx6k0zfnrk/jXV9Sg9zw3xPiF8KOsfxDEDgAH6VH/byP3xXL7h6H86TPoapYOmjCXEmMk9WddBqqs4+brWvFKsq5FefRuyuCDXU6XO7KOa5cThlFXR7uS51OvP2dQj1v5UPvXLuME11evjESccE4rlJDk59TXVg9aZ4XEati2htFFHeuw+dD8aKO9FACiup0L95GDXLDoa6rwzloXJ7GuTGfw2z6HhvXGxj3DxGxEMeOx5/KuVbkCup1754z7VyxpYP+Gg4kbeNkxPWjvRR+Fdh88Hej0o/CigBR9011XhjIgkBHU/0rlR6V1Og/JGPeuTGfwmj6HhvTGxl2JtUt2aNiBmuUeNkYgivQJEEgway59KV5CVXr2rkw2JUFZnv51kssRNVKZx+PajA9TXU/2Er9VxR/wjMbHO8j9a6/rlPqzwP9W8a/hjc5bA9aMeldV/wjEQORIT+ApP7BROgzR9cpdGH+reNXxRsc0iMzAAV1OlQOqLxiiDSlWQfLWxFEsS4FcmJxKkrI97JclnQn7Sof/9k=");
        return map;
    }

    @Test
    public void testTransformedThumbnails() throws Exception
    {
        // The components of the plate identifier come from the dropbox code
        // (resource/test-data/TransformedImageRepresentationsTest/data-set-handler.py)
        PlateIdentifier plate = new PlateIdentifier("TRANSFORMED-THUMB-PLATE", "TEST", null);
        List<ImageDatasetReference> imageDataSets =
                screeningFacade.listRawImageDatasets(Arrays.asList(plate));
        List<DatasetImageRepresentationFormats> representationFormats =
                screeningFacade.listAvailableImageRepresentationFormats(imageDataSets);
        assertEquals(1, representationFormats.size());
        List<ImageRepresentationFormat> formats =
                representationFormats.get(0).getImageRepresentationFormats();

        List<PlateImageReference> plateRefs =
                screeningJsonApi.listPlateImageReferences(sessionToken, imageDataSets.get(0),
                        Arrays.asList(new WellPosition(1, 1)), "Cy5");

        HashSet<Dimension> expectedResolutions = new HashSet<Dimension>();
        expectedResolutions.addAll(Arrays.asList(new Dimension(64, 64), new Dimension(128, 128),
                new Dimension(256, 256), new Dimension(512, 512)));

        for (ImageRepresentationFormat format : formats)
        {

            HashMap<Integer, String> expectedThumbnails = expectedThumbnails();
            if (false == format.isOriginal())
            {
                List<String> thumbnails =
                        screeningJsonApi.loadPhysicalThumbnailsBase64(sessionToken, plateRefs,
                                format);
                String expectedThumbnailImage = expectedThumbnails.get(format.getWidth());
                assertEquals(1, thumbnails.size());
                assertEquals(expectedThumbnailImage, thumbnails.get(0));
            }

            if (format.getFileType() != null)
            {
                // jpg thumbnails
                assertEquals(Integer.valueOf(32), format.getColorDepth());
            } else
            {
                // original image
                assertEquals(Integer.valueOf(8), format.getColorDepth());
            }

            Dimension resolution = new Dimension(format.getWidth(), format.getHeight());
            // Make sure the resolution we specified was found
            assertTrue("" + resolution + " was not expected",
                    expectedResolutions.remove(resolution));
        }
        assertEquals(0, expectedResolutions.size());

        // Check that the representations are JPEG for the following resolutions: 64x64, 128x128,
        // 256x256
        HashSet<Dimension> jpegResolutions = new HashSet<Dimension>();
        jpegResolutions.addAll(Arrays.asList(new Dimension(64, 64), new Dimension(128, 128),
                new Dimension(256, 256)));
        for (ImageRepresentationFormat format : formats)
        {
            Dimension resolution = new Dimension(format.getWidth(), format.getHeight());
            if (jpegResolutions.contains(resolution))
            {
                assertEquals("jpg", format.getFileType().toLowerCase());
                assertEquals(3, format.getTransformations().size());
            }
        }
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
        return "../screening/resource/test-data/TransformedImageRepresentationsTest/";
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
}
