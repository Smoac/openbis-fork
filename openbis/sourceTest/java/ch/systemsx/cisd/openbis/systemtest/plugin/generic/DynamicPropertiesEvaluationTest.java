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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Date;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Tests that dynamic properties are evaluated after certain save/update operations.
 * 
 * @author Piotr Buczek
 */
@Test(groups = "system test")
public class DynamicPropertiesEvaluationTest extends GenericSystemTestCase
{
    private static final String CELL_PLATE = "CELL_PLATE";

    private static final String DESCRIPTION = "DESCRIPTION";

    private TechId createdSampleId = null;

    private NewETPTAssignment createDynamicPropertyAssignment(final EntityKind entityKind,
            final String propertyTypeCode, String entityTypeCode, String script)
    {
        final boolean mandatory = false;
        final String defaultValue = null;
        final String section = null;
        final Long ordinal = 0L;
        final boolean dynamic = true;
        return new NewETPTAssignment(entityKind, propertyTypeCode, entityTypeCode, mandatory,
                defaultValue, section, ordinal, dynamic, script);
    }

    @Test
    public void testRegisterDynamicPropertyAssignment()
    {
        logIntoCommonClientService();

        final EntityKind entityKind = EntityKind.SAMPLE;
        final String propertyTypeCode = DESCRIPTION;
        final String entityTypeCode = CELL_PLATE;
        final String script = "code";
        NewETPTAssignment assignment =
                createDynamicPropertyAssignment(entityKind, propertyTypeCode, entityTypeCode,
                        script);
        commonClientService.assignPropertyType(assignment);

        // properties should be evaluated asynchronously - check values after a few seconds
        sleep(3000);

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getSampleType(CELL_PLATE));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertTrue(samples.getResultSet().getTotalLength() > 0);
        assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();
        for (GridRowModel<Sample> gridRowModel : list)
        {
            Sample sample = gridRowModel.getOriginalObject();
            boolean found = false;
            for (IEntityProperty property : sample.getProperties())
            {
                if (property.getPropertyType().getCode().equals(propertyTypeCode))
                {
                    assertEquals(sample.getCode(), property.getValue());
                    found = true;
                    break;
                }
            }
            assertTrue(
                    "property " + propertyTypeCode + " not found for sample " + sample.getCode(),
                    found);
        }
    }

    @Test(dependsOnMethods = "testRegisterDynamicPropertyAssignment")
    public void testCreateSampleWithDynamicProperty()
    {
        logIntoCommonClientService();

        // register new cell plate sample
        final NewSample newSample = new NewSample();
        final String sampleCode = "NEW_CELL_PLATE_SAMPLE";
        final String identifier = "/CISD/" + sampleCode;
        newSample.setIdentifier(identifier);
        final SampleType sampleType = new SampleType();
        sampleType.setCode(CELL_PLATE);
        newSample.setSampleType(sampleType);
        genericClientService.registerSample("session", newSample);

        // properties should be evaluated asynchronously - check values after a few seconds
        sleep(1000);
        Sample loadedSample = getSpaceSample(identifier);
        createdSampleId = TechId.create(loadedSample);
        boolean found = false;
        for (IEntityProperty property : loadedSample.getProperties())
        {
            if (property.getPropertyType().getCode().equals(DESCRIPTION))
            {
                assertEquals(sampleCode, loadedSample.getCode());
                assertEquals(sampleCode, property.getValue());
                found = true;
                break;
            }
        }
        assertTrue("property " + DESCRIPTION + " not found for sample " + loadedSample.getCode(),
                found);
    }

    @Test(dependsOnMethods = "testCreateSampleWithDynamicProperty")
    public void testUpdateDynamicPropertyAssignment()
    {
        logIntoCommonClientService();

        final EntityKind entityKind = EntityKind.SAMPLE;
        final String propertyTypeCode = DESCRIPTION;
        final String entityTypeCode = CELL_PLATE;
        final String script = "date"; // different script
        NewETPTAssignment assignmentUpdates =
                createDynamicPropertyAssignment(entityKind, propertyTypeCode, entityTypeCode,
                        script);

        // properties should be evaluated asynchronously - check values after a few seconds
        final Date dateBefore = new Date();
        commonClientService.updatePropertyTypeAssignment(assignmentUpdates);
        sleep(3000);
        final Date dateAfter = new Date();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getSampleType(CELL_PLATE));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertTrue(samples.getResultSet().getTotalLength() > 0);
        assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();
        for (GridRowModel<Sample> gridRowModel : list)
        {
            Sample sample = gridRowModel.getOriginalObject();
            boolean found = false;
            for (IEntityProperty property : sample.getProperties())
            {
                if (property.getPropertyType().getCode().equals(propertyTypeCode))
                {
                    assertTrue(dateBefore.getTime() < Long.parseLong(property.getValue()));
                    assertTrue(dateAfter.getTime() > Long.parseLong(property.getValue()));
                    found = true;
                    break;
                }
            }
            assertTrue(
                    "property " + propertyTypeCode + " not found for sample " + sample.getCode(),
                    found);
        }
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

    private Sample getSample(String sampleIdentifier, ListSampleCriteria listCriteria)
    {
        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        GridRowModels<Sample> list = samples.getResultSet().getList();
        for (GridRowModel<Sample> gridRowModel : list)
        {
            Sample sample = gridRowModel.getOriginalObject();
            if (sample.getIdentifier().endsWith(sampleIdentifier.toUpperCase()))
            {
                return sample;
            }
        }
        fail("No sample of type found for identifier " + sampleIdentifier);
        return null; // satisfy compiler
    }

    private Sample getSpaceSample(String sampleIdentifier)
    {
        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        return getSample(sampleIdentifier, listCriteria);
    }

    @AfterClass
    public void cleanup()
    {
        commonClientService.unassignPropertyType(EntityKind.SAMPLE, DESCRIPTION, CELL_PLATE);
        if (createdSampleId != null)
        {
            commonClientService.deleteSample(createdSampleId, "test cleanup");
        }
    }
}
