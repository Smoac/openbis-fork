/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Test cases for corresponding {@link DataStrategyStore} class.
 * 
 * @author Christian Ribeaud
 */
public final class DataStrategyStoreTest extends AbstractFileSystemTestCase
{
    private static final String EXPERIMENT_CODE = "E";

    private static final String PROJECT_CODE = "P";

    private static final String GROUP_CODE = "G";

    private Mockery context;

    private IEncapsulatedOpenBISService limsService;

    private IMailClient mailClient;

    private DataStrategyStore dataStrategyStore;

    private BufferedAppender logRecorder;

    private final static Sample createSampleWithExperiment()
    {
        Sample sample = new Sample();
        sample.setCode("sample code");
        sample.setExperiment(createExperiment());
        return sample;
    }

    private final static Experiment createExperiment()
    {
        final Experiment baseExperiment = new Experiment();
        baseExperiment.setCode(EXPERIMENT_CODE);
        final Project project = new Project();
        project.setCode(PROJECT_CODE);
        final Group group = new Group();
        group.setCode(GROUP_CODE);
        project.setGroup(group);
        baseExperiment.setProject(project);
        return baseExperiment;
    }

    private final File createIncomingDataSetPath()
    {
        return new File(workingDirectory, "Twain");
    }

    @BeforeMethod
    public void startup()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public final void tearDown()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @BeforeClass
    public final void beforeClass()
    {
        context = new Mockery();
        limsService = context.mock(IEncapsulatedOpenBISService.class);
        mailClient = context.mock(IMailClient.class);
        dataStrategyStore = new DataStrategyStore(limsService, mailClient);
    }

    @Test
    public final void testEasiestCases()
    {
        boolean exceptionThrown = false;
        try
        {
            dataStrategyStore.getDataStoreStrategy(null, null);
        } catch (final AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Null incoming data set path not permited", exceptionThrown);
        final File incomingDataSetPath = createIncomingDataSetPath();
        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(null, incomingDataSetPath);
        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.UNIDENTIFIED);
    }

    @Test
    public final void testWithFullDataSetInfo()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        final SampleIdentifier sampleIdentifier = dataSetInfo.getSampleIdentifier();
        final Sample baseSample = createSampleWithExperiment();
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(baseSample));

                    one(limsService).getPropertiesOfTopSampleRegisteredFor(sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));
                }
            });
        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);
        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.IDENTIFIED);
        context.assertIsSatisfied();
    }

    @Test
    public final void testWithoutExperimentIdentifier()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        dataSetInfo.setExperimentIdentifier(null);
        final Sample baseSample = createSampleWithExperiment();
        final SampleIdentifier sampleIdentifier = dataSetInfo.getSampleIdentifier();
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(baseSample));

                    one(limsService).getPropertiesOfTopSampleRegisteredFor(sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));
                }
            });
        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);
        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.IDENTIFIED);
        context.assertIsSatisfied();
    }

    @Test
    public final void testWithNullBaseExperiment()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(dataSetInfo.getSampleIdentifier());
                    will(returnValue(null));
                }
            });

        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);

        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.UNIDENTIFIED);
        final String logContent = logRecorder.getLogContent();
        assertEquals("Unexpected log content: " + logContent, true, logContent
                .startsWith("ERROR NOTIFY.DataStrategyStore"));

        context.assertIsSatisfied();
    }

    @Test
    public final void testWithInvalidBaseExperiment()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        final Sample baseSample = createSampleWithExperiment();
        baseSample.getExperiment().setInvalidation(new Invalidation());
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(dataSetInfo.getSampleIdentifier());
                    will(returnValue(baseSample));
                }
            });
        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);
        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.UNIDENTIFIED);
        final String logContent = logRecorder.getLogContent();
        assertEquals("ERROR NOTIFY.DataStrategyStore - "
                + "Data set for sample 'MY-INSTANCE:/S' can not be registered "
                + "because experiment 'E' has been invalidated.", logContent);

        context.assertIsSatisfied();
    }

    @Test
    public final void testSampleIsNotRegistered()
    {
        final File incomingDataSetPath = createIncomingDataSetPath();
        final DataSetInformation dataSetInfo = IdentifiedDataStrategyTest.createDataSetInfo();
        final Sample baseSample = createSampleWithExperiment();
        final Person person = new Person();
        final String email = "john.doe@freemail.org";
        person.setEmail(email);
        baseSample.getExperiment().setRegistrator(person);
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(dataSetInfo.getSampleIdentifier());
                    will(returnValue(baseSample));

                    one(limsService).getPropertiesOfTopSampleRegisteredFor(
                            dataSetInfo.getSampleIdentifier());
                    will(returnValue(null));

                    String replyTo = null;
                    one(mailClient).sendMessage(
                            with(equal(String.format(DataStrategyStore.SUBJECT_FORMAT, dataSetInfo
                                    .getExperimentIdentifier()))), with(any(String.class)),
                            with(equal(replyTo)), null, with(equal(new String[]
                                { email })));
                }
            });

        final IDataStoreStrategy dataStoreStrategy =
                dataStrategyStore.getDataStoreStrategy(dataSetInfo, incomingDataSetPath);

        assertEquals(dataStoreStrategy.getKey(), DataStoreStrategyKey.INVALID);
        final String logContent = logRecorder.getLogContent();
        assertEquals("Unexpected log content: " + logContent, true, logContent
                .startsWith("ERROR OPERATION.DataStrategyStore"));

        context.assertIsSatisfied();
    }
}
