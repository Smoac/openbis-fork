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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.generic.client.cli.Login;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * @author Franz-Josef Elmer
 */
public class OpenBISScreeningMLTest extends AbstractFileSystemTestCase
{
    private static final FilenameFilter FILTER_TEMP_DIR = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.startsWith(OpenBISScreeningML.TEMP_DIR_PREFIX)
                        && name.endsWith(OpenBISScreeningML.TEMP_DIR_POSTFIX);
            }
        };

    private Mockery context;

    private IScreeningOpenbisServiceFacade openbis;

    private File tempDir;

    private ExperimentIdentifier eId1;

    private ExperimentIdentifier eId2;

    private IDataSetDss ds1;

    private IDataSetDss ds2;

    private IScreeningOpenbisServiceFacadeFactory facadeFactory;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        openbis = context.mock(IScreeningOpenbisServiceFacade.class);
        facadeFactory = context.mock(IScreeningOpenbisServiceFacadeFactory.class);
        ds1 = context.mock(IDataSetDss.class, "ds1");
        ds2 = context.mock(IDataSetDss.class, "ds2");
        FileUtilities.deleteRecursively(workingDirectory);
        workingDirectory.mkdirs();
        OpenBISScreeningML.tempDir = workingDirectory;
        eId1 = new ExperimentIdentifier("E1", "P", "S", "e-1");
        eId2 = new ExperimentIdentifier("E2", "P", "S", "e-2");
        context.checking(new Expectations()
            {
                {
                    one(facadeFactory).tryToCreate("user", "password", "url");
                    will(returnValue(openbis));

                    one(openbis).listExperiments();
                    will(returnValue(Arrays.asList(eId1, eId2)));

                    one(openbis).listPlates();
                    Plate p1 = new Plate("PLATE-1", "S", "s-1", eId1);
                    Plate p2 = new Plate("PLATE-2", "S", "s-2", eId2);
                    will(returnValue(Arrays.asList(p1, p2)));
                }
            });
        OpenBISScreeningML.facadeFactory = facadeFactory;
        OpenBISScreeningML.login("user", "password", "url");
        tempDir = OpenBISScreeningML.tempDir.listFiles(FILTER_TEMP_DIR)[0];
    }

    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testLogout()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).logout();
                }
            });
        File a = new File(tempDir, "a");
        a.mkdirs();
        FileUtilities.writeToFile(new File(a, "1.txt"), "one");
        FileUtilities.writeToFile(new File(tempDir, "2.txt"), "two");

        OpenBISScreeningML.logout();

        assertEquals(false, tempDir.exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckLoggedInFailsBecauseOfMissingSessionTokenFile()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).logout();
                }
            });
        OpenBISScreeningML.logout();

        try
        {
            OpenBISScreeningML.listExperiments();
            fail("RuntimeException expected");
        } catch (RuntimeException ex)
        {
            assertEquals("Not logged in.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCheckLoggedInFailsBecauseOfInvalidSession()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).logout();
                    one(facadeFactory).tryToCreate("session-token-1", "url");
                    will(returnValue(null));
                }
            });
        OpenBISScreeningML.logout();
        Login.OPENBIS_USER_FILE.getParentFile().mkdirs();
        FileUtilities.writeToFile(Login.OPENBIS_TOKEN_FILE, "session-token-1");
        FileUtilities.writeToFile(Login.OPENBIS_SERVER_URL_FILE, "url");

        try
        {
            OpenBISScreeningML.listExperiments();
            fail("RuntimeException expected");
        } catch (RuntimeException ex)
        {
            assertEquals("Login failed.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCheckLoggedInSucceed()
    {
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(openbis).logout();

                    one(facadeFactory).tryToCreate("session-token-1", "url");
                    will(returnValue(openbis));

                    one(openbis).listExperiments();
                    one(openbis).listPlates();
                }
            });
        OpenBISScreeningML.logout();
        Login.OPENBIS_USER_FILE.getParentFile().mkdirs();
        FileUtilities.writeToFile(Login.OPENBIS_TOKEN_FILE, "session-token-1");
        FileUtilities.writeToFile(Login.OPENBIS_SERVER_URL_FILE, "url");

        OpenBISScreeningML.listExperiments();
        OpenBISScreeningML.logout();

        assertEquals(false, Login.OPENBIS_TOKEN_FILE.exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testListExperiments()
    {
        Object[][] experiments = OpenBISScreeningML.listExperiments();

        sort(experiments);
        assertEquals("/S/P/E1", experiments[0][0]);
        assertEquals("e-1", experiments[0][1]);
        assertEquals("S", experiments[0][2]);
        assertEquals("P", experiments[0][3]);
        assertEquals("E1", experiments[0][4]);
        assertEquals("/S/P/E2", experiments[1][0]);
        assertEquals("e-2", experiments[1][1]);
        assertEquals("S", experiments[1][2]);
        assertEquals("P", experiments[1][3]);
        assertEquals("E2", experiments[1][4]);
        assertEquals(2, experiments.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testListPlates()
    {
        Object[][] plates = OpenBISScreeningML.listPlates();

        sort(plates);
        assertEquals("/S/PLATE-1", plates[0][0]);
        assertEquals("s-1", plates[0][1]);
        assertEquals("S", plates[0][2]);
        assertEquals("PLATE-1", plates[0][3]);
        assertEquals("/S/P/E1", plates[0][4]);
        assertEquals("e-1", plates[0][5]);
        assertEquals("S", plates[0][6]);
        assertEquals("P", plates[0][7]);
        assertEquals("E1", plates[0][8]);
        assertEquals("/S/PLATE-2", plates[1][0]);
        assertEquals("s-2", plates[1][1]);
        assertEquals("S", plates[1][2]);
        assertEquals("PLATE-2", plates[1][3]);
        assertEquals("/S/P/E2", plates[1][4]);
        assertEquals("e-2", plates[1][5]);
        assertEquals("S", plates[1][6]);
        assertEquals("P", plates[1][7]);
        assertEquals("E2", plates[1][8]);
        assertEquals(2, plates.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testListPlatesByExperiment()
    {
        try
        {
            OpenBISScreeningML.listPlates("/S/P/E3");
            fail("RuntimeException expected");
        } catch (RuntimeException ex)
        {
            assertEquals("No experiment with that code found.", ex.getMessage());
        }

        Object[][] plates = OpenBISScreeningML.listPlates("/S/P/E1");

        sort(plates);
        assertEquals("/S/PLATE-1", plates[0][0]);
        assertEquals("s-1", plates[0][1]);
        assertEquals("S", plates[0][2]);
        assertEquals("PLATE-1", plates[0][3]);
        assertEquals("/S/P/E1", plates[0][4]);
        assertEquals("e-1", plates[0][5]);
        assertEquals("S", plates[0][6]);
        assertEquals("P", plates[0][7]);
        assertEquals("E1", plates[0][8]);
        assertEquals(1, plates.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetWellProperties()
    {
        context.checking(new Expectations()
            {
                {
                    Plate plate1 = new Plate("PLATE-1", "S", "s-1", eId1);
                    one(openbis).listPlateWells(plate1);
                    WellIdentifier w1 = new WellIdentifier(plate1, new WellPosition(1, 1), "w1");
                    WellIdentifier w2 = new WellIdentifier(plate1, new WellPosition(1, 2), "w2");
                    WellIdentifier w3 = new WellIdentifier(plate1, new WellPosition(2, 1), "w3");
                    will(returnValue(Arrays.asList(w1, w2, w3)));

                    one(openbis).getWellProperties(w2);
                    Map<String, String> properties = new LinkedHashMap<String, String>();
                    properties.put("answer", "42");
                    properties.put("name", "Albert");
                    will(returnValue(properties));
                }
            });

        Object[][] wellProperties = OpenBISScreeningML.getWellProperties("/S/PLATE-1", 1, 2);

        assertEquals("answer", wellProperties[0][0]);
        assertEquals("42", wellProperties[0][1]);
        assertEquals("name", wellProperties[1][0]);
        assertEquals("Albert", wellProperties[1][1]);
        assertEquals(2, wellProperties.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateWellProperties()
    {
        context.checking(new Expectations()
            {
                {
                    Plate plate1 = new Plate("PLATE-1", "S", "s-1", eId1);
                    one(openbis).listPlateWells(plate1);
                    WellIdentifier w1 = new WellIdentifier(plate1, new WellPosition(1, 1), "w1");
                    WellIdentifier w2 = new WellIdentifier(plate1, new WellPosition(1, 2), "w2");
                    WellIdentifier w3 = new WellIdentifier(plate1, new WellPosition(2, 1), "w3");
                    will(returnValue(Arrays.asList(w1, w2, w3)));

                    Map<String, String> properties = new LinkedHashMap<String, String>();
                    properties.put("A", "42");
                    properties.put("B", "43");
                    one(openbis).updateWellProperties(w2, properties);
                }
            });

        OpenBISScreeningML.updateWellProperties("/S/PLATE-1", 1, 2, new Object[][]
            {
                { "A", "42" },
                { "B", "43" } });

        context.assertIsSatisfied();
    }

    @Test
    public void testLoadDataSets()
    {
        final File dataSetFolder =
                new File(OpenBISScreeningML.tempDir, OpenBISScreeningML.DATASETS_FOLDER);
        final File ds1Folder = new File(dataSetFolder, "ds-1");
        File ds2Folder = new File(dataSetFolder, "ds-2");
        ds2Folder.mkdirs();
        final String datasetTypePattern = "blablaCode";
        final String mountPoint = "/mount/openbis/store";
        context.checking(new Expectations()
            {
                {
                    one(openbis).getDataSets(new Plate("PLATE-1", "S", "s-1", eId1),
                            datasetTypePattern);
                    will(returnValue(Arrays.asList(ds1, ds2)));

                    one(ds1).getCode();
                    will(returnValue("ds-1"));

                    one(ds1).getLinkOrCopyOfContents(mountPoint, dataSetFolder);
                    will(returnValue(ds1Folder));

                    one(ds2).getCode();
                    will(returnValue("ds-2"));
                }
            });

        Object[][] result =
                OpenBISScreeningML.loadDataSets("/S/PLATE-1", datasetTypePattern, mountPoint);

        assertEquals("ds-1", result[0][0]);
        assertEquals(ds1Folder.getPath(), result[0][1]);
        assertEquals("ds-2", result[1][0]);
        assertEquals(ds2Folder.getPath(), result[1][1]);
        assertEquals(2, result.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateDataSet()
    {
        final File dataSetFolder =
                new File(OpenBISScreeningML.tempDir, OpenBISScreeningML.DATASETS_FOLDER);
        final File ds1Folder = new File(dataSetFolder, "ds-1");
        ds1Folder.mkdirs();
        final RecordingMatcher<NewDataSetMetadataDTO> metaDataMatcher =
                new RecordingMatcher<NewDataSetMetadataDTO>();
        context.checking(new Expectations()
            {
                {
                    Map<String, String> properties = new LinkedHashMap<String, String>();
                    properties.put("A", "42");
                    properties.put("B", "43");
                    try
                    {
                        one(openbis).putDataSet(with(new Plate("PLATE-1", "S", "s-1", eId1)),
                                with(ds1Folder), with(metaDataMatcher));
                        will(returnValue(ds1));
                    } catch (Exception ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }

                    one(ds1).getCode();
                    will(returnValue("DS-1"));
                }
            });

        Object code =
                OpenBISScreeningML.uploadDataSet("/S/PLATE-1", ds1Folder.getPath(), "my-type",
                        new Object[][]
                            {
                                { "A", "42" },
                                { "B", "43" } });

        assertEquals("DS-1", code);
        assertEquals("my-type", metaDataMatcher.recordedObject().tryDataSetType());
        assertEquals("{A=42, B=43}", metaDataMatcher.recordedObject().getProperties().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadImages()
    {
        final RecordingMatcher<List<PlateImageReference>> imgRefsMatcher1 =
                new RecordingMatcher<List<PlateImageReference>>();
        final RecordingMatcher<List<PlateImageReference>> imgRefsMatcher2 =
                new RecordingMatcher<List<PlateImageReference>>();
        final Sequence sequence = context.sequence("load");
        context.checking(new Expectations()
            {
                {
                    Plate plate = new Plate("PLATE-1", "S", "s-1", eId1);
                    exactly(2).of(openbis).listRawImageDatasets(Arrays.asList(plate));
                    ImageDatasetReference ds1Ref =
                            new ImageDatasetReference("ds1", "", plate, eId1, null, null, null,
                                    null);
                    List<ImageDatasetReference> imageRefs = Arrays.asList(ds1Ref);
                    will(returnValue(imageRefs));

                    exactly(2).of(openbis).listImageMetadata(imageRefs);
                    List<String> channelCodes = Arrays.asList("R", "G");
                    List<String> channelLabels = Arrays.asList("red", "green");
                    ImageDatasetMetadata metaData1 =
                            new ImageDatasetMetadata(ds1Ref, channelCodes, channelLabels, 1, 2,
                                    100, 60, 10, 6);
                    will(returnValue(Arrays.asList(metaData1)));

                    try
                    {
                        one(openbis).loadImages(with(imgRefsMatcher1),
                                with(new BaseMatcher<IImageOutputStreamProvider>()
                                    {
                                        public boolean matches(Object item)
                                        {
                                            if (item instanceof IImageOutputStreamProvider)
                                            {
                                                IImageOutputStreamProvider provider =
                                                        (IImageOutputStreamProvider) item;
                                                List<PlateImageReference> recordedObject =
                                                        imgRefsMatcher1.recordedObject();
                                                for (PlateImageReference ref : recordedObject)
                                                {
                                                    try
                                                    {
                                                        OutputStream outputStream =
                                                                provider.getOutputStream(ref);
                                                        new PrintWriter(outputStream, true)
                                                                .println(ref.toString() + " (1)");
                                                    } catch (IOException ex)
                                                    {
                                                        throw CheckedExceptionTunnel
                                                                .wrapIfNecessary(ex);
                                                    }
                                                }
                                                return true;
                                            }
                                            return false;
                                        }

                                        public void describeTo(Description description)
                                        {
                                        }
                                    }), with(false));
                        inSequence(sequence);
                        one(openbis).loadImages(with(imgRefsMatcher2),
                                with(new BaseMatcher<IImageOutputStreamProvider>()
                                    {
                                        public boolean matches(Object item)
                                        {
                                            if (item instanceof IImageOutputStreamProvider)
                                            {
                                                IImageOutputStreamProvider provider =
                                                        (IImageOutputStreamProvider) item;
                                                List<PlateImageReference> recordedObject =
                                                        imgRefsMatcher2.recordedObject();
                                                for (PlateImageReference ref : recordedObject)
                                                {
                                                    try
                                                    {
                                                        OutputStream outputStream =
                                                                provider.getOutputStream(ref);
                                                        new PrintWriter(outputStream, true)
                                                                .println(ref.toString() + " (2)");
                                                    } catch (IOException ex)
                                                    {
                                                        throw CheckedExceptionTunnel
                                                                .wrapIfNecessary(ex);
                                                    }
                                                }
                                                return true;
                                            }
                                            return false;
                                        }

                                        public void describeTo(Description description)
                                        {
                                        }
                                    }), with(false));
                        inSequence(sequence);
                    } catch (IOException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            });

        Object[][][] result1 = OpenBISScreeningML.loadImages("/S/PLATE-1", 1, 2, 1, new String[]
            { "G" });
        List<PlateImageReference> imgRefs1 = imgRefsMatcher1.recordedObject();
        for (PlateImageReference plateImageReference : imgRefs1)
        {
            assertEquals("ds1", plateImageReference.getDatasetCode());
            assertEquals(1, plateImageReference.getWellPosition().getWellRow());
            assertEquals(2, plateImageReference.getWellPosition().getWellColumn());
        }
        assertEquals("G", imgRefs1.get(0).getChannel());
        assertEquals(1, imgRefs1.get(0).getTile());
        assertEquals(1, imgRefs1.size());
        assertEquals(2, result1.length);
        assertEquals("/images/img_PLATE-1_ds1_row1_col2_G_tile1.tiff", getImagePath(result1, 0));
        assertEquals("Image for [dataset ds1, well [1, 2], channel G, tile 1] (1)", FileUtilities
                .loadToString(new File(result1[0][0][0].toString())).trim());
        assertEquals(1, result1[0].length);
        assertEquals(
                "[G, 1, PLATE-1:A2, /S/PLATE-1, s-1, S, PLATE-1, 1, 2, /S/P/E1, e-1, S, P, E1, ds1]",
                Arrays.asList(result1[1][0]).toString());
        assertEquals(1, result1[1].length);

        Object[][][] result2 = OpenBISScreeningML.loadImages("/S/PLATE-1", 1, 2);
        List<PlateImageReference> imgRefs2 = imgRefsMatcher2.recordedObject();
        for (PlateImageReference plateImageReference : imgRefs2)
        {
            assertEquals("ds1", plateImageReference.getDatasetCode());
            assertEquals(1, plateImageReference.getWellPosition().getWellRow());
            assertEquals(2, plateImageReference.getWellPosition().getWellColumn());
        }
        assertEquals("R", imgRefs2.get(0).getChannel());
        assertEquals(0, imgRefs2.get(0).getTile());
        assertEquals("R", imgRefs2.get(1).getChannel());
        assertEquals(1, imgRefs2.get(1).getTile());
        assertEquals("G", imgRefs2.get(2).getChannel());
        assertEquals(0, imgRefs2.get(2).getTile());
        assertEquals(3, imgRefs2.size());

        assertEquals(2, result2.length);
        assertEquals("/images/img_PLATE-1_ds1_row1_col2_R_tile0.tiff", getImagePath(result2, 0));
        assertEquals("Image for [dataset ds1, well [1, 2], channel R, tile 0] (2)", FileUtilities
                .loadToString(new File(result2[0][0][0].toString())).trim());
        assertEquals("/images/img_PLATE-1_ds1_row1_col2_R_tile1.tiff", getImagePath(result2, 1));
        assertEquals("Image for [dataset ds1, well [1, 2], channel R, tile 1] (2)", FileUtilities
                .loadToString(new File(result2[0][1][0].toString())).trim());
        assertEquals("/images/img_PLATE-1_ds1_row1_col2_G_tile0.tiff", getImagePath(result2, 2));
        assertEquals("Image for [dataset ds1, well [1, 2], channel G, tile 0] (2)", FileUtilities
                .loadToString(new File(result2[0][2][0].toString())).trim());
        assertEquals("/images/img_PLATE-1_ds1_row1_col2_G_tile1.tiff", getImagePath(result2, 3));
        assertEquals("Image for [dataset ds1, well [1, 2], channel G, tile 1] (1)", FileUtilities
                .loadToString(new File(result2[0][3][0].toString())).trim());
        assertEquals(4, result2[0].length);
        assertEquals(
                "[R, 0, PLATE-1:A2, /S/PLATE-1, s-1, S, PLATE-1, 1, 2, /S/P/E1, e-1, S, P, E1, ds1]",
                Arrays.asList(result2[1][0]).toString());
        assertEquals(
                "[R, 1, PLATE-1:A2, /S/PLATE-1, s-1, S, PLATE-1, 1, 2, /S/P/E1, e-1, S, P, E1, ds1]",
                Arrays.asList(result2[1][1]).toString());
        assertEquals(
                "[G, 0, PLATE-1:A2, /S/PLATE-1, s-1, S, PLATE-1, 1, 2, /S/P/E1, e-1, S, P, E1, ds1]",
                Arrays.asList(result2[1][2]).toString());
        assertEquals(
                "[G, 1, PLATE-1:A2, /S/PLATE-1, s-1, S, PLATE-1, 1, 2, /S/P/E1, e-1, S, P, E1, ds1]",
                Arrays.asList(result2[1][3]).toString());
        assertEquals(4, result2[1].length);
        context.assertIsSatisfied();
    }

    private String getImagePath(Object[][][] result, int i)
    {
        return result[0][i][0].toString().substring(tempDir.getPath().length());
    }

    private void sort(Object[][] entities)
    {
        Arrays.sort(entities, new Comparator<Object[]>()
            {
                public int compare(Object[] o1, Object[] o2)
                {
                    return o1[0].toString().compareTo(o2[0].toString());
                }
            });
    }

}
