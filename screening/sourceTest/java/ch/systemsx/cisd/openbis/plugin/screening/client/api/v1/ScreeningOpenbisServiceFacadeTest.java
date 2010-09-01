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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.io.ByteArrayBasedContent;
import ch.systemsx.cisd.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.screening.server.DssServiceRpcScreening;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.server.ScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DssServiceRpcScreeningHolder.class)
public class ScreeningOpenbisServiceFacadeTest extends AssertJUnit
{
    private static final String DATA_SET1 = "ds1";

    private static final String DATA_SET2 = "ds2";

    private static final String URL1 = "url1";

    private static final String URL2 = "url2";

    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private IScreeningApiServer screeningService;

    private IDssServiceFactory dssServiceFactory;

    private ScreeningOpenbisServiceFacade facade;

    private ImageDatasetReference i1id;

    private ImageDatasetReference i2id;

    private IFeatureVectorDatasetIdentifier f1id;

    private IFeatureVectorDatasetIdentifier f2id;

    private IDssServiceRpcScreening dssService1;

    private IDssServiceRpcScreening dssService2;

    private IImageOutputStreamProvider outputStreamProvider;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        screeningService = context.mock(IScreeningApiServer.class);
        dssServiceFactory = context.mock(IDssServiceFactory.class);
        i1id = new ImageDatasetReference(DATA_SET1, URL1, null, null, null, null);
        i2id = new ImageDatasetReference(DATA_SET2, URL2, null, null, null, null);
        f1id = context.mock(IFeatureVectorDatasetIdentifier.class, "f1id");
        f2id = context.mock(IFeatureVectorDatasetIdentifier.class, "f2id");
        dssService1 = context.mock(IDssServiceRpcScreening.class, "dss1");
        dssService2 = context.mock(IDssServiceRpcScreening.class, "dss2");
        outputStreamProvider =
                context.mock(ScreeningOpenbisServiceFacade.IImageOutputStreamProvider.class);
        context.checking(new Expectations()
            {
                {
                    allowing(dssService1).getMajorVersion();
                    will(returnValue(ScreeningOpenbisServiceFacade.MAJOR_VERSION_DSS));

                    allowing(dssService1).getMinorVersion();
                    will(returnValue(DssServiceRpcScreening.MINOR_VERSION));

                    allowing(dssService2).getMajorVersion();
                    will(returnValue(ScreeningOpenbisServiceFacade.MAJOR_VERSION_DSS));

                    allowing(dssService2).getMinorVersion();
                    will(returnValue(DssServiceRpcScreening.MINOR_VERSION));
                }
            });
        context.checking(new Expectations()
            {
                {
                    allowing(f1id).getDatastoreServerUrl();
                    will(returnValue(URL1));

                    allowing(dssServiceFactory).createDssService(URL1);
                    will(returnValue(new DssServiceRpcScreeningHolder(URL1, dssService1)));

                    allowing(f2id).getDatastoreServerUrl();
                    will(returnValue(URL2));

                    allowing(dssServiceFactory).createDssService(URL2);
                    will(returnValue(new DssServiceRpcScreeningHolder(URL2, dssService2)));
                }
            });
        facade =
                new ScreeningOpenbisServiceFacade(SESSION_TOKEN, screeningService,
                        ScreeningServer.MINOR_VERSION, dssServiceFactory);
    }

    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAvailableFeatureCodes()
    {
        context.checking(new Expectations()
            {
                {
                    one(dssService1).listAvailableFeatureNames(SESSION_TOKEN, Arrays.asList(f1id));
                    will(returnValue(Arrays.asList("f1", "f2")));

                    one(dssService2).listAvailableFeatureNames(SESSION_TOKEN, Arrays.asList(f2id));
                    will(returnValue(Arrays.asList("f2", "f3")));
                }
            });
        List<String> names = facade.listAvailableFeatureCodes(Arrays.asList(f1id, f2id));
        Collections.sort(names);

        assertEquals("[f1, f2, f3]", names.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadFeatures()
    {
        final List<String> featureNames = Arrays.asList("A", "B");
        final FeatureVectorDatasetReference r1 =
                new FeatureVectorDatasetReference(DATA_SET1, URL1, null, null, null, null, i1id);
        final FeatureVectorDatasetReference r2 =
                new FeatureVectorDatasetReference(DATA_SET2, URL2, null, null, null, null, i2id);
        final FeatureVectorDataset ds1 = new FeatureVectorDataset(r1, null, null, null);
        final FeatureVectorDataset ds2 = new FeatureVectorDataset(r2, null, null, null);
        final FeatureVectorDataset ds3 = new FeatureVectorDataset(r2, null, null, null);
        context.checking(new Expectations()
            {
                {
                    one(dssService1).loadFeatures(SESSION_TOKEN, Arrays.asList(r1), featureNames);
                    will(returnValue(Arrays.asList(ds1)));

                    one(dssService2).loadFeatures(SESSION_TOKEN, Arrays.asList(r2), featureNames);
                    will(returnValue(Arrays.asList(ds2, ds3)));
                }
            });

        List<FeatureVectorDataset> features =
                facade.loadFeatures(Arrays.asList(r1, r2), featureNames);

        assertSame(ds1, features.get(0));
        assertSame(ds2, features.get(1));
        assertSame(ds3, features.get(2));
        assertEquals(3, features.size());

        context.assertIsSatisfied();
    }

    @Test
    public void testListPlateWells()
    {
        final String geneCode = "MYGENE";
        final MaterialIdentifier materialIdentifier =
                new MaterialIdentifier(MaterialTypeIdentifier.GENE, geneCode);
        final PlateWellReferenceWithDatasets pwRef =
                new PlateWellReferenceWithDatasets(new Plate(null, null, null, ExperimentIdentifier
                        .createFromPermId(null)), new WellPosition(1, 2));
        context.checking(new Expectations()
            {
                {
                    one(screeningService).listPlateWells(SESSION_TOKEN, materialIdentifier, false);
                    will(returnValue(Arrays.asList(pwRef)));
                }
            });

        final List<PlateWellReferenceWithDatasets> ref =
                facade.listPlateWells(materialIdentifier, false);
        assertEquals(1, ref.size());
        assertEquals(pwRef, ref.get(0));

        context.assertIsSatisfied();
    }

    @Test
    public void testLoadImages() throws IOException
    {
        final PlateImageReference r1 = new PlateImageReference(1, 2, 1, "c1", i1id);
        final PlateImageReference r2 = new PlateImageReference(12, 22, 1, "c1", i2id);
        final ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        final ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        context.checking(new Expectations()
            {
                {
                    one(dssService1).loadImages(SESSION_TOKEN, Arrays.asList(r1));
                    ConcatenatedContentInputStream s1 =
                            new ConcatenatedContentInputStream(true, Arrays
                                    .<IContent> asList(new ByteArrayBasedContent("hello 1"
                                            .getBytes(), "h1")));
                    will(returnValue(s1));

                    one(outputStreamProvider).getOutputStream(r1);
                    will(returnValue(stream1));

                    one(dssService2).loadImages(SESSION_TOKEN, Arrays.asList(r2));
                    ConcatenatedContentInputStream s2 =
                            new ConcatenatedContentInputStream(true, Arrays
                                    .<IContent> asList(new ByteArrayBasedContent("hello 2"
                                            .getBytes(), "h2")));
                    will(returnValue(s2));

                    one(outputStreamProvider).getOutputStream(r2);
                    will(returnValue(stream2));
                }
            });

        facade.loadImages(Arrays.asList(r1, r2), outputStreamProvider);

        assertEquals("hello 1", stream1.toString());
        assertEquals("hello 2", stream2.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testListImageMetaData()
    {
        List<String> channelCodes = Arrays.asList("channel1");
        List<String> channelLabels = Arrays.asList("Channel 1");
        final ImageDatasetMetadata m1 =
                new ImageDatasetMetadata(i1id, channelCodes, channelLabels, 1, 1, 1);
        final ImageDatasetMetadata m2 =
                new ImageDatasetMetadata(i1id, channelCodes, channelLabels, 1, 1, 1);
        final ImageDatasetMetadata m3 =
                new ImageDatasetMetadata(i2id, channelCodes, channelLabels, 1, 1, 1);
        context.checking(new Expectations()
            {
                {
                    one(dssService1).listImageMetadata(SESSION_TOKEN, Arrays.asList(i1id));
                    will(returnValue(Arrays.asList(m1, m2)));

                    one(dssService2).listImageMetadata(SESSION_TOKEN, Arrays.asList(i2id));
                    will(returnValue(Arrays.asList(m3)));
                }
            });

        List<ImageDatasetMetadata> metaData = facade.listImageMetadata(Arrays.asList(i1id, i2id));

        assertSame(m1, metaData.get(0));
        assertSame(m2, metaData.get(1));
        assertSame(m3, metaData.get(2));
        assertEquals(3, metaData.size());
        context.assertIsSatisfied();
    }
}
