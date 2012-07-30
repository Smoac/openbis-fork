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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.IOException;

import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IChecksumProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;

/**
 * Checksum provider that uses a hierarchical content provider to get checksums.
 * 
 * @author pkupczyk
 */
public class HierarchicalContentChecksumProvider implements IChecksumProvider
{

    private IHierarchicalContentProvider hierarchicalContentProvider;

    public HierarchicalContentChecksumProvider(
            IHierarchicalContentProvider hierarchicalContentProvider)
    {
        this.hierarchicalContentProvider = hierarchicalContentProvider;
    }

    @Override
    public long getChecksum(String dataSetCode, String relativePath) throws IOException
    {
        IHierarchicalContent content = null;

        try
        {
            content = hierarchicalContentProvider.asContent(dataSetCode);
            IHierarchicalContentNode node = content.getNode(relativePath);
            return node.getChecksumCRC32();
        } finally
        {
            if (content != null)
            {
                content.close();
            }
        }
    }
}
