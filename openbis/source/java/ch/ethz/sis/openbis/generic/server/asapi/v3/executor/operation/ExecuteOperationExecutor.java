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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
@Component
public class ExecuteOperationExecutor implements IExecuteOperationExecutor
{

    @Autowired
    private ISynchronousOperationExecutor synchronousExecutor;

    @Autowired
    private IAsynchronousOperationExecutor asynchronousExecutor;

    @Override
    @Transactional
    public IOperationExecutionResults execute(IOperationContext context, List<? extends IOperation> operations, IOperationExecutionOptions options)
    {
        // PROBLEMS:
        // + we need to move capability and roles allowed annotations from ApplicationServerApi to executors
        // + we need to call the executors that no longer take a session token but already operation context
        // so that they work properly even if a user logs out (e.g. schedules an operation to be executed
        // later and logs out from the system. still the operation should run as if the user executed it.
        // + we have to check the business rules after all operations are done, e.g. samples business rules
        // check connected data sets, which can be added after samples
        // + CreationId has to be shared among all executors (probably in OperationContext) and be probably used by MapExecutors
        // instead of dragging CreationId, ObjectPE map all around
        // + shall we guarantee that we keep execution results at least for some time? if they don't fit in memory we will be forced
        // to write them to disk + they have to survive a server crash/shutdown

        // TODO:
        // + address the problems listed above
        // + cover all operations and get rid of method executors
        // + make execution thread pool configurable
        // + users should be able to see only their own executions (admins should see everything)
        // + control what is returned in OperationExecution with OperationExecutionFetchOptions
        // + store finished executions in a new table (not in entity_operations_log)
        // - notify about finished/failed asynchronous executions (e.g. by email)
        // - make it possible to search for executions with given state, dates, ids etc.
        // - clean the code
        // - automated tests
        // - JS part (add missing @JsonObject to Java DTOs, create JS DTOs, create method in JS facade, JS automated tests)

        // WONT DO:
        // - shall scheduled/running executions survive a server crash/shutdown? (i.e. should they be restarted)
        // - there should be some total memory limit for all v3 caches together (execute operations does not use memory cache)
        
        if (options instanceof SynchronousOperationExecutionOptions)
        {
            return synchronousExecutor.execute(context, operations, (SynchronousOperationExecutionOptions) options);
        } else if (options instanceof AsynchronousOperationExecutionOptions)
        {
            return asynchronousExecutor.execute(context, operations, (AsynchronousOperationExecutionOptions) options);
        } else
        {
            throw new IllegalArgumentException("Operation execution options " + options + " are not supported");
        }
    }
}
