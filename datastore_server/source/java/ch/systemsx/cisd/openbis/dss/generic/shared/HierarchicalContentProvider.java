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

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.PathInfoDBAwareHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * The default implementation of {@link IHierarchicalContentProvider}.
 * 
 * @author Piotr Buczek
 */
public class HierarchicalContentProvider implements IHierarchicalContentProvider
{

    private final IEncapsulatedOpenBISService openbisService;

    private final IDataSetDirectoryProvider directoryProvider;

    private IHierarchicalContentFactory hierarchicalContentFactory;

    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IShareIdManager shareIdManager, IConfigProvider configProvider)
    {
        this(openbisService, new DataSetDirectoryProvider(configProvider.getStoreRoot(),
                shareIdManager));
    }

    private HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IDataSetDirectoryProvider directoryProvider)
    {
        this.openbisService = openbisService;
        this.directoryProvider = directoryProvider;
    }

    @Private
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
        ExternalData externalData = openbisService.tryGetDataSet(dataSetCode);
        if (externalData == null)
        {
            throw new IllegalArgumentException("Unknown data set " + dataSetCode);
        }
        // this is a temporary fix
        DataSet dataSet = externalData.tryGetAsDataSet();
        if (dataSet == null)
        {
            throw new IllegalArgumentException("Not implemented for container data sets.");
        }
        return asContent(dataSet);
    }

    public IHierarchicalContent asContent(final IDatasetLocation datasetLocation)
    {
        // IHierarchicalContent.close() should be called to unlock the dataset
        directoryProvider.getShareIdManager().lock(datasetLocation.getDataSetCode());
        File dataSetDirectory = directoryProvider.getDataSetDirectory(datasetLocation);
        IDelegatedAction onCloseAction = new IDelegatedAction()
            {
                public void execute()
                {
                    directoryProvider.getShareIdManager().releaseLock(
                            datasetLocation.getDataSetCode());
                }
            };
        return asContent(dataSetDirectory, onCloseAction);
    }

    public IHierarchicalContent asContent(File dataSetDirectory)
    {
        return getHierarchicalContentFactory().asHierarchicalContent(dataSetDirectory,
                IDelegatedAction.DO_NOTHING);
    }

    public IHierarchicalContent asContent(File dataSetDirectory, IDelegatedAction onCloseAction)
    {
        return getHierarchicalContentFactory().asHierarchicalContent(dataSetDirectory,
                onCloseAction);
    }

    private IHierarchicalContentFactory getHierarchicalContentFactory()
    {
        if (hierarchicalContentFactory == null)
        {
            hierarchicalContentFactory = PathInfoDBAwareHierarchicalContentFactory.create();
        }
        return hierarchicalContentFactory;
    }

}
