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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.SampleBrowserTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Headless system test counterpart to {@link SampleBrowserTest}. Not included tests:
 * <ul>
 * <li>testChangeColumnSettings()
 * <li>testExportMasterPlates()
 * <li>testExportCellPlates()
 * </ul>
 * 
 * @see SampleBrowserTest
 * @author Piotr Buczek
 */
@Test(groups = "system test")
public class SampleBrowsingTest extends GenericSystemTestCase
{
    private static final String DEFAULT_INSTANCE = "CISD";

    private static final String DEFAULT_GROUP = "CISD";

    private static final String DEFAULT_PLATE_GEOMETRY_VALUE = "384_WELLS_16X24";

    @Test
    public void testListAllSamples()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getAllSampleType());

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(42, samples.getResultSet().getTotalLength());
        assertEquals("[DILUTION_PLATE, REINFECT_PLATE, MASTER_PLATE, CONTROL_LAYOUT, CELL_PLATE]",
                samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        // Test that there are two samples displayed that have different types, and
        // have all properties even those that are assigned only to one of these types
        // (union of property values is displayed).

        // 'ORGANISM' is assigned only to 'CELL_PLATE' sample type
        Sample s1 = getSample(list, createSampleIdentifier("CP-TEST-1"));
        assertEquals("CELL_PLATE", s1.getSampleType().getCode());
        assertEquals(5, s1.getProperties().size());
        checkUserProperty(s1.getProperties(), "ORGANISM", "HUMAN");

        // 'PLATE_GEOMETRY' is assigned only to 'CONTROL_LAYOUT' and 'MASTER PLATE' sample types
        Sample s2 = getSample(list, createSampleIdentifier("C1"));
        assertEquals("CONTROL_LAYOUT", s2.getSampleType().getCode());
        assertEquals(1, s2.getProperties().size());
        checkInternalProperty(s2.getProperties(), "PLATE_GEOMETRY", DEFAULT_PLATE_GEOMETRY_VALUE);

        // test that 3 parents of a 'REINFECT_PLATE' are loaded
        Sample s3 = getSample(list, createSampleIdentifier("RP1-A2X"));
        assertEquals("REINFECT_PLATE", s3.getSampleType().getCode());
        assertEquals("CISD:/CISD/CP1-A2", s3.getGeneratedFrom().getIdentifier());
        assertEquals("CISD:/CISD/DP1-A", s3.getGeneratedFrom().getGeneratedFrom().getIdentifier());
        assertEquals("CISD:/CISD/MP1-MIXED", s3.getGeneratedFrom().getGeneratedFrom()
                .getGeneratedFrom().getIdentifier());
    }

    @Test
    public final void testListMasterPlates()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getSampleType("MASTER_PLATE"));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(5, samples.getResultSet().getTotalLength());
        assertEquals("[MASTER_PLATE]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        Sample s1 = getSample(list, createSampleIdentifier("MP001-1"));
        checkInternalProperty(s1.getProperties(), "PLATE_GEOMETRY", DEFAULT_PLATE_GEOMETRY_VALUE);
        assertNotNull(s1.getInvalidation());
        assertNull(s1.getExperiment());

        Sample s2 = getSample(list, createSampleIdentifier("MP002-1"));
        checkInternalProperty(s2.getProperties(), "PLATE_GEOMETRY", DEFAULT_PLATE_GEOMETRY_VALUE);
        assertNull(s2.getInvalidation());
        assertNull(s2.getExperiment());
    }

    @Test
    public final void testListSharedMasterPlates()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeInstance(true);
        listCriteria.setSampleType(getSampleType("MASTER_PLATE"));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(1, samples.getResultSet().getTotalLength());
        assertEquals("[MASTER_PLATE]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        Sample s = getSample(list, createSharedSampleIdentifier("MP"));
        checkInternalProperty(s.getProperties(), "PLATE_GEOMETRY", DEFAULT_PLATE_GEOMETRY_VALUE);
        assertNull(s.getInvalidation());
        assertNull(s.getExperiment());
    }

    @Test
    public final void testListCellPlates()
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

        Sample s = getSample(list, createSampleIdentifier("3VCP1"));
        assertNotNull(s.getInvalidation());
        assertEquals("/CISD/NEMO/EXP1", s.getExperiment().getIdentifier());
        assertEquals("CISD:/CISD/3V-123", s.getGeneratedFrom().getIdentifier());
        assertEquals("CISD:/CISD/MP001-1", s.getGeneratedFrom().getGeneratedFrom().getIdentifier());
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

    private static String createSharedSampleIdentifier(String sampleCode)
    {
        return createSampleIdentifier(DEFAULT_INSTANCE, null, sampleCode);
    }

    private static String createSampleIdentifier(String instanceCode, String spaceCode,
            String sampleCode)
    {
        return instanceCode + ":/" + (spaceCode == null ? "" : spaceCode + "/") + sampleCode;
    }

    private SampleType getAllSampleType()
    {
        SampleType result = new SampleType();
        result.setCode("(all)");
        return result;
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

    private static Sample getSample(GridRowModels<Sample> list, String identifier)
    {
        for (GridRowModel<Sample> gridRowModel : list)
        {
            Sample sample = gridRowModel.getOriginalObject();
            System.out.println(sample.getIdentifier());
            if (sample.getIdentifier().equals(identifier))
            {
                return sample;
            }
        }
        fail("No sample found for identifier " + identifier);
        return null; // satisfy compiler
    }

}
