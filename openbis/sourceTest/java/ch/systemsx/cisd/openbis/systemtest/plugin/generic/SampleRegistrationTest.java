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
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IdentifierExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class SampleRegistrationTest extends GenericSystemTestCase
{
    @Test
    public void testSimpleRegistration()
    {
        logIntoCommonClientService();

        NewSample sample = new NewSample();
        String identifier = "/cisd/" + commonClientService.generateCode("S-");
        sample.setIdentifier(identifier);
        SampleType sampleType = new SampleType();
        sampleType.setCode("CELL_PLATE");
        sample.setSampleType(sampleType);
        sample.setProperties(new IEntityProperty[]
            { property("COMMENT", "test sample") });
        String[] parents = new String[]
            { "c1", "C2", "CISD:/CISD/C3" };
        sample.setParents(parents);
        genericClientService.registerSample("session", sample);

        Sample s = getSample(identifier);
        List<IEntityProperty> properties = s.getProperties();
        assertEquals("COMMENT", properties.get(0).getPropertyType().getCode());
        assertEquals("test sample", properties.get(0).getValue());
        assertEquals(1, properties.size());
        assertEquals(parents.length, s.getParents().size());
        assertEquals("[CISD:/CISD/C1, CISD:/CISD/C2, CISD:/CISD/C3]", Arrays
                .toString(IdentifierExtractor.extract(s.getParents()).toArray()));
    }

    private Sample getSample(String sampleIdentifier)
    {
        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        GridRowModels<Sample> list = samples.getResultSet().getList();
        for (GridRowModel<Sample> gridRowModel : list)
        {
            Sample sample = gridRowModel.getOriginalObject();
            System.out.println("SAMPLE:" + sample.getIdentifier());
            if (sample.getIdentifier().endsWith(sampleIdentifier.toUpperCase()))
            {
                return sample;
            }
        }
        fail("No sample of type found for identifier " + sampleIdentifier);
        return null; // satisfy compiler
    }
}
