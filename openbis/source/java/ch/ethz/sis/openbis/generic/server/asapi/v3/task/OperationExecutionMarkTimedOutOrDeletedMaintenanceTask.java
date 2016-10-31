/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.task;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
public class OperationExecutionMarkTimedOutOrDeletedMaintenanceTask extends AbstractOperationExecutionMarkMaintenanceTask
{

    @Override
    protected void doExecute()
    {
        markTimedOut();
        markDeleted();
    }

    private void markTimedOut()
    {
        final IOperationContext context = createOperationContext();
        final List<OperationExecution> executions = getExecutionStore().getExecutionsToBeTimedOut(context, new OperationExecutionFetchOptions());

        if (false == executions.isEmpty())
        {
            getOperationLog().info("found " + executions.size() + " execution(s) to be marked " + OperationExecutionAvailability.TIMED_OUT);
        }

        markOperationExecutions(executions, new MarkAction()
            {
                @Override
                public void mark(OperationExecution execution)
                {
                    if (OperationExecutionAvailability.TIME_OUT_PENDING.equals(execution.getAvailability()))
                    {
                        getExecutionStore().executionAvailability(context, execution.getPermId(), OperationExecutionAvailability.TIMED_OUT);
                    } else
                    {
                        if (OperationExecutionAvailability.TIME_OUT_PENDING.equals(execution.getSummaryAvailability()))
                        {
                            getExecutionStore().executionSummaryAvailability(context, execution.getPermId(),
                                    OperationExecutionAvailability.TIMED_OUT);
                        }
                        if (OperationExecutionAvailability.TIME_OUT_PENDING.equals(execution.getDetailsAvailability()))
                        {
                            getExecutionStore().executionDetailsAvailability(context, execution.getPermId(),
                                    OperationExecutionAvailability.TIMED_OUT);
                        }
                    }
                }
            });
    }

    private void markDeleted()
    {
        final IOperationContext context = createOperationContext();
        final List<OperationExecution> executions = getExecutionStore().getExecutionsToBeDeleted(context, new OperationExecutionFetchOptions());

        getOperationLog().info("found " + executions.size() + " execution(s) to be marked " + OperationExecutionAvailability.DELETED);

        markOperationExecutions(executions, new MarkAction()
            {

                @Override
                public void mark(OperationExecution execution)
                {
                    if (OperationExecutionAvailability.DELETE_PENDING.equals(execution.getAvailability()))
                    {
                        getExecutionStore().executionAvailability(context, execution.getPermId(), OperationExecutionAvailability.DELETED);
                    } else
                    {
                        if (OperationExecutionAvailability.DELETE_PENDING.equals(execution.getSummaryAvailability()))
                        {
                            getExecutionStore().executionSummaryAvailability(context, execution.getPermId(), OperationExecutionAvailability.DELETED);
                        }
                        if (OperationExecutionAvailability.DELETE_PENDING.equals(execution.getDetailsAvailability()))
                        {
                            getExecutionStore().executionDetailsAvailability(context, execution.getPermId(), OperationExecutionAvailability.DELETED);
                        }
                    }
                }
            });
    }

}
