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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ScreeningApiImplTest extends AbstractServerTestCase
{
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
    public void testListImageDatasets()
    {
        final PlateIdentifier pi1 = new PlateIdentifier("p1", null);
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(asSampleIdentifier(pi1));
                    one(sampleBO).getSample();
                    SamplePE p1 = plate(pi1, "384_WELLS_16X24");
                    will(returnValue(p1));

                    one(externalDataDAO).listExternalData(p1);
                    will(returnValue(Arrays.asList(imageDataSet(p1, "1"), imageAnalysisDataSet(p1,
                            "2"))));
                }
            });

   List<ImageDatasetReference> dataSets = screeningApi.listImageDatasets(Arrays.asList(pi1));
        
        assertEquals("1", dataSets.get(0).getDatasetCode());
        assertEquals(new Geometry(16, 24), dataSets.get(0).getPlateGeometry());
        assertEquals(new Date(100), dataSets.get(0).getRegistrationDate());
        assertEquals(SERVER_URL, dataSets.get(0).getDatastoreServerUrl());
        assertEquals(pi1, dataSets.get(0).getPlate());
        assertEquals(1, dataSets.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListImageDatasetsWithMissingPlateGeometry()
    {
        final PlateIdentifier pi1 = new PlateIdentifier("p1", null);
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(asSampleIdentifier(pi1));
                    one(sampleBO).getSample();
                    SamplePE p1 = plate(pi1, null);
                    will(returnValue(p1));

                    one(externalDataDAO).listExternalData(p1);
                    will(returnValue(Arrays.asList(imageDataSet(p1, "1"))));
                }
            });

        try
        {
            screeningApi.listImageDatasets(Arrays.asList(pi1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Sample p1 has no property " + ScreeningConstants.PLATE_GEOMETRY, ex
                    .getMessage());
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
    
    private void assertListImageDatasetsFailsFor(final String plateGeometry)
    {
        final PlateIdentifier pi1 = new PlateIdentifier("p1", null);
        context.checking(new Expectations()
        {
            {
                one(screeningBOFactory).createSampleBO(SESSION);
                will(returnValue(sampleBO));
                
                one(sampleBO).loadBySampleIdentifier(asSampleIdentifier(pi1));
                one(sampleBO).getSample();
                SamplePE p1 = plate(pi1, plateGeometry);
                will(returnValue(p1));
                
                one(externalDataDAO).listExternalData(p1);
                will(returnValue(Arrays.asList(imageDataSet(p1, "1"))));
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
    
    private SampleIdentifier asSampleIdentifier(PlateIdentifier plateIdentifier)
    {
        String spaceCode = plateIdentifier.tryGetSpaceCode();
        if (spaceCode == null)
        {
            return new SampleIdentifier(DatabaseInstanceIdentifier.createHome(), plateIdentifier
                    .getPlateCode());
        }
        return new SampleIdentifier(new SpaceIdentifier(DatabaseInstanceIdentifier.createHome(),
                spaceCode), plateIdentifier.getPlateCode());
    }
    
    private SamplePE plate(PlateIdentifier plateIdentifier, String plateGeometryOrNull)
    {
        SamplePE sample = new SamplePE();
        sample.setCode(plateIdentifier.getPlateCode());
        if (plateGeometryOrNull != null)
        {
            SamplePropertyPE property = new SamplePropertyPE();
            SampleTypePropertyTypePE etpt = new SampleTypePropertyTypePE();
            PropertyTypePE propertyType = new PropertyTypePE();
            propertyType.setCode(ScreeningConstants.PLATE_GEOMETRY);
            etpt.setPropertyType(propertyType);
            property.setEntityTypePropertyType(etpt);
            VocabularyTermPE term = new VocabularyTermPE();
            term.setCode(plateGeometryOrNull);
            property.setVocabularyTerm(term);
            sample.addProperty(property);
        }
        return sample;
    }

    private ExternalDataPE imageDataSet(SamplePE sample, String code)
    {
        ExternalDataPE dataSet = createDataSet(sample, code);
        dataSet.setDataSetType(dataSetType(ScreeningConstants.IMAGE_DATASET_TYPE));
        return dataSet;
    }

    private ExternalDataPE imageAnalysisDataSet(SamplePE sample, String code)
    {
        ExternalDataPE dataSet = createDataSet(sample, code);
        dataSet.setDataSetType(dataSetType(ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE));
        return dataSet;
    }
    
    private ExternalDataPE createDataSet(SamplePE sample, String code)
    {
        ExternalDataPE dataSet = new ExternalDataPE();
        dataSet.setCode(code);
        dataSet.setSample(sample);
        DataStorePE dataStorePE = new DataStorePE();
        dataStorePE.setDownloadUrl(SERVER_URL);
        dataSet.setDataStore(dataStorePE);
        dataSet.setRegistrationDate(new Date(Long.parseLong(code) * 100));
        return dataSet;
    }
    
    private DataSetTypePE dataSetType(String code)
    {
        DataSetTypePE type = new DataSetTypePE();
        type.setCode(code);
        return type;
    }
}
