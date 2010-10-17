/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.batch;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleBatchUpdatesDTO;

/**
 * {@link IBatchOperation} updating samples.
 * 
 * @author Izabela Adamczyk
 */
public class SampleBatchUpdate implements IBatchOperation<SampleBatchUpdatesDTO>
{
    private final ISampleTable businessTable;

    private final List<SampleBatchUpdatesDTO> entities;

    public SampleBatchUpdate(ISampleTable businessTable, List<SampleBatchUpdatesDTO> entities)
    {
        this.businessTable = businessTable;
        this.entities = entities;
    }

    public void execute(List<SampleBatchUpdatesDTO> updates)
    {
        businessTable.prepareForUpdate(updates);
        businessTable.save();
    }

    public List<SampleBatchUpdatesDTO> getAllEntities()
    {
        return entities;
    }

    public String getEntityName()
    {
        return "sample";
    }

    public String getOperationName()
    {
        return "update";
    }

}