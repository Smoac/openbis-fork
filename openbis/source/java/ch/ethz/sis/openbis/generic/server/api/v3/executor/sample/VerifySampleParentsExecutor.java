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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.relationship.IGetParentChildRelationshipIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleGenericBusinessRules;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class VerifySampleParentsExecutor implements IVerifySampleParentsExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IGetParentChildRelationshipIdExecutor getParentChildRelationshipIdExecutor;

    @SuppressWarnings("unused")
    private VerifySampleParentsExecutor()
    {
    }

    public VerifySampleParentsExecutor(IDAOFactory daoFactory, IGetParentChildRelationshipIdExecutor getParentChildRelationshipIdExecutor)
    {
        this.daoFactory = daoFactory;
        this.getParentChildRelationshipIdExecutor = getParentChildRelationshipIdExecutor;
    }

    @Override
    public void verify(IOperationContext context, Collection<SamplePE> samples)
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        TechId parentChildRelationshipId = new TechId(getParentChildRelationshipIdExecutor.get(context));
        final HashSet<TechId> alreadyCheckedParents = new HashSet<TechId>();

        for (SamplePE sample : samples)
        {
            TechId sampleTechId = TechId.create(sample);

            context.pushContextDescription("verify parent relations for sample " + sample.getCode());

            Collection<TechId> currentLevel = new LinkedList<TechId>();
            currentLevel.add(sampleTechId);
            alreadyCheckedParents.add(sampleTechId);

            while (false == currentLevel.isEmpty())
            {
                Collection<TechId> nextLevel = sampleDAO.listSampleIdsByChildrenIds(currentLevel, parentChildRelationshipId);
                if (nextLevel.contains(sampleTechId))
                {
                    throw new UserFailureException("Circular parent dependency found");
                }
                currentLevel = CollectionUtils.select(nextLevel, new Predicate<TechId>()
                    {
                        @Override
                        public boolean evaluate(TechId object)
                        {
                            return alreadyCheckedParents.contains(object) == false;
                        }
                    });
                alreadyCheckedParents.addAll(currentLevel);
            }

            SampleGenericBusinessRules.assertValidParents(sample);
            SampleGenericBusinessRules.assertValidChildren(sample);

            context.popContextDescription();
        }
    }

}
