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
import java.io.IOException;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.highwatermark.FileWithHighwaterMark;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.common.MarkerFile;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;

/**
 * An {@link IFileStore} implementation for local stores.
 * 
 * @author Tomasz Pylak
 */
public class FileStoreLocal extends FileStore implements IExtendedFileStore
{
    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, FileStoreLocal.class);

    private final IPathMover mover;

    private final IPathRemover remover;

    private final HighwaterMarkWatcher highwaterMarkWatcher;

    public FileStoreLocal(final FileWithHighwaterMark highwaterMarkWatcher,
            final String desription, final IFileSysOperationsFactory factory)
    {
        super(highwaterMarkWatcher, null, desription, factory);
        this.remover = factory.getRemover();
        this.mover = factory.getMover();
        this.highwaterMarkWatcher = createHighwaterMarkWatcher(highwaterMarkWatcher);
    }

    private final static HighwaterMarkWatcher createHighwaterMarkWatcher(
            final FileWithHighwaterMark fileWithHighwaterMark)
    {
        final HighwaterMarkWatcher highwaterMarkWatcher =
                new HighwaterMarkWatcher(fileWithHighwaterMark.getHighwaterMark());
        highwaterMarkWatcher.setPath(fileWithHighwaterMark.getFile());
        return highwaterMarkWatcher;
    }

    public final Status delete(final StoreItem item)
    {
        return remover.remove(getChildFile(item));
    }

    public final boolean exists(final StoreItem item)
    {
        return getChildFile(item).exists();
    }

    public final long lastChanged(final StoreItem item, final long stopWhenFindYounger)
    {
        return FileUtilities.lastChanged(getChildFile(item), true, stopWhenFindYounger);
    }

    public final long lastChangedRelative(final StoreItem item,
            final long stopWhenFindYoungerRelative)
    {
        return FileUtilities.lastChangedRelative(getChildFile(item), true,
                stopWhenFindYoungerRelative);
    }

    public final String tryCheckDirectoryFullyAccessible(final long timeOutMillis)
    {
        final boolean available = FileUtilities.isAvailable(getPath(), timeOutMillis);
        if (available == false)
        {
            return String.format(
                    "Path '%s' which is supposed to be a %s directory is not available.",
                    getPath(), getDescription());
        }
        return FileUtilities.checkDirectoryFullyAccessible(getPath(), getDescription());
    }

    public final IStoreCopier getCopier(final IFileStore destinationDirectory)
    {
        boolean requiresDeletion = false;
        final IStoreCopier simpleCopier =
                constructStoreCopier(destinationDirectory, requiresDeletion);
        if (requiresDeletionBeforeCreation(destinationDirectory, simpleCopier))
        {
            requiresDeletion = true;
            return constructStoreCopier(destinationDirectory, requiresDeletion);
        } else
        {
            return simpleCopier;
        }
    }

    public final IExtendedFileStore tryAsExtended()
    {
        return this;
    }

    public final boolean createNewFile(final StoreItem item)
    {
        try
        {
            final File itemFile = getChildFile(item);
            itemFile.createNewFile();
            return itemFile.exists(); // success also when file existed before
        } catch (final IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    public final File tryMoveLocal(final StoreItem sourceItem, final File destinationDir,
            final String newFilePrefix)
    {
        return mover.tryMove(getChildFile(sourceItem), destinationDir, newFilePrefix);
    }

    @Override
    public final String toString()
    {
        final String pathStr = getPath().getPath();
        return "[local fs]" + pathStr;
    }

    public final String getLocationDescription(final StoreItem item)
    {
        return getChildFile(item).getPath();
    }

    public final StoreItem[] tryListSortByLastModified(final ISimpleLogger loggerOrNull)
    {
        final File[] files = FileUtilities.tryListFiles(getPath(), loggerOrNull);
        if (files != null)
        {
            FileUtilities.sortByLastModified(files);
            return StoreItem.asItems(files);
        } else
        {
            return null;
        }
    }

    public final HighwaterMarkWatcher getHighwaterMarkWatcher()
    {
        return highwaterMarkWatcher;
    }

    // ------

    /**
     * @return <code>true</code> if the <var>simpleCopier</var> on the file system where the
     *         <var>destinationDirectory</var> resides requires deleting an existing file before it
     *         can be overwritten.
     */
    protected final boolean requiresDeletionBeforeCreation(final IFileStore destinationDirectory,
            final IStoreCopier simpleCopier)
    {
        final StoreItem item = MarkerFile.createRequiresDeletionBeforeCreationMarker();
        createNewFile(item);
        simpleCopier.copy(item);
        boolean requiresDeletion;
        try
        {
            // If we have e.g. a Cellera NAS server, the next call will raise an IOException.
            requiresDeletion = Status.OK.equals(simpleCopier.copy(item)) == false;
            logCopierOverwriteState(destinationDirectory, requiresDeletion);
        } catch (final Exception e)
        {
            logFIleSystemNeedsOverwrite(destinationDirectory);
            requiresDeletion = true;
        } finally
        {
            // We don't check for success because there is nothing we can do if we fail.
            delete(item);
            destinationDirectory.delete(item);
        }
        return requiresDeletion;
    }

    private final static void logFIleSystemNeedsOverwrite(final IFileStore destinationDirectory)
    {
        if (machineLog.isInfoEnabled())
        {
            machineLog.info(String.format(
                    "The file system on '%s' requires deletion before creation of existing files.",
                    destinationDirectory));
        }
    }

    private final static void logCopierOverwriteState(final IFileStore destinationDirectory,
            final boolean requiresDeletion)
    {
        if (machineLog.isInfoEnabled())
        {
            if (requiresDeletion)
            {
                machineLog
                        .info(String
                                .format(
                                        "Copier on directory '%s' requires deletion before creation of existing files.",
                                        destinationDirectory));
            } else
            {
                machineLog.info(String.format(
                        "Copier on directory '%s' works with overwriting existing files.",
                        destinationDirectory));
            }
        }
    }

}
