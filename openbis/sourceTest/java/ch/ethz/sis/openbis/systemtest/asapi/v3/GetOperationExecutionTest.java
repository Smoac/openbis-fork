/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionProgress;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionDetails;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionState;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionSummary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class GetOperationExecutionTest extends AbstractOperationExecutionTest
{

    @Test
    public void testGetByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(spaceCreation()));

        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        OperationExecution executionBefore = getExecution(sessionToken, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNull(executionBefore);

        v3api.executeOperations(sessionToken, operations, options);

        OperationExecution executionAfter = getExecution(sessionToken, options.getExecutionId(), emptyOperationExecutionFetchOptions());

        assertNotNull(executionAfter);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetForRegularUser()
    {
        String sessionTokenAdmin = v3api.login(TEST_USER, PASSWORD);
        String sessionTokenUser = v3api.login(TEST_SPACE_USER, PASSWORD);

        OperationExecutionPermId executionId = new OperationExecutionPermId();
        List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(spaceCreation()));

        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(executionId);

        OperationExecution executionBefore = getExecution(sessionTokenAdmin, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNull(executionBefore);

        executionBefore = getExecution(sessionTokenUser, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNull(executionBefore);

        v3api.executeOperations(sessionTokenAdmin, operations, options);

        OperationExecution executionAfter = getExecution(sessionTokenAdmin, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNotNull(executionAfter);

        executionAfter = getExecution(sessionTokenUser, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNull(executionAfter);

        v3api.logout(sessionTokenAdmin);
        v3api.logout(sessionTokenUser);
    }

    @Test
    public void testGetForAdminUser()
    {
        String sessionTokenAdmin = v3api.login(TEST_USER, PASSWORD);
        String sessionTokenUser = v3api.login(TEST_SPACE_USER, PASSWORD);

        OperationExecutionPermId executionId = new OperationExecutionPermId();
        List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(spaceCreation()));

        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(executionId);

        OperationExecution executionBefore = getExecution(sessionTokenAdmin, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNull(executionBefore);

        executionBefore = getExecution(sessionTokenUser, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNull(executionBefore);

        v3api.executeOperations(sessionTokenUser, operations, options);

        OperationExecution executionAfter = getExecution(sessionTokenAdmin, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNotNull(executionAfter);

        executionAfter = getExecution(sessionTokenUser, options.getExecutionId(), emptyOperationExecutionFetchOptions());
        assertNotNull(executionAfter);

        v3api.logout(sessionTokenAdmin);
        v3api.logout(sessionTokenUser);
    }

    @Test
    public void testGetWithEmptyFetchOptions()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(spaceCreation()));

        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        Date before = new Date();

        v3api.executeOperations(sessionToken, operations, options);

        Date after = new Date();

        OperationExecution execution = getExecution(sessionToken, options.getExecutionId(), emptyOperationExecutionFetchOptions());

        assertNotNull(execution);
        assertEquals(execution.getPermId(), options.getExecutionId());
        assertEquals(execution.getCode(), options.getExecutionId().getPermId());

        assertEquals(execution.getDescription(), options.getDescription());
        assertEquals(execution.getState(), OperationExecutionState.FINISHED);

        assertTrue(execution.getCreationDate().compareTo(before) >= 0);
        assertTrue(execution.getStartDate().compareTo(before) >= 0);
        assertTrue(execution.getFinishDate().compareTo(before) >= 0);

        assertTrue(execution.getCreationDate().compareTo(after) <= 0);
        assertTrue(execution.getStartDate().compareTo(after) <= 0);
        assertTrue(execution.getFinishDate().compareTo(after) <= 0);

        assertTrue(execution.getStartDate().compareTo(execution.getCreationDate()) >= 0);
        assertTrue(execution.getFinishDate().compareTo(execution.getStartDate()) >= 0);

        assertSummaryNotFetched(execution);
        assertDetailsNotFetched(execution);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithSummary()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceCreation creation = spaceCreation();

        List<? extends IOperation> operations = Arrays.asList(new CreateSpacesOperation(creation));

        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        v3api.executeOperations(sessionToken, operations, options);

        OperationExecutionFetchOptions fo = new OperationExecutionFetchOptions();
        fo.withSummary();
        fo.withSummary().withOperations();
        fo.withSummary().withProgress();
        fo.withSummary().withError();
        fo.withSummary().withResults();

        OperationExecution execution = getExecution(sessionToken, options.getExecutionId(), fo);

        assertNotNull(execution);

        OperationExecutionSummary summary = execution.getSummary();

        assertEquals(summary.getOperations().size(), 1);
        assertEquals(summary.getOperations().get(0), "CreateSpacesOperation 1 creation(s)");

        // reported by a separate thread
        if (summary.getProgress() != null)
        {
            AssertionUtil.assertContains("checking access (1/1)", summary.getProgress());
        }
        assertEquals(summary.getError(), null);

        assertEquals(summary.getResults().size(), 1);
        assertEquals(summary.getResults().get(0), "CreateSpacesOperationResult[" + creation.getCode() + "]");

        assertDetailsNotFetched(execution);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithDetails()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // create operations

        SpaceCreation creationBefore = spaceCreation();
        CreateSpacesOperation operationBefore = new CreateSpacesOperation(creationBefore);
        List<? extends IOperation> operationsBefore = Arrays.asList(operationBefore);

        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        // executeOperations

        SynchronousOperationExecutionResults executeResults =
                (SynchronousOperationExecutionResults) v3api.executeOperations(sessionToken, operationsBefore, options);
        assertEquals(executeResults.getResults().size(), 1);

        // getExecution

        OperationExecutionFetchOptions fo = new OperationExecutionFetchOptions();
        fo.withDetails();
        fo.withDetails().withOperations();
        fo.withDetails().withProgress();
        fo.withDetails().withError();
        fo.withDetails().withResults();

        OperationExecution execution = getExecution(sessionToken, options.getExecutionId(), fo);

        assertNotNull(execution);

        OperationExecutionDetails details = execution.getDetails();

        // check operations

        List<? extends IOperation> operationsAfter = details.getOperations();
        assertEquals(operationsAfter.size(), 1);

        CreateSpacesOperation operationAfter = (CreateSpacesOperation) operationsAfter.get(0);
        assertEquals(operationAfter.getCreations().size(), 1);

        SpaceCreation creationAfter = operationAfter.getCreations().get(0);
        assertEquals(creationAfter.getCode(), creationBefore.getCode());

        // check progress

        IOperationExecutionProgress progress = details.getProgress();
        AssertionUtil.assertContains("checking access (1/1)", progress.getMessage());
        assertEquals(progress.getNumItemsProcessed(), Integer.valueOf(1));
        assertEquals(progress.getTotalItemsToProcess(), Integer.valueOf(1));

        // check error

        assertEquals(details.getError(), null);

        // check results

        List<? extends IOperationResult> results = details.getResults();
        assertEquals(results.size(), 1);

        CreateSpacesOperationResult result = (CreateSpacesOperationResult) results.get(0);
        assertEquals(result.getObjectIds().size(), 1);
        assertEquals(result.getObjectIds().get(0), new SpacePermId(creationBefore.getCode()));

        assertSummaryNotFetched(execution);

        v3api.logout(sessionToken);
    }

}
