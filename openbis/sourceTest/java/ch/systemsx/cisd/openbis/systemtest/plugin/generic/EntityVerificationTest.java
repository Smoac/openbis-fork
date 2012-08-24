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

package ch.systemsx.cisd.openbis.systemtest.plugin.generic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Tests that the entity verification scripts are called when creating or updating the entities
 * 
 * @author Jakub Straszewski
 */
// NOTE: we depend on transaction beeing committed as part of this test.
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class EntityVerificationTest extends GenericSystemTestCase
{
    // create sample of a type, that has attached script that forbids creation

    private static final String IMPOSSIBLE_TYPE = "IMPOSSIBLE";

    private static final String IMPOSSIBLE_TO_UPDATE_TYPE = "IMPOSSIBLE_TO_UPDATE";

    private void registerNewSample(String identifier, String type)
    {
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(identifier);
        final SampleType sampleType = new SampleType();
        sampleType.setCode(type);
        newSample.setSampleType(sampleType);
        genericClientService.registerSample(systemSessionToken, newSample);
    }

    @BeforeMethod
    public void setUp()
    {
        logIntoCommonClientService();
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

    @Test
    public void testRegisterImpossible()
    {
        try
        {
            registerNewSample("/CISD/EVT1", IMPOSSIBLE_TYPE);
            fail("Registering of sample with impossible type should fail");
        } catch (Exception ufe)
        {
            assertTrue(ufe.getMessage().contains("Validation of sample"));
        }
    }

    @Test
    public void testRegisterImpossibleToUpdate()
    {
        registerNewSample("/CISD/EVT1", IMPOSSIBLE_TO_UPDATE_TYPE);

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getSampleType(IMPOSSIBLE_TO_UPDATE_TYPE));

        List<Sample> samples = etlService.listSamples(systemSessionToken, listCriteria);

        assertEquals("one sample should be registered", 1, samples.size());

        Sample sample = samples.get(0);

        String[] modifiedParentCodesOrNull = new String[]
            { "DYNA-TEST-1" };
        String containerIdentifierOrNull = null;
        SampleIdentifier sampleIdentifier = SampleIdentifier.create("CISD", "EVT1");
        Date version = sample.getModificationDate();
        ExperimentIdentifier experimentIdentifierOrNull = null;
        TechId sampleId = new TechId(sample.getId());
        List<IEntityProperty> properties = Collections.emptyList();
        Collection<NewAttachment> attachments = Collections.emptyList();
        SampleUpdatesDTO update =
                new SampleUpdatesDTO(sampleId, properties, experimentIdentifierOrNull, attachments,
                        version, sampleIdentifier, containerIdentifierOrNull,
                        modifiedParentCodesOrNull);

        try
        {
            etlService.updateSample(systemSessionToken, update);
            fail("update of sample with impossible to update type should fail");
        } catch (Exception ufe)
        {
            assertTrue(ufe.getMessage().contains("Validation of sample"));
        }

        // cleanup
        commonServer.deleteSamples(systemSessionToken, Collections.singletonList(sampleId), "Yup",
                DeletionType.PERMANENT);
    }
}
