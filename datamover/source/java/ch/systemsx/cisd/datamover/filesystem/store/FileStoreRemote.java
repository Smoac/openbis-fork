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
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;

/**
 * @author Tomasz Pylak
 */
// TODO 2007-10-09, Tomasz Pylak: Ssh tunneling mode should be implemented here. This class is a
// dummy implementation.
// Methods with NOTE should be written.
public class FileStoreRemote extends FileStore
{
    public FileStoreRemote(File path, String host, String kind, IFileSysOperationsFactory factory)
    {
        super(path, host, true, kind, factory);
    }

    @Override
    public IExtendedFileStore tryAsExtended()
    {
        return null;
    }

    @Override
    public Status delete(StoreItem item)
    {
        // NOTE: implement this
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
    public long lastChanged(StoreItem item, long stopWhenFindYounger)
    {
        // NOTE: implement this
        return 0;
    }

    @Override
    public long lastChangedRelative(StoreItem item, long stopWhenFindYoungerRelative)
    {
        // NOTE: implement this
        return 0;
    }

    @Override
    public String tryCheckDirectoryFullyAccessible(final long timeOutMillis)
    {
        // NOTE: implement this
        return null;
    }

    @Override
    public String toString()
    {
        String pathStr = path.getPath();
        return "[remote fs]" + hostOrNull + ":" + pathStr;
    }

    @Override
    public String getLocationDescription(StoreItem item)
    {
        return hostOrNull + ":" + getChildFile(item).getPath();
    }

    @Override
    public StoreItem[] tryListSortByLastModified(ISimpleLogger loggerOrNull)
    {
        // NOTE: implement this
        return null;
    }
}
