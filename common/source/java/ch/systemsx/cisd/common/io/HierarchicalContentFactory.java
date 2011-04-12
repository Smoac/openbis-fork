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

package ch.systemsx.cisd.common.io;

import java.io.File;

import ch.systemsx.cisd.common.utilities.IDelegatedAction;

/**
 * The default implementation of {@link IHierarchicalContentFactory}.
 * 
 * @author Piotr Buczek
 */
public class HierarchicalContentFactory implements IHierarchicalContentFactory
{
    public IHierarchicalContent asHierarchicalContent(File file, IDelegatedAction onCloseAction)
    {
        return new DefaultFileBasedHierarchicalContent(this, file, onCloseAction);
    }

    public IHierarchicalContentNode asHierarchicalContentNode(IHierarchicalContent rootContent,
            File file)
    {
        // TODO 2011-04-12, Piotr Buczek: uncomment when HDF5 abstraction is fully implemented
        // if (FilenameUtils.isExtension(file.getName(), Arrays.asList("h5", "h5ar")))
        // {
        // return new HDF5ContainerBasedHierarchicalContentNode(this, rootContent, file);
        // }
        return new DefaultFileBasedHierarchicalContentNode(this, rootContent, file);
    }

}
