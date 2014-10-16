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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.common;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.MapObjectById;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.IObjectId;

/**
 * @author pkupczyk
 */
public abstract class AbstractMapObjectByIdExecutor<ID extends IObjectId, OBJECT> implements IMapObjectByIdExecutor<ID, OBJECT>
{

    @Override
    public Map<ID, OBJECT> map(IOperationContext context, Collection<? extends ID> ids)
    {
        if (ids == null)
        {
            throw new IllegalArgumentException("Ids were null");
        }
        if (ids.isEmpty())
        {
            return Collections.emptyMap();
        }
        return new MapObjectById<ID, OBJECT>().map(createListers(context), ids);
    }

    protected abstract List<IListObjectById<? extends ID, OBJECT>> createListers(IOperationContext context);

}
