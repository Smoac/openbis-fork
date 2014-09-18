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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.relationship.IGetParentChildRelationshipIdExecutor;
import ch.systemsx.cisd.common.collection.CycleFoundException;
import ch.systemsx.cisd.common.collection.GroupingDAG;
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
        Map<Long, Collection<Long>> graph = getGraph(context, samples);

        checkCycles(samples, graph);

        for (SamplePE sample : samples)
        {
            SampleGenericBusinessRules.assertValidParents(sample);
            SampleGenericBusinessRules.assertValidChildren(sample);
        }
    }

    private Map<Long, Collection<Long>> getGraph(IOperationContext context, Collection<SamplePE> samples)
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        TechId parentChildRelationshipId = new TechId(getParentChildRelationshipIdExecutor.get(context));
        Map<Long, Collection<Long>> parentIdsMap = new HashMap<Long, Collection<Long>>();
        Set<Long> currentLevelIds = new HashSet<Long>();
        Set<Long> visitedIds = new HashSet<Long>();

        for (SamplePE sample : samples)
        {
            currentLevelIds.add(sample.getId());
        }

        while (false == currentLevelIds.isEmpty())
        {
            Map<Long, Set<Long>> currentLevelParentIdsMap = sampleDAO.mapSampleIdsByChildrenIds(currentLevelIds, parentChildRelationshipId.getId());

            visitedIds.addAll(currentLevelIds);
            currentLevelIds = new HashSet<Long>();

            for (Map.Entry<Long, Set<Long>> currentLevelParentIdsEntry : currentLevelParentIdsMap.entrySet())
            {
                Long sampleId = currentLevelParentIdsEntry.getKey();
                Set<Long> parentIds = currentLevelParentIdsEntry.getValue();
                parentIdsMap.put(sampleId, parentIds);

                for (Long parentId : parentIds)
                {
                    if (false == visitedIds.contains(parentId))
                    {
                        currentLevelIds.add(parentId);
                    }
                }
            }
        }

        return parentIdsMap;
    }

    @SuppressWarnings("rawtypes")
    private void checkCycles(Collection<SamplePE> samples, Map<Long, Collection<Long>> graph)
    {
        try
        {
            GroupingDAG.groupByDepencies(graph);
        } catch (CycleFoundException e)
        {
            Map<Long, SamplePE> sampleMap = new HashMap<Long, SamplePE>();
            for (SamplePE sample : samples)
            {
                sampleMap.put(sample.getId(), sample);
            }

            Collection<String> cycle = new LinkedList<String>();
            cycle.add(sampleMap.get(e.getCycleRoot()).getIdentifier());

            Iterator iterator = e.getCycle().iterator();
            while (iterator.hasNext())
            {
                cycle.add(sampleMap.get(iterator.next()).getIdentifier());
            }
            
            cycle.add(sampleMap.get(e.getCycleRoot()).getIdentifier());

            throw new UserFailureException("Circular parent dependency found: " + cycle);
        }
    }
}
