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
import java.io.FileFilter;
import java.io.IOException;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.datamover.filesystem.impl.RecursiveHardLinkMaker;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathCopier;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathImmutableCopier;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;
import ch.systemsx.cisd.datamover.filesystem.intf.IReadPathOperations;
import ch.systemsx.cisd.datamover.filesystem.remote.XcopyCopier;
import ch.systemsx.cisd.datamover.filesystem.remote.rsync.RsyncCopier;
import ch.systemsx.cisd.datamover.intf.IFileSysParameters;

/**
 * @author Tomasz Pylak on Sep 7, 2007
 */
public class FileSysOperationsFactory implements IFileSysOperationsFactory
{
    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, FileSysOperationsFactory.class);

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, FileSysOperationsFactory.class);

    final private IFileSysParameters parameters;

    public FileSysOperationsFactory(IFileSysParameters parameters)
    {
        assert parameters != null;
        
        this.parameters = parameters;
    }

    public IPathRemover getRemover()
    {
        return new IPathRemover()
            {
                private final Status STATUS_FAILED_DELETION =
                        new Status(StatusFlag.FATAL_ERROR, "Failed to remove path.");

                public Status remove(File path)
                {
                    final boolean deletionOK = FileUtilities.deleteRecursively(path);
                    return deletionOK ? Status.OK : STATUS_FAILED_DELETION;
                }
            };
    }

    public IReadPathOperations getReadPathOperations()
    {
        return new IReadPathOperations()
        {

            public boolean exists(File file)
            {
                return file.exists();
            }

            public long lastChanged(File path)
            {
                return FileUtilities.lastChanged(path);
            }

            public File[] tryListFiles(File directory, FileFilter filter, ISimpleLogger loggerOrNull)
            {
                return FileUtilities.tryListFiles(directory, filter, loggerOrNull);
            }

            public File[] tryListFiles(File directory, ISimpleLogger loggerOrNull)
            {
                return FileUtilities.tryListFiles(directory, FileUtilities.ACCEPT_ALL_FILTER, loggerOrNull);
            }
        };
    }

    public IPathImmutableCopier getImmutableCopier()
    {
        String lnExec = parameters.getHardLinkExecutable();
        if (lnExec != null)
        {
            return RecursiveHardLinkMaker.create(lnExec);
        }

        IPathImmutableCopier copier = null;
        if (OSUtilities.isWindows() == false)
        {
            copier = RecursiveHardLinkMaker.tryCreate();
            if (copier != null)
            {
                return copier;
            }
        }
        return createFakedImmCopier();
    }

    private IPathImmutableCopier createFakedImmCopier()
    {
        final IPathCopier normalCopier = suggestPathCopier(false);
        return new IPathImmutableCopier()
            {
                public File tryCopy(File file, File destinationDirectory)
                {
                    Status status = normalCopier.copy(file, destinationDirectory);
                    if (StatusFlag.OK.equals(status.getFlag()))
                    {
                        return new File(destinationDirectory, file.getName());
                    } else
                    {
                        notificationLog.error(String.format("Copy of '%s' to '%s' failed: %s.", file.getPath(),
                                destinationDirectory.getPath(), status));
                        return null;
                    }
                }
            };
    }

    private IPathCopier suggestPathCopier(boolean requiresDeletionBeforeCreation)
    {
        final File rsyncExecutable = findRsyncExecutable(parameters.getRsyncExecutable());
        final File sshExecutable = findSshExecutable(parameters.getSshExecutable());
        if (rsyncExecutable != null)
        {
            return new RsyncCopier(rsyncExecutable, sshExecutable, requiresDeletionBeforeCreation);
        } else if (OSUtilities.isWindows())
        {
            return new XcopyCopier(OSUtilities.findExecutable("xcopy"), requiresDeletionBeforeCreation);
        } else
        {
            throw new ConfigurationFailureException("Unable to find a copy engine.");
        }
    }

    private static File findRsyncExecutable(final String rsyncExecutablePath)
    {
        final File rsyncExecutable;
        if (rsyncExecutablePath != null)
        {
            rsyncExecutable = new File(rsyncExecutablePath);
        } else if (OSUtilities.isWindows() == false)
        {
            rsyncExecutable = OSUtilities.findExecutable("rsync");
        } else
        {
            rsyncExecutable = null;
        }
        if (rsyncExecutable != null && OSUtilities.executableExists(rsyncExecutable) == false)
        {
            throw ConfigurationFailureException.fromTemplate("Cannot find rsync executable '%s'.", rsyncExecutable
                    .getAbsoluteFile());
        }
        return rsyncExecutable;
    }

    private static File findSshExecutable(String sshExecutablePath)
    {
        final File sshExecutable;
        if (sshExecutablePath != null)
        {
            if (sshExecutablePath.length() > 0)
            {
                sshExecutable = new File(sshExecutablePath);
            } else
            // Explicitly disable tunneling via ssh on the command line.
            {
                sshExecutable = null;
            }
        } else
        {
            sshExecutable = OSUtilities.findExecutable("ssh");
        }
        if (sshExecutable != null && OSUtilities.executableExists(sshExecutable) == false)
        {
            throw ConfigurationFailureException.fromTemplate("Cannot find ssh executable '%s'.", sshExecutable
                    .getAbsoluteFile());
        }
        return sshExecutable;
    }

    /**
     * @return <code>true</code> if the <var>copyProcess</var> on the file system where the <var>destinationDirectory</var>
     *         resides requires deleting an existing file before it can be overwritten.
     */
    private static boolean requiresDeletionBeforeCreation(IPathCopier copyProcess, File destinationDirectory)
    {
        assert copyProcess != null;
        assert destinationDirectory != null;
        assert destinationDirectory.isDirectory();

        String fileName = ".requiresDeletionBeforeCreation";
        final File destinationFile = new File(destinationDirectory, fileName);
        final File tmpSourceDir = new File(destinationDirectory, ".DataMover-OverrideTest");
        final File sourceFile = new File(tmpSourceDir, fileName);
        try
        {
            tmpSourceDir.mkdir();
            sourceFile.createNewFile();
            destinationFile.createNewFile();
            // If we have e.g. a Cellera NAS server, the next call will raise an IOException.
            final boolean OK = Status.OK.equals(copyProcess.copy(sourceFile, destinationDirectory));
            if (machineLog.isInfoEnabled())
            {
                if (OK)
                {
                    machineLog.info(String.format("Copier %s on directory '%s' works with overwriting existing files.",
                            copyProcess.getClass().getSimpleName(), destinationDirectory.getAbsolutePath()));
                } else
                {
                    machineLog.info(String.format(
                            "Copier %s on directory '%s' requires deletion before creation of existing files.",
                            copyProcess.getClass().getSimpleName(), destinationDirectory.getAbsolutePath()));
                }
            }
            return (OK == false);
        } catch (IOException e)
        {
            if (machineLog.isInfoEnabled())
            {
                machineLog.info(String.format(
                        "The file system on '%s' requires deletion before creation of existing files.",
                        destinationDirectory.getAbsolutePath()));
            }
            return true;
        } finally
        {
            // We don't check for success because there is nothing we can do if we fail.
            sourceFile.delete();
            tmpSourceDir.delete();
            destinationFile.delete();
        }
    }

    public IPathCopier getCopierNoDeletionRequired()
    {
        return suggestPathCopier(false);
    }

    public IPathCopier getCopier(File destinationDirectory)
    {
        IPathCopier copyProcess = suggestPathCopier(false);
        if (requiresDeletionBeforeCreation(copyProcess, destinationDirectory))
        {
            return suggestPathCopier(true);
        } else
        {
            return copyProcess;
        }
    }
}
