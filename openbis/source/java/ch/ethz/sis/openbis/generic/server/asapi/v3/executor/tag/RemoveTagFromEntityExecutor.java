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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.tag.TagAuthorization;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class RemoveTagFromEntityExecutor implements IRemoveTagFromEntityExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapTagByIdExecutor mapTagByIdExecutor;

    @SuppressWarnings("unused")
    private RemoveTagFromEntityExecutor()
    {
    }

    public RemoveTagFromEntityExecutor(IMapTagByIdExecutor mapTagByIdExecutor)
    {
        this.mapTagByIdExecutor = mapTagByIdExecutor;
    }

    @Override
    public void removeTag(IOperationContext context, IEntityWithMetaprojects entity, Collection<? extends ITagId> tagIds)
    {
        TagAuthorization authorization = new TagAuthorization(context, daoFactory);
        Map<ITagId, MetaprojectPE> tagMap = mapTagByIdExecutor.map(context, tagIds);

        for (MetaprojectPE tag : tagMap.values())
        {
            authorization.checkAccess(tag);
            entity.removeMetaproject(tag);
        }
    }

}
