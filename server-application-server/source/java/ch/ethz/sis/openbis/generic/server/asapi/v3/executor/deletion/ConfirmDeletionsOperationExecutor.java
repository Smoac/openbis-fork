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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.confirm.ConfirmDeletionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.confirm.ConfirmDeletionsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class ConfirmDeletionsOperationExecutor extends OperationExecutor<ConfirmDeletionsOperation, ConfirmDeletionsOperationResult>
        implements IConfirmDeletionsOperationExecutor
{

    @Autowired
    private IConfirmDeletionExecutor executor;

    @Override
    protected Class<? extends ConfirmDeletionsOperation> getOperationClass()
    {
        return ConfirmDeletionsOperation.class;
    }

    @Override
    protected ConfirmDeletionsOperationResult doExecute(IOperationContext context, ConfirmDeletionsOperation operation)
    {
        executor.confirm(context, operation.getDeletionIds(), operation.isForceDeletion(),
                operation.isForceDeletionOfDependentDeletions());
        return new ConfirmDeletionsOperationResult();
    }

}
