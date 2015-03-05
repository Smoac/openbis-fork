/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IVerifyEntityPropertyExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class VerifyDataSetExecutor implements IVerifyDataSetExecutor
{

    @Autowired
    private IVerifyEntityPropertyExecutor verifyEntityPropertyExecutor;

    @Autowired
    private IVerifyDataSetContainersExecutor verifyDataSetContainersExecutor;

    @Autowired
    private IVerifyDataSetParentsExecutor verifyDataSetParentsExecutor;

    @Override
    public void verify(IOperationContext context, Collection<DataPE> dataSets)
    {
        for (DataPE dataSet : dataSets)
        {
            if (dataSet.getExperiment() == null && dataSet.tryGetSample() == null)
            {
                throw new IllegalArgumentException("Data set '" + dataSet.getCode() + "' has both experiment and sample set to null");
            }
        }

        verifyEntityPropertyExecutor.verify(context, dataSets);
        verifyDataSetContainersExecutor.verify(context, dataSets);
        verifyDataSetParentsExecutor.verify(context, dataSets);
    }

}
