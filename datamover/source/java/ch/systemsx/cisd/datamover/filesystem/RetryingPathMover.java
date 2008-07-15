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
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.CallableExecutor;
import ch.systemsx.cisd.common.process.FileRenamingCallable;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;

/**
 * Basic file system operations helper.
 * 
 * @author Tomasz Pylak
 */
class RetryingPathMover implements IPathMover
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RetryingPathMover.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, RetryingPathMover.class);

    private final int maxRetriesOnFailure;

    private final long millisToSleepOnFailure;

    RetryingPathMover(final int maxRetriesOnFailure, final long millisToSleepOnFailure)
    {
        this.maxRetriesOnFailure = maxRetriesOnFailure;
        this.millisToSleepOnFailure = millisToSleepOnFailure;
    }

    public File tryMove(final File sourceFile, final File destinationDir)
    {
        return tryMove(sourceFile, destinationDir, "");
    }

    public File tryMove(final File sourcePath, final File destinationDirectory,
            final String prefixTemplate)
    {
        assert destinationDirectory != null;
        assert prefixTemplate != null;
        assert sourcePath != null;

        if (checkDirectoryAccessible(destinationDirectory) == false)
        {
            return null;
        }
        final String destinationPath =
                createDestinationPath(sourcePath, null, destinationDirectory, prefixTemplate);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Moving path '%s' to '%s'", sourcePath.getPath(),
                    destinationPath));
        }
        final File destFile = new File(destinationPath);
        final Boolean renamed =
                new CallableExecutor(maxRetriesOnFailure, millisToSleepOnFailure)
                        .executeCallable(new FileRenamingCallable(sourcePath, destFile));
        if (renamed == null || renamed == false)
        {
            notificationLog.error(String.format(
                    "Moving path '%s' to directory '%s' failed, giving up.", sourcePath,
                    destinationDirectory));
            return null;
        } else
        {
            return destFile;
        }
    }

    private boolean checkDirectoryAccessible(final File destinationDirectory)
    {
        String errorMessage =
                FileUtilities.checkDirectoryFullyAccessible(destinationDirectory, "destination");
        if (errorMessage != null)
        {
            operationLog.error("Unaccessible directory: " + errorMessage);
            return false;
        } else
        {
            return true;
        }
    }

    /**
     * Creates a destination path for copying <var>sourcePath</var> to <var>destinationDirectory</var>
     * with prefix defined by <var>prefixTemplate</var>. Note that '%t' in <var>prefixTemplate</var>
     * will be replaced by the current time stamp in format YYYYmmddhhMMss.
     */
    private static String createDestinationPath(final File sourcePath,
            final String destinationHostOrNull, final File destinationDirectory,
            final String prefixTemplate)
    {
        assert sourcePath != null;
        assert destinationDirectory != null;
        assert prefixTemplate != null;

        if (destinationHostOrNull != null)
        {
            return destinationHostOrNull + ":" + destinationDirectory.getPath() + File.separator
                    + createPrefix(prefixTemplate) + sourcePath.getName();

        } else
        {
            return destinationDirectory.getAbsolutePath() + File.separator
                    + createPrefix(prefixTemplate) + sourcePath.getName();
        }
    }

    private static String createPrefix(final String prefixTemplate)
    {
        return StringUtils.replace(prefixTemplate, "%t", DateFormatUtils.format(System
                .currentTimeMillis(), "yyyyMMddHHmmss"));
    }
}