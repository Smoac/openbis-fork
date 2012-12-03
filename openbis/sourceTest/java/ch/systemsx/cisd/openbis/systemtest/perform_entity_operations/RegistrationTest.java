/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.perform_entity_operations;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.MaterialTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * @author Franz-Josef Elmer
 */
public class RegistrationTest extends SystemTestCase
{
    @Test
    public void testRegisterMetaproject()
    {
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        NewMetaproject metaProject = new NewMetaproject("RM-TEST", "a test", "test");
        metaProject.setEntities(Arrays.<IObjectId> asList(new ExperimentTechIdId(2L),
                new SampleTechIdId(1L), new DataSetTechIdId(4L), new MaterialTechIdId(1L)));
        builder.metaProject(metaProject);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
    }

    @Test
    public void testRegisterExperimentPlateAndWells()
    {
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        String identifier = "/TEST-SPACE/TEST-PROJECT/EXP-1";
        NewExperiment experiment = new NewExperiment(identifier, "COMPOUND_HCS");
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("DESCRIPTION");
        property.setPropertyType(propertyType);
        property.setValue("hello");
        experiment.setProperties(new IEntityProperty[]
            { property });
        builder.experiment(experiment);
        String sampleIdentifier = "/TEST-SPACE/PLATE-1";
        builder.sample(new NewSampleBuilder(sampleIdentifier).experiment(identifier)
                .type("CELL_PLATE").get());
        builder.sample(new NewSampleBuilder(sampleIdentifier + ":A1").container(sampleIdentifier)
                .experiment(identifier).type("WELL").get());
        builder.sample(new NewSampleBuilder(sampleIdentifier + ":A2").container(sampleIdentifier)
                .experiment(identifier).type("WELL").get());

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        Experiment loadedExperiment =
                commonServer.getExperimentInfo(systemSessionToken,
                        ExperimentIdentifierFactory.parse(identifier));
        List<Sample> plates =
                assertSamples(
                        "[/TEST-SPACE/PLATE-1]",
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment.getId())));
        assertSamples("[/TEST-SPACE/PLATE-1:A1, /TEST-SPACE/PLATE-1:A2]",
                ListSampleCriteria.createForContainer(new TechId(plates.get(0))));
    }

    private List<Sample> assertSamples(String expectedSamples, ListSampleCriteria criteria)
    {
        List<Sample> samples = commonServer.listSamples(systemSessionToken, criteria);
        List<String> identifiers = new ArrayList<String>();
        for (Sample sample : samples)
        {
            identifiers.add(sample.getIdentifier());
        }
        Collections.sort(identifiers);
        assertEquals(expectedSamples, identifiers.toString());
        return samples;
    }
}
