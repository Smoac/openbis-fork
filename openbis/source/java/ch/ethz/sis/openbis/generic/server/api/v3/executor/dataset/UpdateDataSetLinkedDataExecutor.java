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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.LinkedDataUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetLinkedDataExecutor implements IUpdateDataSetLinkedDataExecutor
{

    @Autowired
    private IUpdateDataSetExternalDmsExecutor updateDataSetExternalDmsExecutor;

    @Override
    public void update(IOperationContext context, Map<DataSetUpdate, DataPE> entitiesMap)
    {
        for (Map.Entry<DataSetUpdate, DataPE> entry : entitiesMap.entrySet())
        {
            DataSetUpdate update = entry.getKey();
            DataPE entity = entry.getValue();

            if (entity instanceof LinkDataPE && update.getLinkedData() != null && update.getLinkedData().isModified())
            {
                update(context, update.getLinkedData().getValue(), (LinkDataPE) entity);
            }
        }

        updateDataSetExternalDmsExecutor.update(context, entitiesMap);
    }

    private void update(IOperationContext context, LinkedDataUpdate update, LinkDataPE entity)
    {
        if (update.getExternalCode() != null && update.getExternalCode().isModified())
        {
            entity.setExternalCode(update.getExternalCode().getValue());
        }
    }
}
