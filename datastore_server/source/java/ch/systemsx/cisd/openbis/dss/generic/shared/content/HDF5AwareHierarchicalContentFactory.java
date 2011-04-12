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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.io.HierarchicalContentFactory;
import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;

/**
 * The implementation of {@link IHierarchicalContentFactory} that is aware of HDF5 containers.
 * 
 * @author Piotr Buczek
 */
public class HDF5AwareHierarchicalContentFactory extends HierarchicalContentFactory
{

    @Override
    public IHierarchicalContentNode asHierarchicalContentNode(IHierarchicalContent rootContent,
            File file)
    {
        if (FilenameUtils.isExtension(file.getName(), Arrays.asList("h5", "h5r")))
        {
            return new HDF5ContainerBasedHierarchicalContentNode(this, rootContent, file);
        } else
        {
            return super.asHierarchicalContentNode(rootContent, file);
        }
    }
}
