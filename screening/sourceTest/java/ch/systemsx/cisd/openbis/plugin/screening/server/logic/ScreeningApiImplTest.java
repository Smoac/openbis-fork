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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * @author Franz-Josef Elmer
 */
public class ScreeningApiImplTest extends AbstractServerTestCase
{
    private static final String SERVER_DOWNLOAD_URL = "server-url/"
            + GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

    private static final String SERVER_URL = "server-url";

    private IScreeningBusinessObjectFactory screeningBOFactory;

    private ScreeningApiImpl screeningApi;

    @BeforeMethod
    public void beforeMethod()
    {
        screeningBOFactory = context.mock(IScreeningBusinessObjectFactory.class);
        screeningApi = new ScreeningApiImpl(SESSION, screeningBOFactory, daoFactory);
    }

    @Test
    public void testListPlateWellsFromAugmentedCode()
    {
        final String identifier = "/SPACE/PLATE";
        final PlateIdentifier pi = PlateIdentifier.createFromAugmentedCode(identifier);
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));
                    one(sampleBO).loadBySampleIdentifier(SampleIdentifierFactory.parse(identifier));
                    one(sampleBO).getSample();
                    SamplePE sample = new SamplePE();
                    sample.setId(1L);
                    will(returnValue(sample));

                    one(screeningBOFactory).createSampleLister(SESSION);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample w1 = createWellSample("w1", "A01");
                    Sample w2 = createWellSample("w2", "A02");
                    will(returnValue(Arrays.asList(w1, w2)));
                }
            });

        List<WellIdentifier> wells = screeningApi.listPlateWells(pi);
        assertEquals(2, wells.size());
        assertEquals(wellIdentifier(pi, "w1", 1, 1), wells.get(0));
        assertEquals(wellIdentifier(pi, "w2", 1, 2), wells.get(1));
        context.assertIsSatisfied();
    }

    @Test
    public void testListPlateWellsFromPermId()
    {
        final String permId = "PLATE_PERM_ID";
        final PlateIdentifier pi = PlateIdentifier.createFromPermId(permId);
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(SESSION);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample plate = new Sample();
                    plate.setId(1L);
                    will(returnValue(Arrays.asList(plate)));

                    one(screeningBOFactory).createSampleLister(SESSION);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample w1 = createWellSample("w1", "A01");
                    Sample w2 = createWellSample("w2", "A02");
                    will(returnValue(Arrays.asList(w1, w2)));
                }
            });

        List<WellIdentifier> wells = screeningApi.listPlateWells(pi);
        assertEquals(2, wells.size());
        assertEquals(wellIdentifier(pi, "w1", 1, 1), wells.get(0));
        assertEquals(wellIdentifier(pi, "w2", 1, 2), wells.get(1));
        context.assertIsSatisfied();
    }

    private static Sample createWellSample(String permId, String code)
    {
        Sample result = new Sample();
        result.setPermId(permId);
        result.setCode(code);
        return result;
    }

    private static WellIdentifier wellIdentifier(PlateIdentifier plate, String wellPermId, int row,
            int col)
    {
        return new WellIdentifier(plate, new WellPosition(row, col), wellPermId);
    }

    @Test
    public void testListImageDatasets()
    {
        final PlateIdentifier pi1 = createSharedPlate("p1");
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(SESSION);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample p1 = plateSample(pi1, "384_WELLS_16X24");
                    will(returnValue(Arrays.asList(p1)));

                    one(screeningBOFactory).createDatasetLister(SESSION);
                    will(returnValue(datasetLister));
                    one(datasetLister).listBySampleIds(with(Arrays.asList((long) 1)));
                    will(returnValue(Arrays.asList(imageDataSet(p1, "1", 1),
                            imageAnalysisDataSet(p1, "2", 2))));
                }
            });

        List<ImageDatasetReference> dataSets = screeningApi.listImageDatasets(Arrays.asList(pi1));
        assertEquals("1", dataSets.get(0).getDatasetCode());
        assertEquals(Geometry.createFromRowColDimensions(16, 24), dataSets.get(0)
                .getPlateGeometry());
        assertEquals(new Date(100), dataSets.get(0).getRegistrationDate());
        assertEquals(SERVER_URL, dataSets.get(0).getDatastoreServerUrl());
        // FIXME this check doesn't work because of space code (as well as 2 other cases below)
        // assertEquals(pi1, dataSets.get(0).getPlate());
        assertEquals(pi1.getPlateCode(), dataSets.get(0).getPlate().getPlateCode());
        assertEquals(1, dataSets.size());
        context.assertIsSatisfied();
    }

    private static PlateIdentifier createSharedPlate(String plateCode)
    {
        return PlateIdentifier.createFromAugmentedCode("/" + plateCode);
    }

    @Test
    public void testListImageDatasetsWithMissingPlateGeometry()
    {
        final PlateIdentifier pi1 = createSharedPlate("p1");
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(SESSION);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample p1 = plateSample(pi1, null);
                    will(returnValue(Arrays.asList(p1)));

                    one(screeningBOFactory).createDatasetLister(SESSION);
                    will(returnValue(datasetLister));
                    one(datasetLister).listBySampleIds(with(Arrays.asList((long) 1)));
                    will(returnValue(Arrays.asList(imageDataSet(p1, "1", 1))));
                }
            });

        try
        {
            screeningApi.listImageDatasets(Arrays.asList(pi1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Sample '/p1' has no property " + ScreeningConstants.PLATE_GEOMETRY,
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testListImageDatasetsWithPlateGeometryWithMissingUnderscore()
    {
        assertListImageDatasetsFailsFor("2X4");
        context.assertIsSatisfied();
    }

    @Test
    public void testListImageDatasetsWithPlateGeometryWithMissingX()
    {
        assertListImageDatasetsFailsFor("abc_2.4");
        context.assertIsSatisfied();
    }

    @Test
    public void testListImageDatasetsWithPlateGeometryWithWidthNotANumber()
    {
        assertListImageDatasetsFailsFor("abc_aX4");
        context.assertIsSatisfied();
    }

    @Test
    public void testListImageDatasetsWithPlateGeometryWithHeightNotANumber()
    {
        assertListImageDatasetsFailsFor("abc_2Xb");
        context.assertIsSatisfied();
    }

    @Test
    public void testListFeatureVectorDatasets()
    {
        final PlateIdentifier pi1 = createSharedPlate("p1");
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(SESSION);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample p1 = plateSample(pi1, "384_WELLS_16X24");
                    will(returnValue(Arrays.asList(p1)));

                    exactly(2).of(screeningBOFactory).createDatasetLister(SESSION);
                    will(returnValue(datasetLister));
                    long imageDatasetId = 1;
                    one(datasetLister).listBySampleIds(with(Arrays.asList(imageDatasetId)));
                    will(returnValue(Arrays.asList(
                            imageDataSet(p1, "" + imageDatasetId, imageDatasetId),
                            imageAnalysisDataSet(p1, "2", 2))));

                    one(datasetLister).listByParentTechIds(Arrays.asList(imageDatasetId));
                    long analysisDatasetId = 3;
                    will(returnValue(Arrays.asList(imageAnalysisDataSet(null, ""
                            + analysisDatasetId, analysisDatasetId))));

                    one(datasetLister).listParentIds(Arrays.asList(analysisDatasetId));
                    Map<Long, Set<Long>> parentToChildrenMap = new HashMap<Long, Set<Long>>();
                    parentToChildrenMap.put(analysisDatasetId,
                            new HashSet<Long>(Arrays.asList(imageDatasetId)));
                    will(returnValue(parentToChildrenMap));
                }
            });

        List<FeatureVectorDatasetReference> dataSets =
                screeningApi.listFeatureVectorDatasets(Arrays.asList(pi1));

        assertEquals(2, dataSets.size());

        assertEquals("2", dataSets.get(0).getDatasetCode());
        assertEquals(Geometry.createFromRowColDimensions(16, 24), dataSets.get(0)
                .getPlateGeometry());
        assertEquals(new Date(200), dataSets.get(0).getRegistrationDate());
        assertEquals(SERVER_URL, dataSets.get(0).getDatastoreServerUrl());
        // assertEquals(pi1, dataSets.get(0).getPlate());
        assertEquals(pi1.getPlateCode(), dataSets.get(0).getPlate().getPlateCode());

        assertEquals("3", dataSets.get(1).getDatasetCode());
        assertEquals(Geometry.createFromRowColDimensions(16, 24), dataSets.get(1)
                .getPlateGeometry());
        assertEquals(new Date(300), dataSets.get(1).getRegistrationDate());
        assertEquals(SERVER_URL, dataSets.get(1).getDatastoreServerUrl());
        assertEquals(pi1.getPlateCode(), dataSets.get(1).getPlate().getPlateCode());
        // assertEquals(pi1, dataSets.get(1).getPlate());
        context.assertIsSatisfied();
    }

    private void assertListImageDatasetsFailsFor(final String plateGeometry)
    {
        final PlateIdentifier pi1 = createSharedPlate("p1");
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(SESSION);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample p1 = plateSample(pi1, plateGeometry);
                    will(returnValue(Arrays.asList(p1)));

                    one(screeningBOFactory).createDatasetLister(SESSION);
                    will(returnValue(datasetLister));
                    one(datasetLister).listBySampleIds(with(Arrays.asList((long) 1)));
                    will(returnValue(Arrays.asList(imageDataSet(p1, "1", 1))));
                }
            });

        try
        {
            screeningApi.listImageDatasets(Arrays.asList(pi1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Invalid property " + ScreeningConstants.PLATE_GEOMETRY + ": "
                    + plateGeometry.toUpperCase(), ex.getMessage());
        }
    }

    private Sample plateSample(PlateIdentifier plateIdentifier, String plateGeometryOrNull)
    {
        Sample sample = new Sample();
        sample.setId((long) 1);
        sample.setCode(plateIdentifier.getPlateCode());
        sample.setIdentifier(plateIdentifier.toString());
        if (plateGeometryOrNull != null)
        {
            IEntityProperty property = new EntityProperty();
            PropertyType propertyType = new PropertyType();
            propertyType.setCode(ScreeningConstants.PLATE_GEOMETRY);
            property.setPropertyType(propertyType);
            VocabularyTerm term = new VocabularyTerm();
            term.setCode(plateGeometryOrNull.toUpperCase());
            property.setVocabularyTerm(term);
            sample.setProperties(Arrays.asList(property));
        } else
        {
            sample.setProperties(new ArrayList<IEntityProperty>());
        }
        return sample;
    }

    private ExternalData imageDataSet(Sample sample, String code, long id)
    {
        ExternalData dataSet = createDataSet(sample, code, id);
        dataSet.setDataSetType(dataSetType(ScreeningConstants.HCS_IMAGE_DATASET_TYPE));
        dataSet.setExperiment(new Experiment());
        dataSet.getExperiment().setProject(new Project());
        dataSet.getExperiment().getProject().setSpace(new Space());
        return dataSet;
    }

    private ExternalData imageAnalysisDataSet(Sample sample, String code, long id)
    {
        ExternalData dataSet = createDataSet(sample, code, id);
        dataSet.setDataSetType(dataSetType(ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE));
        dataSet.setExperiment(new Experiment());
        dataSet.getExperiment().setProject(new Project());
        dataSet.getExperiment().getProject().setSpace(new Space());
        return dataSet;
    }

    private ExternalData createDataSet(Sample sample, String code, long id)
    {
        ExternalData dataSet = new ExternalData();
        dataSet.setId(id);
        dataSet.setCode(code);
        dataSet.setSample(sample);
        DataStore dataStore = new DataStore();
        dataStore.setDownloadUrl(SERVER_DOWNLOAD_URL);
        dataStore.setHostUrl(SERVER_URL);
        dataSet.setDataStore(dataStore);
        dataSet.setRegistrationDate(new Date(Long.parseLong(code) * 100));
        return dataSet;
    }

    private DataSetType dataSetType(String code)
    {
        DataSetType type = new DataSetType();
        type.setCode(code);
        return type;
    }

}
