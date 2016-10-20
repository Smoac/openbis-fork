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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.IOperationExecutionStore;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author pkupczyk
 */
@Component
public class AsynchronousOperationThreadPoolExecutor implements IAsynchronousOperationThreadPoolExecutor
{

    private static final Logger log = LogFactory.getLogger(LogCategory.OPERATION, AsynchronousOperationThreadPoolExecutor.class);

    @Autowired
    private IOperationExecutionStore executionStore;

    @Autowired
    private IOperationsExecutor operationsExecutor;

    public AsynchronousOperationThreadPoolExecutor()
    {
    }

    AsynchronousOperationThreadPoolExecutor(IOperationExecutionStore executionStore, IOperationsExecutor operationsExecutor)
    {
        this.executionStore = executionStore;
        this.operationsExecutor = operationsExecutor;
    }

    @Override
    @Transactional
    public void execute(IOperationContext context, OperationExecutionPermId executionId, List<? extends IOperation> operations)
    {
        try
        {
            executionStore.executionRunning(context, executionId);
            List<IOperationResult> results = operationsExecutor.execute(context, operations);
            executionStore.executionFinished(context, executionId, results);
        } catch (Exception e)
        {
            log.error(e);
            executionStore.executionFailed(context, executionId, new OperationExecutionError(e));
            throw e;
        }
    }

}
