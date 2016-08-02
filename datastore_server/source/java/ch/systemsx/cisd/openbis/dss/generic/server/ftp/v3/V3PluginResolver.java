/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpFileResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpNonExistingFile;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;

class V3PluginResolver implements V3Resolver
{

    @Override
    public V3FtpFile resolve(String fullPath, String[] subPath, FtpPathResolverContext context)
    {
        if (subPath.length == 0)
        {
            return listDataSetTypes(fullPath, context);
        }

        String dataSetType = subPath[0];
        if (subPath.length == 1)
        {
            return listDataSetsOfGivenType(fullPath, dataSetType, context);
        }

        return resolveFileSearch(fullPath, subPath, context);
    }

    private V3FtpFile resolveFileSearch(String fullPath, String[] subPath, FtpPathResolverContext context)
    {
        String dataSetCode = subPath[1];
        String requestedFileName = subPath.length == 2 ? null : subPath[2];
        if (subPath.length > 3)
        {
            throw new IllegalArgumentException("This resolver can't resolve path of that length");
        }

        List<DataSet> dataSetsToSearch = searchForDataSetAndParents(dataSetCode, context);

        if (requestedFileName != null)
        {
            IHierarchicalContentNode result = findRequestedNode(dataSetsToSearch, requestedFileName, context.getContentProvider());
            if (result != null)
            {
                return new V3FtpFileResponse(fullPath, result);
            } else
            {
                return new V3FtpNonExistingFile(fullPath, "Unable to locate requested file");
            }
        }

        V3FtpDirectoryResponse response = new V3FtpDirectoryResponse(fullPath);
        for (IHierarchicalContentNode file : findAllNodes(dataSetsToSearch, context.getContentProvider()))
        {
            response.addFile(file.getName(), file);
        }
        return response;
    }

    private List<IHierarchicalContentNode> findAllNodes(List<DataSet> dataSetsToSearch, IHierarchicalContentProvider contentProvider)
    {
        List<IHierarchicalContentNode> result = new ArrayList<>();
        for (DataSet dataSet : dataSetsToSearch)
        {
            Stack<IHierarchicalContentNode> nodes = new Stack<>();
            nodes.push(contentProvider.asContent(dataSet.getCode()).getRootNode());

            while (false == nodes.isEmpty())
            {
                IHierarchicalContentNode node = nodes.pop();
                if (node.isDirectory())
                {
                    for (IHierarchicalContentNode child : node.getChildNodes())
                    {
                        nodes.push(child);
                    }
                } else
                {
                    result.add(node);
                }
            }
        }
        return result;
    }

    private IHierarchicalContentNode findRequestedNode(List<DataSet> dataSetsToSearch, String requestedFileName,
            IHierarchicalContentProvider contentProvider)
    {
        for (DataSet dataSet : dataSetsToSearch)
        {
            IHierarchicalContent content = contentProvider.asContent(dataSet.getCode());
            IHierarchicalContentNode result = findRequestedNode(content.getRootNode(), requestedFileName);
            if (result != null)
            {
                return result;
            }
        }
        return null;
    }

    private IHierarchicalContentNode findRequestedNode(IHierarchicalContentNode node, String requestedFileName)
    {
        if (node.isDirectory())
        {
            for (IHierarchicalContentNode subNode : node.getChildNodes())
            {
                IHierarchicalContentNode result = findRequestedNode(subNode, requestedFileName);
                if (result != null)
                {
                    return result;
                }
            }
        } else
        {
            if (node.getName().equals(requestedFileName))
            {
                return node;
            }
        }
        return null;
    }

    // returns a list of data sets starting with requested data set and continuedby it's parents
    private List<DataSet> searchForDataSetAndParents(String dataSetCode, FtpPathResolverContext context)
    {
        DataSetPermId dataId = new DataSetPermId(dataSetCode);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withParents();
        DataSet dataSet = context.getV3Api().getDataSets(context.getSessionToken(), Collections.singletonList(dataId), fetchOptions).get(dataId);

        List<DataSet> dataSetsToSearch = new ArrayList<>();
        dataSetsToSearch.add(dataSet);
        dataSetsToSearch.addAll(dataSet.getParents());
        return dataSetsToSearch;
    }

    private V3FtpFile listDataSetsOfGivenType(String fullPath, String dataSetType, FtpPathResolverContext context)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withParents();
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        searchCriteria.withType().withCode().thatEquals(dataSetType);
        List<DataSet> dataSets = context.getV3Api().searchDataSets(context.getSessionToken(), searchCriteria, fetchOptions).getObjects();

        V3FtpDirectoryResponse result = new V3FtpDirectoryResponse(fullPath);
        for (DataSet dataSet : dataSets)
        {
            result.addDirectory(dataSet.getCode());
        }
        return result;
    }

    private V3FtpFile listDataSetTypes(String fullPath, FtpPathResolverContext context)
    {
        List<DataSetType> dataSetTypes =
                context.getV3Api().searchDataSetTypes(context.getSessionToken(), new DataSetTypeSearchCriteria(), new DataSetTypeFetchOptions())
                        .getObjects();

        V3FtpDirectoryResponse response = new V3FtpDirectoryResponse(fullPath);
        for (DataSetType type : dataSetTypes)
        {
            response.addDirectory(type.getCode());
        }
        return response;
    }

}