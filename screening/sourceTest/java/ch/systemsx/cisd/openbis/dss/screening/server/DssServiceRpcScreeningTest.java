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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.framework.ProxyFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.convert.NativeTaggedArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor.DssServiceRpcAuthorizationMethodInterceptor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreeningInternal;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * Test cases for the {@link DssServiceRpcScreening}.
 * 
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcScreeningTest extends AssertJUnit
{
    private static final String SESSION_TOKEN = "session";

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IImagingReadonlyQueryDAO dao;

    private IFeatureVectorDatasetIdentifier featureVectorDatasetIdentifier1;

    private IFeatureVectorDatasetIdentifier featureVectorDatasetIdentifier2;

    private DssServiceRpcScreening screeningService;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dao = context.mock(IImagingReadonlyQueryDAO.class);
        featureVectorDatasetIdentifier1 = create("ds1");
        featureVectorDatasetIdentifier2 = create("ds2");

        screeningService = new DssServiceRpcScreening("targets", dao, service, false);
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

    private void assertFeatureVector(int expectedRowNumber, int expectedColumnNumber,
            FeatureVector featureVector, double... expectedValues)
    {
        assertEquals(expectedRowNumber, featureVector.getWellPosition().getWellRow());
        assertEquals(expectedColumnNumber, featureVector.getWellPosition().getWellColumn());

        assertEquals(asList(expectedValues), asList(featureVector.getValues()));
    }

    private List<Double> asList(double[] values)
    {
        List<Double> list = new ArrayList<Double>();
        for (double value : values)
        {
            list.add(value);
        }
        return list;
    }

    private void prepareCreateFeatureVectorDataSet(final long dataSetID,
            final String... featureCodes)
    {
        prepareGetFeatureDefinitions(dataSetID, featureCodes);
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
