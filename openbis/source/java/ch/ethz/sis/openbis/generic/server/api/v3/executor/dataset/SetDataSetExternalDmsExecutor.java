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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.externaldms.IMapExternalDmsByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.externaldms.IExternalDmsId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetExternalDmsExecutor extends
        AbstractSetEntityToOneRelationExecutor<DataSetCreation, DataPE, IExternalDmsId, ExternalDataManagementSystemPE>
        implements ISetDataSetExternalDmsExecutor
{

    @Autowired
    private IMapExternalDmsByIdExecutor mapExternalDmsByIdExecutor;

    @Override
    protected IExternalDmsId getRelatedId(DataSetCreation creation)
    {
        return creation.getExternalDmsId();
    }

    @Override
    protected Map<IExternalDmsId, ExternalDataManagementSystemPE> map(IOperationContext context, List<IExternalDmsId> relatedIds)
    {
        return mapExternalDmsByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, DataPE entity, IExternalDmsId relatedId, ExternalDataManagementSystemPE related)
    {
        if (entity instanceof LinkDataPE && relatedId == null)
        {
            throw new UserFailureException("External data management system id cannot be null for a linked data set.");
        }
    }

    @Override
    protected void set(IOperationContext context, DataPE entity, ExternalDataManagementSystemPE related)
    {
        if (entity instanceof LinkDataPE)
        {
            ((LinkDataPE) entity).setExternalDataManagementSystem(related);
        }
    }

}