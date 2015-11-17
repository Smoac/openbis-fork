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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractSetEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetRelatedDataSetsExecutor extends AbstractSetEntityMultipleRelationsExecutor<DataSetCreation, DataPE, IDataSetId> implements
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
    protected void addRelatedIds(Set<IDataSetId> relatedIds, DataSetCreation creation)
    {
        addRelatedIds(relatedIds, creation.getContainerIds());
        addRelatedIds(relatedIds, creation.getComponentIds());
        addRelatedIds(relatedIds, creation.getParentIds());
        addRelatedIds(relatedIds, creation.getChildIds());
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
    protected void set(IOperationContext context, Map<DataSetCreation, DataPE> creationsMap, Map<IDataSetId, DataPE> relatedMap)
    {
        setDataSetContainerExecutor.set(context, creationsMap, relatedMap);
        setDataSetComponentsExecutor.set(context, creationsMap, relatedMap);
        setDataSetParentsExecutor.set(context, creationsMap, relatedMap);
        setDataSetChildrenExecutor.set(context, creationsMap, relatedMap);
    }

}
