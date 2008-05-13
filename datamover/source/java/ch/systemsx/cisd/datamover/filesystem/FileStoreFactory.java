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

package ch.systemsx.cisd.datamover.filesystem;

import java.io.File;

import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.store.FileStoreLocal;
import ch.systemsx.cisd.datamover.filesystem.store.FileStoreRemote;
import ch.systemsx.cisd.datamover.filesystem.store.FileStoreRemoteMounted;

/**
 * A {@link FileStore} factory.
 * 
 * @author Tomasz Pylak
 */
// TODO 2008-05-13, Christian Ribeaud: this factory should return IFileStore and not the concrete
// class FileStore.
public final class FileStoreFactory
{
    private FileStoreFactory()
    {
        // This class can not be instantiated.
    }

    /** use when file store is on a local host */
    public static final FileStore createLocal(final File path, final String kind,
            final IFileSysOperationsFactory factory)
    {
        return new FileStoreLocal(path, kind, factory);
    }

    /** use when file store is on a remote share mounted on local host */
    public static final FileStore createRemoteShare(final File path, final String kind,
            final IFileSysOperationsFactory factory)
    {
        return new FileStoreRemoteMounted(path, kind, factory);
    }

    /**
     * use when file store is on a remote share mounted on local host
     * 
     * @param factory
     */
    public static final FileStore createRemoteHost(final File path, final String host,
            final String kind, final IFileSysOperationsFactory factory)
    {
        return new FileStoreRemote(path, host, kind, factory);
    }

    /**
     * Returns the most convenient <code>IFileStore</code> implementation with given <var>values</var>.
     */
    public final static FileStore createStore(final File directory, final String kind,
            final String hostOrNull, final boolean isRemote, final IFileSysOperationsFactory factory)
    {
        if (hostOrNull != null)
        {
            assert isRemote == true;
            return createRemoteHost(directory, hostOrNull, kind, factory);
        } else
        {
            if (isRemote)
            {
                return createRemoteShare(directory, kind, factory);
            } else
            {
                return createLocal(directory, kind, factory);
            }
        }
    }
}
