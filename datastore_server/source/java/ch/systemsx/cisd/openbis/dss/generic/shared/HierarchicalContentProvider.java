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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.PathInfoDBAwareHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
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
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            HierarchicalContentProvider.class);

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
            operationLog.error(String.format("Data set '%s' not found in openBIS server.",
                    dataSetCode));
            throw new IllegalArgumentException("Unknown data set: " + dataSetCode);
        }
        if (externalData.isContainer())
        {
            ContainerDataSet container = externalData.tryGetAsContainerDataSet();
            List<IHierarchicalContent> componentContents = new ArrayList<IHierarchicalContent>();
            for (ExternalData component : container.getContainedDataSets())
            {
                IHierarchicalContent componentContent = tryCreateComponentContent(component);
                if (componentContent != null)
                {
                    componentContents.add(componentContent);
                }
            }
            return getHierarchicalContentFactory().asVirtualHierarchicalContent(componentContents);
        } else
        {
            return asContent(asDataSet(externalData));
        }
    }

    private IHierarchicalContent tryCreateComponentContent(ExternalData component)
    {
        try
        {
            if (component.isContainer())
            {
                return asContent(component.getCode());
            } else
            {
                return asContent(asDataSet(component));
            }
        } catch (IllegalArgumentException ex)
        {
            operationLog.info("ignoring contained data set " + component.getCode() + ": "
                    + ex.getMessage());
            return null;
        }
    }

    public IHierarchicalContent asContent(final IDatasetLocation datasetLocation)
    {
        // NOTE: remember to call IHierarchicalContent.close() to unlock the dataset when finished
        // working with the IHierarchivalContent
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

    private static DataSet asDataSet(ExternalData externalData)
    {
        DataSet dataSet = externalData.tryGetAsDataSet();
        if (dataSet == null)
        {
            throw new IllegalArgumentException(
                    "Couldn't retrieve full data set infomation from data set "
                            + externalData.getCode());
        }
        return dataSet;
    }

}
