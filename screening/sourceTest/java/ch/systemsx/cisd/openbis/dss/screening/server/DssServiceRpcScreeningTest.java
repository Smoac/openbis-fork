/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.screening.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.SerializationUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.framework.ProxyFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.convert.NativeTaggedArray;
import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor.DssServiceRpcAuthorizationMethodInterceptor;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtilsTest;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreeningInternal;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingTransformerDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgExperimentDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

/**
 * Test cases for the {@link DssServiceRpcScreening}.
 * 
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcScreeningTest extends AssertJUnit
{
    private static final class ImageTransformerFactory implements IImageTransformerFactory
    {
        private static final long serialVersionUID = 1L;

        private static int counter;

        private int id = counter++;

        public IImageTransformer createTransformer()
        {
            return null;
        }
    }

    private static final String EXPERIMENT_PERM_ID = "exp-123";

    private static final long EXPERIMENT_ID = 333;

    private static final String URL1 = "url1";

    private static final String SESSION_TOKEN = "session";

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IImagingReadonlyQueryDAO dao;

    private IFeatureVectorDatasetIdentifier featureVectorDatasetIdentifier1;

    private IFeatureVectorDatasetIdentifier featureVectorDatasetIdentifier2;

    private DssServiceRpcScreening screeningService;

    private ImageTransformerFactory transformerFactory;

    private IImagingTransformerDAO transformerDAO;

    private IImagingDatasetLoader imageLoader;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dao = context.mock(IImagingReadonlyQueryDAO.class);
        transformerDAO = context.mock(IImagingTransformerDAO.class);
        imageLoader = context.mock(IImagingDatasetLoader.class);
        transformerFactory = new ImageTransformerFactory();
        featureVectorDatasetIdentifier1 = create("ds1");
        featureVectorDatasetIdentifier2 = create("ds2");
        final ImageDatasetParameters imageParameters = new ImageDatasetParameters();
        imageParameters.setRowsNum(7);
        imageParameters.setColsNum(4);
        imageParameters.setTileRowsNum(1);
        imageParameters.setTileColsNum(2);
        context.checking(new Expectations()
            {
                {
                    allowing(imageLoader).getImageParameters();
                    will(returnValue(imageParameters));
                }
            });

        screeningService =
                new DssServiceRpcScreening("targets", dao, transformerDAO, service, false)
                    {
                        @Override
                        IImagingDatasetLoader createImageLoader(String datasetCode, File datasetRoot)
                        {
                            return imageLoader;
                        }
                    };
    }

    private IFeatureVectorDatasetIdentifier create(final String dataSetCode)
    {
        final IFeatureVectorDatasetIdentifier identifier =
                context.mock(IFeatureVectorDatasetIdentifier.class, dataSetCode);
        context.checking(new Expectations()
            {
                {
                    allowing(identifier).getDatasetCode();
                    will(returnValue(dataSetCode));
                }
            });
        return identifier;
    }

    @AfterMethod
    public final void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testListPlateImageReferences()
    {
        final DatasetIdentifier ds = new DatasetIdentifier("ds1", URL1);
        final List<WellPosition> wellPositions = Arrays.asList(new WellPosition(1, 3));
        final String channel = "dapi";
        prepareGetHomeDatabaseInstance();

        List<PlateImageReference> plateImageReferences =
                screeningService
                        .listPlateImageReferences(SESSION_TOKEN, ds, wellPositions, channel);

        assertEquals("[Image for [dataset ds1, well [1, 3], channel DAPI, tile 0], "
                + "Image for [dataset ds1, well [1, 3], channel DAPI, tile 1]]",
                plateImageReferences.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testListAvailableFeatureNames()
    {
        prepareAssetDataSetsAreAccessible();
        prepareGetFeatureDefinitions(1, "f1", "f2");
        prepareGetFeatureDefinitions(2, "f2", "f3");

        List<String> names =
                screeningService.listAvailableFeatureNames(SESSION_TOKEN, Arrays.asList(
                        featureVectorDatasetIdentifier1, featureVectorDatasetIdentifier2));

        assertEquals("[f1, f2, f3]", names.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthorization()
    {
        prepareAssetDataSetsAreAccessible();
        prepareGetFeatureDefinitions(1, "f1", "f2");
        prepareGetFeatureDefinitions(2, "f2", "f3");

        // Add the expectation for checkDataSetCollectionAccess again -- we expect it to be invoked
        // once more from the authorization code.
        context.checking(new Expectations()
            {
                {
                    one(service).checkDataSetCollectionAccess(SESSION_TOKEN,
                            Arrays.asList("ds1", "ds2"));
                }
            });

        TestMethodInterceptor interceptor = new TestMethodInterceptor();
        IDssServiceRpcScreeningInternal serviceInternal = getAdvisedService(interceptor);

        List<String> names =
                serviceInternal.listAvailableFeatureCodes(SESSION_TOKEN, Arrays.asList(
                        featureVectorDatasetIdentifier1, featureVectorDatasetIdentifier2));

        assertTrue(interceptor.methodInvoked);

        assertEquals("[f1, f2, f3]", names.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadFeatures()
    {
        prepareAssetDataSetsAreAccessible();
        FeatureVectorDatasetReference r1 = createFeatureVectorDatasetReference("ds1");
        FeatureVectorDatasetReference r2 = createFeatureVectorDatasetReference("ds2");
        prepareCreateFeatureVectorDataSet(1, "F1", "F2");
        prepareCreateFeatureVectorDataSet(2, "F2");

        List<FeatureVectorDataset> dataSets =
                screeningService.loadFeatures(SESSION_TOKEN, Arrays.asList(r1, r2),
                        Arrays.asList("f1", "f2"));

        assertSame(r1, dataSets.get(0).getDataset());
        assertEquals("[F1, F2]", dataSets.get(0).getFeatureCodes().toString());
        assertEquals("[F1, F2]", dataSets.get(0).getFeatureLabels().toString());
        assertFeatureVector(1, 1, dataSets.get(0).getFeatureVectors().get(0), 244.5, 245.5);
        assertFeatureVector(1, 2, dataSets.get(0).getFeatureVectors().get(1), 242.25, 243.25);
        assertEquals(2, dataSets.get(0).getFeatureVectors().size());
        assertSame(r2, dataSets.get(1).getDataset());
        assertEquals("[F2]", dataSets.get(1).getFeatureCodes().toString());
        assertEquals("[F2]", dataSets.get(1).getFeatureLabels().toString());
        assertFeatureVector(1, 1, dataSets.get(1).getFeatureVectors().get(0), 249.0);
        assertFeatureVector(1, 2, dataSets.get(1).getFeatureVectors().get(1), 244.5);
        assertEquals(2, dataSets.get(1).getFeatureVectors().size());
        assertEquals(2, dataSets.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadImages() throws IOException
    {
        final String channel = "GFP";
        prepareGetHomeDatabaseInstance();
        context.checking(new Expectations()
            {
                {
                    Size thumbnailSize = new Size(2, 1);
                    one(imageLoader).tryGetImage(
                            channel,
                            ImageChannelStackReference.createHCSFromLocations(new Location(3, 1),
                                    new Location(1, 1)), thumbnailSize);
                    will(returnValue(new AbsoluteImageReference(image("img1.jpg"), "img1", null,
                            null, thumbnailSize)));
                    one(imageLoader).tryGetImage(
                            channel,
                            ImageChannelStackReference.createHCSFromLocations(new Location(3, 1),
                                    new Location(2, 1)), thumbnailSize);
                    will(returnValue(new AbsoluteImageReference(image("img1.gif"), "img1", null,
                            null, thumbnailSize)));
                }
            });

        InputStream images =
                screeningService.loadImages(SESSION_TOKEN, new DatasetIdentifier("ds1", "url1"),
                        Arrays.asList(new WellPosition(1, 3)), channel, new ImageSize(2, 1));

        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(images);
        BufferedImage image1 = extractNextImage(imagesWriter);
        assertEquals(1, image1.getWidth());
        assertEquals(1, image1.getHeight());
        BufferedImage image2 = extractNextImage(imagesWriter);
        assertEquals(1, image2.getWidth());
        assertEquals(1, image2.getHeight());

        context.assertIsSatisfied();
    }

    BufferedImage extractNextImage(ConcatenatedFileOutputStreamWriter imagesWriter)
            throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imagesWriter.writeNextBlock(outputStream);
        return ImageUtil.loadImage(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    @Test
    public void testGetImageTransformerFactoryForChannel()
    {
        final DatasetIdentifier ds1 = new DatasetIdentifier("ds1", "url1");
        final DatasetIdentifier ds2 = new DatasetIdentifier("ds2", "url1");
        final String channel = "dapi";
        prepareGetExperimentPermIDs(ds1, ds2);
        context.checking(new Expectations()
            {
                {
                    one(dao).tryGetChannelForExperimentPermId(EXPERIMENT_PERM_ID, channel);
                    ImgChannelDTO channelDTO =
                            new ImgChannelDTO("dapi", null, null, new Long(42), null, "dapi");
                    channelDTO.setSerializedImageTransformerFactory(SerializationUtils
                            .serialize(transformerFactory));
                    will(returnValue(channelDTO));
                }
            });

        IImageTransformerFactory result =
                screeningService.getImageTransformerFactoryOrNull(SESSION_TOKEN,
                        Arrays.<IDatasetIdentifier> asList(ds1, ds2), channel);

        assertEquals(transformerFactory.id, ((ImageTransformerFactory) result).id);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetImageTransformerFactoryForExperiment()
    {
        final DatasetIdentifier ds1 = new DatasetIdentifier("ds1", "url1");
        final DatasetIdentifier ds2 = new DatasetIdentifier("ds2", "url1");
        prepareGetExperimentPermIDs(ds1, ds2);
        context.checking(new Expectations()
            {
                {
                    one(dao).tryGetExperimentByPermId(EXPERIMENT_PERM_ID);
                    ImgExperimentDTO experiment = new ImgExperimentDTO();
                    experiment.setSerializedImageTransformerFactory(SerializationUtils
                            .serialize(transformerFactory));
                    will(returnValue(experiment));
                }
            });

        IImageTransformerFactory result =
                screeningService.getImageTransformerFactoryOrNull(SESSION_TOKEN,
                        Arrays.<IDatasetIdentifier> asList(ds1, ds2),
                        ScreeningConstants.MERGED_CHANNELS);

        assertEquals(transformerFactory.id, ((ImageTransformerFactory) result).id);
        context.assertIsSatisfied();
    }

    @Test
    public void testSaveImageTransformerFactoryForDatasetChannel()
    {
        final DatasetIdentifier ds1 = new DatasetIdentifier("ds1", "url1");
        final DatasetIdentifier ds2 = new DatasetIdentifier("ds2", "url1");
        final String channel = "dapi";
        context.checking(new Expectations()
            {
                {
                    one(service).checkInstanceAdminAuthorization(SESSION_TOKEN);

                    long datasetId = 123;
                    ImgDatasetDTO dataset = createDataset(datasetId);

                    one(dao).tryGetDatasetByPermId("ds1");
                    will(returnValue(dataset));
                    one(dao).tryGetDatasetByPermId("ds2");
                    will(returnValue(dataset));

                    exactly(2).of(transformerDAO).saveTransformerFactoryForDatasetChannel(
                            datasetId, channel, transformerFactory);

                    one(transformerDAO).commit();
                }
            });

        screeningService.saveImageTransformerFactory(SESSION_TOKEN,
                Arrays.<IDatasetIdentifier> asList(ds1, ds2), channel, transformerFactory);

        context.assertIsSatisfied();
    }

    private ImgDatasetDTO createDataset(long datasetId)
    {
        ImgDatasetDTO dataset = new ImgDatasetDTO(null, null, null, null, false);
        dataset.setId(datasetId);
        return dataset;
    }

    @Test
    public void testSaveImageTransformerFactoryForExperiment()
    {
        final DatasetIdentifier ds1 = new DatasetIdentifier("ds1", "url1");
        context.checking(new Expectations()
            {
                {
                    one(service).checkInstanceAdminAuthorization(SESSION_TOKEN);

                    Long containerId = 312L;

                    long datasetId = 123;
                    ImgDatasetDTO dataset = createDataset(datasetId);
                    dataset.setContainerId(containerId);

                    long experimentId = 888;
                    ImgContainerDTO container = new ImgContainerDTO(null, null, null, experimentId);
                    container.setId(containerId);

                    one(dao).tryGetDatasetByPermId("ds1");
                    will(returnValue(dataset));

                    one(dao).getContainerById(containerId);
                    will(returnValue(container));

                    one(transformerDAO).saveTransformerFactoryForExperiment(experimentId,
                            transformerFactory);
                    one(transformerDAO).commit();
                }
            });

        screeningService.saveImageTransformerFactory(SESSION_TOKEN,
                Arrays.<IDatasetIdentifier> asList(ds1), ScreeningConstants.MERGED_CHANNELS,
                transformerFactory);

        context.assertIsSatisfied();
    }

    public void prepareGetExperimentPermIDs(final DatasetIdentifier... dataSetIdentifiers)
    {
        context.checking(new Expectations()
            {
                {
                    ExternalData externalData = new ExternalData();
                    Experiment experiment = new Experiment();
                    experiment.setPermId(EXPERIMENT_PERM_ID);
                    experiment.setId(EXPERIMENT_ID);
                    externalData.setExperiment(experiment);
                    for (DatasetIdentifier datasetIdentifier : dataSetIdentifiers)
                    {
                        one(service).tryGetDataSet(SESSION_TOKEN,
                                datasetIdentifier.getDatasetCode());
                        will(returnValue(externalData));
                    }
                }
            });
    }

    private IContent image(String fileName)
    {
        return new FileBasedContent(new File(ImageChannelsUtilsTest.TEST_IMAGE_FOLDER, fileName));
    }

    private void assertFeatureVector(int expectedRowNumber, int expectedColumnNumber,
            FeatureVector featureVector, Object... expectedValues)
    {
        assertEquals(expectedRowNumber, featureVector.getWellPosition().getWellRow());
        assertEquals(expectedColumnNumber, featureVector.getWellPosition().getWellColumn());

        assertEquals(Arrays.asList(expectedValues).toString(), featureVector.getValueObjects()
                .toString());
    }

    private void prepareGetHomeDatabaseInstance()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).getHomeDatabaseInstance();
                    DatabaseInstance databaseInstance = new DatabaseInstance();
                    databaseInstance.setUuid("12345");
                    will(returnValue(databaseInstance));
                }
            });
    }

    private void prepareCreateFeatureVectorDataSet(final long dataSetID,
            final String... featureCodes)
    {
        prepareGetFeatureDefinitions(dataSetID, featureCodes);
        prepareGetFeatureVocabularyTerms(dataSetID);
        context.checking(new Expectations()
            {
                {
                    one(dao).getContainerById(100 + dataSetID);
                    will(returnValue(new ImgContainerDTO("12-34", 1, 2, 0)));

                    one(service).tryToGetSampleIdentifier("12-34");
                    will(returnValue(new SampleIdentifier(new SpaceIdentifier("1", "S"), "P1")));

                    for (String code : featureCodes)
                    {
                        one(dao).getFeatureValues(new ImgFeatureDefDTO(code, code, "", 0));
                        int offset = Integer.parseInt(code, 16);
                        PlateFeatureValues array =
                                new PlateFeatureValues(NativeTaggedArray
                                        .toByteArray(new MDFloatArray(new float[][]
                                            {
                                                { 3.5f * dataSetID + offset },
                                                { 1.25f * dataSetID + offset } })));
                        will(returnValue(Arrays
                                .asList(new ImgFeatureValuesDTO(0.0, 0.0, array, 0L))));
                    }
                }
            });
    }

    private void prepareGetFeatureDefinitions(final long dataSetID, final String... featureCodes)
    {
        context.checking(new Expectations()
            {
                {
                    String permID = "ds" + dataSetID;
                    one(dao).tryGetDatasetByPermId(permID);
                    ImgDatasetDTO dataSet =
                            new ImgDatasetDTO(permID, null, null, 100 + dataSetID, false);
                    dataSet.setId(dataSetID);
                    will(returnValue(dataSet));

                    one(dao).listFeatureDefsByDataSetId(dataSetID);
                    List<ImgFeatureDefDTO> defs = new ArrayList<ImgFeatureDefDTO>();
                    for (String code : featureCodes)
                    {
                        defs.add(new ImgFeatureDefDTO(code, code, "", 0));
                    }
                    will(returnValue(defs));
                }
            });
    }

    private void prepareGetFeatureVocabularyTerms(final long dataSetID)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).listFeatureVocabularyTermsByDataSetId(dataSetID);
                    will(returnValue(new ArrayList<ImgFeatureVocabularyTermDTO>()));
                }
            });
    }

    private void prepareAssetDataSetsAreAccessible()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).checkDataSetCollectionAccess(SESSION_TOKEN,
                            Arrays.asList("ds1", "ds2"));
                }
            });
    }

    private FeatureVectorDatasetReference createFeatureVectorDatasetReference(String dataSetCode)
    {
        return new FeatureVectorDatasetReference(dataSetCode, "", null, null, null, null, null);
    }

    // Used for the authorization test
    private static class TestMethodInterceptor extends DssServiceRpcAuthorizationMethodInterceptor
            implements MethodInterceptor
    {
        private boolean methodInvoked = false;

        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable
        {
            Object result = super.invoke(methodInvocation);
            methodInvoked = true;
            return result;
        }
    }

    private IDssServiceRpcScreeningInternal getAdvisedService(
            TestMethodInterceptor testMethodInterceptor)
    {
        ProxyFactory pf = new ProxyFactory();
        pf.addAdvisor(new DssServiceRpcAuthorizationAdvisor(testMethodInterceptor));
        pf.setTarget(screeningService);
        pf.addInterface(IDssServiceRpcScreeningInternal.class);
        return (IDssServiceRpcScreeningInternal) pf.getProxy();
    }
}
