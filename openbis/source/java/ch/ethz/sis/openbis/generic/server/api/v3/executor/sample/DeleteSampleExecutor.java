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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.sample.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.DeletionTechId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
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
public class DeleteSampleExecutor implements IDeleteSampleExecutor
{

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Override
    public IDeletionId delete(IOperationContext context, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (sampleIds == null)
        {
            throw new IllegalArgumentException("Sample ids cannot be null");
        }
        if (deletionOptions == null)
        {
            throw new IllegalArgumentException("Deletion options cannot be null");
        }
        if (deletionOptions.getReason() == null)
        {
            throw new IllegalArgumentException("Deletion reason cannot be null");
        }

        Map<ISampleId, SamplePE> sampleMap = mapSampleByIdExecutor.map(context, sampleIds);
        List<TechId> sampleTechIds = new LinkedList<TechId>();

        for (Map.Entry<ISampleId, SamplePE> entry : sampleMap.entrySet())
        {
            ISampleId sampleId = entry.getKey();
            SamplePE sample = entry.getValue();

            if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), sample))
            {
                throw new UnauthorizedObjectAccessException(sampleId);
            }

            updateModificationDateAndModifierOfRelatedEntities(context, sample);
            sampleTechIds.add(new TechId(sample.getId()));
        }

        return trash(context, sampleTechIds, deletionOptions);
    }

    private void updateModificationDateAndModifierOfRelatedEntities(IOperationContext context, SamplePE sample)
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

    private IDeletionId trash(IOperationContext context, List<TechId> sampleTechIds, SampleDeletionOptions deletionOptions)
    {
        ITrashBO trashBO = businessObjectFactory.createTrashBO(context.getSession());
        trashBO.createDeletion(deletionOptions.getReason());
        trashBO.trashSamples(sampleTechIds);
        DeletionPE deletion = trashBO.getDeletion();
        return new DeletionTechId(deletion.getId());
    }

}
