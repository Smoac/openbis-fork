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

package ch.systemsx.cisd.bds.storage.filesystem;

import java.io.File;

import ch.systemsx.cisd.bds.exception.StorageException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;

/**
 * Implementation of {@link IStorage} based on the file system.
 * 
 * @author Franz-Josef Elmer
 */
public class FileStorage implements IStorage
{
    private final IDirectory root;

    private boolean mounted;

    /**
     * Creates an instance with the specified folder as the root directory.
     * 
     * @throws StorageException if <code>folder</code> does not exist or is not a directory in the file system.
     */
    public FileStorage(final File folder)
    {
        root = NodeFactory.createDirectoryNode(folder);
    }

    //
    // IStorage
    //

    @Override
    public final boolean isMounted()
    {
        return mounted;
    }

    @Override
    public final IDirectory getRoot()
    {
        if (isMounted() == false)
        {
            throw new StorageException("Can not get root of an unmounted storage.");
        }
        return root;
    }

    @Override
    public final void mount()
    {
        mounted = true;
    }

    @Override
    public final void unmount()
    {
        mounted = false;
    }

}
