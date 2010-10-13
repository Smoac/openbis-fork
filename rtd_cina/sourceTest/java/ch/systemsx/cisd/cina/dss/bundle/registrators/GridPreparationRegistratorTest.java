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
import java.util.Collections;

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
    private static final String TEST_USER_NAME = "test";

    private static final String SESSION_TOKEN = "session-token";

    private static final String RAW_IMAGES_DATA_SET_CODE = "RAW_IMAGES_DATA_SET_CODE";

    private static final String METADATA_DATA_SET_CODE = "METADATA_DATA_SET_CODE";

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

    private static abstract class MatcherNoDesc<T> extends BaseMatcher<T>
    {

        public void describeTo(Description description)
        {

        }

    }

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
                        }), with(TEST_USER_NAME));
                    will(returnValue(new Long(1)));

                    // The Replica does not yet exist
                    one(openbisService).tryGetSampleWithExperiment(
                            with(new SampleIdentifierFactory(REPLICA_SAMPLE_IDENTIFIER)
                                    .createIdentifier()));
                    will(returnValue(null));

                    // Create the Replica
                    one(openbisService).registerSample(with(new MatcherNoDesc<NewSample>()
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
                        }), with(TEST_USER_NAME));
                    will(returnValue(new Long(2)));
                }
            });

        setupExistingGridPrepExpectations();
        setupExistingReplicaExpectations();
        setupHandleRawDataSetExpectations("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/RawData/ReplicTest");
        setupHandleMetadataDataSetExpectations("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/ReplicTest");

        createRegistrator(dataSetFile);
        registrator.register();

        context.assertIsSatisfied();
    }

    private void setupOpenBisExpectations()
    {
        final SampleType gridPrepSampleType = new SampleType();
        gridPrepSampleType.setCode(CinaConstants.GRID_PREP_SAMPLE_TYPE_CODE);
        gridPrepSampleType.setAutoGeneratedCode(true);
        gridPrepSampleType.setGeneratedCodePrefix("GridPrep-");

        final SampleType replicaSampleType = new SampleType();
        replicaSampleType.setCode(CinaConstants.REPLICA_SAMPLE_TYPE_CODE);
        replicaSampleType.setAutoGeneratedCode(true);
        replicaSampleType.setGeneratedCodePrefix("Replica-");

        DataSetType dataSetType = new DataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
        final DataSetTypeWithVocabularyTerms rawImagesDataSetTypeWithTerms =
                new DataSetTypeWithVocabularyTerms();
        rawImagesDataSetTypeWithTerms.setDataSetType(dataSetType);

        dataSetType = new DataSetType(CinaConstants.METADATA_DATA_SET_TYPE_CODE);
        final DataSetTypeWithVocabularyTerms metadataDataSetTypeWithTerms =
                new DataSetTypeWithVocabularyTerms();
        metadataDataSetTypeWithTerms.setDataSetType(dataSetType);

        dataSetType = new DataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
        final DataSetTypeWithVocabularyTerms imageDataSetTypeWithTerms =
                new DataSetTypeWithVocabularyTerms();
        imageDataSetTypeWithTerms.setDataSetType(dataSetType);

        externalData = new ExternalData();
        externalData.setCode("1");

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(openbisService).getSampleType(CinaConstants.GRID_PREP_SAMPLE_TYPE_CODE);
                    will(returnValue(gridPrepSampleType));
                    one(openbisService).getSampleType(CinaConstants.REPLICA_SAMPLE_TYPE_CODE);
                    will(returnValue(replicaSampleType));
                    one(openbisService).getDataSetType(CinaConstants.RAW_IMAGES_DATA_SET_TYPE_CODE);
                    will(returnValue(rawImagesDataSetTypeWithTerms));
                    one(openbisService).getDataSetType(CinaConstants.METADATA_DATA_SET_TYPE_CODE);
                    will(returnValue(metadataDataSetTypeWithTerms));
                    one(openbisService).getDataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
                    will(returnValue(imageDataSetTypeWithTerms));
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

    private void setupHandleRawDataSetExpectations(final String path)
    {
        // Create the Raw Images Data Set
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(RAW_IMAGES_DATA_SET_CODE);
        dataSetInformation.setSampleCode(REPLICA_SAMPLE_CODE);
        dataSetInformation.setSpaceCode(SPACE_CODE);
        dataSetInformation.setInstanceCode(DB_CODE);

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(with(new File(path)),
                            with(new MatcherNoDesc<DataSetInformation>()
                                {
                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof DataSetInformation)
                                        {
                                            DataSetInformation dataSetInfo =
                                                    (DataSetInformation) item;
                                            assertEquals(REPLICA_SAMPLE_CODE,
                                                    dataSetInfo.getSampleCode());
                                            return true;
                                        }
                                        return false;
                                    }
                                }));
                    will(returnValue(Collections.singletonList(dataSetInformation)));
                }
            });
    }

    private void setupHandleMetadataDataSetExpectations(final String path)
    {
        // Create the Raw Images Data Set
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(METADATA_DATA_SET_CODE);
        dataSetInformation.setSampleCode(REPLICA_SAMPLE_CODE);
        dataSetInformation.setSpaceCode(SPACE_CODE);
        dataSetInformation.setInstanceCode(DB_CODE);

        externalData = new ExternalData();
        externalData.setCode("1");

        final File dataSetFile = new File(path);

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(with(dataSetFile),
                            with(new MatcherNoDesc<DataSetInformation>()
                                {
                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof DataSetInformation)
                                        {
                                            DataSetInformation dataSetInfo =
                                                    (DataSetInformation) item;
                                            assertEquals(REPLICA_SAMPLE_CODE,
                                                    dataSetInfo.getSampleCode());
                                            return true;
                                        }
                                        return false;
                                    }
                                }));
                    will(returnValue(Collections.singletonList(dataSetInformation)));
                    one(openbisService).tryGetDataSet(SESSION_TOKEN, METADATA_DATA_SET_CODE);
                    will(returnValue(externalData));
                    one(delegator).getFileForExternalData(externalData);
                    will(returnValue(dataSetFile.getParentFile()));
                }
            });
    }

    private void createRegistrator(final File dataSet)
    {
        registrator = new GridPreparationRegistrator(createBundleRegistrationState(), dataSet);
    }

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
        sessionContext.setSessionToken(SESSION_TOKEN);
        sessionContext.setUserEmail("test@test.bar");
        sessionContext.setUserName(TEST_USER_NAME);

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
        return new BundleRegistrationState(delegator, openbisService);
    }

}
