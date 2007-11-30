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
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.common.MarkerFile;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore.ExtendedFileStore;

/**
 * @author Tomasz Pylak on Oct 9, 2007
 */
public class FileStoreLocal extends ExtendedFileStore
{
    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, FileStoreLocal.class);

    private final IPathMover mover;

    private final IPathRemover remover;

    public FileStoreLocal(File file, String desription, IFileSysOperationsFactory factory)
    {
        super(file, null, false, desription, factory);
        this.remover = factory.getRemover();
        this.mover = factory.getMover();
    }

    @Override
    public Status delete(StoreItem item)
    {
        return remover.remove(getChildFile(item));
    }

    @Override
    public boolean exists(StoreItem item)
    {
        return getChildFile(item).exists();
    }

    @Override
    public long lastChanged(StoreItem item, long stopWhenFindYounger)
    {
        return FileUtilities.lastChanged(getChildFile(item), true, stopWhenFindYounger);
    }

    @Override
    public String tryCheckDirectoryFullyAccessible()
    {
        return FileUtilities.checkDirectoryFullyAccessible(super.getPath(), super.getDescription());
    }

    @Override
    public IStoreCopier getCopier(FileStore destinationDirectory)
    {
        boolean requiresDeletion = false;
        final IStoreCopier simpleCopier = constructStoreCopier(destinationDirectory, requiresDeletion);
        if (requiresDeletionBeforeCreation(destinationDirectory, simpleCopier))
        {
            requiresDeletion = true;
            return constructStoreCopier(destinationDirectory, requiresDeletion);
        } else
        {
            return simpleCopier;
        }
    }

    @Override
    public ExtendedFileStore tryAsExtended()
    {
        return this;
    }

    @Override
    public boolean createNewFile(StoreItem item)
    {
        try
        {
            File itemFile = getChildFile(item);
            itemFile.createNewFile();
            return itemFile.exists(); // success also when file existed before
        } catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public File tryMoveLocal(StoreItem sourceItem, File destinationDir, String newFilePrefix)
    {
        return mover.tryMove(getChildFile(sourceItem), destinationDir, newFilePrefix);
    }

    @Override
    public String toString()
    {
        String pathStr = super.path.getPath();
        return "[local fs]" + pathStr;
    }

    @Override
    public String getLocationDescription(StoreItem item)
    {
        return getChildFile(item).getPath();
    }

    @Override
    public StoreItem[] tryListSortByLastModified(ISimpleLogger loggerOrNull)
    {
        File[] files = FileUtilities.tryListFiles(path, loggerOrNull);
        if (files != null)
        {
            FileUtilities.sortByLastModified(files);
            return asItems(files);
        } else
        {
            return null;
        }
    }

    private static StoreItem[] asItems(File[] files)
    {
        StoreItem[] items = new StoreItem[files.length];
        for (int i = 0; i < items.length; i++)
        {
            items[i] = new StoreItem(files[i].getName());
        }
        return items;
    }

    // ------

    /**
     * @return <code>true</code> if the <var>simpleCopier</var> on the file system where the
     *         <var>destinationDirectory</var> resides requires deleting an existing file before it can be overwritten.
     */
    protected boolean requiresDeletionBeforeCreation(FileStore destinationDirectory, final IStoreCopier simpleCopier)
    {
        StoreItem item = MarkerFile.createRequiresDeletionBeforeCreationMarker();
        createNewFile(item);
        simpleCopier.copy(item);
        boolean requiresDeletion;
        try
        {
            // If we have e.g. a Cellera NAS server, the next call will raise an IOException.
            requiresDeletion = Status.OK.equals(simpleCopier.copy(item)) == false;
            logCopierOverwriteState(destinationDirectory, requiresDeletion);
        } catch (Exception e)
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

    private static void logFIleSystemNeedsOverwrite(FileStore destinationDirectory)
    {
        if (machineLog.isInfoEnabled())
        {
            machineLog.info(String.format(
                    "The file system on '%s' requires deletion before creation of existing files.",
                    destinationDirectory));
        }
    }

    private static void logCopierOverwriteState(FileStore destinationDirectory, boolean requiresDeletion)
    {
        if (machineLog.isInfoEnabled())
        {
            if (requiresDeletion)
            {
                machineLog.info(String.format(
                        "Copier on directory '%s' requires deletion before creation of existing files.",
                        destinationDirectory));
            } else
            {
                machineLog.info(String.format("Copier on directory '%s' works with overwriting existing files.",
                        destinationDirectory));
            }
        }
    }
}
