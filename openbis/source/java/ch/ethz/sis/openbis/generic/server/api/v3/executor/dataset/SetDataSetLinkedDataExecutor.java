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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.LinkedDataCreation;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetLinkedDataExecutor implements ISetDataSetLinkedDataExecutor
{

    @Autowired
    private ISetDataSetExternalDmsExecutor setDataSetExternalDmsExecutor;

    @Override
    public void set(IOperationContext context, Map<DataSetCreation, DataPE> entitiesMap)
    {
        for (Map.Entry<DataSetCreation, DataPE> entry : entitiesMap.entrySet())
        {
            DataSetCreation creation = entry.getKey();
            LinkedDataCreation linkedCreation = creation.getLinkedData();
            DataPE entity = entry.getValue();

            if (entity instanceof LinkDataPE)
            {
                if (linkedCreation == null)
                {
                    throw new UserFailureException("Linked data cannot be null for a link data set.");
                }
                set(context, linkedCreation, (LinkDataPE) entity);
            } else
            {
                if (linkedCreation != null)
                {
                    throw new UserFailureException("Linked data cannot be set for a non-link data set.");
                }
            }
        }

        setDataSetExternalDmsExecutor.set(context, entitiesMap);
    }

    private void set(IOperationContext context, LinkedDataCreation linkedCreation, LinkDataPE dataSet)
    {
        dataSet.setExternalCode(linkedCreation.getExternalCode());
    }

}
