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

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Tests that the entity validation scripts are called when creating or updating the entities
 * 
 * @author Jakub Straszewski
 */
// NOTE: we depend on transaction beeing committed as part of this test.
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class EntityValidationTest extends GenericSystemTestCase
{

    private static final String IMPOSSIBLE_TYPE = "IMPOSSIBLE";

    private static final String IMPOSSIBLE_TO_UPDATE_TYPE = "IMPOSSIBLE_TO_UPDATE";

    private static final int TEST_SCRIPT_ID = 8;

    /**
     * shortcut for registerNewSample(identifier, type, null)
     */
    private void registerNewSample(String identifier, String type)
    {
        registerNewSample(identifier, type, null);
    }

    private void registerNewSample(String identifier, String type, String experimentIdentifierOrNull)
    {
        final NewSample newSample = prepareNewSample(identifier, type, experimentIdentifierOrNull);
        genericClientService.registerSample(systemSessionToken, newSample);
    }

    private NewSample prepareNewSample(String identifier, String type,
            String experimentIdentifierOrNull)
    {
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(identifier);
        final SampleType sampleType = new SampleType();
        sampleType.setCode(type);
        newSample.setSampleType(sampleType);
        if (experimentIdentifierOrNull != null)
        {
            newSample.setExperimentIdentifier(experimentIdentifierOrNull);
        }
        return newSample;
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

        Sample sample = getSampleFromSpaceAndType("CISD", IMPOSSIBLE_TO_UPDATE_TYPE, "EVT1");

        SampleUpdatesDTO update = createSampleUpdates(sample, "DYNA-TEST-1");

        try
        {
            etlService.updateSample(systemSessionToken, update);
            fail("update of sample with impossible to update type should fail");
        } catch (Exception ufe)
        {
            assertTrue(ufe.getMessage(), ufe.getMessage().contains("Validation of sample"));
        }

        deleteSample(sample);
    }

    private void deleteSample(Sample sample)
    {
        // cleanup
        commonServer.deleteSamples(systemSessionToken,
                Collections.singletonList(new TechId(sample.getId())), "Yup",
                DeletionType.PERMANENT);
    }

    @Test
    public void testPerformEntityOperation()
    {
        NewSample sample = prepareNewSample("/TEST-SPACE/NEV-TEST", "NORMAL", null);
        sample.setParents("EV-PARENT-NORMAL");
        performSampleCreation(sample);
    }

    private void performSampleCreation(NewSample sampleToCreate)
    {
        List<NewSpace> spaceRegistrations = Collections.emptyList();
        List<NewProject> projectRegistrations = Collections.emptyList();
        List<NewExperiment> experimentRegistrations = Collections.emptyList();
        List<ExperimentUpdatesDTO> experimentUpdates =
                Collections.<ExperimentUpdatesDTO> emptyList();
        List<SampleUpdatesDTO> sampleUpdates = Collections.emptyList();
        List<NewSample> sampleRegistrations = Arrays.asList(sampleToCreate);
        Map<String, List<NewMaterial>> materialRegistrations = Collections.emptyMap();
        List<MaterialUpdateDTO> materialUpdates = Collections.emptyList();
        List<? extends NewExternalData> dataSetRegistrations = Collections.emptyList();
        List<DataSetBatchUpdatesDTO> dataSetUpdates = Collections.emptyList();
        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetails(null, null, spaceRegistrations,
                        projectRegistrations, experimentRegistrations, experimentUpdates,
                        sampleUpdates, sampleRegistrations, materialRegistrations, materialUpdates,
                        dataSetRegistrations, dataSetUpdates);
        etlService.performEntityOperations(systemSessionToken, details);
    }

    @Test
    public void testSampleUpdateTriggerValidationOfParentsChildren()
    {
        // setting the parent of this sample, forces the validation of parent (as it is aslo being
        // changed)
        // as the consequence validation of INVALID sample is forced - via the validation script of
        // the parent
        // the validation of INVALID sample should fail
        Sample sample = getSampleFromSpaceAndType("TEST-SPACE", "WELL", "EV-NOT_INVALID");

        SampleUpdatesDTO update = createSampleUpdates(sample, "EV-PARENT");

        try
        {
            etlService.updateSample(systemSessionToken, update);

            Script script =
                    commonServer.getScriptInfo(systemSessionToken, new TechId(TEST_SCRIPT_ID));
            fail("update of sample with impossible to update type should fail. Validation script is:\n"
                    + script.getScript());

        } catch (Exception ufe)
        {
            assertTrue(ufe.getMessage(), ufe.getMessage().contains("Validation of sample"));
            assertTrue(ufe.getMessage(), ufe.getMessage().contains("Cannot update this entity"));
        }
    }

    @Test
    public void testSampleUpdateTriggerValidationOfParentsChildrenDoesNotHappen()
    {
        Sample sample = getSampleFromSpaceAndType("TEST-SPACE", "WELL", "EV-NOT_INVALID");

        SampleUpdatesDTO update = createSampleUpdates(sample, "EV-PARENT-NORMAL");

        etlService.updateSample(systemSessionToken, update);
    }

    private SampleUpdatesDTO createSampleUpdates(Sample sample, String parentCode)
    {
        String[] modifiedParentCodesOrNull = new String[]
            { parentCode };
        String containerIdentifierOrNull = null;
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(sample);
        Date version = sample.getModificationDate();
        Experiment experiment = sample.getExperiment();
        ExperimentIdentifier experimentIdentifierOrNull =
                (experiment == null) ? null : new ExperimentIdentifier(experiment);
        List<IEntityProperty> properties = Collections.emptyList();
        Collection<NewAttachment> attachments = Collections.emptyList();
        SampleUpdatesDTO update =
                new SampleUpdatesDTO(new TechId(sample.getId()), properties,
                        experimentIdentifierOrNull, attachments, version, sampleIdentifier,
                        containerIdentifierOrNull, modifiedParentCodesOrNull);
        return update;
    }

    /**
     * finds given sample via ETL service
     */
    private Sample getSampleFromSpaceAndType(String spaceCode, String sampleType, String sampleCode)
    {
        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode(spaceCode);
        listCriteria.setSampleType(getSampleType(sampleType));

        List<Sample> samples = etlService.listSamples(systemSessionToken, listCriteria);

        for (Sample sample : samples)
        {
            if (sample.getCode().equals(sampleCode))
            {
                return sample;
            }
        }
        fail(String
                .format("No sample %s (%s) found in space %s", sampleCode, sampleType, spaceCode));
        return null;
    }
}
