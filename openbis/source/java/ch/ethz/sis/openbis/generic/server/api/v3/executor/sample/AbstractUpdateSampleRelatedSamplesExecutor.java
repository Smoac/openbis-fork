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

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
public abstract class AbstractUpdateSampleRelatedSamplesExecutor
{

    public void update(IOperationContext context, Map<SampleUpdate, SamplePE> updateMap, Map<ISampleId, SamplePE> samplesMap)
    {
        for (SampleUpdate update : updateMap.keySet())
        {
            IdListUpdateValue<? extends ISampleId> listUpdate = getRelatedSamplesUpdate(context, update);

            if (listUpdate != null && listUpdate.hasActions())
            {
                SamplePE sample = updateMap.get(update);

                for (ListUpdateAction<? extends ISampleId> action : listUpdate.getActions())
                {
                    Collection<SamplePE> relatedSamples = new LinkedList<SamplePE>();

                    if (action instanceof ListUpdateActionSet<?> || action instanceof ListUpdateActionAdd<?>)
                    {
                        for (ISampleId relatedId : action.getItems())
                        {
                            SamplePE relatedSample = samplesMap.get(relatedId);
                            if (relatedSample == null)
                            {
                                throw new ObjectNotFoundException(relatedId);
                            }
                            if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), relatedSample))
                            {
                                throw new UnauthorizedObjectAccessException(relatedId);
                            }
                            relatedSamples.add(relatedSample);
                        }
                        if (action instanceof ListUpdateActionSet<?>)
                        {
                            setRelatedSamples(context, sample, relatedSamples);
                        } else
                        {
                            addRelatedSamples(context, sample, relatedSamples);
                        }
                    } else if (action instanceof ListUpdateActionRemove<?>)
                    {
                        for (ISampleId relatedId : action.getItems())
                        {
                            SamplePE relatedSample = samplesMap.get(relatedId);
                            if (relatedSample != null)
                            {
                                relatedSamples.add(relatedSample);
                            }
                            if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), relatedSample))
                            {
                                throw new UnauthorizedObjectAccessException(relatedId);
                            }
                        }
                        removeRelatedSamples(context, sample, relatedSamples);
                    }
                }
            }

        }
    }

    protected abstract IdListUpdateValue<? extends ISampleId> getRelatedSamplesUpdate(IOperationContext context, SampleUpdate update);

    protected abstract void setRelatedSamples(IOperationContext context, SamplePE sample, Collection<SamplePE> relatedSamples);

    protected abstract void addRelatedSamples(IOperationContext context, SamplePE sample, Collection<SamplePE> relatedSamples);

    protected abstract void removeRelatedSamples(IOperationContext context, SamplePE sample, Collection<SamplePE> relatedSamples);

}
