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
    String getName();

    List<IHierarchicalContentNode> getChildNodes();

    /** @throws UnsupportedOperationException if the backing store is not a file. */
    File getFile() throws UnsupportedOperationException;

    /**
     * Returns a read only {@link IRandomAccessFile} with file content of the node.
     * 
     * @throws IOExceptionUnchecked if an I/O error occurs.
     */
    IRandomAccessFile getFileContent() throws IOExceptionUnchecked;

    /** @throws IOExceptionUnchecked if an I/O error occurs. */
    InputStream getInputStream() throws IOExceptionUnchecked;

}
