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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.as.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractSetEntityMultipleRelationsExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleRelatedSamplesExecutor extends AbstractSetEntityMultipleRelationsExecutor<SampleCreation, SamplePE, ISampleId> implements
        ISetSampleRelatedSamplesExecutor
{

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private ISetSampleContainerExecutor setSampleContainerExecutor;

    @Autowired
    private ISetSampleComponentsExecutor setSampleComponentsExecutor;

    @Autowired
    private ISetSampleParentsExecutor setSampleParentsExecutor;

    @Autowired
    private ISetSampleChildrenExecutor setSampleChildrenExecutor;

    @Override
    protected void addRelatedIds(Set<ISampleId> relatedIds, SampleCreation creation)
    {
        addRelatedIds(relatedIds, creation.getContainerId());
        addRelatedIds(relatedIds, creation.getComponentIds());
        addRelatedIds(relatedIds, creation.getParentIds());
        addRelatedIds(relatedIds, creation.getChildIds());
    }

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, List<ISampleId> relatedIds)
    {
        return mapSampleByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, ISampleId relatedId, SamplePE related)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void set(IOperationContext context, Map<SampleCreation, SamplePE> creationsMap, Map<ISampleId, SamplePE> relatedMap)
    {
        setSampleContainerExecutor.set(context, creationsMap, relatedMap);
        setSampleComponentsExecutor.set(context, creationsMap, relatedMap);
        setSampleParentsExecutor.set(context, creationsMap, relatedMap);
        setSampleChildrenExecutor.set(context, creationsMap, relatedMap);
    }

}
