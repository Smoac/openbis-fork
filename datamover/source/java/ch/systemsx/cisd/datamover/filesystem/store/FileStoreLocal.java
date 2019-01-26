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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusWithResult;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.LastModificationChecker;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.filesystem.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.filesystem.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.datamover.common.MarkerFile;
import ch.systemsx.cisd.datamover.filesystem.intf.AbstractFileStore;
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
public class FileStoreLocal extends AbstractFileStore implements IExtendedFileStore
{
    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            FileStoreLocal.class);

    private final IPathMover mover;

    private final IPathRemover remover;

    private final HighwaterMarkWatcher highwaterMarkWatcher;

    private final LastModificationChecker lastModificationChecker;

    public FileStoreLocal(final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark,
            final String description, final IFileSysOperationsFactory factory,
            final boolean skipAccessibilityTest, final long remoteConnectionTimeoutMillis)
    {
        super(hostAwareFileWithHighwaterMark, description, factory, skipAccessibilityTest,
                remoteConnectionTimeoutMillis);
        this.remover = factory.getRemover();
        this.mover = factory.getMover();
        this.highwaterMarkWatcher = createHighwaterMarkWatcher(hostAwareFileWithHighwaterMark);
        this.lastModificationChecker = new LastModificationChecker(getLocalFile());
    }

    private final static HighwaterMarkWatcher createHighwaterMarkWatcher(
            final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark)
    {
        final HighwaterMarkWatcher highwaterMarkWatcher =
                new HighwaterMarkWatcher(hostAwareFileWithHighwaterMark.getHighwaterMark());
        highwaterMarkWatcher.setPath(hostAwareFileWithHighwaterMark);
        return highwaterMarkWatcher;
    }

    @Override
    public final Status delete(final StoreItem item)
    {
        return remover.remove(getChildFile(item));
    }

    @Override
    public final BooleanStatus exists(final StoreItem item)
    {
        boolean exists = getChildFile(item).exists();
        return BooleanStatus.createFromBoolean(exists);
    }

    @Override
    public final StatusWithResult<Long> lastChanged(final StoreItem item,
            final long stopWhenFindYounger)
    {
        return lastModificationChecker.lastChanged(item, stopWhenFindYounger);
    }

    @Override
    public final StatusWithResult<Long> lastChangedRelative(final StoreItem item,
            final long stopWhenFindYoungerRelative)
    {
        return lastModificationChecker.lastChangedRelative(item, stopWhenFindYoungerRelative);
    }

    @Override
    public final BooleanStatus checkDirectoryFullyAccessible(final long timeOutMillis)
    {
        if (skipAccessibilityTest)
        {
            return BooleanStatus.createTrue();
        }
        final boolean available =
                FileUtils.waitFor(getLocalFile(),
                        (int) (timeOutMillis / DateUtils.MILLIS_PER_SECOND));
        String unaccesibleMsg;
        if (available == false)
        {
            unaccesibleMsg =
                    String.format(
                            "Path '%s' which is supposed to be a %s directory is not available.",
                            getPath(), getDescription());
            return BooleanStatus.createFalse(unaccesibleMsg);
        } else
        {
            unaccesibleMsg =
                    FileUtilities.checkDirectoryFullyAccessible(getLocalFile(), getDescription());
        }
        if (unaccesibleMsg != null)
        {
            return BooleanStatus.createFalse(unaccesibleMsg);
        } else
        {
            return BooleanStatus.createTrue();
        }
    }

    @Override
    public boolean isRemote()
    {
        return false;
    }

    @Override
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

    @Override
    public final IExtendedFileStore tryAsExtended()
    {
        return this;
    }

    @Override
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

    @Override
    public final File tryMoveLocal(final StoreItem sourceItem, final File destinationDir,
            final String newFilePrefix)
    {
        return mover.tryMove(getChildFile(sourceItem), destinationDir, newFilePrefix);
    }

    @Override
    public final String getLocationDescription(final StoreItem item)
    {
        return getChildFile(item).getPath();
    }

    @Override
    public StoreItem asStoreItem(String locationDescription)
    {
        return new StoreItem(FilenameUtils.getName(locationDescription));
    }

    @Override
    public final StoreItem[] tryListSortByLastModified(final ISimpleLogger loggerOrNull)
    {
        final File[] files = FileUtilities.tryListFiles(getLocalFile(), loggerOrNull);
        if (files != null)
        {
            FileUtilities.sortByLastModified(files);
            return StoreItem.asItems(files);
        } else
        {
            return null;
        }
    }

    @Override
    public final HighwaterMarkWatcher getHighwaterMarkWatcher()
    {
        return highwaterMarkWatcher;
    }

    // ------

    /**
     * @return <code>true</code> if the <var>simpleCopier</var> on the file system where the <var>destinationStore</var> resides requires deleting an
     *         existing file before it can be overwritten.
     */
    protected final boolean requiresDeletionBeforeCreation(final IFileStore destinationStore,
            final IStoreCopier copier)
    {
        if (skipAccessibilityTest && destinationStore.isRemote())
        {
            return true; // This is the safe default.
        }
        try
        {
            copier.check();
        } catch (ConfigurationFailureException ex)
        {
            machineLog.warn("Cannot determine whether copying to '" + destinationStore
                    + "' requires deletion before copying, assuming 'true'", ex);
            return true; // This is the safe default.
        }
        final StoreItem item = MarkerFile.createRequiresDeletionBeforeCreationMarker();
        createNewFile(item);
        copier.copy(item, null);
        boolean requiresDeletion;
        // A CIFS mount from a Cellera NAS server is an example that gives 'true' here.
        requiresDeletion = Status.OK.equals(copier.copy(item, null)) == false;
        logCopierOverwriteState(destinationStore, requiresDeletion);

        // We don't check for success because there is nothing we can do if we fail.
        delete(item);
        destinationStore.delete(item);

        return requiresDeletion;
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
                                .format("Copier on directory '%s' requires deletion before creation of existing files.",
                                        destinationDirectory));
            } else
            {
                machineLog.info(String.format(
                        "Copier on directory '%s' works with overwriting existing files.",
                        destinationDirectory));
            }
        }
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        final String pathStr = getLocalFile().getAbsolutePath();
        return "[local fs] " + pathStr;
    }

}
