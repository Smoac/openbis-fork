/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractVerifyEntityCyclesExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship.IGetRelationshipIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship.IGetRelationshipIdExecutor.RelationshipType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleGenericBusinessRules;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class VerifySampleParentsExecutor extends AbstractVerifyEntityCyclesExecutor<SamplePE> implements IVerifySampleParentsExecutor
{

    @Autowired
    private IGetRelationshipIdExecutor getRelationshipIdExecutor;

    @Override
    public void verify(IOperationContext context, CollectionBatch<SamplePE> batch)
    {
        super.verify(context, batch);

        for (SamplePE sample : batch.getObjects())
        {
            SampleGenericBusinessRules.assertValidParents(sample);
            SampleGenericBusinessRules.assertValidChildren(sample);
        }
    }

    @Override
    protected Long getId(SamplePE entity)
    {
        return entity.getId();
    }

    @Override
    protected String getIdentifier(Long entityId)
    {
        SamplePE sample = daoFactory.getSampleDAO().tryGetByTechId(new TechId(entityId));
        return sample.getIdentifier();
    }

    @Override
    protected Map<Long, Set<Long>> getRelatedIdsMap(IOperationContext context, Set<Long> entityIds)
    {
        Long relationshipId = getRelationshipIdExecutor.get(context, RelationshipType.PARENT_CHILD);
        return daoFactory.getSampleDAO().mapSampleIdsByChildrenIds(entityIds, relationshipId);
    }

}
