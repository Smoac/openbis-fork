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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.convert.NativeTaggedArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
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

    private IImagingQueryDAO dao;

    private IFeatureVectorDatasetIdentifier featureVectorDatasetIdentifier1;

    private IFeatureVectorDatasetIdentifier featureVectorDatasetIdentifier2;

    private DssServiceRpcScreening screeningService;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dao = context.mock(IImagingQueryDAO.class);
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
    public void testLoadFeatures()
    {
        prepareAssetDataSetsAreAccessible();
        FeatureVectorDatasetReference r1 = createFeatureVectorDatasetReference("ds1");
        FeatureVectorDatasetReference r2 = createFeatureVectorDatasetReference("ds2");
        prepareCreateFeatureVectorDataSet(1, "f1", "f2");
        prepareCreateFeatureVectorDataSet(2, "f2");

        List<FeatureVectorDataset> dataSets =
                screeningService.loadFeatures(SESSION_TOKEN, Arrays.asList(r1, r2), Arrays.asList(
                        "f1", "f2"));

        assertSame(r1, dataSets.get(0).getDataset());
        assertEquals("[f1, f2]", dataSets.get(0).getFeatureNames().toString());
        assertFeatureVector(1, 1, dataSets.get(0).getFeatureVectors().get(0), 244.5, 245.5);
        assertFeatureVector(1, 2, dataSets.get(0).getFeatureVectors().get(1), 242.25, 243.25);
        assertEquals(2, dataSets.get(0).getFeatureVectors().size());
        assertSame(r2, dataSets.get(1).getDataset());
        assertEquals("[f1, f2]", dataSets.get(1).getFeatureNames().toString());
        assertFeatureVector(1, 1, dataSets.get(1).getFeatureVectors().get(0), Float.NaN, 249.0);
        assertFeatureVector(1, 2, dataSets.get(1).getFeatureVectors().get(1), Float.NaN, 244.5);
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
            final String... featureNames)
    {
        prepareGetFeatureDefinitions(dataSetID, featureNames);
        context.checking(new Expectations()
            {
                {
                    one(dao).getContainerById(100 + dataSetID);
                    will(returnValue(new ImgContainerDTO("12-34", 1, 2, 0)));

                    one(service).tryToGetSampleIdentifier("12-34");
                    will(returnValue(new SampleIdentifier(new SpaceIdentifier("1", "S"), "P1")));

                    for (String name : featureNames)
                    {
                        one(dao).getFeatureValues(new ImgFeatureDefDTO(name, "", 0));
                        int offset = Integer.parseInt(name, 16);
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

    private void prepareGetFeatureDefinitions(final long dataSetID, final String... featureNames)
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
                    for (String name : featureNames)
                    {
                        defs.add(new ImgFeatureDefDTO(name, "", 0));
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
}
