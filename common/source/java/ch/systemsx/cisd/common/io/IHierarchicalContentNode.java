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
import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * Read only abstraction over a node in {@link IHierarchicalContent} that provides access to a file
 * and its content.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public interface IHierarchicalContentNode
{
    /** Returns name of this node/file. */
    String getName();

    /** Returns relative path of this node or empty string for root node. */
    String getRelativePath();

    /** Returns relative path of this node's parent or <code>null</code> for root node. */
    String getParentRelativePath();

    /**
     * Returns <code>true</code> if the content node exists.
     */
    boolean exists();

    /**
     * Returns <code>true</code> if this node is an abstraction of a directory, <code>false</code>
     * otherwise.
     */
    boolean isDirectory();
    
    /**
     * Returns the time this node or the persistent object containing this node has been modified.
     * 
     * @return A long value representing the time of last modification, measured in milliseconds
     *         since the epoch (00:00:00 GMT, January 1, 1970).
     */
    long getLastModified();

    /**
     * List of child nodes of this node.
     * <p>
     * NOTE: Call {@link #isDirectory()} first to make sure this node is a directory.
     * 
     * @throws UnsupportedOperationException if the node is not an abstraction of a directory.
     */
    List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException;

    /**
     * Returns a file abstracted by this node.
     * 
     * @throws UnsupportedOperationException if the backing store is not a normal file/directory.
     */
    File getFile() throws UnsupportedOperationException;

    /**
     * Returns the length (in bytes) of a file abstracted by this node.
     * <p>
     * NOTE: Call {@link #isDirectory()} first to make sure this node is NOT a directory.
     * 
     * @throws UnsupportedOperationException if the node is an abstraction of a directory.
     */
    long getFileLength() throws UnsupportedOperationException;

    /**
     * Returns a read only {@link IRandomAccessFile} with file content of the node. *
     * <p>
     * NOTE: Call {@link #isDirectory()} first to make sure this node is NOT a directory.
     * 
     * @throws UnsupportedOperationException if the node is an abstraction of a directory.
     * @throws IOExceptionUnchecked if an I/O error occurs.
     */
    IRandomAccessFile getFileContent() throws UnsupportedOperationException, IOExceptionUnchecked;

    /**
     * Returns an {@link InputStream} with content of the node.
     * <p>
     * NOTE: Call {@link #isDirectory()} first to make sure this node is NOT a directory.
     * 
     * @throws UnsupportedOperationException if the node is an abstraction of a directory.
     * @throws IOExceptionUnchecked if an I/O error occurs.
     */
    InputStream getInputStream() throws UnsupportedOperationException, IOExceptionUnchecked;

}
