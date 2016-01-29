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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Collection;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.Progress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToManyRelationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetParentsExecutor extends AbstractSetEntityToManyRelationExecutor<DataSetCreation, DataPE, IDataSetId> implements
        ISetDataSetParentsExecutor
{

    @Override
    protected Collection<? extends IDataSetId> getRelatedIds(IOperationContext context, DataSetCreation creation)
    {
        return creation.getParentIds();
    }

    @Override
    protected void setRelated(IOperationContext context, DataPE child, Collection<DataPE> parents)
    {
        context.pushProgress(new Progress("set parents for dataset " + child.getCode()));

        for (DataPE parent : parents)
        {
            relationshipService.addParentToDataSet(context.getSession(), child, parent);
        }

        context.popProgress();
    }

}
