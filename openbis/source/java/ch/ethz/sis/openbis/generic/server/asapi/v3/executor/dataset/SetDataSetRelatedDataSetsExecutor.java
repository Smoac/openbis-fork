/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetRelatedDataSetsExecutor extends AbstractSetEntityMultipleRelationsExecutor<DataSetCreation, DataPE, IDataSetId, DataPE>
        implements
        ISetDataSetRelatedDataSetsExecutor
{

    @Autowired
    private IMapDataSetByIdExecutor mapExecutor;

    @Autowired
    private ISetDataSetContainerExecutor setDataSetContainerExecutor;

    @Autowired
    private ISetDataSetComponentsExecutor setDataSetComponentsExecutor;

    @Autowired
    private ISetDataSetParentsExecutor setDataSetParentsExecutor;

    @Autowired
    private ISetDataSetChildrenExecutor setDataSetChildrenExecutor;

    @Override
    protected void addRelatedIds(Set<IDataSetId> relatedIds, DataSetCreation creation, DataPE entity)
    {
        addRelatedIds(relatedIds, creation.getContainerIds());
        addRelatedIds(relatedIds, creation.getComponentIds());
        addRelatedIds(relatedIds, creation.getParentIds());
        addRelatedIds(relatedIds, creation.getChildIds());
    }

    @Override
    protected void addRelated(Map<IDataSetId, DataPE> relatedMap, DataSetCreation creation, DataPE entity)
    {
        addRelated(relatedMap, creation.getCreationId(), entity);
    }

    @Override
    protected Map<IDataSetId, DataPE> map(IOperationContext context, List<IDataSetId> relatedIds)
    {
        return mapExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, IDataSetId relatedId, DataPE related)
    {
        if (false == new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void set(IOperationContext context, MapBatch<DataSetCreation, DataPE> batch, Map<IDataSetId, DataPE> relatedMap)
    {
        setDataSetContainerExecutor.set(context, batch, relatedMap);
        setDataSetComponentsExecutor.set(context, batch, relatedMap);
        setDataSetParentsExecutor.set(context, batch, relatedMap);
        setDataSetChildrenExecutor.set(context, batch, relatedMap);
    }

}
