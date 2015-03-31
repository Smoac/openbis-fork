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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.sample.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.DeletionTechId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class DeleteSampleExecutor extends AbstractDeleteEntityExecutor<IDeletionId, ISampleId, SamplePE, SampleDeletionOptions> implements
        IDeleteSampleExecutor
{

    @Autowired
    IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, List<? extends ISampleId> entityIds)
    {
        return mapSampleByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, ISampleId entityId, SamplePE entity)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(entityId);
        }
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, SamplePE sample)
    {
        ExperimentPE experiment = sample.getExperiment();
        if (experiment != null)
        {
            RelationshipUtils.updateModificationDateAndModifier(experiment, context.getSession());
        }
        SamplePE container = sample.getContainer();
        if (container != null)
        {
            RelationshipUtils.updateModificationDateAndModifier(container, context.getSession());
        }
        List<SamplePE> parents = sample.getParents();
        if (parents != null)
        {
            for (SamplePE parent : parents)
            {
                RelationshipUtils.updateModificationDateAndModifier(parent, context.getSession());
            }
        }
        Set<SampleRelationshipPE> childRelationships = sample.getChildRelationships();
        if (childRelationships != null)
        {
            for (SampleRelationshipPE childRelationship : childRelationships)
            {
                SamplePE childSample = childRelationship.getChildSample();
                RelationshipUtils.updateModificationDateAndModifier(childSample, context.getSession());
            }
        }
    }

    @Override
    protected IDeletionId delete(IOperationContext context, Collection<SamplePE> entities, SampleDeletionOptions deletionOptions)
    {
        ITrashBO trashBO = businessObjectFactory.createTrashBO(context.getSession());
        trashBO.createDeletion(deletionOptions.getReason());
        trashBO.trashSamples(TechId.createList(entities));
        DeletionPE deletion = trashBO.getDeletion();
        return new DeletionTechId(deletion.getId());
    }

}
