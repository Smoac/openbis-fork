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
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * The default implementation of {@link IHierarchicalContentProvider}.
 * 
 * @author Piotr Buczek
 */
public class HierarchicalContentProvider implements IHierarchicalContentProvider
{

    private final IDataSetDirectoryProvider directoryProvider;

    private final IHierarchicalContentFactory hierarchicalContentFactory;

    private final IEncapsulatedOpenBISService openbisService;

    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IShareIdManager shareIdManager, IConfigProvider configProvider)
    {
        this(openbisService, new DataSetDirectoryProvider(configProvider.getStoreRoot(),
                shareIdManager), new HierarchicalContentFactory());
    }

    // for tests
    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IDataSetDirectoryProvider directoryProvider,
            IHierarchicalContentFactory hierarchicalContentFactory)
    {
        this.openbisService = openbisService;
        this.directoryProvider = directoryProvider;
        this.hierarchicalContentFactory = hierarchicalContentFactory;
    }

    public IHierarchicalContent asContent(String dataSetCode)
    {
        // this is temporary implementation - it shouldn't access openBIS after LMS-2172 is done
        ExternalData dataSet = openbisService.tryGetDataSet(dataSetCode);

        return asContent(dataSet);
    }

    public IHierarchicalContent asContent(IDatasetLocation datasetLocation)
    {
        // this is temporary implementation - it should access DB instead of filesystem
        // IHierarchicalContent.close() should be called to unlock the dataset
        directoryProvider.getShareIdManager().lock(datasetLocation.getDatasetCode());
        File dataSetDirectory = directoryProvider.getDataSetDirectory(datasetLocation);

        return asContent(dataSetDirectory);
    }

    public IHierarchicalContent asContent(File dataSetDirectory)
    {
        return hierarchicalContentFactory.asHierarchicalContent(dataSetDirectory,
                IDelegatedAction.DO_NOTHING);
    }

    public IHierarchicalContent asContent(File dataSetDirectory, IDelegatedAction onCloseAction)
    {
        return hierarchicalContentFactory.asHierarchicalContent(dataSetDirectory, onCloseAction);
    }

}
