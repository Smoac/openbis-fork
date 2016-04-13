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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToManyRelationExecutor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdateScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexUpdateOperation;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
public abstract class UpdateTagEntitiesExecutor<RELATED_ID, RELATED_PE extends IEntityWithMetaprojects>
        extends AbstractUpdateEntityToManyRelationExecutor<TagUpdate, MetaprojectPE, RELATED_ID, RELATED_PE>
{

    @Autowired
    private IDAOFactory daoFactory;

    protected abstract Class<RELATED_PE> getRelatedClass();

    protected abstract RELATED_PE getCurrentlyRelated(MetaprojectAssignmentPE entity);

    @Override
    public void update(IOperationContext context, Map<TagUpdate, MetaprojectPE> entitiesMap, Map<RELATED_ID, RELATED_PE> relatedMap)
    {
        super.update(context, entitiesMap, relatedMap);

        daoFactory.getSessionFactory().getCurrentSession().flush();

        IFullTextIndexUpdateScheduler indexUpdater = daoFactory.getPersistencyResources().getIndexUpdateScheduler();
        List<Long> relatedIds = new ArrayList<Long>();

        for (RELATED_PE related : relatedMap.values())
        {
            if (related != null)
            {
                relatedIds.add(related.getId());
            }
        }

        if (false == relatedIds.isEmpty())
        {
            indexUpdater.scheduleUpdate(IndexUpdateOperation.reindex(getRelatedClass(), relatedIds));
        }
    }

    @Override
    protected Collection<RELATED_PE> getCurrentlyRelated(MetaprojectPE entity)
    {
        List<RELATED_PE> relatedList = new ArrayList<RELATED_PE>();

        for (MetaprojectAssignmentPE assignment : entity.getAssignments())
        {
            RELATED_PE related = getCurrentlyRelated(assignment);
            if (related != null)
            {
                relatedList.add(related);
            }
        }

        return relatedList;
    }

    @Override
    protected void add(IOperationContext context, MetaprojectPE entity, RELATED_PE related)
    {
        related.addMetaproject(entity);
    }

    @Override
    protected void remove(IOperationContext context, MetaprojectPE entity, RELATED_PE related)
    {
        related.removeMetaproject(entity);
    }

}
