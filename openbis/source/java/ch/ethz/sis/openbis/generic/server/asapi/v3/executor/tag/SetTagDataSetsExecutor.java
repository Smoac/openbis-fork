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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IMapDataSetByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class SetTagDataSetsExecutor extends AbstractSetEntityMultipleRelationsExecutor<TagCreation, MetaprojectPE, IDataSetId, DataPE>
        implements ISetTagDataSetsExecutor
{

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetExecutor;

    @Autowired
    private ISetTagDataSetsWithCacheExecutor setTagDataSetsWithCacheExecutor;

    @Override
    protected void addRelatedIds(Set<IDataSetId> relatedIds, TagCreation creation, MetaprojectPE entity)
    {
        addRelatedIds(relatedIds, creation.getDataSetIds());
    }

    @Override
    protected void addRelated(Map<IDataSetId, DataPE> relatedMap, TagCreation creation, MetaprojectPE entity)
    {
        // nothing to do here
    }

    @Override
    protected Map<IDataSetId, DataPE> map(IOperationContext context, List<IDataSetId> relatedIds)
    {
        return mapDataSetExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, IDataSetId relatedId, DataPE related)
    {
        if (false == new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }

    }

    @Override
    protected void set(IOperationContext context, MapBatch<TagCreation, MetaprojectPE> batch, Map<IDataSetId, DataPE> relatedMap)
    {
        setTagDataSetsWithCacheExecutor.set(context, batch, relatedMap);
    }

}
