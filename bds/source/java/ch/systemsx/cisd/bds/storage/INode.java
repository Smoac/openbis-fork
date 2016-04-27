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
     * Returns the path of this node.
     */
    public String getPath();

    /**
     * Returns the parent directory of this node or <code>null</code> if it is the root node.
     */
    public IDirectory tryGetParent();

    /**
     * Returns this node as a directory, or <code>null</code>, if this node is no directory.
     */
    public IDirectory tryAsDirectory();

    /**
     * Returns this node as a file, or <code>null</code>, if this node is no file.
     */
    public IFile tryAsFile();

    /**
     * Whether this <code>INode</code> is valid. As a minimum, a node needs to exist and be readable in order to be valid. Sub-classes can define
     * additional requirements of validity.
     */
    public boolean isValid();

    /**
     * Extracts this node to the specified directory of the file system.
     * <p>
     * All descendants are also extracted. This is a copy operation.
     * </p>
     */
    public void extractTo(final java.io.File directory);

    /**
     * Moves this node and all descendants to the specified directory of the file system. This node will be automatically removed from its parent.
     */
    public void moveTo(final java.io.File directory);
}
