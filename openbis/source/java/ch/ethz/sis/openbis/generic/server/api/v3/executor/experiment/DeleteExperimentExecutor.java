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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.experiment.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.DeletionTechId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class DeleteExperimentExecutor implements IDeleteExperimentExecutor
{

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Override
    public IDeletionId delete(IOperationContext context, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (experimentIds == null)
        {
            throw new IllegalArgumentException("Experiment ids cannot be null");
        }
        if (deletionOptions == null)
        {
            throw new IllegalArgumentException("Deletion options cannot be null");
        }
        if (deletionOptions.getReason() == null)
        {
            throw new IllegalArgumentException("Deletion reason cannot be null");
        }

        Map<IExperimentId, ExperimentPE> experimentMap = mapExperimentByIdExecutor.map(context, experimentIds);
        List<TechId> experimentTechIds = new LinkedList<TechId>();

        for (Map.Entry<IExperimentId, ExperimentPE> entry : experimentMap.entrySet())
        {
            IExperimentId experimentId = entry.getKey();
            ExperimentPE experiment = entry.getValue();

            if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), experiment))
            {
                throw new UnauthorizedObjectAccessException(experimentId);
            }

            updateModificationDateAndModifierOfRelatedProject(context, experiment);
            experimentTechIds.add(new TechId(experiment.getId()));
        }

        return trash(context, experimentTechIds, deletionOptions);
    }

    private void updateModificationDateAndModifierOfRelatedProject(IOperationContext context, ExperimentPE experiment)
    {
        RelationshipUtils.updateModificationDateAndModifier(experiment.getProject(), context.getSession());
    }

    private IDeletionId trash(IOperationContext context, List<TechId> experimentTechIds, ExperimentDeletionOptions deletionOptions)
    {
        ITrashBO trashBO = businessObjectFactory.createTrashBO(context.getSession());
        trashBO.createDeletion(deletionOptions.getReason());
        trashBO.trashExperiments(experimentTechIds);
        DeletionPE deletion = trashBO.getDeletion();
        return new DeletionTechId(deletion.getId());
    }

}
