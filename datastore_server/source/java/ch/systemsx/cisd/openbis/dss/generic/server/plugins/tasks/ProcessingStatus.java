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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Holder of finished processing {@link Status}es of all handled data sets.
 * 
 * @author Piotr Buczek
 */
public class ProcessingStatus
{

    private Map<Status, List<String/* dataset code */>> datasetByStatus =
            new LinkedHashMap<Status, List<String>>();

    public void addDatasetStatus(String datasetCode, Status status)
    {
        List<String> datasets = datasetByStatus.get(status);
        if (datasets == null)
        {
            datasets = new ArrayList<String>();
            datasetByStatus.put(status, datasets);
        }
        datasets.add(datasetCode);
    }

    public List<Status> getErrorStatuses()
    {
        List<Status> result = new ArrayList<Status>(datasetByStatus.keySet());
        result.remove(Status.OK);
        return result;
    }

    public List<String/* dataset code */> getDatasetsByStatus(Status status)
    {
        return datasetByStatus.get(status);
    }

    public void addDatasetStatus(DatasetDescription dataset, Status status)
    {
        addDatasetStatus(dataset.getDatasetCode(), status);
    }

}
