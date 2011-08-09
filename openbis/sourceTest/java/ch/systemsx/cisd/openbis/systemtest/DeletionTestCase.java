/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * @author Kaloyan Enimanev
 */
public class DeletionTestCase extends SystemTestCase
{
    private static final String REASON = "REASON";

    protected IETLLIMSService etlService;

    protected IDAOFactory daoFactory;

    protected IDeletionDAO deletionDAO;

    private String sessionToken;

    private List<Experiment> registeredExperiments;

    private List<Sample> registeredSamples;

    private List<DeletionPE> preExistingDeletions;

    @Autowired
    public final void setEtlService(IETLLIMSService etlService)
    {
        this.etlService = etlService;
    }

    @Autowired
    public final void setDaoFactory(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @BeforeMethod
    public void setUp() {
        registeredExperiments = new ArrayList<Experiment>();
        registeredSamples = new ArrayList<Sample>();

        SessionContext sessionContext = logIntoCommonClientService();
        sessionToken = sessionContext.getSessionID();
        deletionDAO = daoFactory.getDeletionDAO();

        preExistingDeletions = deletionDAO.listAllEntities();

        // experiments
        createExperiment("E1");
        createExperiment("E2");

        // samples
        createSample("E1", "S1");
        createComponentSample("E1", "S1", "S1.1");
        createComponentSample("E1", "S1", "S1.2");
        createComponentSample("E2", "S1", "S1.3");
        createChildSample("E1", "S1.2", "S1.2.1");
        createChildSample("E2", "S1.2", "S1.2.2");
        createChildSample("E1", "S1.2.1", "S1.2.1.1");
        createChildSample("E2", "S1.2.1", "S1.2.1.2");

        createChildSample("E2", "S1.3", "S1.3.1");
        createComponentSample("E1", "S1.3.1", "S1.3.1.1");
        createComponentSample("E1", "S1.3.1", "S1.3.1.2");
        createComponentSample("E2", "S1.3.1", "S1.3.1.3");

        // nested children
        createChildSample("E1", "S1", "S1.4");
        createChildSample("E1", "S1.4", "S1.4.1");
        createChildSample("E2", "S1.4.1", "S1.4.1.1");
        createChildSample("E1", "S1.4.1.1", "S1.4.1.1.1");
        createChildSample("E1", "S1.4.1.1", "S1.4.1.1.2");
        createChildSample("E2", "S1.4.1.1.1", "S1.4.1.1.1.1");

        // try also components under nested children
        createComponentSample("E1", "S1.4.1.1.1.1", "S1.4.1.1.1.1.1");
        createComponentSample("E1", "S1.4.1.1.1.1", "S1.4.1.1.1.1.2");
        createComponentSample("E1", "S1.4.1.1.1.1", "S1.4.1.1.1.1.3");
        createChildSample("E1", "S1.4.1.1.1.1.3", "S1.4.1.1.1.1.3.1");
        createChildSample("E1", "S1.4.1.1.1.1.3", "S1.4.1.1.1.1.3.2");

        // an empty samples
        createChildSample("E1", "S1", "S1.5");

        // some more samples in an "S2"-branch
        createSample("E1", "S2");
        createChildSample("E1", "S2", "S2.1");
        createChildSample("E2", "S2", "S2.2");
        createChildSample("E1", "S2", "S2.3");
        createComponentSample("E1", "S2.1", "S2.2");

    }

    @AfterMethod(alwaysRun = true)
    public void tearDown()
    {
        List<Experiment> existingExperiments = new ArrayList<Experiment>();
        for (Experiment exp : registeredExperiments)
        {
            if (isExistingExperiment(exp.getCode()))
            {
                existingExperiments.add(exp);
            }
        }
        commonServer.deleteExperiments(sessionToken, TechId.createList(existingExperiments),
                REASON, DeletionType.TRASH);
        commonServer.deletePermanently(sessionToken, TechId.createList(listDeletions()));
    }

    @Test
    @NotTransactional
    public void testDeleteExperimentE1()
    {
        Experiment e1 = findExperimentByCode("E1");
        final TechId experimentId = new TechId(e1);
        // delete
        commonServer.deleteExperiments(sessionToken, Collections.singletonList(experimentId),
                REASON, DeletionType.TRASH);
        
        assertExperimentDoesNotExist(e1.getCode());
        assertSamplesDoNotExist(registeredSamples);
        
        List<DeletionPE> deletions = listDeletions();
        Assert.assertEquals(1, deletions.size());
        
        // revert
        final TechId deletionId1 = TechId.create(deletions.get(0));
        commonServer.revertDeletions(sessionToken, Collections.singletonList(deletionId1));

        assertExperimentExists(e1.getCode());
        assertSamplesExist(registeredSamples);

        // delete permanently
        commonServer.deleteExperiments(sessionToken, Collections.singletonList(experimentId),
                REASON, DeletionType.TRASH);
        final TechId deletionId2 = TechId.create(listDeletions().get(0));
        commonServer.deletePermanently(sessionToken, Collections.singletonList(deletionId2));
        assertExperimentDoesNotExist(e1.getCode());
        assertSamplesDoNotExist(registeredSamples);
    }

    @Test
    @NotTransactional
    public void testDeleteSampleS14()
    {
        Sample s14 = findSampleByCode("S1.4");
        final TechId sampleId = new TechId(s14);
        // delete
        commonServer.deleteSamples(sessionToken, Collections.singletonList(sampleId), REASON,
                DeletionType.TRASH);

        List<Sample> deletedSamples = getSamplesWithPrefix(s14.getCode());
        assertExperimentExists("E1");
        assertSamplesDoNotExist(deletedSamples);

        List<DeletionPE> deletions = listDeletions();
        Assert.assertEquals(1, deletions.size());

        // revert
        final TechId deletionId1 = TechId.create(deletions.get(0));
        commonServer.revertDeletions(sessionToken, Collections.singletonList(deletionId1));

        assertSamplesExist(deletedSamples);

        // delete permanently
        commonServer.deleteSamples(sessionToken, Collections.singletonList(sampleId), REASON,
                DeletionType.TRASH);
        final TechId deletionId2 = TechId.create(listDeletions().get(0));
        commonServer.deletePermanently(sessionToken, Collections.singletonList(deletionId2));
        assertExperimentExists("E1");
        assertSamplesDoNotExist(deletedSamples);
    }

    private void assertExperimentExists(String expCode)
    {
        final String error = String.format("Experiment '%s' must exist", expCode);
        Assert.assertTrue(error, isExistingExperiment(expCode));
    }

    private void assertExperimentDoesNotExist(String expCode)
    {
        final String error = String.format("Experiment '%s' should not exist", expCode);
        Assert.assertFalse(error, isExistingExperiment(expCode));
    }

    private boolean isExistingExperiment(String expCode)
    {
        Experiment experiment = findExperimentByCode(expCode);
        try
        {
            commonServer.getExperimentInfo(sessionToken, new TechId(experiment));
            return true;
        } catch (UserFailureException ufe)
        {
            return false;
        }

    }

    private void assertSamplesExist(List<Sample> samples)
    {
        for (Sample sample : samples)
        {
            // will fail if sample is not found
            commonServer.getSampleInfo(sessionToken, new TechId(sample));
        }
    }

    private void assertSamplesDoNotExist(List<Sample> samples)
    {
        for (Sample sample : samples)
        {
            try
            {
                commonServer.getSampleInfo(sessionToken, new TechId(sample));
                final String error =
                        String.format("Sample '%s' should not exist", sample.getIdentifier());
                Assert.fail(error);
            } catch (UserFailureException ufe)
            {
                // OK
            }
        }
    }

    private List<Sample> getSamplesWithPrefix(String codePrefix)
    {
        List<Sample> result = new ArrayList<Sample>();
        for (Sample sample : registeredSamples)
        {
            if (sample.getCode().startsWith(codePrefix))
            {
                result.add(sample);
            }
        }
        return result;
    }

    private void createExperiment(String code)
    {
        ExperimentIdentifier expIdentifier =
                new ExperimentIdentifier(null, "CISD", "DEFAULT", code);
        NewExperiment experiment = new NewExperiment(expIdentifier.toString(), "COMPOUND_HCS");
        final GenericEntityProperty property = createDescriptionProperty();
        experiment.setProperties(new IEntityProperty[] { property });
        long id = etlService.registerExperiment(sessionToken, experiment);

        Experiment exp = commonServer.getExperimentInfo(sessionToken, new TechId(id));
        Assert.assertNotNull(exp);
        registeredExperiments.add(exp);
    }

    private void createSample(String experimentCode, NewSample newSample)
    {
        Experiment exp = findExperimentByCode(experimentCode);
        newSample.setExperimentIdentifier(exp.getIdentifier());
        long id = etlService.registerSample(sessionToken, newSample, null);

        SampleParentWithDerived sampParentAndDerived =
                commonServer.getSampleInfo(sessionToken, new TechId(id));
        final Sample sample = sampParentAndDerived.getParent();
        Assert.assertNotNull(sample);
        registeredSamples.add(sample);
    }

    private void createSample(String experimentCode, String sampleCode)
    {
        NewSample newSample = createNewSample(sampleCode);
        createSample(experimentCode, newSample);
    }

    private void createChildSample(String experimentCode, String parentCode, String sampleCode)
    {
        NewSample newSample = createNewSample(sampleCode);
        newSample.setParentsOrNull(new String[]
            { parentCode });
        createSample(experimentCode, newSample);
    }

    private void createComponentSample(String experimentCode, String containerCode,
            String sampleCode)
    {
        NewSample newSample = createNewSample(sampleCode);
        Sample container = findSampleByCode(containerCode);
        newSample.setContainerIdentifier(container.getIdentifier());
        createSample(experimentCode, newSample);
    }

    private NewSample createNewSample(String sampleCode)
    {
        NewSample newSample = new NewSample();
        newSample.setIdentifier("/CISD/" + sampleCode);
        newSample.setSampleType(new SampleType());
        newSample.getSampleType().setCode("CELL_PLATE");
        return newSample;
    }

    private Experiment findExperimentByCode(String experimentCode)
    {
        for (Experiment experiment : registeredExperiments)
        {
            if (experiment.getCode().equals(experimentCode))
            {
                return experiment;
            }
        }
        return null;
    }

    private Sample findSampleByCode(String sampleCode)
    {
        for (Sample sample : registeredSamples)
        {
            if (sample.getCode().equals(sampleCode))
            {
                return sample;
            }
        }
        return null;
    }

    private GenericEntityProperty createDescriptionProperty()
    {
        final GenericEntityProperty property = new GenericEntityProperty();
        property.setPropertyType(new PropertyType());
        property.getPropertyType().setCode("DESCRIPTION");
        property.setValue("some test description");
        return property;
    }

    private List<DeletionPE> listDeletions()
    {
        List<DeletionPE> allDeletions = deletionDAO.listAllEntities();
        ArrayList<DeletionPE> result = new ArrayList<DeletionPE>();
        for (DeletionPE deletion : allDeletions)
        {
            if (preExistingDeletions.contains(deletion))
            {
                // ignore pre-existing deletions
            } else
            {
                result.add(deletion);
            }
        }
        return result;
    }

}
