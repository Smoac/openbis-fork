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
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.DssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.IContentCache;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.IDssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.PathInfoDBAwareHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.RemoteHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;

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

    private final ISessionTokenProvider sessionTokenProvider;

    private final String dataStoreCode;

    private final boolean trustAllCertificates;

    private final IContentCache cache;

    private final IDssServiceRpcGenericFactory serviceFactory;

    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IShareIdManager shareIdManager, IConfigProvider configProvider,
            IContentCache contentCache, 
            ISessionTokenProvider sessionTokenProvider,
            ExposablePropertyPlaceholderConfigurer infoProvider)
    {
        this(openbisService, new DataSetDirectoryProvider(configProvider.getStoreRoot(),
                shareIdManager), null, new DssServiceRpcGenericFactory(), contentCache,
                sessionTokenProvider, configProvider.getDataStoreCode(), infoProvider);
    }

    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IShareIdManager shareIdManager, IConfigProvider configProvider,
            IContentCache contentCache, IHierarchicalContentFactory hierarchicalContentFactory,
            IDssServiceRpcGenericFactory serviceFactory,
            ISessionTokenProvider sessionTokenProvider,
            ExposablePropertyPlaceholderConfigurer infoProvider)
    {
        this(openbisService, new DataSetDirectoryProvider(configProvider.getStoreRoot(),
                shareIdManager), hierarchicalContentFactory, serviceFactory, contentCache,
                sessionTokenProvider, configProvider.getDataStoreCode(), infoProvider);
    }

    @Private
    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IDataSetDirectoryProvider directoryProvider,
            IHierarchicalContentFactory hierarchicalContentFactory,
            IDssServiceRpcGenericFactory serviceFactory, IContentCache contentCache,
            ISessionTokenProvider sessionTokenProvider, String dataStoreCode,
            ExposablePropertyPlaceholderConfigurer infoProvider)
    {
        this(openbisService, directoryProvider, hierarchicalContentFactory, serviceFactory,
                contentCache, sessionTokenProvider, dataStoreCode, infoProvider != null
                        && "true".equalsIgnoreCase(infoProvider.getResolvedProps().getProperty(
                                "trust-all-certificates")));
    }
    
    private HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IDataSetDirectoryProvider directoryProvider,
            IHierarchicalContentFactory hierarchicalContentFactory,
            IDssServiceRpcGenericFactory serviceFactory, IContentCache contentCache,
            ISessionTokenProvider sessionTokenProvider, String dataStoreCode, boolean trustAllCertificates)
    {
        this.openbisService = openbisService;
        this.directoryProvider = directoryProvider;
        this.hierarchicalContentFactory = hierarchicalContentFactory;
        this.serviceFactory = serviceFactory;
        this.cache = contentCache;
        this.sessionTokenProvider = sessionTokenProvider;
        this.dataStoreCode = dataStoreCode;
        this.trustAllCertificates = trustAllCertificates;
        
    }
    
    @Override
    public IHierarchicalContentProvider cloneFor(ISessionTokenProvider anotherSessionTokenProvider)
    {
        return new HierarchicalContentProvider(openbisService, directoryProvider,
                hierarchicalContentFactory, serviceFactory, cache, anotherSessionTokenProvider,
                dataStoreCode, trustAllCertificates);
    }

    @Override
    public IHierarchicalContent asContent(String dataSetCode)
    {
        IDatasetLocationNode locationNode = openbisService.tryGetDataSetLocation(dataSetCode);
        if (locationNode == null)
        {
            operationLog.error(String.format("Data set '%s' not found in openBIS server.",
                    dataSetCode));
            throw new IllegalArgumentException("Unknown data set: " + dataSetCode);
        }

        return asContent(locationNode);
    }

    @Override
    public IHierarchicalContent asContent(AbstractExternalData dataSet)
    {
        return asContent(new ExternalDataLocationNode(dataSet));
    }

    private IHierarchicalContent asContent(IDatasetLocationNode locationNode)
    {
        if (isLocal(locationNode))
        {
            if (locationNode.isContainer())
            {
                List<IHierarchicalContent> componentContents =
                        new ArrayList<IHierarchicalContent>();
                for (IDatasetLocationNode component : locationNode.getComponents())
                {
                    IHierarchicalContent componentContent = tryCreateComponentContent(component);
                    if (componentContent != null)
                    {
                        componentContents.add(componentContent);
                    }
                }
                return getHierarchicalContentFactory().asVirtualHierarchicalContent(
                        componentContents);
            } else
            {
                return asContent(locationNode.getLocation());
            }
        } else
        {
            ISingleDataSetPathInfoProvider provider = null;
            if (PathInfoDataSourceProvider.isDataSourceDefined())
            {
                IDataSetPathInfoProvider dataSetPathInfoProvider =
                        ServiceProvider.getDataSetPathInfoProvider();
                provider =
                        dataSetPathInfoProvider.tryGetSingleDataSetPathInfoProvider(locationNode
                                .getLocation().getDataSetCode());
            }

            if (trustAllCertificates)
            {
                SslCertificateHelper.trustAnyCertificate(locationNode.getLocation()
                        .getDataStoreUrl());
            }
            return new RemoteHierarchicalContent(locationNode, provider, serviceFactory,
                    sessionTokenProvider, cache);
        }
    }

    private boolean isLocal(IDatasetLocationNode node)
    {
        return this.dataStoreCode.equals(node.getLocation().getDataStoreCode());
    }

    private IHierarchicalContent tryCreateComponentContent(
            IDatasetLocationNode componentLocationNode)
    {
        try
        {
            if (componentLocationNode.isContainer())
            {
                return asContent(componentLocationNode);
            } else
            {
                return asContent(componentLocationNode.getLocation());
            }
        } catch (IllegalArgumentException ex)
        {
            operationLog
                    .info("ignoring contained data set "
                            + componentLocationNode.getLocation().getDataSetCode() + ": "
                            + ex.getMessage());
            return null;
        }
    }

    @Override
    public IHierarchicalContent asContent(final IDatasetLocation datasetLocation)
    {
        // NOTE: remember to call IHierarchicalContent.close() to unlock the data set when finished
        // working with the IHierarchivalContent
        directoryProvider.getShareIdManager().lock(datasetLocation.getDataSetCode());
        File dataSetDirectory = directoryProvider.getDataSetDirectory(datasetLocation);
        IDelegatedAction onCloseAction = new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    directoryProvider.getShareIdManager().releaseLock(
                            datasetLocation.getDataSetCode());
                }
            };
        return asContent(dataSetDirectory, onCloseAction);
    }

    @Override
    public IHierarchicalContent asContent(File dataSetDirectory)
    {
        return getHierarchicalContentFactory().asHierarchicalContent(dataSetDirectory,
                IDelegatedAction.DO_NOTHING);
    }

    public IHierarchicalContent asContent(File dataSetDirectory, IDelegatedAction onCloseAction)
    {
        try
        {
            return getHierarchicalContentFactory().asHierarchicalContent(dataSetDirectory,
                    onCloseAction);
        } catch (RuntimeException ex)
        {
            onCloseAction.execute();
            throw ex;
        }
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
