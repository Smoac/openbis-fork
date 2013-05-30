/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DistributedArchiver extends AbstractArchiverProcessingPlugin
{
    private static final long serialVersionUID = 1L;
    
    public DistributedArchiver(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, null, null);
    }
    
    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets, ArchiverTaskContext context)
    {
        List<String> dataSetCodes = new ArrayList<String>();
        for (DatasetDescription datasetDescription : datasets)
        {
            dataSetCodes.add(datasetDescription.getDataSetCode());
        }
        List<AbstractExternalData> dataSets = getService().listDataSetsByCode(dataSetCodes);
        return null;
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets, ArchiverTaskContext context)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected DatasetProcessingStatuses doDeleteFromArchive(List<? extends IDatasetLocation> datasets)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset, ArchiverTaskContext context)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
