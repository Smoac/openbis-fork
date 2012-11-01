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

package ch.systemsx.cisd.openbis.systemtest.optimistic_locking;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentOptimisticLockingTest extends OptimisticLockingTestCase
{

    @Test
    public void testRegisterExperiment()
    {
        NewExperiment experiment = toolBox.experiment(1);
        logIntoCommonClientService();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker(1000);

        genericClientService.registerExperiment(SESSION_KEY, SESSION_KEY, experiment);

        Experiment experimentInfo =
                commonServer.getExperimentInfo(systemSessionToken, new ExperimentIdentifierFactory(
                        experiment.getIdentifier()).createIdentifier());
        assertEquals("test", experimentInfo.getRegistrator().getUserId());
        assertEquals("test", experimentInfo.getModifier().getUserId());
        timeIntervalChecker.assertDateInInterval(experimentInfo.getRegistrationDate());
        timeIntervalChecker.assertDateInInterval(experimentInfo.getModificationDate());
        assertEquals("DESCRIPTION: hello 1", experiment.getProperties()[0].toString());
    }

    @Test
    public void testChangePropertyOfAnExistingExperiment()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        String sessionToken = logIntoCommonClientService().getSessionID();
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(toolBox.createProjectIdentifier(experiment.getProject()
                .getIdentifier()));
        updates.setAttachments(ToolBox.NO_ATTACHMENTS);
        List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        properties.addAll(experiment.getProperties());
        properties.get(0).setValue("testChangePropertyOfAnExistingExperiment");
        updates.setProperties(properties);
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        genericServer.updateExperiment(sessionToken, updates);

        Experiment retrievedExperiment = toolBox.loadExperiment(experiment);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, retrievedExperiment,
                "test");
        assertEquals("DESCRIPTION: testChangePropertyOfAnExistingExperiment", retrievedExperiment
                .getProperties().get(0).toString());
    }

    @Test
    public void testChangeProjectOfAnExistingExperiment()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        assertEquals(toolBox.project1.getIdentifier(), experiment.getProject().getIdentifier());
        String sessionToken = logIntoCommonClientService().getSessionID();
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(toolBox.createProjectIdentifier(toolBox.project2
                .getIdentifier()));
        updates.setAttachments(ToolBox.NO_ATTACHMENTS);
        updates.setProperties(experiment.getProperties());
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        genericServer.updateExperiment(sessionToken, updates);

        Experiment retrievedExperiment =
                commonServer.getExperimentInfo(systemSessionToken, new TechId(experiment));
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, retrievedExperiment,
                "test");
        assertEquals(toolBox.project2.getIdentifier(), retrievedExperiment.getProject()
                .getIdentifier());
    }

    @Test
    public void testUpdateExperimentWithOldVersion()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        String sessionToken = logIntoCommonClientService().getSessionID();
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(toolBox.createProjectIdentifier(toolBox.project2
                .getIdentifier()));
        updates.setAttachments(ToolBox.NO_ATTACHMENTS);
        updates.setProperties(experiment.getProperties());
        genericServer.updateExperiment(sessionToken, updates);

        try
        {
            genericServer.updateExperiment(sessionToken, updates);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Experiment has been modified in the meantime. Reopen tab to be able "
                    + "to continue with refreshed data.", ex.getMessage());
        }
    }

    @Test
    public void testMoveSampleBetweenExperiments()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        NewSample sample = toolBox.sample(1, experiment1);
        genericServer.registerSample(systemSessionToken, sample, ToolBox.NO_ATTACHMENTS);
        Sample loadedSample = toolBox.loadSample(sample);
        SampleUpdatesDTO sampleUpdate =
                new SampleUpdatesDTO(new TechId(loadedSample), ToolBox.NO_PROPERTIES,
                        ExperimentIdentifierFactory.parse(experiment2.getIdentifier()),
                        ToolBox.NO_ATTACHMENTS, loadedSample.getModificationDate(),
                        SampleIdentifierFactory.parse(sample), null, null);
        sampleUpdate.setUpdateExperimentLink(true);
        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetailsBuilder().user(ToolBox.USER_ID)
                        .sampleUpdate(sampleUpdate).getDetails();
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        etlService.performEntityOperations(sessionToken, details);

        Experiment loadedExperiment1 = toolBox.loadExperiment(experiment1);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment1,
                ToolBox.USER_ID);
        List<Sample> samples1 =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment1)));
        assertEquals("[]", samples1.toString());
        Experiment loadedExperiment2 = toolBox.loadExperiment(experiment2);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment2,
                ToolBox.USER_ID);
        List<Sample> samples2 =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment2)));
        assertEquals(sample.getIdentifier(), samples2.get(0).getIdentifier());
    }

    @Test
    public void testRemoveSampleFromExperiment()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewSample sample = toolBox.sample(1, experiment);
        genericServer.registerSample(systemSessionToken, sample, ToolBox.NO_ATTACHMENTS);
        ExperimentUpdatesDTO update = new ExperimentUpdatesDTO();
        update.setExperimentId(new TechId(experiment));
        update.setSampleCodes(new String[0]);
        update.setProperties(ToolBox.NO_PROPERTIES);
        update.setAttachments(ToolBox.NO_ATTACHMENTS);
        update.setProjectIdentifier(toolBox.createProjectIdentifier(experiment.getProject()
                .getIdentifier()));
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        genericServer.updateExperiment(sessionToken, update);

        Experiment loadedExperiment = toolBox.loadExperiment(experiment);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment, "test");
        List<Sample> samples =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment)));
        assertEquals("[]", samples.toString());
    }

    @Test
    public void testMoveDataSetBetweenExperiments()
    {
        Experiment exp1 = toolBox.createAndLoadExperiment(1);
        Experiment exp2 = toolBox.createAndLoadExperiment(2);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        NewDataSet dataSet = toolBox.dataSet("DS-1", exp1);
        builder.dataSet(dataSet);
        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        List<ExternalData> dataSets =
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(exp1));
        ExternalData loadedDataSet = dataSets.get(0);
        assertEquals(dataSet.getCode(), loadedDataSet.getCode());
        DataSetUpdatesDTO update = new DataSetUpdatesDTO();
        update.setDatasetId(new TechId(loadedDataSet));
        update.setVersion(loadedDataSet.getModificationDate());
        update.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(exp2.getIdentifier()));
        update.setProperties(ToolBox.NO_PROPERTIES);
        update.setFileFormatTypeCode(dataSet.getFileFormatType().getCode());
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        genericServer.updateDataSet(sessionToken, update);

        Experiment loadedExperiment1 = toolBox.loadExperiment(exp1);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment1, "test");
        List<ExternalData> dataSets1 =
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(exp1));
        assertEquals("[]", dataSets1.toString());
        Experiment loadedExperiment2 = toolBox.loadExperiment(exp2);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment2, "test");
        List<ExternalData> dataSets2 =
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(exp2));
        assertEquals(dataSet.getCode(), dataSets2.get(0).getCode());
    }

    private void checkModifierAndModificationDateOfExperiment(
            TimeIntervalChecker timeIntervalChecker, Experiment experiment, String userId)
    {
        assertEquals(userId, experiment.getModifier().getUserId());
        timeIntervalChecker.assertDateInInterval(experiment.getModificationDate());
    }

}
