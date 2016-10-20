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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
public abstract class OperationExecutor<OPERATION extends IOperation, RESULT extends IOperationResult> implements IOperationExecutor
{

    @SuppressWarnings("unchecked")
    @Override
    public Map<IOperation, IOperationResult> execute(IOperationContext context, List<? extends IOperation> operations)
    {
        Map<OPERATION, RESULT> results = new HashMap<OPERATION, RESULT>();
        Class<? extends OPERATION> operationClass = getOperationClass();

        for (IOperation operation : operations)
        {
            if (operation != null && operationClass.isAssignableFrom(operation.getClass()))
            {
                OPERATION theOperation = (OPERATION) operation;
                RESULT result = doExecute(context, theOperation);
                results.put(theOperation, result);
            }
        }

        return (Map<IOperation, IOperationResult>) results;
    }

    protected abstract Class<? extends OPERATION> getOperationClass();

    protected abstract RESULT doExecute(IOperationContext context, OPERATION operation);

}
