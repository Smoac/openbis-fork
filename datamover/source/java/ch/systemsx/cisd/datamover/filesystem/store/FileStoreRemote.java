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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.highwatermark.AlwaysAboveFreeSpaceProvider;
import ch.systemsx.cisd.common.highwatermark.FileWithHighwaterMark;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.highwatermark.RemoteFreeSpaceProvider;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
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
public class FileStoreRemote extends FileStore
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileStoreRemote.class);

    private final HighwaterMarkWatcher highwaterMarkWatcher;

    public FileStoreRemote(final FileWithHighwaterMark fileWithHighwaterMark, final String host,
            final String kind, final IFileSysOperationsFactory factory)
    {
        super(fileWithHighwaterMark, host, true, kind, factory);
        assert host != null : "Unspecified host";
        highwaterMarkWatcher = createHighwaterMarkWatcher(fileWithHighwaterMark, host, factory);
    }

    private final static HighwaterMarkWatcher createHighwaterMarkWatcher(
            final FileWithHighwaterMark fileWithHighwaterMark, final String host,
            final IFileSysOperationsFactory factory)
    {
        final File sshExecutable = factory.tryFindSshExecutable();
        final IFreeSpaceProvider freeSpaceProvider;
        if (sshExecutable != null)
        {
            freeSpaceProvider = new RemoteFreeSpaceProvider(host, sshExecutable);
        } else
        {
            operationLog.warn("Impossible to remotely watch the 'high water mark' "
                    + "('ssh' executable not found).");
            freeSpaceProvider = AlwaysAboveFreeSpaceProvider.INSTANCE;
        }
        final HighwaterMarkWatcher highwaterMarkWatcher =
                new HighwaterMarkWatcher(fileWithHighwaterMark.getHighwaterMark(),
                        freeSpaceProvider);
        highwaterMarkWatcher.setPath(fileWithHighwaterMark.getFile());
        return highwaterMarkWatcher;
    }

    //
    // FileStore
    //

    public final IExtendedFileStore tryAsExtended()
    {
        return null;
    }

    public final Status delete(final StoreItem item)
    {
        throw new NotImplementedException();
    }

    public final boolean exists(final StoreItem item)
    {
        return factory.getCopier(false).existsRemotely(getPath(), tryGetHost());
    }

    public final IStoreCopier getCopier(final FileStore destinationDirectory)
    {
        final boolean requiresDeletion = false;
        return constructStoreCopier(destinationDirectory, requiresDeletion);
    }

    public final long lastChanged(final StoreItem item, final long stopWhenFindYounger)
    {
        throw new NotImplementedException();
    }

    public final long lastChangedRelative(final StoreItem item,
            final long stopWhenFindYoungerRelative)
    {
        throw new NotImplementedException();
    }

    public final String tryCheckDirectoryFullyAccessible(final long timeOutMillis)
    {
        throw new NotImplementedException();
    }

    @Override
    public final String toString()
    {
        final String pathStr = getPath().getPath();
        return "[remote fs]" + tryGetHost() + ":" + pathStr;
    }

    public final String getLocationDescription(final StoreItem item)
    {
        return tryGetHost() + ":" + getChildFile(item).getPath();
    }

    public final StoreItem[] tryListSortByLastModified(final ISimpleLogger loggerOrNull)
    {
        throw new NotImplementedException();
    }

    public final HighwaterMarkWatcher getHighwaterMarkWatcher()
    {
        return highwaterMarkWatcher;
    }

}
