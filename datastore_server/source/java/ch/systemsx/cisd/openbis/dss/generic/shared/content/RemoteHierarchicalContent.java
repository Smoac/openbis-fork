/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.util.List;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * Implementation of HierchicalContent that is stored on remote datastore server.
 * 
 * @author anttil
 */
public class RemoteHierarchicalContent implements IHierarchicalContent
{

    private final IDatasetLocationNode location;

    private final IDataSetPathInfoProvider pathInfoProvider;

    private final OpenBISSessionHolder sessionHolder;

    private final IDssServiceRpcGeneric local;

    private final String sessionWorkspaceRoot;

    public RemoteHierarchicalContent(IDatasetLocationNode location,
            IDataSetPathInfoProvider pathInfoProvider,
            OpenBISSessionHolder sessionHolder,
            IDssServiceRpcGeneric local,
            String sessionWorkspaceRoot)
    {
        this.location = location;
        this.pathInfoProvider = pathInfoProvider;
        this.sessionHolder = sessionHolder;
        this.local = local;
        this.sessionWorkspaceRoot = sessionWorkspaceRoot;
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {

        ISingleDataSetPathInfoProvider provider =
                pathInfoProvider.tryGetSingleDataSetPathInfoProvider(location.getLocation()
                        .getDataSetCode());

        IDssServiceRpcGeneric remoteDss =
                HttpInvokerUtils.createServiceStub(IDssServiceRpcGeneric.class, location
                        .getLocation()
                        .getDataStoreUrl() + "/datastore_server/rmi-dss-api-v1", 300000);

        DataSetPathInfo info = null;
        if (provider != null)
        {
            info = provider.getRootPathInfo();
        }

        if (info == null)
        {
            FileInfoDssDTO[] files =
                    remoteDss.listFilesForDataSet(sessionHolder.getSessionToken(),
                            location.getLocation().getDataSetCode(), "", false);

            FileInfoDssDTO fileInfo = files[0];
            info = new DataSetPathInfo();
            info.setChecksumCRC32(fileInfo.tryGetCrc32Checksum());
            info.setDirectory(fileInfo.isDirectory());
            info.setFileName(fileInfo.getPathInDataSet());
            info.setLastModified(null);
            info.setRelativePath(fileInfo.getPathInDataSet());
            info.setSizeInBytes(fileInfo.getFileSize());
        }

        return new RemoteHierarchicalContentNode(
                location.getLocation().getDataSetCode(),
                info,
                provider,
                local,
                remoteDss,
                sessionHolder,
                sessionWorkspaceRoot);
    }

    @Override
    public IHierarchicalContentNode getNode(String relativePath) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String relativePathPattern)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String startingPath,
            String fileNamePattern)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close()
    {
    }

}
