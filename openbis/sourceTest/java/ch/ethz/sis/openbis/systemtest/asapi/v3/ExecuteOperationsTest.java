/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability.AVAILABLE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability.TIMED_OUT;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability.TIME_OUT_PENDING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.PropertyHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionState;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.UpdateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.UpdateSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.UpdateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.UpdateSpacesOperationResult;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class ExecuteOperationsTest extends AbstractOperationExecutionTest
{

    private static final int SECONDS_PER_HOUR = 60 * 60;

    private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;

    @Autowired
    private PlatformTransactionManager txManager;

    @Override
    @BeforeClass(alwaysRun = true)
    public void beforeClass()
    {
        super.beforeClass();
        getMarkTimeOutPendingMaintenancePlugin().shutdown();
        getMarkTimedOutOrDeletedMaintenancePlugin().shutdown();
    }

    @Override
    @AfterClass(alwaysRun = true)
    public void afterClass()
    {
        super.afterClass();
        getMarkTimeOutPendingMaintenancePlugin().start();
        getMarkTimedOutOrDeletedMaintenancePlugin().start();
    }

    @Test
    public void testExecuteWithDuplicatedExecutionId()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        v3api.executeOperations(sessionToken, Arrays.asList(new CreateSpacesOperation(spaceCreation())), options);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.executeOperations(sessionToken, Arrays.asList(new CreateSpacesOperation(spaceCreation())), options);
                }
            }, "already exists in the database");
    }

    @Test
    public void testExecuteWithMultipleSynchronousOperationsThatAllSucceed()
    {
        testExecuteWithMultipleOperationsThatAllSucceed(true);
    }

    @Test
    public void testExecuteWithMultipleAsynchronousOperationsThatAllSucceed()
    {
        testExecuteWithMultipleOperationsThatAllSucceed(false);
    }

    private void testExecuteWithMultipleOperationsThatAllSucceed(boolean synchronous)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceCreation creation1 = spaceCreation();
        SpaceUpdate update2 = new SpaceUpdate();
        update2.setSpaceId(new SpacePermId(creation1.getCode()));
        update2.setDescription("updated description");
        SpaceCreation creation3 = spaceCreation();

        List<? extends IOperation> operations =
                Arrays.asList(new CreateSpacesOperation(creation1), new UpdateSpacesOperation(update2), new CreateSpacesOperation(creation3));

        List<? extends IOperationResult> results = null;

        if (synchronous)
        {
            SynchronousOperationExecutionOptions synchronousOptions = new SynchronousOperationExecutionOptions();
            SynchronousOperationExecutionResults synchronousResults = (SynchronousOperationExecutionResults) v3api.executeOperations(sessionToken,
                    operations, synchronousOptions);
            results = synchronousResults.getResults();
        } else
        {
            AsynchronousOperationExecutionOptions asynchronousOptions = new AsynchronousOperationExecutionOptions();
            AsynchronousOperationExecutionResults asynchronousResults = (AsynchronousOperationExecutionResults) v3api.executeOperations(sessionToken,
                    operations, asynchronousOptions);
            OperationExecution asynchronousExecution = waitAndGetExecutionInState(sessionToken, asynchronousResults.getExecutionId(),
                    OperationExecutionState.FINISHED, fullOperationExecutionFetchOptions());
            results = asynchronousExecution.getDetails().getResults();
        }

        assertEquals(results.size(), 3);

        CreateSpacesOperationResult result1 = (CreateSpacesOperationResult) results.get(0);
        UpdateSpacesOperationResult result2 = (UpdateSpacesOperationResult) results.get(1);
        CreateSpacesOperationResult result3 = (CreateSpacesOperationResult) results.get(2);

        SpacePermId spaceId1 = new SpacePermId(creation1.getCode());
        SpacePermId spaceId3 = new SpacePermId(creation3.getCode());

        assertEquals(result1.getObjectIds(), Arrays.asList(spaceId1));
        assertEquals(result2.getObjectIds(), Arrays.asList(spaceId1));
        assertEquals(result3.getObjectIds(), Arrays.asList(spaceId3));

        Map<ISpaceId, Space> spaces = v3api.getSpaces(sessionToken, Arrays.asList(spaceId1, spaceId3), new SpaceFetchOptions());

        assertEquals(spaces.size(), 2);
        assertEquals(spaces.get(spaceId1).getCode(), creation1.getCode());
        assertEquals(spaces.get(spaceId1).getDescription(), update2.getDescription().getValue());
        assertEquals(spaces.get(spaceId3).getCode(), creation3.getCode());

        v3api.deleteSpaces(sessionToken, Arrays.asList(spaceId1, spaceId3), new SpaceDeletionOptions().setReason("test"));
    }

    @Test
    public void testExecuteWithMultipleSynchronousOperationsThatSomeFail()
    {
        testExecuteWithMultipleOperationsThatSomeFail(true);
    }

    @Test
    public void testExecuteWithMultipleAsynchronousOperationsThatSomeFail()
    {
        testExecuteWithMultipleOperationsThatSomeFail(false);
    }

    private void testExecuteWithMultipleOperationsThatSomeFail(boolean synchronous)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SpaceCreation creation1 = spaceCreation();
        final SpaceUpdate update = new SpaceUpdate();
        update.setSpaceId(null);
        final SpaceCreation creation2 = spaceCreation();

        final SpacePermId id1 = new SpacePermId(creation1.getCode());
        final SpacePermId id2 = new SpacePermId(creation2.getCode());

        final List<? extends IOperation> operations =
                Arrays.asList(new CreateSpacesOperation(creation1), new UpdateSpacesOperation(update), new CreateSpacesOperation(creation2));

        if (synchronous)
        {
            TransactionStatus transaction =
                    txManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

            try
            {
                Assert.assertFalse(transaction.isRollbackOnly());
                assertUserFailureException(new IDelegatedAction()
                    {
                        @Override
                        public void execute()
                        {
                            v3api.executeOperations(sessionToken, operations, new SynchronousOperationExecutionOptions());
                        }
                    }, "Space id cannot be null");
                Assert.assertTrue(transaction.isRollbackOnly());

                Map<ISpaceId, Space> spaces = v3api.getSpaces(sessionToken, Arrays.asList(id1, id2), new SpaceFetchOptions());
                assertEquals(spaces.size(), 2); // operations are grouped by type before execution, therefore 2 creations got already executed before
                                                // the failing update
            } finally
            {
                txManager.rollback(transaction);
            }

            Map<ISpaceId, Space> spaces = v3api.getSpaces(sessionToken, Arrays.asList(id1, id2), new SpaceFetchOptions());
            assertEquals(spaces.size(), 0);
        } else
        {
            AsynchronousOperationExecutionResults asynchronousResults = (AsynchronousOperationExecutionResults) v3api.executeOperations(sessionToken,
                    operations, new AsynchronousOperationExecutionOptions());
            waitAndGetExecutionInState(sessionToken, asynchronousResults.getExecutionId(), OperationExecutionState.FAILED,
                    emptyOperationExecutionFetchOptions());

            Map<ISpaceId, Space> spaces = v3api.getSpaces(sessionToken, Arrays.asList(id1, id2), new SpaceFetchOptions());
            assertEquals(spaces.size(), 0);
        }
    }

    @Test
    public void testExecuteWithRelatedOperationsThatCreateAndUseTheSameObjectByCreationId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation parentCreation = new SampleCreation();
        parentCreation.setCode("SAMPLE_PARENT");
        parentCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        parentCreation.setSpaceId(new SpacePermId("CISD"));
        parentCreation.setCreationId(new CreationId("parentid"));

        SampleCreation childCreation = new SampleCreation();
        childCreation.setCode("SAMPLE_CHILDREN");
        childCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        childCreation.setSpaceId(new SpacePermId("CISD"));
        childCreation.setCreationId(new CreationId("childid"));
        childCreation.setParentIds(Arrays.asList(parentCreation.getCreationId()));

        List<? extends IOperation> operations = Arrays.asList(new CreateSamplesOperation(parentCreation), new CreateSamplesOperation(childCreation));
        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();

        SynchronousOperationExecutionResults results = (SynchronousOperationExecutionResults) v3api.executeOperations(sessionToken,
                operations, options);

        assertEquals(results.getResults().size(), 2);

        CreateSamplesOperationResult parentResult = (CreateSamplesOperationResult) results.getResults().get(0);
        CreateSamplesOperationResult childResult = (CreateSamplesOperationResult) results.getResults().get(1);

        ISampleId parentId = parentResult.getObjectIds().get(0);
        ISampleId childId = childResult.getObjectIds().get(0);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withParents();

        Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken, Arrays.asList(parentId, childId), fo);

        assertEquals(samples.size(), 2);

        Sample parent = samples.get(parentId);
        Sample child = samples.get(childId);

        assertEquals(parent.getCode(), parentCreation.getCode());
        assertEquals(child.getCode(), childCreation.getCode());
        assertEquals(child.getParents().size(), 1);
        assertEquals(child.getParents().get(0), parent);
    }

    @Test
    public void testExecuteWithRelatedOperationsThatCreateAndUpdateTheSameObjectByIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_REUSED");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("COMMENT", "created");

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/CISD/SAMPLE_REUSED"));
        update.setProperty("COMMENT", "updated");

        List<? extends IOperation> operations = Arrays.asList(new CreateSamplesOperation(creation), new UpdateSamplesOperation(update));
        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();

        SynchronousOperationExecutionResults results = (SynchronousOperationExecutionResults) v3api.executeOperations(sessionToken,
                operations, options);

        assertEquals(results.getResults().size(), 2);

        CreateSamplesOperationResult createResult = (CreateSamplesOperationResult) results.getResults().get(0);
        UpdateSamplesOperationResult updateResult = (UpdateSamplesOperationResult) results.getResults().get(1);

        SamplePermId createId = createResult.getObjectIds().get(0);
        SamplePermId updateId = updateResult.getObjectIds().get(0);

        assertEquals(createId, updateId);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();
        fo.withHistory();

        Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken, Arrays.asList(createId), fo);

        assertEquals(samples.size(), 1);

        Sample sample = samples.get(createId);

        assertEquals(sample.getPermId(), createId);
        assertEquals(sample.getProperties().size(), 1);
        assertEquals(sample.getProperty("COMMENT"), "updated");
        assertEquals(sample.getHistory().size(), 1);

        PropertyHistoryEntry historyEntry = (PropertyHistoryEntry) sample.getHistory().get(0);

        assertEquals(historyEntry.getPropertyName(), "COMMENT");
        assertEquals(historyEntry.getPropertyValue(), "created");
    }

    @Test
    public void testExecuteWithSynchronousOperationThatSucceedsAndExecutionIdNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceCreation creation = spaceCreation();

        List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(creation));
        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();

        List<OperationExecution> beforeList = listExecutions(sessionToken);

        SynchronousOperationExecutionResults results = (SynchronousOperationExecutionResults) v3api.executeOperations(sessionToken,
                operations, options);

        assertEquals(results.getResults().size(), 1);

        CreateSpacesOperationResult result = (CreateSpacesOperationResult) results.getResults().get(0);
        assertEquals(result.getObjectIds(), Arrays.asList(new SpacePermId(creation.getCode())));

        List<OperationExecution> afterList = listExecutions(sessionToken);

        assertEquals(extractCodes(beforeList), extractCodes(afterList));
    }

    @Test
    public void testExecuteWithSynchronousOperationThatSucceedsAndExecutionIdNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceCreation creation = spaceCreation();

        List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(creation));
        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        OperationExecution beforeExecution = getExecution(sessionToken, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNull(beforeExecution);

        SynchronousOperationExecutionResults results = (SynchronousOperationExecutionResults) v3api.executeOperations(sessionToken,
                operations, options);

        assertEquals(results.getResults().size(), 1);

        CreateSpacesOperationResult result = (CreateSpacesOperationResult) results.getResults().get(0);
        assertEquals(result.getObjectIds(), Arrays.asList(new SpacePermId(creation.getCode())));

        OperationExecution afterExecution =
                getExecutionInState(sessionToken, options.getExecutionId(), OperationExecutionState.FINISHED, emptyOperationExecutionFetchOptions());

        assertNotNull(afterExecution);

        assertAvailabilities(afterExecution, AVAILABLE, defaultAvalability(), AVAILABLE, defaultSummaryAvalability(), AVAILABLE,
                defaultDetailsAvalability());
    }

    @Test
    public void testExecuteWithSynchronousOperationThatFailsAndExecutionIdNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(spaceCreation(null)));
        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();

        List<OperationExecution> beforeList = listExecutions(sessionToken);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.executeOperations(sessionToken, operations, options);
                }
            }, "Code cannot be empty");

        List<OperationExecution> afterList = listExecutions(sessionToken);

        assertEquals(extractCodes(beforeList), extractCodes(afterList));
    }

    @Test
    public void testExecuteWithSynchronousOperationThatFailsAndExecutionIdNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(spaceCreation(null)));
        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        OperationExecution beforeExecution = getExecution(sessionToken, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNull(beforeExecution);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.executeOperations(sessionToken, operations, options);
                }
            }, "Code cannot be empty");

        OperationExecution afterExecution =
                getExecutionInState(sessionToken, options.getExecutionId(), OperationExecutionState.FAILED, emptyOperationExecutionFetchOptions());

        assertNotNull(afterExecution);
        assertAvailabilities(afterExecution, AVAILABLE, defaultAvalability(), AVAILABLE, defaultSummaryAvalability(), AVAILABLE,
                defaultDetailsAvalability());
    }

    @Test
    public void testExecuteWithAsynchronousOperationThatSucceeds()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceCreation creation = spaceCreation();

        List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(creation));
        AsynchronousOperationExecutionOptions options = new AsynchronousOperationExecutionOptions();

        List<String> beforeList = extractCodes(listExecutions(sessionToken));

        AsynchronousOperationExecutionResults results = (AsynchronousOperationExecutionResults) v3api.executeOperations(sessionToken,
                operations, options);

        List<String> afterList = extractCodes(listExecutions(sessionToken));

        beforeList.add(results.getExecutionId().getPermId());
        assertEquals(beforeList, afterList);

        OperationExecution execution = waitAndGetExecutionInState(sessionToken, results.getExecutionId(), OperationExecutionState.FINISHED,
                fullOperationExecutionFetchOptions());

        assertNotNull(execution);
        assertAvailabilities(execution, AVAILABLE, defaultAvalability(), AVAILABLE, defaultSummaryAvalability(), AVAILABLE,
                defaultDetailsAvalability());

        CreateSpacesOperationResult operationResult = (CreateSpacesOperationResult) execution.getDetails().getResults().get(0);
        assertEquals(1, operationResult.getObjectIds().size());
        v3api.deleteSpaces(sessionToken, operationResult.getObjectIds(), new SpaceDeletionOptions().setReason("test"));
    }

    @Test
    public void testExecuteWithAsynchronousOperationThatFails()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceCreation creation = spaceCreation(null);

        List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(creation));
        AsynchronousOperationExecutionOptions options = new AsynchronousOperationExecutionOptions();

        List<String> beforeList = extractCodes(listExecutions(sessionToken));

        AsynchronousOperationExecutionResults results = (AsynchronousOperationExecutionResults) v3api.executeOperations(sessionToken,
                operations, options);

        List<String> afterList = extractCodes(listExecutions(sessionToken));

        beforeList.add(results.getExecutionId().getPermId());
        assertEquals(beforeList, afterList);

        OperationExecution execution = waitAndGetExecutionInState(sessionToken, results.getExecutionId(), OperationExecutionState.FAILED,
                fullOperationExecutionFetchOptions());

        assertNotNull(execution);
        assertAvailabilities(execution, AVAILABLE, defaultAvalability(), AVAILABLE, defaultSummaryAvalability(), AVAILABLE,
                defaultDetailsAvalability());

        AssertionUtil.assertContains("Code cannot be empty", execution.getSummary().getError());
    }

    @Test
    public void testExecuteWithNullAvailabilities()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution = executeWithAvailabilities(sessionToken, null, null, null);
        assertAvailabilities(execution, AVAILABLE, defaultAvalability(), AVAILABLE, defaultSummaryAvalability(), AVAILABLE,
                defaultDetailsAvalability());
    }

    @Test
    public void testExecuteWithZeroAvailabilities()
    {
        final Integer availability = 0;
        final Integer summaryAvailability = 0;
        final Integer detailsAvailability = 0;

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution = executeWithAvailabilities(sessionToken, availability, summaryAvailability, detailsAvailability);
        assertAvailabilities(execution, TIME_OUT_PENDING, availability, TIME_OUT_PENDING, summaryAvailability, TIME_OUT_PENDING, detailsAvailability);

        getMarkTimedOutOrDeletedMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), emptyOperationExecutionFetchOptions());
        assertNull(execution);
    }

    @Test
    public void testExecuteWithNonZeroAvailabilities()
    {
        final Integer availability = 1;
        final Integer summaryAvailability = 1;
        final Integer detailsAvailability = 1;

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution = executeWithAvailabilities(sessionToken, availability, summaryAvailability, detailsAvailability);
        assertAvailabilities(execution, AVAILABLE, availability, AVAILABLE, summaryAvailability, AVAILABLE, detailsAvailability);

        sleep(availability * DateUtils.MILLIS_PER_SECOND);
        getMarkTimeOutPendingMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), emptyOperationExecutionFetchOptions());
        assertAvailabilities(execution, TIME_OUT_PENDING, availability, TIME_OUT_PENDING, summaryAvailability, TIME_OUT_PENDING, detailsAvailability);

        getMarkTimedOutOrDeletedMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), emptyOperationExecutionFetchOptions());
        assertNull(execution);
    }

    @Test
    public void testExecuteWithNegativeAvailabilities()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution =
                executeWithAvailabilities(sessionToken, -1, -1, -1);
        assertAvailabilities(execution, TIME_OUT_PENDING, 0, TIME_OUT_PENDING, 0, TIME_OUT_PENDING, 0);
    }

    @Test
    public void testExecuteWithGreaterThanMaxAvailabilities()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution =
                executeWithAvailabilities(sessionToken, defaultAvalability() + 1, defaultSummaryAvalability() + 1, defaultDetailsAvalability() + 1);
        assertAvailabilities(execution, AVAILABLE, defaultAvalability(), AVAILABLE, defaultSummaryAvalability(), AVAILABLE,
                defaultDetailsAvalability());
    }

    @Test
    public void testExecuteWithZeroAvailability()
    {
        final Integer availability = 0;
        final Integer summaryAvailability = SECONDS_PER_HOUR;
        final Integer detailsAvailability = SECONDS_PER_DAY;

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution = executeWithAvailabilities(sessionToken, availability, summaryAvailability, detailsAvailability);
        assertAvailabilities(execution, TIME_OUT_PENDING, availability, AVAILABLE, summaryAvailability, AVAILABLE, detailsAvailability);

        getMarkTimedOutOrDeletedMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), emptyOperationExecutionFetchOptions());
        assertNull(execution);
    }

    @Test
    public void testExecuteWithNonZeroAvailability()
    {
        final Integer availability = 1;
        final Integer summaryAvailability = SECONDS_PER_HOUR;
        final Integer detailsAvailability = SECONDS_PER_DAY;

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution = executeWithAvailabilities(sessionToken, availability, summaryAvailability, detailsAvailability);
        assertAvailabilities(execution, AVAILABLE, availability, AVAILABLE, summaryAvailability, AVAILABLE, detailsAvailability);

        sleep(availability * DateUtils.MILLIS_PER_SECOND);
        getMarkTimeOutPendingMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), emptyOperationExecutionFetchOptions());
        assertAvailabilities(execution, TIME_OUT_PENDING, availability, AVAILABLE, summaryAvailability, AVAILABLE, detailsAvailability);

        getMarkTimedOutOrDeletedMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), emptyOperationExecutionFetchOptions());
        assertNull(execution);
    }

    @Test
    public void testExecuteWithZeroSummaryAvailability()
    {
        final Integer availability = SECONDS_PER_HOUR;
        final Integer summaryAvailability = 0;
        final Integer detailsAvailability = SECONDS_PER_DAY;

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution = executeWithAvailabilities(sessionToken, availability, summaryAvailability, detailsAvailability);
        assertAvailabilities(execution, AVAILABLE, availability, TIME_OUT_PENDING, summaryAvailability, AVAILABLE, detailsAvailability);
        assertNotNull(execution.getSummary());

        getMarkTimedOutOrDeletedMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), fullOperationExecutionFetchOptions());
        assertAvailabilities(execution, AVAILABLE, availability, TIMED_OUT, summaryAvailability, AVAILABLE, detailsAvailability);
        assertNull(execution.getSummary());
    }

    @Test
    public void testExecuteWithNonZeroSummaryAvailability()
    {
        final Integer availability = SECONDS_PER_HOUR;
        final Integer summaryAvailability = 1;
        final Integer detailsAvailability = SECONDS_PER_DAY;

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution = executeWithAvailabilities(sessionToken, availability, summaryAvailability, detailsAvailability);
        assertAvailabilities(execution, AVAILABLE, availability, AVAILABLE, summaryAvailability, AVAILABLE, detailsAvailability);
        assertNotNull(execution.getSummary());

        sleep(summaryAvailability * DateUtils.MILLIS_PER_SECOND);
        getMarkTimeOutPendingMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), fullOperationExecutionFetchOptions());
        assertAvailabilities(execution, AVAILABLE, availability, TIME_OUT_PENDING, summaryAvailability, AVAILABLE, detailsAvailability);
        assertNotNull(execution.getSummary());

        getMarkTimedOutOrDeletedMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), fullOperationExecutionFetchOptions());
        assertAvailabilities(execution, AVAILABLE, availability, TIMED_OUT, summaryAvailability, AVAILABLE, detailsAvailability);
        assertNull(execution.getSummary());
    }

    @Test
    public void testExecuteWithZeroDetailsAvailability()
    {
        final Integer availability = SECONDS_PER_HOUR;
        final Integer summaryAvailability = SECONDS_PER_DAY;
        final Integer detailsAvailability = 0;

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution = executeWithAvailabilities(sessionToken, availability, summaryAvailability, detailsAvailability);
        assertAvailabilities(execution, AVAILABLE, availability, AVAILABLE, summaryAvailability, TIME_OUT_PENDING, detailsAvailability);
        assertNotNull(execution.getDetails());

        getMarkTimedOutOrDeletedMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), fullOperationExecutionFetchOptions());
        assertAvailabilities(execution, AVAILABLE, availability, AVAILABLE, summaryAvailability, TIMED_OUT, detailsAvailability);
        assertNull(execution.getDetails());
    }

    @Test
    public void testExecuteWithNonZeroDetailsAvailability()
    {
        final Integer availability = SECONDS_PER_HOUR;
        final Integer summaryAvailability = SECONDS_PER_DAY;
        final Integer detailsAvailability = 1;

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        OperationExecution execution = executeWithAvailabilities(sessionToken, availability, summaryAvailability, detailsAvailability);
        assertAvailabilities(execution, AVAILABLE, availability, AVAILABLE, summaryAvailability, AVAILABLE, detailsAvailability);
        assertNotNull(execution.getDetails());

        sleep(detailsAvailability * DateUtils.MILLIS_PER_SECOND);
        getMarkTimeOutPendingMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), fullOperationExecutionFetchOptions());
        assertAvailabilities(execution, AVAILABLE, availability, AVAILABLE, summaryAvailability, TIME_OUT_PENDING, detailsAvailability);
        assertNotNull(execution.getDetails());

        getMarkTimedOutOrDeletedMaintenancePlugin().execute();

        execution = getExecution(sessionToken, execution.getPermId(), fullOperationExecutionFetchOptions());
        assertAvailabilities(execution, AVAILABLE, availability, AVAILABLE, summaryAvailability, TIMED_OUT, detailsAvailability);
        assertNull(execution.getDetails());
    }

    private OperationExecution executeWithAvailabilities(String sessionToken, Integer availability, Integer summaryAvailability,
            Integer detailsAvailability)
    {
        List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(spaceCreation()));

        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());
        options.setAvailabilityTime(availability);
        options.setSummaryAvailabilityTime(summaryAvailability);
        options.setDetailsAvailabilityTime(detailsAvailability);

        v3api.executeOperations(sessionToken, operations, options);

        return getExecution(sessionToken, options.getExecutionId(), fullOperationExecutionFetchOptions());
    }

    private void assertAvailabilities(OperationExecution execution, OperationExecutionAvailability availability,
            Integer availabilityTime, OperationExecutionAvailability summaryAvailability, Integer summaryAvailabilityTime,
            OperationExecutionAvailability detailsAvailability, Integer detailsAvailabilityTime)
    {
        assertEquals(execution.getAvailability(), availability);
        assertEquals(execution.getSummaryAvailability(), summaryAvailability);
        assertEquals(execution.getDetailsAvailability(), detailsAvailability);

        assertEquals(execution.getAvailabilityTime(), availabilityTime);
        assertEquals(execution.getSummaryAvailabilityTime(), summaryAvailabilityTime);
        assertEquals(execution.getDetailsAvailabilityTime(), detailsAvailabilityTime);
    }

    private int defaultAvalability()
    {
        return SECONDS_PER_DAY * 365;
    }

    private int defaultSummaryAvalability()
    {
        return SECONDS_PER_DAY * 30;
    }

    private int defaultDetailsAvalability()
    {
        return SECONDS_PER_DAY;
    }

}