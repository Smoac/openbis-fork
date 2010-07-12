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

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Head-less system test for experiment registration 
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class ExperimentRegistrationTest extends GenericSystemTestCase
{
    @Test
    public void testRegisterExperimentWithoutMissingMandatoryProperty()
    {
        logIntoCommonClientService();
        String experimentCode = commonClientService.generateCode("EXP");
        String experimentIdentifier = "/cisd/default/" + experimentCode;
        NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");
        
        try
        {
            genericClientService.registerExperiment("attachments", "samples", newExperiment);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Value of mandatory property 'DESCRIPTION' not specified.", ex.getMessage());
        }
    }
    
    @Test
    public void testRegisterExperiment()
    {
        logIntoCommonClientService();
        String experimentCode = commonClientService.generateCode("EXP");
        String experimentIdentifier = "/cisd/default/" + experimentCode;
        NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[] {property("DESCRIPTION", "my experiment")});
        genericClientService.registerExperiment("attachments", "samples", newExperiment);
        
        Experiment experiment = genericClientService.getExperimentInfo(experimentIdentifier);
        assertEquals(experimentCode, experiment.getCode());
        assertEquals(experimentIdentifier.toUpperCase(), experiment.getIdentifier());
        assertEquals("SIRNA_HCS", experiment.getExperimentType().getCode());
        List<IEntityProperty> properties = experiment.getProperties();
        assertEquals("DESCRIPTION", properties.get(0).getPropertyType().getCode());
        assertEquals("my experiment", properties.get(0).tryGetAsString());
        assertEquals(1, properties.size());
    }
    
    @Test
    public void testRegisterExperimentWithSamples()
    {
        logIntoCommonClientService();
        String experimentCode = commonClientService.generateCode("EXP");
        String experimentIdentifier = "/cisd/default/" + experimentCode;
        NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[] {property("DESCRIPTION", "my experiment")});
        newExperiment.setSamples(new String[] {"3vcp8"});
        genericClientService.registerExperiment("attachments", "samples", newExperiment);
        
        Experiment experiment = genericClientService.getExperimentInfo(experimentIdentifier);
        TechId experimentId = new TechId(experiment.getId());
        ListSampleCriteria listCriteria = ListSampleCriteria.createForExperiment(experimentId);
        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        
        assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());
        Sample sample = samples.getResultSet().getList().get(0).getOriginalObject();
        assertEquals("3VCP8", sample.getCode());
        assertEquals(experiment.getId(), sample.getExperiment().getId());
        assertEquals(1, samples.getResultSet().getList().size());
    }
    
    private IEntityProperty property(String type, String value)
    {
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(type);
        property.setPropertyType(propertyType);
        property.setValue(value);
        return property;
    }
}
