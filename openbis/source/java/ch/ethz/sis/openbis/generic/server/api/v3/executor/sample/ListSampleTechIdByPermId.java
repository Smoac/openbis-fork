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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.QueryTool;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.TechIdStringIdentifierRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ListSampleTechIdByPermId implements IListObjectById<SamplePermId, Long>
{
    private Map<Long, SamplePermId> permIdsByTechIds = new HashMap<Long, SamplePermId>(); 

    @Override
    public Class<SamplePermId> getIdClass()
    {
        return SamplePermId.class;
    }

    @Override
    public SamplePermId createId(Long techId)
    {
        return permIdsByTechIds.get(techId);
    }

    @Override
    public List<Long> listByIds(List<SamplePermId> ids)
    {
        SampleQuery query = QueryTool.getManagedQuery(SampleQuery.class);
        
        List<String> permIds = new ArrayList<>(ids.size());
        for (SamplePermId permId : ids)
        {
            permIds.add(permId.getPermId());
        } 
        List<Long> techIds = new ArrayList<>();
        for (TechIdStringIdentifierRecord record : query.listSampleTechIdsByPermIds(permIds.toArray(new String[permIds.size()])))
        {
            techIds.add(record.id);
            permIdsByTechIds.put(record.id, new SamplePermId(record.identifier));
        }
        return techIds;
    }

}
