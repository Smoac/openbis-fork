/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;

import ch.systemsx.cisd.common.io.HierarchicalContentFactory;
import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * @author Piotr Buczek
 */
public class HierarchicalContentProvider implements IHierarchicalContentProvider
{

    private IDataSetDirectoryProvider directoryProvider;

    private IHierarchicalContentFactory hierarchicalContentFactory;

    private IEncapsulatedOpenBISService openbisService;

    private IDataSetDirectoryProvider getDataSetDirectoryProvider()
    {
        if (directoryProvider == null)
        {
            IShareIdManager shareIdManager = ServiceProvider.getShareIdManager();
            IConfigProvider configProvider = ServiceProvider.getConfigProvider();
            directoryProvider =
                    new DataSetDirectoryProvider(configProvider.getStoreRoot(), shareIdManager);
        }
        return directoryProvider;
    }

    private IHierarchicalContentFactory getHierarchicalContentFactory()
    {
        if (hierarchicalContentFactory == null)
        {
            hierarchicalContentFactory = new HierarchicalContentFactory();
        }
        return hierarchicalContentFactory;
    }

    private IEncapsulatedOpenBISService getOpenBISService()
    {
        if (openbisService == null)
        {
            openbisService = ServiceProvider.getOpenBISService();
        }
        return openbisService;
    }

    public IHierarchicalContent asContent(String dataSetCode)
    {
        // this is temporary implementation - it shouldn't access openBIS after LMS-2172 is done
        ExternalData dataSet = getOpenBISService().tryGetDataSet(dataSetCode);
        
        return asContent(dataSet);
    }

    public IHierarchicalContent asContent(IDatasetLocation datasetLocation)
    {
        // this is temporary implementation - it should access DB instead of filesystem
        File dataSetDirectory = getDataSetDirectoryProvider().getDataSetDirectory(datasetLocation);
        return asContent(dataSetDirectory);
    }

    private IHierarchicalContent asContent(File dataSetDirectory)
    {
        return getHierarchicalContentFactory().asHierarchicalContent(dataSetDirectory);
    }

}
