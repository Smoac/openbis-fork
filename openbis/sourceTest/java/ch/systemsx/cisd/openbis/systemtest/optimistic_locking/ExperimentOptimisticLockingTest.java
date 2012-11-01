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
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentOptimisticLockingTest extends OptimisticLockingTestCase
{
    private static final String DATA_STORE_CODE = "STANDARD";

    private static final LocatorType LOCATOR_TYPE = new LocatorType(
            LocatorType.DEFAULT_LOCATOR_TYPE_CODE);

    private static final FileFormatType FILE_FORMAT_TYPE = new FileFormatType("XML");

    private static final DataSetType DATA_SET_TYPE = new DataSetType("UNKNOWN");

    private static final List<IEntityProperty> NO_PROPERTIES = Collections
            .<IEntityProperty> emptyList();

    private static final List<NewAttachment> NO_ATTACHMENTS = Collections
            .<NewAttachment> emptyList();

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ExperimentOptimisticLockingTest.class);

    private static final String REGISTERED = "registered";

    private static final String FIRST_REGISTERED = "First registered";

    protected static final String SAMPLE_TYPE_CODE = "NORMAL";

    @Test
    public void testRegisterExperiment()
    {
        NewExperiment experiment = experiment(1);
        logIntoCommonClientService();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

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
        Experiment experiment = createAndLoadExperiment(1);
        String sessionToken = logIntoCommonClientService().getSessionID();
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(createProjectIdentifier(experiment.getProject()
                .getIdentifier()));
        updates.setAttachments(NO_ATTACHMENTS);
        List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        properties.addAll(experiment.getProperties());
        properties.get(0).setValue("testChangePropertyOfAnExistingExperiment");
        updates.setProperties(properties);
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        genericServer.updateExperiment(sessionToken, updates);

        Experiment retrievedExperiment = loadExperiment(experiment);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, retrievedExperiment,
                "test");
        assertEquals("DESCRIPTION: testChangePropertyOfAnExistingExperiment", retrievedExperiment
                .getProperties().get(0).toString());
    }

    @Test
    public void testChangeProjectOfAnExistingExperiment()
    {
        Experiment experiment = createAndLoadExperiment(1);
        assertEquals(project1.getIdentifier(), experiment.getProject().getIdentifier());
        String sessionToken = logIntoCommonClientService().getSessionID();
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(createProjectIdentifier(project2.getIdentifier()));
        updates.setAttachments(NO_ATTACHMENTS);
        updates.setProperties(experiment.getProperties());
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        genericServer.updateExperiment(sessionToken, updates);

        Experiment retrievedExperiment =
                commonServer.getExperimentInfo(systemSessionToken, new TechId(experiment));
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, retrievedExperiment,
                "test");
        assertEquals(project2.getIdentifier(), retrievedExperiment.getProject().getIdentifier());
    }

    @Test
    public void testUpdateExperimentWithOldVersion()
    {
        Experiment experiment = createAndLoadExperiment(1);
        String sessionToken = logIntoCommonClientService().getSessionID();
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(createProjectIdentifier(project2.getIdentifier()));
        updates.setAttachments(NO_ATTACHMENTS);
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
    public void testRegisterSamplesForSameExperimentInTwoThreads()
    {
        final NewExperiment experiment = experiment(1);
        genericServer.registerExperiment(systemSessionToken, experiment, NO_ATTACHMENTS);
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("samples main").logger(operationLog)
                        .getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("samples second").logger(operationLog)
                        .getChannel();
        final IServiceConversationProgressListener listener =
                new AbstractServiceConversationProgressListener(operationLog)
                    {
                        @Override
                        public void handleProgress(String phaseName, int totalItemsToProcess,
                                int numItemsProcessed)
                        {
                            logger.info(phaseName + " " + numItemsProcessed + "/"
                                    + totalItemsToProcess);
                            if (phaseName.equals("createContainerSamples")
                                    && numItemsProcessed == 1 && totalItemsToProcess == 2)
                            {
                                messageChannelMain.send(FIRST_REGISTERED);
                            }
                        }
                    };
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    NewSample sample3 = sample(3, experiment);

                    String sessionToken =
                            genericServer.tryToAuthenticate("test", "a").getSessionToken();
                    messageChannelMain.assertNextMessage(FIRST_REGISTERED);
                    genericServer.registerSample(sessionToken, sample3, NO_ATTACHMENTS);
                    messageChannelSecond.send(REGISTERED);
                }
            }).start();
        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(USER_ID).batchSize(1);
        NewSample sample1 = sample(1, experiment);
        NewSample sample2 = sample(2, experiment);
        builder.sample(sample1).sample(sample2);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        messageChannelSecond.assertNextMessage(REGISTERED);

        Experiment experimentInfo = loadExperiment(experiment);
        List<Sample> samples =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(experimentInfo)));
        assertEquals("[OLT-S1, OLT-S2, OLT-S3]", extractCodes(samples).toString());
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, experimentInfo, "test");
    }

    @Test
    public void testMoveSampleBetweenExperiments()
    {
        Experiment experiment1 = createAndLoadExperiment(1);
        Experiment experiment2 = createAndLoadExperiment(2);
        NewSample sample = sample(1, experiment1);
        genericServer.registerSample(systemSessionToken, sample, NO_ATTACHMENTS);
        Sample loadedSample = loadSample(sample);
        SampleUpdatesDTO sampleUpdate =
                new SampleUpdatesDTO(new TechId(loadedSample), NO_PROPERTIES,
                        ExperimentIdentifierFactory.parse(experiment2.getIdentifier()),
                        NO_ATTACHMENTS, loadedSample.getModificationDate(),
                        SampleIdentifierFactory.parse(sample), null, null);
        sampleUpdate.setUpdateExperimentLink(true);
        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetailsBuilder().user(USER_ID).sampleUpdate(sampleUpdate)
                        .getDetails();
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        etlService.performEntityOperations(sessionToken, details);

        Experiment loadedExperiment1 = loadExperiment(experiment1);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment1,
                USER_ID);
        List<Sample> samples1 =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment1)));
        assertEquals("[]", samples1.toString());
        Experiment loadedExperiment2 = loadExperiment(experiment2);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment2,
                USER_ID);
        List<Sample> samples2 =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment2)));
        assertEquals(sample.getIdentifier(), samples2.get(0).getIdentifier());
    }

    @Test
    public void testRemoveSampleFromExperiment()
    {
        Experiment experiment = createAndLoadExperiment(1);
        NewSample sample = sample(1, experiment);
        genericServer.registerSample(systemSessionToken, sample, NO_ATTACHMENTS);
        ExperimentUpdatesDTO update = new ExperimentUpdatesDTO();
        update.setExperimentId(new TechId(experiment));
        update.setSampleCodes(new String[0]);
        update.setProperties(NO_PROPERTIES);
        update.setAttachments(NO_ATTACHMENTS);
        update.setProjectIdentifier(createProjectIdentifier(experiment.getProject().getIdentifier()));
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        genericServer.updateExperiment(sessionToken, update);

        Experiment loadedExperiment = loadExperiment(experiment);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment, "test");
        List<Sample> samples =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment)));
        assertEquals("[]", samples.toString());
    }

    @Test
    public void testMoveDataSetBetweenExperiments()
    {
        Experiment exp1 = createAndLoadExperiment(1);
        Experiment exp2 = createAndLoadExperiment(2);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        NewDataSet dataSet = dataSet("DS-1", exp1);
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
        update.setProperties(NO_PROPERTIES);
        update.setFileFormatTypeCode(dataSet.getFileFormatType().getCode());
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        genericServer.updateDataSet(sessionToken, update);

        Experiment loadedExperiment1 = loadExperiment(exp1);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment1, "test");
        List<ExternalData> dataSets1 =
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(exp1));
        assertEquals("[]", dataSets1.toString());
        Experiment loadedExperiment2 = loadExperiment(exp2);
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment2, "test");
        List<ExternalData> dataSets2 =
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(exp2));
        assertEquals(dataSet.getCode(), dataSets2.get(0).getCode());
    }

    @Test
    public void testRegisterDataSetsForSameExperimentInTwoThreads()
    {
        final Experiment experiment = createAndLoadExperiment(1);
        final MessageChannel messageChannelMain =
                new MessageChannelBuilder(10000).name("data sets main").logger(operationLog)
                        .getChannel();
        final MessageChannel messageChannelSecond =
                new MessageChannelBuilder(10000).name("data sets second").logger(operationLog)
                        .getChannel();
        final IServiceConversationProgressListener listener =
                new AbstractServiceConversationProgressListener(operationLog)
                    {
                        @Override
                        public void handleProgress(String phaseName, int totalItemsToProcess,
                                int numItemsProcessed)
                        {
                            if (phaseName.equals("createDataSets") && numItemsProcessed == 1
                                    && totalItemsToProcess == 2)
                            {
                                messageChannelMain.send(FIRST_REGISTERED);
                                System.out.println(loadExperiment(experiment).getModifier()
                                        .getUserId());
                            }
                        }
                    };
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    messageChannelMain.assertNextMessage(FIRST_REGISTERED);
                    AtomicEntityOperationDetails details =
                            new AtomicEntityOperationDetailsBuilder().user("test")
                                    .dataSet(dataSet("DS3", experiment)).getDetails();
                    etlService.performEntityOperations(systemSessionToken, details);
                    messageChannelSecond.send(REGISTERED);
                }
            }).start();
        ServiceConversationsThreadContext.setProgressListener(listener);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(USER_ID).batchSize(1);
        builder.dataSet(dataSet("DS1", experiment)).dataSet(dataSet("DS2", experiment));

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        messageChannelSecond.assertNextMessage(REGISTERED);

        Experiment loadedExperiment = loadExperiment(experiment);
        List<ExternalData> dataSets =
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment));
        assertEquals("[DS1, DS2, DS3]", extractCodes(dataSets).toString());
        checkModifierAndModificationDateOfExperiment(timeIntervalChecker, loadedExperiment, "test");
    }

    private void checkModifierAndModificationDateOfExperiment(
            TimeIntervalChecker timeIntervalChecker, Experiment experiment, String userId)
    {
        assertEquals(userId, experiment.getModifier().getUserId());
        timeIntervalChecker.assertDateInInterval(experiment.getModificationDate());
    }

    private Experiment createAndLoadExperiment(int number)
    {
        NewExperiment experiment = experiment(number);
        genericServer.registerExperiment(systemSessionToken, experiment, NO_ATTACHMENTS);
        return loadExperiment(experiment);
    }

    private Experiment loadExperiment(final IIdentifierHolder experiment)
    {
        return commonServer.getExperimentInfo(systemSessionToken,
                ExperimentIdentifierFactory.parse(experiment.getIdentifier()));
    }

    private NewSample sample(int number, IIdentifierHolder experiment)
    {
        NewSample sample = sample(number);
        sample.setExperimentIdentifier(experiment.getIdentifier());
        return sample;
    }

    private NewSample sample(int number)
    {
        NewSample sample = new NewSample();
        sample.setIdentifier("/" + SPACE_1 + "/OLT-S" + number);
        sample.setSampleType(new SampleTypeBuilder().code(SAMPLE_TYPE_CODE).getSampleType());
        return sample;
    }

    private Sample loadSample(IIdentifierHolder sample)
    {
        return etlService.tryGetSampleWithExperiment(systemSessionToken,
                SampleIdentifierFactory.parse(sample.getIdentifier()));
    }

    private NewDataSet dataSet(String code, Experiment experiment)
    {
        NewDataSet dataSet = dataSet(code);
        dataSet.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(experiment
                .getIdentifier()));
        return dataSet;
    }

    private NewDataSet dataSet(String code)
    {
        NewDataSet dataSet = new NewDataSet();
        dataSet.setCode(code);
        dataSet.setDataSetType(DATA_SET_TYPE);
        dataSet.setFileFormatType(FILE_FORMAT_TYPE);
        dataSet.setDataSetProperties(Collections.<NewProperty> emptyList());
        dataSet.setLocation("a/b/c/" + code);
        dataSet.setLocatorType(LOCATOR_TYPE);
        dataSet.setStorageFormat(StorageFormat.PROPRIETARY);
        dataSet.setDataStoreCode(DATA_STORE_CODE);
        return dataSet;
    }

}
