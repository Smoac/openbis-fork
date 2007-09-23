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
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Basic file system operations helper.
 * 
 * @author Tomasz Pylak on Aug 27, 2007
 */
public class LocalFileSystem
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, LocalFileSystem.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, LocalFileSystem.class);

    // TODO 2007-09-11, Bernd Rinn: make this configurable

    private static final int MAX_RETRIES_ON_FAILURE = 12;

    private static final long MILLIS_TO_SLEEP_ON_FAILURE = 5000;

    /**
     * Moves source file to destination directory.
     */
    public static File tryMoveLocal(File sourceFile, File destinationDir)
    {
        return tryMoveLocal(sourceFile, destinationDir, "");
    }

    /**
     * Moves source file to destination directory, putting <var>prefixTemplate</var> in front of its name after
     * replacing '%t' with the current time stamp.
     */
    public static File tryMoveLocal(File sourcePath, File destinationDirectory, String prefixTemplate)
    {
        assert destinationDirectory != null;
        assert FileUtilities.checkDirectoryFullyAccessible(destinationDirectory, "destination") == null : "Directory is not fully accessible "
                + destinationDirectory.getAbsolutePath();
        assert prefixTemplate != null;
        assert sourcePath != null;

        final String destinationPath = createDestinationPath(sourcePath, null, destinationDirectory, prefixTemplate);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Moving path '%s' to '%s'", sourcePath.getPath(), destinationPath));
        }
        File destFile = new File(destinationPath);
        int failures = 0;
        boolean movedOK = false;
        while(true)
        {
            movedOK = sourcePath.renameTo(destFile);
            if (movedOK)
            {
                break;
            } else {
                if (sourcePath.exists() == false)
                {
                    operationLog.error(String.format("Path '%s' doesn't exist, so it can't be moved to '%s'.",
                            sourcePath, destinationDirectory));
                    break;
                }
                ++failures;
                operationLog.warn(String.format("Moving path '%s' to directory '%s' failed (attempt %d).", sourcePath,
                        destinationDirectory, failures));
                if (failures >= MAX_RETRIES_ON_FAILURE)
                {
                    break;
                }
                try
                {
                    Thread.sleep(MILLIS_TO_SLEEP_ON_FAILURE);
                } catch (InterruptedException ex)
                {
                    break;
                }
            }
        }
        if (movedOK == false)
        {
            notificationLog.error(String.format("Moving path '%s' to directory '%s' failed, giving up.", sourcePath,
                    destinationDirectory));
            return null;
        } else
        {
            return destFile;
        }
    }

    /**
     * Creates a destination path for copying <var>sourcePath</var> to <var>destinationDirectory</var> with prefix
     * defined by <var>prefixTemplate</var>. Note that '%t' in <var>prefixTemplate</var> will be replaced by the
     * current time stamp in format YYYYmmddhhMMss.
     */
    private static String createDestinationPath(File sourcePath, String destinationHostOrNull,
            File destinationDirectory, String prefixTemplate)
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
            return destinationDirectory.getAbsolutePath() + File.separator + createPrefix(prefixTemplate)
                    + sourcePath.getName();
        }
    }

    private static String createPrefix(String prefixTemplate)
    {
        return StringUtils.replace(prefixTemplate, "%t", DateFormatUtils.format(System.currentTimeMillis(),
                "yyyyMMddHHmmss"));
    }

}