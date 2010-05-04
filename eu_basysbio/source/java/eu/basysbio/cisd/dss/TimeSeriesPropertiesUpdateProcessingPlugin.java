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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Extracts header properties from TSV file belonging to data set and updates data set properties.
 * 
 * @author Izabela Adamczyk
 */
public class TimeSeriesPropertiesUpdateProcessingPlugin extends AbstractDatastorePlugin implements
        IProcessingPluginTask
{

    public TimeSeriesPropertiesUpdateProcessingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    private static final long serialVersionUID = 1L;

    public ProcessingStatus process(List<DatasetDescription> datasets,
            Map<String, String> parameterBindings)
    {
        final ProcessingStatus result = new ProcessingStatus();
        for (DatasetDescription dataset : datasets)
        {
            Status status = processDataset(dataset);
            result.addDatasetStatus(dataset, status);
        }
        return result;
    }

    private Status processDataset(DatasetDescription dataset)
    {
        try
        {
            File file = getDataSubDir(dataset);
            List<NewProperty> newProperties =
                    HeaderUtils.extractHeaderProperties(file, true);
            SpaceIdentifier space =
                    new SpaceIdentifier(dataset.getDatabaseInstanceCode(), dataset.getGroupCode());
            ServiceProvider.getOpenBISService().updateDataSet(dataset.getDatasetCode(),
                    newProperties, space);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }
    }
}
