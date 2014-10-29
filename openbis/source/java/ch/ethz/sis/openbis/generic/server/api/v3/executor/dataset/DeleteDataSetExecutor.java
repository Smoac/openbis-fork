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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.dataset.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.DeletionTechId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class DeleteDataSetExecutor implements IDeleteDataSetExecutor
{

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    ICommonBusinessObjectFactory businessObjectFactory;

    @Override
    public IDeletionId delete(IOperationContext context, List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (dataSetIds == null)
        {
            throw new IllegalArgumentException("Data set ids cannot be null");
        }
        if (deletionOptions == null)
        {
            throw new IllegalArgumentException("Deletion options cannot be null");
        }
        if (deletionOptions.getReason() == null)
        {
            throw new IllegalArgumentException("Deletion reason cannot be null");
        }

        Map<IDataSetId, DataPE> dataSetMap = mapDataSetByIdExecutor.map(context, dataSetIds);

        for (Map.Entry<IDataSetId, DataPE> entry : dataSetMap.entrySet())
        {
            IDataSetId dataSetId = entry.getKey();
            DataPE dataSet = entry.getValue();

            if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), dataSet.getExperiment()))
            {
                throw new UnauthorizedObjectAccessException(dataSetId);
            }

            updateModificationDateAndModifierOfRelatedEntities(context, dataSet);
        }

        return trash(context, dataSetMap.values(), deletionOptions);
    }

    private void updateModificationDateAndModifierOfRelatedEntities(IOperationContext context, DataPE dataSet)
    {
        ExperimentPE experiment = dataSet.getExperiment();
        RelationshipUtils.updateModificationDateAndModifier(experiment, context.getSession());
        SamplePE sample = dataSet.tryGetSample();
        if (sample != null)
        {
            RelationshipUtils.updateModificationDateAndModifier(sample, context.getSession());
        }
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

    private IDeletionId trash(IOperationContext context, Collection<DataPE> dataSets, DataSetDeletionOptions deletionOptions)
    {
        List<TechId> dataSetTechIds = new LinkedList<TechId>();
        for (DataPE dataSet : dataSets)
        {
            dataSetTechIds.add(new TechId(dataSet.getId()));
        }

        ITrashBO trashBO = businessObjectFactory.createTrashBO(context.getSession());
        trashBO.createDeletion(deletionOptions.getReason());
        trashBO.trashDataSets(dataSetTechIds);
        DeletionPE deletion = trashBO.getDeletion();
        return new DeletionTechId(deletion.getId());
    }

}
