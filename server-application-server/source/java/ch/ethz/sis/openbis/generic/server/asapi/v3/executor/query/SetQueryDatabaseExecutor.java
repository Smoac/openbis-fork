/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;

/**
 * @author pkupczyk
 */
@Component
public class SetQueryDatabaseExecutor extends AbstractSetEntityToOneRelationExecutor<QueryCreation, QueryPE, IQueryDatabaseId, DatabaseDefinition>
        implements ISetQueryDatabaseExecutor
{

    @Autowired
    private IMapQueryDatabaseByIdExecutor mapQueryDatabaseByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "query-database";
    }

    @Override
    protected IQueryDatabaseId getRelatedId(QueryCreation creation)
    {
        return creation.getDatabaseId();
    }

    @Override
    protected Map<IQueryDatabaseId, DatabaseDefinition> map(IOperationContext context, List<IQueryDatabaseId> relatedIds)
    {
        return mapQueryDatabaseByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, QueryPE entity, IQueryDatabaseId relatedId, DatabaseDefinition related)
    {
        // nothing to do
    }

    @Override
    protected void set(IOperationContext context, QueryPE entity, DatabaseDefinition related)
    {
        entity.setQueryDatabaseKey(related.getKey());
    }
}
