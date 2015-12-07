/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.api.v3.helper.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.TechIdStringIdentifierRecord;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractListTechIdByPermId<ID extends ObjectPermId> extends AbstractListTechIdById<ID>
{

    @Override
    protected Map<Long, ID> createIdsByTechIdsMap(List<ID> ids)
    {
        List<String> permIds = new ArrayList<>(ids.size());
        for (ID permId : ids)
        {
            permIds.add(permId.getPermId());
        } 
        String[] permIdsAsArray = permIds.toArray(new String[permIds.size()]);
        Map<Long, ID> result = new HashMap<>();
        List<TechIdStringIdentifierRecord> queryTechIds = queryTechIds(permIdsAsArray);
        for (TechIdStringIdentifierRecord record : queryTechIds)
        {
            result.put(record.id, createPermId(record.identifier));
        }
        return result;
    }
    
    protected abstract List<TechIdStringIdentifierRecord> queryTechIds(String[] permIds);
    
    protected abstract ID createPermId(String permIdAsString);

}
