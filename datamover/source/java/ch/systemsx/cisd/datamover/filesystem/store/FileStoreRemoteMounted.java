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
public class FileStoreRemoteMounted extends FileStore
{
    private final FileStoreLocal localImpl;

    public FileStoreRemoteMounted(File file, String desription, IFileSysOperationsFactory factory)
    {
        super(file, null, true, desription, factory);
        this.localImpl = new FileStoreLocal(file, desription, factory);
    }

    @Override
    public IExtendedFileStore tryAsExtended()
    {
        return null;
    }

    @Override
    public IStoreCopier getCopier(FileStore destinationDirectory)
    {
        boolean requiresDeletion = false;
        return constructStoreCopier(destinationDirectory, requiresDeletion);
    }

    @Override
    public String toString()
    {
        String pathStr = path.getPath();
        return "[mounted remote fs]" + pathStr;
    }

    @Override
    public String getLocationDescription(StoreItem item)
    {
        return localImpl.getLocationDescription(item);
    }

    @Override
    public Status delete(StoreItem item)
    {
        return localImpl.delete(item);
    }

    @Override
    public boolean exists(StoreItem item)
    {
        return localImpl.exists(item);
    }

    @Override
    public long lastChanged(StoreItem item, long stopWhenFindYounger)
    {
        return localImpl.lastChanged(item, stopWhenFindYounger);
    }

    @Override
    public long lastChangedRelative(StoreItem item, long stopWhenFindYoungerRelative)
    {
        return localImpl.lastChangedRelative(item, stopWhenFindYoungerRelative);
    }

    @Override
    public String tryCheckDirectoryFullyAccessible(final long timeOutMillis)
    {
        return localImpl.tryCheckDirectoryFullyAccessible(timeOutMillis);
    }

    @Override
    public StoreItem[] tryListSortByLastModified(ISimpleLogger loggerOrNull)
    {
        return localImpl.tryListSortByLastModified(loggerOrNull);
    }
}
