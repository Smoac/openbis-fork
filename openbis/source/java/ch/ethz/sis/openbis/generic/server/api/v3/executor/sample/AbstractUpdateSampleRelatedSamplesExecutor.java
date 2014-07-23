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
import java.util.LinkedList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
public abstract class AbstractUpdateSampleRelatedSamplesExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    protected AbstractUpdateSampleRelatedSamplesExecutor()
    {
    }

    public AbstractUpdateSampleRelatedSamplesExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    public void update(IOperationContext context, Map<SampleUpdate, SamplePE> updateMap, Map<ISampleId, Long> techIdMap)
    {
        for (SampleUpdate update : updateMap.keySet())
        {
            ListUpdateValue<? extends ISampleId> listUpdate = getRelatedSamplesUpdate(context, update);

            if (listUpdate != null && listUpdate.hasActions())
            {
                SamplePE sample = updateMap.get(update);

                for (ListUpdateAction<? extends ISampleId> action : listUpdate.getActions())
                {
                    Collection<Long> relatedTechIds = new LinkedList<Long>();

                    for (ISampleId relatedId : action.getIds())
                    {
                        relatedTechIds.add(techIdMap.get(relatedId));
                    }

                    if (action instanceof ListUpdateActionSet<?>)
                    {
                        setRelatedSamples(context, sample, relatedTechIds);
                    } else if (action instanceof ListUpdateActionAdd<?>)
                    {
                        addRelatedSamples(context, sample, relatedTechIds);
                    } else if (action instanceof ListUpdateActionRemove<?>)
                    {
                        removeRelatedSamples(context, sample, relatedTechIds);
                    }
                }
            }

        }
    }

    protected abstract ListUpdateValue<? extends ISampleId> getRelatedSamplesUpdate(IOperationContext context, SampleUpdate update);

    protected abstract void setRelatedSamples(IOperationContext context, SamplePE sample, Collection<Long> relatedSamplesIds);

    protected abstract void addRelatedSamples(IOperationContext context, SamplePE sample, Collection<Long> relatedSamplesIds);

    protected abstract void removeRelatedSamples(IOperationContext context, SamplePE sample, Collection<Long> relatedSamplesIds);

    public IDAOFactory getDaoFactory()
    {
        return daoFactory;
    }

}
