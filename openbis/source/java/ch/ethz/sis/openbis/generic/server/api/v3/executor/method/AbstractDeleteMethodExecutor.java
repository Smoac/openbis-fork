/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.IDeleteEntityExecutor;

/**
 * @author pkupczyk
 */
public abstract class AbstractDeleteMethodExecutor<DELETION_ID, OBJECT_ID, DELETION_OPTIONS> extends AbstractMethodExecutor implements
        IDeleteMethodExecutor<DELETION_ID, OBJECT_ID, DELETION_OPTIONS>
{

    @Override
    public DELETION_ID delete(final String sessionToken, final List<? extends OBJECT_ID> objectIds, final DELETION_OPTIONS deletionOptions)
    {
        return executeInContext(sessionToken, new IMethodAction<DELETION_ID>()
            {
                @Override
                public DELETION_ID execute(IOperationContext context)
                {
                    return getDeleteExecutor().delete(context, objectIds, deletionOptions);
                }
            });
    }

    protected abstract IDeleteEntityExecutor<DELETION_ID, OBJECT_ID, DELETION_OPTIONS> getDeleteExecutor();

}
