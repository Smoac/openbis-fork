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

import org.apache.commons.lang.time.DateUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;

/**
 * @author pkupczyk
 */
public class OperationExecutionMarkTimeOutPendingMaintenanceTask extends AbstractOperationExecutionMarkMaintenanceTask
{

    @Override
    protected void doExecute()
    {
        markTimeoutPending();
    }

    private void markTimeoutPending()
    {
        final List<OperationExecution> executions =
                getExecutionStore().getExecutionsToBeTimeOutPending(getOperationContext(), new OperationExecutionFetchOptions());

        if (false == executions.isEmpty())
        {
            getOperationLog().info("found " + executions.size() + " execution(s) to be marked " + OperationExecutionAvailability.TIME_OUT_PENDING);
        }

        final long now = System.currentTimeMillis();

        markOperationExecutions(executions, new MarkAction()
            {

                @Override
                public void mark(OperationExecution execution)
                {
                    if (execution.getFinishDate() != null)
                    {
                        if (now > execution.getFinishDate().getTime() + execution.getAvailabilityTime() * DateUtils.MILLIS_PER_SECOND)
                        {
                            getExecutionStore().executionAvailability(getOperationContext(), execution.getPermId(),
                                    OperationExecutionAvailability.TIME_OUT_PENDING);
                        }
                        if (now > execution.getFinishDate().getTime() + execution.getSummaryAvailabilityTime() * DateUtils.MILLIS_PER_SECOND)
                        {
                            getExecutionStore().executionSummaryAvailability(getOperationContext(), execution.getPermId(),
                                    OperationExecutionAvailability.TIME_OUT_PENDING);
                        }
                        if (now > execution.getFinishDate().getTime() + execution.getDetailsAvailabilityTime() * DateUtils.MILLIS_PER_SECOND)
                        {
                            getExecutionStore().executionDetailsAvailability(getOperationContext(), execution.getPermId(),
                                    OperationExecutionAvailability.TIME_OUT_PENDING);
                        }
                    }
                }
            });
    }

}
