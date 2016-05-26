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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IUnarchiveDataSetExecutor;

/**
 * @author pkupczyk
 */
@Component
public class UnarchiveDataSetMethodExecutor extends AbstractMethodExecutor implements IUnarchiveDataSetMethodExecutor
{

    @Autowired
    private IUnarchiveDataSetExecutor executor;

    @Override
    public void unarchive(String sessionToken, final List<? extends IDataSetId> dataSetIds, final DataSetUnarchiveOptions options)
    {
        executeInContext(sessionToken, new IMethodAction<Void>()
            {
                @Override
                public Void execute(IOperationContext context)
                {
                    executor.unarchive(context, dataSetIds, options);
                    return null;
                }
            });
    }

}
