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

/**
 * Node representing a directory.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDirectory extends INode, Iterable<INode>
{
    /**
     * Returns the child node with specified name.
     * 
     * @return <code>null</code> if there is no child node named <code>name</code>.
     */
    public INode tryGetNode(final String name);

    /**
     * Makes a new subdirectory in this directory. Does nothing if the subdirectory already exists.
     * 
     * @param name Name of the new subdirectory.
     * @return the new subdirectory.
     */
    public IDirectory makeDirectory(final String name);

    /**
     * Adds the specified real file to this directory. The content of <code>file</code> will be copied. If it is a
     * folder also its complete content including all subfolders will be copied.
     * 
     * @param nameOrNull the name of the returned node. If <code>null</code>, then given <var>file</var> name is taken.
     * @param move whether given <var>file</var> should be copied or moved.
     * @return the new node. It will be a {@link ILink} if <code>file</code> is a symbolic link, a {@link IDirectory}
     *         if <code>file</code> is a folder, or {@link IFile} if <code>file</code> is a plain file.
     */
    // TODO 2007-12-03, Tomasz Pylak review: this generic interface should not use java.io.File. Is the 'move' parameter
    // possible to implement in HDF5? Maybe those operations should be done before, depending on the implementation
    // which is used?
    public INode addFile(final File file, final String nameOrNull, final boolean move);

    /**
     * Removes given <var>node</var> from this directory.
     */
    public void removeNode(final INode node);

    /**
     * Adds a plain file named <code>key</code> with content <code>value</code> to this directory.
     */
    public IFile addKeyValuePair(final String key, final String value);

    /**
     * Adds the link named <code>name</code> to this directory which refers to the specified node.
     * 
     * @return <code>null</code> if the operation did not succeed.
     */
    public ILink tryAddLink(final String name, final INode node);
}
