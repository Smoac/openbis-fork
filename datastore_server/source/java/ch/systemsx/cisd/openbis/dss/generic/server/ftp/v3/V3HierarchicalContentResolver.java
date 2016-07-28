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

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;

class V3HierarchicalContentResolver extends V3Resolver
{

    private IHierarchicalContent content;

    public V3HierarchicalContentResolver(IHierarchicalContent content, FtpPathResolverContext resolverContext)
    {
        super(resolverContext);
        this.content = content;
    }

    @Override
    public V3FtpFile resolve(String fullPath, String[] subPath)
    {
        IHierarchicalContentNode rootNode;
        if (subPath.length == 0)
        {
            rootNode = content.getRootNode();
        } else
        {
            rootNode = content.getNode(StringUtils.join(subPath, "/"));
        }

        if (false == rootNode.isDirectory())
        {
            return new V3FtpFileResponse(fullPath, rootNode);
        }

        V3FtpDirectoryResponse response = new V3FtpDirectoryResponse(fullPath);
        for (IHierarchicalContentNode node : rootNode.getChildNodes())
        {
            if (node.isDirectory())
            {
                response.AddDirectory(node.getName());
            } else
            {
                response.AddFile(node.getName(), node);
            }
        }
        return response;
    }

}