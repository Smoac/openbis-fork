/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IMapSampleByIdExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SetTagSamplesExecutor extends AbstractSetEntityMultipleRelationsExecutor<TagCreation, MetaprojectPE, ISampleId, SamplePE>
        implements ISetTagSamplesExecutor
{

    @Autowired
    private IMapSampleByIdExecutor mapSampleExecutor;

    @Autowired
    private ISetTagSamplesWithCacheExecutor setTagSamplesWithCacheExecutor;

    @Override
    protected void addRelatedIds(Set<ISampleId> relatedIds, TagCreation creation, MetaprojectPE entity)
    {
        addRelatedIds(relatedIds, creation.getSampleIds());
    }

    @Override
    protected void addRelated(Map<ISampleId, SamplePE> relatedMap, TagCreation creation, MetaprojectPE entity)
    {
        // nothing to do here
    }

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, List<ISampleId> relatedIds)
    {
        return mapSampleExecutor.map(context, relatedIds);
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
    protected void set(IOperationContext context, Map<TagCreation, MetaprojectPE> creationsMap, Map<ISampleId, SamplePE> relatedMap)
    {
        setTagSamplesWithCacheExecutor.set(context, creationsMap, relatedMap);
    }

}
