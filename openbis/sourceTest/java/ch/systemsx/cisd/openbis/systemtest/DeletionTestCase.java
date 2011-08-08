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

    private List<Experiment> registeredExperiments = new ArrayList<Experiment>();

    private List<Sample> registeredSamples = new ArrayList<Sample>();

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
        commonServer.deleteExperiments(sessionToken, TechId.createList(registeredExperiments),
                "test reason", DeletionType.TRASH);
        commonServer.deletePermanently(sessionToken, TechId.createList(listDeletions()));
    }

    @Test
    public void testDeleteExperimentE1()
    {
        Experiment e1 = findExperimentByCode("E1");
        final TechId experimentId = new TechId(e1);
        commonServer.deleteExperiments(sessionToken, Collections.singletonList(experimentId),
                REASON, DeletionType.TRASH);
        
        assertExperimentDoesNotExist(e1.getCode());
        assertSamplesDoNotExist(registeredSamples);
        
        List<DeletionPE> deletions = listDeletions();
        Assert.assertEquals(1, deletions.size());
        
        final TechId deletionId = TechId.create(deletions.get(0));
        commonServer.revertDeletions(sessionToken, Collections.singletonList(deletionId));

        Experiment revertedExperiment = commonServer.getExperimentInfo(sessionToken, experimentId);
        Assert.assertNotNull(revertedExperiment);
    }

    private void assertExperimentDoesNotExist(String expCode)
    {
        Experiment experiment = findExperimentByCode(expCode);
        try
        {
            commonServer.getExperimentInfo(sessionToken, new TechId(experiment));
            final String error = String.format("Experiment '%s' should not exist", expCode);
            Assert.fail(error);
        } catch (UserFailureException ufe)
        {
            // OK
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
