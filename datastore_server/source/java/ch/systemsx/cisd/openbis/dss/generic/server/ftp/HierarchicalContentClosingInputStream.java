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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.systemsx.cisd.common.io.IHierarchicalContent;

/**
 * An {@link InputStream} implementation which closes an associated {@link IHierarchicalContent}
 * together with an underlying target {@link InputStream}.
 * 
 * @author Kaloyan Enimanev
 */
public class HierarchicalContentClosingInputStream extends FilterInputStream
{
    private final IHierarchicalContent hierarchicalContent;

    public HierarchicalContentClosingInputStream(InputStream target,
            IHierarchicalContent hierarchicalContent)
    {
        super(target);
        this.hierarchicalContent = hierarchicalContent;
    }

    @Override
    public void close() throws IOException
    {
        // no error can be throw here
        hierarchicalContent.close();

        // can throw IOException
        super.close();
    }

}
