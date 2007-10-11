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
 * @author Tomasz Pylak on Oct 11, 2007
 */
public class FileStoreFactory
{
    /** use when file store is on a local host */
    public static final FileStore createLocal(File path, String kind, IFileSysOperationsFactory factory)
    {
        return new FileStoreLocal(path, kind, factory);
    }

    /** use when file store is on a remote share mounted on local host */
    public static final FileStore createRemoteShare(File path, String kind, IFileSysOperationsFactory factory)
    {
        return new FileStoreRemoteMounted(path, kind, factory);
    }

    /**
     * use when file store is on a remote share mounted on local host
     * 
     * @param factory
     */
    public static final FileStore createRemoteHost(File path, String host, String kind,
            IFileSysOperationsFactory factory)
    {
        return new FileStoreRemote(path, host, kind, factory);
    }
}
