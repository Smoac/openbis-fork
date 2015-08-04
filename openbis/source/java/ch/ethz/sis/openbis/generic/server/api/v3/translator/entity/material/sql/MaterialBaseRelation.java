/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.QueryTool;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relation;

/**
 * @author pkupczyk
 */
public class MaterialBaseRelation implements Relation
{

    private Collection<Long> materialIds;

    private Map<Long, MaterialBaseRecord> recordMap = new HashMap<Long, MaterialBaseRecord>();

    public MaterialBaseRelation(Collection<Long> materialIds)
    {
        this.materialIds = materialIds;
    }

    @Override
    public void load()
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        List<MaterialBaseRecord> records = query.getMaterials(new LongOpenHashSet(materialIds));

        for (MaterialBaseRecord record : records)
        {
            recordMap.put(record.id, record);
        }
    }

    public MaterialBaseRecord getRecord(Long materialId)
    {
        return recordMap.get(materialId);
    }

}
