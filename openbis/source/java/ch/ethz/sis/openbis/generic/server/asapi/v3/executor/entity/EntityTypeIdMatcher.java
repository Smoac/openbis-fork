/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityTypeConverter;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;

/**
 * @author pkupczyk
 */
public class EntityTypeIdMatcher extends SimpleFieldMatcher<EntityTypePE>
{

    @Override
    protected boolean isMatching(IOperationContext context, EntityTypePE object, ISearchCriteria criteria)
    {
        Object id = ((IdSearchCriteria<?>) criteria).getId();
        return isMatching(id, object);
    }

    public static boolean isMatching(Object id, EntityTypePE object)
    {
        if (id == null)
        {
            return true;
        } else if (id instanceof EntityTypePermId)
        {
            EntityTypePermId permId = (EntityTypePermId) id;

            if (permId.getPermId() == null)
            {
                throw new UserFailureException("Entity type perm id cannot be null");
            }
            if (permId.getEntityKind() == null)
            {
                throw new UserFailureException("Entity type entity kind cannot be null");
            }

            return permId.getPermId().equals(object.getPermId())
                    && permId.getEntityKind().equals(EntityTypeConverter.convert(object.getEntityKind()));
        } else
        {
            throw new IllegalArgumentException("Unknown id: " + id.getClass());
        }
    }
}
