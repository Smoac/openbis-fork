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

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
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

    @Test
    public void testAssignDynamicProperty()
    {
        logIntoCommonClientService();

        final EntityKind entityKind = EntityKind.SAMPLE;
        final String propertyTypeCode = DESCRIPTION;
        final String entityTypeCode = CELL_PLATE;
        final boolean mandatory = false;
        final String defaultValue = null;
        final String section = null;
        final Long ordinal = 0L;
        final boolean dynamic = true;
        final String script = "code";
        NewETPTAssignment assignment =
                new NewETPTAssignment(entityKind, propertyTypeCode, entityTypeCode, mandatory,
                        defaultValue, section, ordinal, dynamic, script);
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

}
