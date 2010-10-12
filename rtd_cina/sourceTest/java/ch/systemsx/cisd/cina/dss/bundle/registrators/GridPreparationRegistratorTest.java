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

package ch.systemsx.cisd.cina.dss.bundle.registrators;

import java.io.File;
import java.io.IOException;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.cina.dss.bundle.registrators.BundleDataSetHelper.BundleRegistrationState;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class GridPreparationRegistratorTest extends AbstractFileSystemTestCase
{
    // Constants used in the test
    private static final String DB_CODE = "DB";

    private static final String SPACE_CODE = "SPACE";

    private static final String EXPERIMENT_IDENTIFIER = DB_CODE + ":/" + SPACE_CODE
            + "/PROJECT/EXP-1";

    private static final String GRID_SAMPLE_CODE = "GRID-CODE";

    private static final String GRID_SAMPLE_IDENTIFIER = DB_CODE + ":/" + SPACE_CODE + "/"
            + GRID_SAMPLE_CODE;

    private static final String REPLICA_SAMPLE_CODE = "REPLICA-CODE";

    private static final String REPLICA_SAMPLE_IDENTIFIER = DB_CODE + ":/" + SPACE_CODE + "/"
            + REPLICA_SAMPLE_CODE;

    private Mockery context;

    private IEncapsulatedOpenBISService openbisService;

    private IDataSetHandlerRpc delegator;

    private ExternalData externalData;

    private GridPreparationRegistrator registrator;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        context = new Mockery();
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        delegator = context.mock(IDataSetHandlerRpc.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions =
        { AssertionError.class })
    public void testRegistratorWithoutUserSuppliedExperiment()
    {
        setupOpenBisExpectations();
        setupSessionContextExpectations();

        File dataSetFile =
                new File("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/");
        createRegistrator(dataSetFile);

        context.checking(new Expectations()
            {
                {
                    final DataSetInformation dataSetInfo = new DataSetInformation();

                    one(delegator).getCallerDataSetInformation();
                    will(returnValue(dataSetInfo));
                }
            });

        registrator.register();

        context.assertIsSatisfied();
    }

    /**
     * First set up expectations for the case that the entities do not exist, then set up the
     * expectations for existing entities to simulate registering new entities.
     */
    @Test
    public void testRegistratorForNewEntities()
    {
        setupOpenBisExpectations();
        setupSessionContextExpectations();
        setupCallerDataSetInfoExpectations();

        File dataSetFile =
                new File("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/");

        context.checking(new Expectations()
            {
                // private long uniqueId = 1;
                //
                // private void delegatorHandleDataSetExpectation(final String path)
                // {
                // final DataSetInformation dataSetInformation = new DataSetInformation();
                // dataSetInformation.setDataSetCode("Derived");
                // dataSetInformation.setSampleCode("" + uniqueId++);
                // dataSetInformation.setSpaceCode(SPACE_CODE);
                // dataSetInformation.setInstanceCode("Test");
                //
                // allowing(delegator).linkAndHandleDataSet(with(new File(path)),
                // with(any(DataSetInformation.class)));
                // will(returnValue(Collections.singletonList(dataSetInformation)));
                // }

                {
                    // The Grid Prep does not yet exist
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(GRID_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(null));

                    // Create the Grid Prep
                    one(openbisService).registerSample(with(new BaseMatcher<NewSample>()
                        {
                            public boolean matches(Object item)
                            {
                                if (item instanceof NewSample)
                                {
                                    NewSample newSample = (NewSample) item;
                                    assertEquals(GRID_SAMPLE_IDENTIFIER, newSample.getIdentifier());
                                    assertEquals(EXPERIMENT_IDENTIFIER.toString(),
                                            newSample.getExperimentIdentifier());
                                    assertEquals(null, newSample.getParentIdentifier());
                                    return true;
                                }
                                return false;
                            }

                            public void describeTo(Description description)
                            {
                            }
                        }), with("test"));
                    will(returnValue(new Long(1)));

                    // The Replica does not yet exist
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(REPLICA_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(null));

                    // Create the Replica
                    one(openbisService).registerSample(with(new BaseMatcher<NewSample>()
                        {
                            public boolean matches(Object item)
                            {
                                if (item instanceof NewSample)
                                {
                                    NewSample newSample = (NewSample) item;
                                    assertEquals(REPLICA_SAMPLE_IDENTIFIER,
                                            newSample.getIdentifier());
                                    assertEquals(EXPERIMENT_IDENTIFIER.toString(),
                                            newSample.getExperimentIdentifier());
                                    assertEquals(GRID_SAMPLE_IDENTIFIER,
                                            newSample.getParentIdentifier());
                                    return true;
                                }
                                return false;
                            }

                            public void describeTo(Description description)
                            {
                            }
                        }), with("test"));
                    will(returnValue(new Long(2)));

                    // delegatorHandleDataSetExpectation("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/Replica for MRC files/MRC for Thomas/test20090422_BacklashRef.mrc");
                    // delegatorHandleDataSetExpectation("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/Replica for MRC files/MRC for Thomas/test20090424_TrackAtZeroRef.mrc");
                    // delegatorHandleDataSetExpectation("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/Replica for STEM files/STEM/stem_134588_1.imag");
                }
            });

        setupExistingGridPrepExpectations();
        setupExistingReplicaExpectations();

        createRegistrator(dataSetFile);
        registrator.register();

        context.assertIsSatisfied();
    }

    private void setupOpenBisExpectations()
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(CinaConstants.REPLICA_SAMPLE_TYPE_CODE);
        sampleType.setAutoGeneratedCode(true);
        sampleType.setGeneratedCodePrefix("Replica-");

        DataSetType dataSetType = new DataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
        final DataSetTypeWithVocabularyTerms dataSetTypeWithTerms =
                new DataSetTypeWithVocabularyTerms();
        dataSetTypeWithTerms.setDataSetType(dataSetType);

        externalData = new ExternalData();
        externalData.setCode("1");

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(openbisService).getSampleType(CinaConstants.REPLICA_SAMPLE_TYPE_CODE);
                    will(returnValue(sampleType));
                    one(openbisService).getDataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
                    will(returnValue(dataSetTypeWithTerms));
                    // one(openbisService).tryGetDataSet("session-token", externalData.getCode());
                    // will(returnValue(externalData));
                }
            });
    }

    private void setupExistingGridPrepExpectations()
    {
        final Sample sample = new Sample();
        Experiment exp = new Experiment();
        exp.setIdentifier(EXPERIMENT_IDENTIFIER);
        sample.setExperiment(exp);
        sample.setIdentifier(GRID_SAMPLE_IDENTIFIER);

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(GRID_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(sample));
                }
            });
    }

    private void setupExistingReplicaExpectations()
    {
        final Sample sample = new Sample();
        Experiment exp = new Experiment();
        exp.setIdentifier(EXPERIMENT_IDENTIFIER);
        sample.setExperiment(exp);
        sample.setIdentifier(REPLICA_SAMPLE_IDENTIFIER);

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(REPLICA_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(sample));
                }
            });
    }

    private void createRegistrator(final File dataSet)
    {
        registrator = new GridPreparationRegistrator(createBundleRegistrationState(), dataSet);
    }

    // private void setupNewSampleExpectations(final File dataSet)
    // {
    // setupSessionContextExpectations();
    //
    // final DataSetInformation dataSetInformation = new DataSetInformation();
    // dataSetInformation.setDataSetCode(externalData.getCode());
    // dataSetInformation.setSampleCode("GRID-1");
    // dataSetInformation.setSpaceCode(SPACE_CODE);
    // dataSetInformation.setInstanceCode("Test");
    //
    // final Sample sample = new Sample();
    // Experiment exp = new Experiment();
    // exp.setIdentifier("/Space/Exp-1");
    // sample.setExperiment(exp);
    // sample.setIdentifier(dataSetInformation.getSampleIdentifier().toString());
    //
    // // set up the expectations
    // context.checking(new Expectations()
    // {
    // {
    // one(delegator).handleDataSet(dataSet);
    // will(returnValue(Collections.singletonList(dataSetInformation)));
    // one(delegator).getFileForExternalData(externalData);
    // will(returnValue(dataSet.getParentFile()));
    // }
    // });
    //
    // createRegistrator(dataSet);
    // }

    private void setupCallerDataSetInfoExpectations()
    {

        final DataSetInformation callerDataSetInfo = new DataSetInformation();
        callerDataSetInfo.setSpaceCode(SPACE_CODE);
        callerDataSetInfo.setInstanceCode(DB_CODE);
        callerDataSetInfo.setExperimentIdentifier(new ExperimentIdentifierFactory(
                EXPERIMENT_IDENTIFIER).createIdentifier());

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(delegator).getCallerDataSetInformation();
                    will(returnValue(callerDataSetInfo));
                }
            });
    }

    private void setupSessionContextExpectations()
    {
        final SessionContextDTO sessionContext = new SessionContextDTO();
        sessionContext.setSessionToken("session-token");
        sessionContext.setUserEmail("test@test.bar");
        sessionContext.setUserName("test");

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(delegator).getSessionContext();
                    will(returnValue(sessionContext));
                }
            });

    }

    private BundleRegistrationState createBundleRegistrationState()
    {
        SampleType replicaSampleType =
                openbisService.getSampleType(CinaConstants.REPLICA_SAMPLE_TYPE_CODE);
        DataSetTypeWithVocabularyTerms imageDataSetType =
                openbisService.getDataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
        return new BundleRegistrationState(delegator, openbisService, replicaSampleType,
                imageDataSetType);
    }

}
