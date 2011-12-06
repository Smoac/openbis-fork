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

package ch.systemsx.cisd.etlserver.registrator;

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.hamcrest.core.IsAnything;
import org.jmock.Expectations;
import org.python.core.PyException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.eodsql.MockDataSet;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetRegistratorTest extends AbstractJythonDataSetHandlerTest
{
    private static final String SCRIPTS_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/etlserver/registrator/";

    private static final String DATA_SET_CODE = "data-set-code";

    private static final String CONTAINER_DATA_SET_CODE = "container-data-set-code";

    private static final DataSetType DATA_SET_TYPE = new DataSetType("O1");

    private static final String EXPERIMENT_PERM_ID = "experiment-perm-id";

    private static final String EXPERIMENT_IDENTIFIER = "/SPACE/PROJECT/EXP";

    private static final String SAMPLE_PERM_ID = "sample-perm-id";

    private BufferedAppender logAppender;

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();

        logAppender = new BufferedAppender();

        didDataSetRollbackHappen = false;
        didServiceRollbackHappen = false;
    }

    @Test
    public void testSimpleTransaction()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("simple-transaction.py");
        createHandler(properties, false, true);
        createData();

        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });

        handler.handle(markerFile);

        TestingDataSetHandler theHandler = (TestingDataSetHandler) handler;
        assertTrue(theHandler.didCommitTransactionFunctionRunHappen);
        assertFalse(theHandler.didRollbackTransactionFunctionRunHappen);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(1, atomicatOperationDetails.recordedObject().getDataSetRegistrations().size());

        NewExternalData dataSet =
                atomicatOperationDetails.recordedObject().getDataSetRegistrations().get(0);

        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());
        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());
        assertEquals(new File(stagingDirectory, DATA_SET_CODE + "-storage"),
                MockStorageProcessor.instance.rootDirs.get(0));
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        File incomingDir = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"),
                incomingDir);
        assertEquals("hello world1",
                FileUtilities.loadToString(new File(datasetLocation, "read1.me")).trim());
        assertEquals("", logAppender.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testFileNotFound()
    {
        setUpHomeDataBaseExpectations();
        Properties properties = createThreadPropertiesRelativeToScriptsFolder("file-not-found.py");
        createHandler(properties, false, true);
        createData();

        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                }
            });

        try
        {
            handler.handle(markerFile);
            fail("Expected a FileNotFound exception.");
        } catch (PyException pyException)
        {
            IOExceptionUnchecked tunnel = (IOExceptionUnchecked) pyException.getCause();
            FileNotFoundException ex = (FileNotFoundException) tunnel.getCause();
            assertTrue(ex.getMessage().startsWith("Neither '/non/existent/path' nor '"));
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testSimpleTransactionExplicitRollback()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("simple-transaction-rollback.py");
        createHandler(properties, true, false);
        createData();
        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));
                }
            });

        handler.handle(markerFile);
        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);
        assertEquals("[]", Arrays.asList(stagingDirectory.list()).toString());
        assertEquals(
                "hello world1",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_1/read1.me")).trim());
        assertEquals(
                "hello world2",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_2/read2.me")).trim());

        TestingDataSetHandler theHandler = (TestingDataSetHandler) handler;
        assertFalse(theHandler.didRollbackDataSetRegistrationFunctionRun);
        assertFalse(theHandler.didRollbackServiceFunctionRun);

        // These do not get called when the caller herself invokes a rollback
        assertFalse(theHandler.didTransactionRollbackHappen);
        assertFalse(theHandler.didRollbackTransactionFunctionRunHappen);

        context.assertIsSatisfied();
    }

    @Test
    public void testSimpleTransactionRollback()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("simple-transaction.py");
        createHandler(properties, false, false);
        createData();
        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
                    one(openBisService)
                            .performEntityOperations(
                                    with(new IsAnything<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>()));
                    will(throwException(new AssertionError("Fail")));
                }
            });

        handler.handle(markerFile);
        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);
        assertEquals("[]", Arrays.asList(stagingDirectory.list()).toString());
        assertEquals(
                "hello world1",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_1/read1.me")).trim());
        assertEquals(
                "hello world2",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_2/read2.me")).trim());

        TestingDataSetHandler theHandler = (TestingDataSetHandler) handler;
        assertFalse(theHandler.didRollbackDataSetRegistrationFunctionRun);
        assertFalse(theHandler.didRollbackServiceFunctionRun);
        assertTrue(theHandler.didTransactionRollbackHappen);
        assertTrue(theHandler.didRollbackTransactionFunctionRunHappen);

        context.assertIsSatisfied();
    }

    @Test
    public void testTwoSimpleDataSets()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("two-simple-datasets.py");
        createHandler(properties, false, true);
        createData();
        ExperimentBuilder builder1 = new ExperimentBuilder().identifier("/SPACE/PROJECT/EXP1");
        final Experiment experiment1 = builder1.getExperiment();
        ExperimentBuilder builder2 = new ExperimentBuilder().identifier("/SPACE/PROJECT/EXP2");
        final Experiment experiment2 = builder2.getExperiment();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> operations =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE + 1));

                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment1.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment1));

                    one(dataSetValidator).assertValidDataSet(
                            DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE + 1),
                                    "sub_data_set_1"));

                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE + 2));

                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment2.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment2));

                    one(dataSetValidator).assertValidDataSet(
                            DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE + 2),
                                    "sub_data_set_2"));

                    one(openBisService).performEntityOperations(with(operations));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });

        handler.handle(markerFile);

        assertEquals(2, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(2, MockStorageProcessor.instance.calledCommitCount);
        assertEquals(2, operations.recordedObject().getDataSetRegistrations().size());

        NewExternalData dataSet1 = operations.recordedObject().getDataSetRegistrations().get(0);
        NewExternalData dataSet2 = operations.recordedObject().getDataSetRegistrations().get(1);

        assertEquals(experiment1.getIdentifier(), dataSet1.getExperimentIdentifierOrNull()
                .toString());
        assertEquals(DATA_SET_CODE + 1, dataSet1.getCode());
        assertEquals(DATA_SET_TYPE, dataSet1.getDataSetType());
        File datasetLocation1 =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE + 1,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation1), dataSet1.getLocation());
        assertEquals(new File(stagingDirectory, DATA_SET_CODE + 1 + "-storage"),
                MockStorageProcessor.instance.rootDirs.get(0));
        File incomingDir1 = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE + 1), "sub_data_set_1"),
                incomingDir1);
        assertEquals("hello world1",
                FileUtilities.loadToString(new File(datasetLocation1, "read1.me")).trim());
        assertEquals(experiment2.getIdentifier(), dataSet2.getExperimentIdentifierOrNull()
                .toString());
        assertEquals(DATA_SET_CODE + 2, dataSet2.getCode());
        assertEquals(DATA_SET_TYPE, dataSet2.getDataSetType());
        File datasetLocation2 =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE + 2,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation2), dataSet2.getLocation());
        assertEquals(new File(stagingDirectory, DATA_SET_CODE + 2 + "-storage"),
                MockStorageProcessor.instance.rootDirs.get(1));
        File incomingDir2 = MockStorageProcessor.instance.incomingDirs.get(1);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE + 2), "sub_data_set_2"),
                incomingDir2);
        assertEquals("hello world2",
                FileUtilities.loadToString(new File(datasetLocation2, "read2.me")).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithNewExperiment()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-new-experiment.py");
        createHandler(properties, false, true);
        createData();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));

                    one(openBisService).createPermId();
                    will(returnValue(EXPERIMENT_PERM_ID));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(1, atomicatOperationDetails.recordedObject().getDataSetRegistrations().size());

        NewExternalData dataSet =
                atomicatOperationDetails.recordedObject().getDataSetRegistrations().get(0);

        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        NewProperty newProp = new NewProperty("dataSetProp", "dataSetPropValue");
        assertTrue(dataSet.getExtractableData().getDataSetProperties().contains(newProp));

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        File incomingDir = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"),
                incomingDir);
        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithNewSample()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-new-sample.py");
        createHandler(properties, false, true);
        createData();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));

                    one(openBisService).createPermId();
                    will(returnValue(EXPERIMENT_PERM_ID));

                    one(openBisService).createPermId();
                    will(returnValue(SAMPLE_PERM_ID));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails operations =
                atomicatOperationDetails.recordedObject();

        assertEquals(1, operations.getDataSetRegistrations().size());
        assertEquals(0, operations.getExperimentUpdates().size());
        assertEquals(0, operations.getSampleUpdates().size());
        assertEquals(1, operations.getSampleRegistrations().size());
        assertEquals(1, operations.getExperimentRegistrations().size());

        NewSample newSample = operations.getSampleRegistrations().get(0);
        assertEquals(SAMPLE_PERM_ID, newSample.getPermID());
        assertEquals(EXPERIMENT_IDENTIFIER, newSample.getExperimentIdentifier());
        assertEquals("sample_type", newSample.getSampleType().getCode());

        NewExperiment newExperiment = operations.getExperimentRegistrations().get(0);
        assertEquals(EXPERIMENT_PERM_ID, newExperiment.getPermID());
        assertEquals(EXPERIMENT_IDENTIFIER, newExperiment.getIdentifier());
        assertEquals("experiment_type", newExperiment.getExperimentTypeCode());

        NewExternalData dataSet = operations.getDataSetRegistrations().get(0);
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        File incomingDir = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"),
                incomingDir);
        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithNewMaterial()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-new-material.py");
        createHandler(properties, false, true);
        createData();

        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));

                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails operations =
                atomicatOperationDetails.recordedObject();

        assertEquals(1, operations.getDataSetRegistrations().size());
        assertEquals(0, operations.getExperimentUpdates().size());
        assertEquals(0, operations.getSampleUpdates().size());
        assertEquals(0, operations.getSampleRegistrations().size());
        assertEquals(0, operations.getExperimentRegistrations().size());
        assertEquals(1, operations.getMaterialRegistrations().size());

        NewMaterial newMaterial =
                operations.getMaterialRegistrations().get("new-material-type").get(0);
        assertEquals("new-material", newMaterial.getCode());
        assertEquals("[material-prop: material-prop-value]",
                Arrays.asList(newMaterial.getProperties()).toString());

        NewExternalData dataSet = operations.getDataSetRegistrations().get(0);
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithDataSetUpdate()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-dataset-update.py");
        createHandler(properties, false, true);
        createData();

        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();

        final ContainerDataSet containerDataSet = new ContainerDataSet();
        containerDataSet.setId(1L);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));

                    exactly(2).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(openBisService).tryGetDataSet(CONTAINER_DATA_SET_CODE);
                    will(returnValue(containerDataSet));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "data_set"));

                    one(openBisService).performEntityOperations(with(atomicOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails operations =
                atomicOperationDetails.recordedObject();

        assertEquals(1, operations.getDataSetRegistrations().size());
        assertEquals(0, operations.getExperimentUpdates().size());
        assertEquals(0, operations.getSampleUpdates().size());
        assertEquals(0, operations.getSampleRegistrations().size());
        assertEquals(0, operations.getExperimentRegistrations().size());
        assertEquals(1, operations.getDataSetUpdates().size());

        NewExternalData dataSet = operations.getDataSetRegistrations().get(0);
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        DataSetUpdatesDTO dataSetUpdate = operations.getDataSetUpdates().get(0);
        assertEquals(Arrays.asList(DATA_SET_CODE),
                Arrays.asList(dataSetUpdate.getModifiedContainedDatasetCodesOrNull()));

        EntityProperty propertyChanged =
                new PropertyBuilder("newProp").value("newValue").getProperty();
        assertEquals(Arrays.asList(propertyChanged).toString(), dataSetUpdate.getProperties()
                .toString());
        assertEquals(EXPERIMENT_IDENTIFIER, dataSetUpdate.getExperimentIdentifierOrNull()
                .toString());

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        context.assertIsSatisfied();
    }

    @Test
    public void testScriptDies()
    {
        setUpHomeDataBaseExpectations();

        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("dying-script.py");

        createHandler(threadProperties, false);
        createData();

        handler.handle(markerFile);

        assertTrue(logAppender.getLogContent(), logAppender.getLogContent().length() > 0);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);
        context.assertIsSatisfied();
    }

    private void createData()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");

        subDataSet1 = createDirectory(incomingDataSetFile, "sub_data_set_1");
        subDataSet2 = createDirectory(incomingDataSetFile, "sub_data_set_2");

        FileUtilities.writeToFile(new File(subDataSet1, "read1.me"), "hello world1");
        FileUtilities.writeToFile(new File(subDataSet2, "read2.me"), "hello world2");

        markerFile = new File(workingDirectory, IS_FINISHED_PREFIX + "data_set");
        FileUtilities.writeToFile(markerFile, "");
    }

    @Test
    public void testRollbackService()
    {
        setUpHomeDataBaseExpectations();

        // Create a handler that throws an exception during registration
        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("rollback-dying-script.py");
        createHandler(threadProperties, false);

        createData();

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        assertTrue(logAppender.getLogContent(), logAppender.getLogContent().length() > 0);

        TestingDataSetHandler theHandler = (TestingDataSetHandler) handler;
        assertFalse(theHandler.didRollbackDataSetRegistrationFunctionRun);
        assertTrue(theHandler.didRollbackServiceFunctionRun);
        context.assertIsSatisfied();
    }

    @Test
    public void testScriptPathDeletedLater()
    {
        setUpHomeDataBaseExpectations();
        String scriptPath = "foo.py";
        Properties threadProperties = createThreadProperties(scriptPath);

        // test the situation where script has been deleted later
        File scriptFile = new File(scriptPath);
        FileUtilities.writeToFile(scriptFile, "x");
        createHandler(threadProperties, false);
        FileUtilities.delete(scriptFile);

        createData();

        handler.handle(markerFile);

        assertFalse(didDataSetRollbackHappen);
        assertTrue(didServiceRollbackHappen);
        context.assertIsSatisfied();
    }

    @Test
    public void testScriptPathMissing()
    {
        setUpHomeDataBaseExpectations();
        String scriptPath = "foo.py";
        Properties threadProperties = createThreadProperties(scriptPath);

        // it should not be possible to create a handler if script does not exist
        try
        {
            createHandler(threadProperties, false);
            fail("The script should does not exist");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(ex.getMessage(), "Script file 'foo.py' does not exist!");
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testSearching()
    {
        setUpHomeDataBaseExpectations();
        Properties threadProperties = createThreadPropertiesRelativeToScriptsFolder("search.py");
        createHandler(threadProperties, false, true);

        createData();

        setUpSearchExpectations();

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        TestingDataSetHandler theHandler = (TestingDataSetHandler) handler;
        assertFalse(didServiceRollbackHappen);
        assertFalse(theHandler.didTransactionRollbackHappen);
        assertFalse(theHandler.didRollbackDataSetRegistrationFunctionRun);
        assertFalse(theHandler.didRollbackServiceFunctionRun);

        context.assertIsSatisfied();
    }

    @Test
    public void testQuerying()
    {
        setUpHomeDataBaseExpectations();
        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("query-interface-test.py");
        createHandler(threadProperties, false, true);

        createData();

        setUpQueryExpectations();

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        TestingDataSetHandler theHandler = (TestingDataSetHandler) handler;
        assertFalse(didServiceRollbackHappen);
        assertFalse(theHandler.didTransactionRollbackHappen);
        context.assertIsSatisfied();
    }

    @Test
    public void testDynamicQueryCommitFail()
    {
        setUpHomeDataBaseExpectations();
        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("dynamic-query-failure-test.py");
        createHandler(threadProperties, false, true);

        createData();

        setUpDynamicQueryExpectations();

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        TestingDataSetHandler theHandler = (TestingDataSetHandler) handler;
        assertFalse(didServiceRollbackHappen);
        assertFalse(theHandler.didTransactionRollbackHappen);
        assertTrue(theHandler.didSecondaryTransactionErrorNotificationHappen);
        context.assertIsSatisfied();
    }

    private Properties createThreadProperties(String scriptPath)
    {
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());
        threadProperties.put(JythonTopLevelDataSetHandler.SCRIPT_PATH_KEY, scriptPath);
        return threadProperties;
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return SCRIPTS_FOLDER;
    }

    @Test
    public void testNoScriptPath()
    {
        setUpHomeDataBaseExpectations();

        // omit the script path
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());

        try
        {
            createHandler(threadProperties, false);
            fail("Should not be able to create the handler without specifiying a script");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(
                    "Given key 'script-path' not found in properties '[delete-unidentified, storage-processor, incoming-data-completeness-condition, incoming-dir]'",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    private void createHandler(Properties threadProperties, final boolean registrationShouldFail)
    {
        createHandler(threadProperties, registrationShouldFail, false);
    }

    private void setUpSearchExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    ProjectIdentifier projectIdentifier =
                            new ProjectIdentifierFactory("/SPACE/PROJECT").createIdentifier();
                    oneOf(openBisService).listExperiments(projectIdentifier);

                    Experiment experiment = new Experiment();
                    experiment.setIdentifier("/SPACE/PROJECT/EXP-CODE");
                    experiment.setCode("EXP-CODE");
                    Person registrator = new Person();
                    registrator.setEmail("email@email.com");
                    experiment.setRegistrator(registrator);
                    will(returnValue(Arrays.asList(experiment)));

                    SearchCriteria searchCriteria = createTestSearchCriteria("DATA_SET_TYPE");
                    oneOf(openBisService).searchForDataSets(searchCriteria);
                    will(returnValue(Collections.EMPTY_LIST));

                    searchCriteria = createTestSearchCriteria("SAMPLE_TYPE");
                    oneOf(openBisService).searchForSamples(searchCriteria);
                    will(returnValue(Collections.EMPTY_LIST));

                    oneOf(openBisService)
                            .performEntityOperations(
                                    with(any(ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails.class)));
                }
            });
    }

    private void setUpQueryExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    oneOf(dataSourceQueryService).select("path-info-db",
                            "SELECT * from data_set_files WHERE parent_id is NULL");
                    Object[] args =
                        { 155555 };
                    will(returnValue(new MockDataSet<Map<String, Object>>()));
                    oneOf(dataSourceQueryService).select("path-info-db",
                            "SELECT * from data_set_files WHERE parent_id = ?1", args);
                    will(returnValue(new MockDataSet<Map<String, Object>>()));
                }
            });
    }

    private void setUpDynamicQueryExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    oneOf(dynamicTransactionQuery)
                            .select("SELECT * from data_set_files WHERE parent_id is NULL",
                                    (Object[]) null);
                    will(returnValue(new MockDataSet<Map<String, Object>>()));

                    oneOf(openBisService)
                            .performEntityOperations(
                                    with(any(ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails.class)));

                }
            });
    }

    protected SearchCriteria createTestSearchCriteria(String typeString)
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, typeString));
        sc.addMatchClause(MatchClause.createPropertyMatch("PROP", "VALUE"));
        return sc;
    }
}
