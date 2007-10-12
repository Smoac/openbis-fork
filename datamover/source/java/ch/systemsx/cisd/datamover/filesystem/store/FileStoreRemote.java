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

package ch.systemsx.cisd.datamover.filesystem.store;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.datamover.common.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;

/**
 * @author Tomasz Pylak on Oct 9, 2007
 */
public class FileStoreRemote extends FileStore
{
    public FileStoreRemote(File path, String host, String kind, IFileSysOperationsFactory factory)
    {
        super(path, host, true, kind, factory);
    }

    @Override
    public ExtendedFileStore tryAsExtended()
    {
        return null;
    }

    @Override
    public Status delete(StoreItem item)
    {
        // TODO 2007-10-09, Tomasz Pylak: implement ssh tunneling mode
        return Status.OK;
    }

    @Override
    public boolean exists(StoreItem item)
    {
        return factory.getCopier(false).existsRemotely(path, hostOrNull);
    }

    @Override
    public IStoreCopier getCopier(FileStore destinationDirectory)
    {
        boolean requiresDeletion = false;
        return constructStoreCopier(destinationDirectory, requiresDeletion);
    }

    @Override
    public long lastChanged(StoreItem item)
    {
        // TODO 2007-10-09, Tomasz Pylak: implement ssh tunneling mode
        return 0;
    }

    @Override
    public String tryCheckDirectoryFullyAccessible()
    {
        // TODO 2007-10-09, Tomasz Pylak: implement ssh tunneling mode. E.g. check if directory exists
        return null;
    }

    @Override
    public String toString()
    {
        String pathStr = path.getPath();
        return "[remote fs]" + hostOrNull + ":" + pathStr;
    }
}
