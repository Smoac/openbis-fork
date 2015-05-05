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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.dataset.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.DeletionTechId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class DeleteDataSetExecutor extends AbstractDeleteEntityExecutor<IDeletionId, IDataSetId, DataPE, DataSetDeletionOptions> implements
        IDeleteDataSetExecutor
{

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Override
    protected Map<IDataSetId, DataPE> map(IOperationContext context, List<? extends IDataSetId> entityIds)
    {
        return mapDataSetByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IDataSetId entityId, DataPE entity)
    {
        if (false == new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), entity.getSpace()))
        {
            throw new UnauthorizedObjectAccessException(entityId);
        }
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, DataPE dataSet)
    {
        RelationshipUtils.updateModificationDateAndModifier(dataSet.getExperiment(), context.getSession());
        RelationshipUtils.updateModificationDateAndModifier(dataSet.tryGetSample(), context.getSession());
        updateModificationDateAndModifierOfRelatedDataSets(context, dataSet.getChildren());
        updateModificationDateAndModifierOfRelatedDataSets(context, dataSet.getParents());
        Set<DataSetRelationshipPE> relationships = dataSet.getParentRelationships();
        for (DataSetRelationshipPE relationship : RelationshipUtils.getContainerComponentRelationships(relationships))
        {
            RelationshipUtils.updateModificationDateAndModifier(relationship.getParentDataSet(), context.getSession());
        }
    }

    private void updateModificationDateAndModifierOfRelatedDataSets(IOperationContext context, List<DataPE> dataSets)
    {
        if (dataSets != null)
        {
            for (DataPE child : dataSets)
            {
                RelationshipUtils.updateModificationDateAndModifier(child, context.getSession());
            }
        }
    }

    @Override
    protected IDeletionId delete(IOperationContext context, Collection<DataPE> dataSets, DataSetDeletionOptions deletionOptions)
    {
        ITrashBO trashBO = businessObjectFactory.createTrashBO(context.getSession());
        trashBO.createDeletion(deletionOptions.getReason());
        trashBO.trashDataSets(asTechIds(dataSets));
        DeletionPE deletion = trashBO.getDeletion();
        return new DeletionTechId(deletion.getId());
    }

}
