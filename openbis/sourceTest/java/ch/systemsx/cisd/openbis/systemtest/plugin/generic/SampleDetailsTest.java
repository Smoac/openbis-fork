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

package ch.systemsx.cisd.openbis.systemtest.plugin.generic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.SampleBrowserTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewerTest;

/**
 * Headless system test counterpart to {@link GenericSampleViewerTest}. Not included:
 * <ul>
 * <li>loading of sample children
 * <li>loading of sample components
 * <li>detailed test of indirectly connected data sets
 * </ul>
 * 
 * @see SampleBrowserTest
 * @author Piotr Buczek
 */
@Test(groups = "system test")
public class SampleDetailsTest extends GenericSystemTestCase
{

    private static final String CISD_ID_PREFIX = "CISD:/CISD/";

    private static final String DEFAULT_INSTANCE = "CISD";

    private static final String DEFAULT_GROUP = "CISD";

    private static final String PERMLINK_TEMPLATE =
            "http://localhost/openbis/index.html?viewMode=simple#entity=SAMPLE&permId=%s";

    private static final String CONTROL_LAYOUT_EXAMPLE = "CL1";

    private static final String CELL_PLATE_EXAMPLE = "3VCP1";

    private static final String CELL_PLATE_EXAMPLE_ID = CISD_ID_PREFIX + CELL_PLATE_EXAMPLE;

    private static final String CELL_PLATE_EXAMPLE_EXPERIMENT_ID = "/CISD/NEMO/EXP1";

    private static final String CONTROL_LAYOUT_EXAMPLE_PERM_ID = "200811050919915-8";

    private static final String CELL_PLATE_EXAMPLE_PERM_ID = "200811050946559-983";

    private static final String CONTROL_LAYOUT_EXAMPLE_PERMLINK =
            String.format(PERMLINK_TEMPLATE, CONTROL_LAYOUT_EXAMPLE_PERM_ID);

    private static final String CELL_PLATE_EXAMPLE_PERMLINK =
            String.format(PERMLINK_TEMPLATE, CELL_PLATE_EXAMPLE_PERM_ID);

    private static final String DIRECTLY_CONNECTED_DATA_SET_CODE = "20081105092158673-1";

    private static final String INDIRECTLY_CONNECTED_DATA_SET_CODE = "20081105092159188-3";

    private static final String DEFAULT_DATA_SET_TYPE = "HCS_IMAGE";

    @Test
    public void testGetMasterPlateDetails()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getSampleType("CONTROL_LAYOUT"));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(6, samples.getResultSet().getTotalLength());
        assertEquals("[CONTROL_LAYOUT]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        Sample sListed = getSample(list, createSampleIdentifier(CONTROL_LAYOUT_EXAMPLE));
        Sample sDetails =
                genericClientService.getSampleGenerationInfo(TechId.create(sListed)).getParent();
        assertEquals(sListed.getIdentifier(), sDetails.getIdentifier());
        assertEquals(sListed.getPermId(), sDetails.getPermId());
        assertEquals(CONTROL_LAYOUT_EXAMPLE_PERM_ID, sDetails.getPermId());
        assertEquals(sListed.getPermlink(), sDetails.getPermlink());
        assertEquals(CONTROL_LAYOUT_EXAMPLE_PERMLINK, sDetails.getPermlink());
        assertEquals(sListed.getSampleType(), sDetails.getSampleType());
        assertEquals("CONTROL_LAYOUT", sDetails.getSampleType().getCode());
        assertEquals(sListed.getRegistrator().toString(), sDetails.getRegistrator().toString());
        assertEquals("John Doe", sDetails.getRegistrator().toString());
        assertEquals(sListed.getProperties().size(), sDetails.getProperties().size());
        checkInternalProperty(sDetails.getProperties(), "PLATE_GEOMETRY", "384_WELLS_16X24");
        checkUserProperty(sDetails.getProperties(), "DESCRIPTION", "test control layout");
    }

    @Test
    public void testGetCellPlateDetails()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getSampleType("CELL_PLATE"));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(17, samples.getResultSet().getTotalLength());
        assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        Sample sListed = getSample(list, createSampleIdentifier(CELL_PLATE_EXAMPLE));
        Sample sDetails =
                genericClientService.getSampleGenerationInfo(TechId.create(sListed)).getParent();
        assertEquals(sListed.getIdentifier(), sDetails.getIdentifier());
        assertEquals(sListed.getPermId(), sDetails.getPermId());
        assertEquals(CELL_PLATE_EXAMPLE_PERM_ID, sDetails.getPermId());
        assertEquals(sListed.getPermlink(), sDetails.getPermlink());
        assertEquals(CELL_PLATE_EXAMPLE_PERMLINK, sDetails.getPermlink());
        assertEquals(sListed.getSampleType(), sDetails.getSampleType());
        assertEquals("CELL_PLATE", sDetails.getSampleType().getCode());
        assertEquals(sListed.getRegistrator().toString(), sDetails.getRegistrator().toString());
        assertEquals("John Doe", sDetails.getRegistrator().toString());

        assertEquals(sListed.getExperiment().getIdentifier(), sDetails.getExperiment()
                .getIdentifier());
        assertEquals(CELL_PLATE_EXAMPLE_EXPERIMENT_ID, sDetails.getExperiment().getIdentifier());

        assertEquals("Doe", sDetails.getInvalidation().getRegistrator().getLastName());
        assertEquals("wrong-code", sDetails.getInvalidation().getReason());

        assertEquals(1, sDetails.getParents().size());
        final Sample parent = sDetails.getParents().iterator().next();
        assertEquals("3V-123", parent.getCode());
        assertNotNull(parent.getInvalidation());

        assertEquals(sListed.getProperties().size(), sDetails.getProperties().size());

    }

    @Test
    public void testGetSampleDataSets()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getSampleType("CELL_PLATE"));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(15, samples.getResultSet().getTotalLength());
        assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        Sample sample = getSample(list, createSampleIdentifier(CELL_PLATE_EXAMPLE));

        // directly connected
        boolean showOnlyDirectlyConnected = true;

        final ResultSetWithEntityTypes<ExternalData> directlyConnectedResults =
                commonClientService.listSampleDataSets(TechId.create(sample),
                        DefaultResultSetConfig.<String, ExternalData> createFetchAll(),
                        showOnlyDirectlyConnected);

        assertEquals(1, directlyConnectedResults.getAvailableEntityTypes().size());
        assertEquals(DEFAULT_DATA_SET_TYPE, directlyConnectedResults.getAvailableEntityTypes()
                .iterator().next().getCode());
        assertEquals(1, directlyConnectedResults.getResultSet().getTotalLength());
        final ExternalData directlyConnectedDataSet =
                getDataSet(directlyConnectedResults.getResultSet().getList(),
                        DIRECTLY_CONNECTED_DATA_SET_CODE);
        checkDataSet(directlyConnectedDataSet, DIRECTLY_CONNECTED_DATA_SET_CODE,
                CELL_PLATE_EXAMPLE_ID, CELL_PLATE_EXAMPLE_EXPERIMENT_ID, "TIFF", "xxx/yyy/zzz");

        // indirectly connected
        showOnlyDirectlyConnected = false;
        ResultSetWithEntityTypes<ExternalData> indirectlyConnectedResults =
                commonClientService.listSampleDataSets(TechId.create(sample),
                        DefaultResultSetConfig.<String, ExternalData> createFetchAll(),
                        showOnlyDirectlyConnected);

        assertEquals(1, indirectlyConnectedResults.getAvailableEntityTypes().size());
        assertEquals(DEFAULT_DATA_SET_TYPE, indirectlyConnectedResults.getAvailableEntityTypes()
                .iterator().next().getCode());
        assertEquals(6, indirectlyConnectedResults.getResultSet().getTotalLength());

        final ExternalData directlyConnectedDataSet2 =
                getDataSet(directlyConnectedResults.getResultSet().getList(),
                        DIRECTLY_CONNECTED_DATA_SET_CODE);
        checkDataSet(directlyConnectedDataSet2, directlyConnectedDataSet.getCode(),
                directlyConnectedDataSet.getSample().getIdentifier(), directlyConnectedDataSet
                        .getExperiment().getIdentifier(), directlyConnectedDataSet
                        .getFileFormatType().getCode(), directlyConnectedDataSet.getLocation());

        final ExternalData indirectlyConnectedDataSet =
                getDataSet(indirectlyConnectedResults.getResultSet().getList(),
                        INDIRECTLY_CONNECTED_DATA_SET_CODE);
        checkDataSet(indirectlyConnectedDataSet, INDIRECTLY_CONNECTED_DATA_SET_CODE, null,
                CELL_PLATE_EXAMPLE_EXPERIMENT_ID, "3VPROPRIETARY", "analysis/result");
        // TODO 2010-21-09, Piotr Buczek: check datasets connected to a different experiment
    }

    private void checkUserProperty(List<IEntityProperty> properties, String propertyCode,
            String propertyValue)
    {
        checkProperty(properties, false, propertyCode, propertyValue);
    }

    private void checkInternalProperty(List<IEntityProperty> properties, String propertyCode,
            String propertyValue)
    {
        checkProperty(properties, true, propertyCode, propertyValue);
    }

    private void checkProperty(List<IEntityProperty> properties, boolean internalNamespace,
            String propertyCode, String propertyValue)
    {
        String fullPropertyCode = internalNamespace ? "$" + propertyCode : propertyCode;
        for (IEntityProperty property : properties)
        {
            if (property.getPropertyType().getCode().equals(fullPropertyCode))
            {
                assertEquals(property.tryGetAsString(), propertyValue);
                return;
            }
        }
        fail("No property found with code " + fullPropertyCode);
    }

    private static String createSampleIdentifier(String sampleCode)
    {
        return createSampleIdentifier(DEFAULT_INSTANCE, DEFAULT_GROUP, sampleCode);
    }

    private static String createSampleIdentifier(String instanceCode, String spaceCode,
            String sampleCode)
    {
        return instanceCode + ":/" + (spaceCode == null ? "" : spaceCode + "/") + sampleCode;
    }

    private SampleType getSampleType(String sampleTypeCode)
    {
        List<SampleType> sampleTypes = commonClientService.listSampleTypes();
        for (SampleType sampleType : sampleTypes)
        {
            if (sampleType.getCode().equals(sampleTypeCode))
            {
                return sampleType;
            }
        }
        fail("No sample type found with code " + sampleTypeCode);
        return null; // satisfy compiler
    }

    private static final boolean DEBUG = false;

    private static Sample getSample(GridRowModels<Sample> list, String identifier)
    {
        for (GridRowModel<Sample> gridRowModel : list)
        {
            Sample sample = gridRowModel.getOriginalObject();
            if (DEBUG)
            {
                System.out.println(sample.getIdentifier());
            }
            if (sample.getIdentifier().equals(identifier))
            {
                return sample;
            }
        }
        fail("No sample found for identifier " + identifier);
        return null; // satisfy compiler
    }

    private static ExternalData getDataSet(GridRowModels<ExternalData> list, String identifier)
    {
        for (GridRowModel<ExternalData> gridRowModel : list)
        {
            ExternalData externalData = gridRowModel.getOriginalObject();
            if (DEBUG)
            {
                System.out.println(externalData.getIdentifier());
            }
            if (externalData.getIdentifier().equals(identifier))
            {
                return externalData;
            }
        }
        fail("No data set found for identifier " + identifier);
        return null; // satisfy compiler
    }

    private static void checkDataSet(ExternalData dataSet, String expectedCode,
            String expectedSampleIdentifierOrNull, String expectedExperimentIdentifier,
            String expectedFileFormatType, String expectedLocation)
    {
        assertEquals(expectedCode, dataSet.getCode());
        if (expectedSampleIdentifierOrNull == null)
        {
            assertEquals(null, dataSet.getSample());
        } else
        {
            assertEquals(expectedSampleIdentifierOrNull, dataSet.getSample().getIdentifier());
        }
        assertEquals(expectedExperimentIdentifier, dataSet.getExperiment().getIdentifier());
        assertEquals(expectedFileFormatType, dataSet.getFileFormatType().getCode());
        assertEquals(expectedLocation, dataSet.getLocation());
    }

}
