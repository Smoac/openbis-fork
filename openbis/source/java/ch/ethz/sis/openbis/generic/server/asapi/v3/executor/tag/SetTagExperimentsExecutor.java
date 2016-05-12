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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class SetTagExperimentsExecutor extends AbstractSetEntityMultipleRelationsExecutor<TagCreation, MetaprojectPE, IExperimentId, ExperimentPE>
        implements ISetTagExperimentsExecutor
{

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentExecutor;

    @Autowired
    private ISetTagExperimentsWithCacheExecutor setTagExperimentsWithCacheExecutor;

    @Override
    protected void addRelatedIds(Set<IExperimentId> relatedIds, TagCreation creation, MetaprojectPE entity)
    {
        addRelatedIds(relatedIds, creation.getExperimentIds());
    }

    @Override
    protected void addRelated(Map<IExperimentId, ExperimentPE> relatedMap, TagCreation creation, MetaprojectPE entity)
    {
        // nothing to do here
    }

    @Override
    protected Map<IExperimentId, ExperimentPE> map(IOperationContext context, List<IExperimentId> relatedIds)
    {
        return mapExperimentExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, IExperimentId relatedId, ExperimentPE related)
    {
        if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void set(IOperationContext context, MapBatch<TagCreation, MetaprojectPE> batch, Map<IExperimentId, ExperimentPE> relatedMap)
    {
        setTagExperimentsWithCacheExecutor.set(context, batch, relatedMap);
    }

}
