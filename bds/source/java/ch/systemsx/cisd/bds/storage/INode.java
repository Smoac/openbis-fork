/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.storage;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Abstraction of a node in a hierarchical data structure.
 * 
 * @author Franz-Josef Elmer
 */
public interface INode
{
    /**
     * Returns the name of this node.
     */
    public String getName();

    /**
     * Returns the parent directory of this node or <code>null</code> if it is the root node.
     */
    public IDirectory tryToGetParent();

    /**
     * Extracts this node to the specified directory of the file system.
     * <p>
     * All descendants are also extracted. This is a copy operation.
     * </p>
     * 
     * @throws UserFailureException if this or a descended node is a link referring to a node which is not this node or
     *             a descended node.
     * @throws EnvironmentFailureException if extraction causes an IOException.
     */
    public void extractTo(final File directory) throws UserFailureException, EnvironmentFailureException;
}
