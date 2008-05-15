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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.IPathImmutableCopier;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.common.utilities.RecursiveHardLinkMaker;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathCopier;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;
import ch.systemsx.cisd.datamover.filesystem.remote.rsync.RsyncCopier;
import ch.systemsx.cisd.datamover.intf.IFileSysParameters;

/**
 * @author Tomasz Pylak
 */
public class FileSysOperationsFactory implements IFileSysOperationsFactory
{
    /** The maximal number of retries when the move operation fails. */
    private static final int MAX_RETRIES_ON_FAILURE = 12;

    /** The time to sleep when the move operation has failed before retrying it. */
    private static final long MILLIS_TO_SLEEP_ON_FAILURE = 5000;

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, FileSysOperationsFactory.class);

    private final IFileSysParameters parameters;

    public FileSysOperationsFactory(final IFileSysParameters parameters)
    {
        assert parameters != null;

        this.parameters = parameters;
    }

    private final static File findRsyncExecutable(final String rsyncExecutablePath)
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
            throw ConfigurationFailureException.fromTemplate("Cannot find rsync executable '%s'.",
                    rsyncExecutable.getAbsoluteFile());
        }
        return rsyncExecutable;
    }

    private final IPathImmutableCopier createFakedImmCopier()
    {
        final IPathCopier normalCopier = getCopier(false);
        return new IPathImmutableCopier()
            {
                //
                // IPathImmutableCopier
                //

                public final File tryCopy(final File file, final File destinationDirectory,
                        final String nameOrNull)
                {
                    final Status status = normalCopier.copy(file, destinationDirectory);
                    if (StatusFlag.OK.equals(status.getFlag()))
                    {
                        return new File(destinationDirectory, file.getName());
                    } else
                    {
                        notificationLog.error(String.format("Copy of '%s' to '%s' failed: %s.",
                                file.getPath(), destinationDirectory.getPath(), status));
                        return null;
                    }
                }
            };
    }

    //
    // IFileSysOperationsFactory
    //

    public final IPathRemover getRemover()
    {
        return new RetryingPathRemover(MAX_RETRIES_ON_FAILURE, MILLIS_TO_SLEEP_ON_FAILURE);
    }

    public final IPathImmutableCopier getImmutableCopier()
    {
        final String lnExec = parameters.getHardLinkExecutable();
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

    public final IPathCopier getCopier(final boolean requiresDeletionBeforeCreation)
    {
        final File rsyncExecutable = findRsyncExecutable(parameters.getRsyncExecutable());
        final File sshExecutable = tryFindSshExecutable();
        if (rsyncExecutable != null)
        {
            return new RsyncCopier(rsyncExecutable, sshExecutable, requiresDeletionBeforeCreation,
                    parameters.isRsyncOverwrite());
        } else
        {
            throw new ConfigurationFailureException("Unable to find a copy engine.");
        }
    }

    public final File tryFindSshExecutable()
    {
        final String sshExecutablePath = parameters.getSshExecutable();
        File sshExecutable = null;
        if (StringUtils.isNotBlank(sshExecutablePath))
        {
            sshExecutable = new File(sshExecutablePath);
        } else
        {
            sshExecutable = OSUtilities.findExecutable("ssh");
        }
        if (sshExecutable != null && OSUtilities.executableExists(sshExecutable) == false)
        {
            throw ConfigurationFailureException.fromTemplate("Cannot find ssh executable '%s'.",
                    sshExecutable.getAbsoluteFile());
        }
        return sshExecutable;
    }

    public final IPathMover getMover()
    {
        return new RetryingPathMover(MAX_RETRIES_ON_FAILURE, MILLIS_TO_SLEEP_ON_FAILURE);
    }
}
