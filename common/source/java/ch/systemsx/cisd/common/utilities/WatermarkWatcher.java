/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A watermark watcher.
 * <p>
 * This class is thread-safe.
 * </p>
 * 
 * @see FileSystemUtils
 * @author Christian Ribeaud
 */
public final class WatermarkWatcher
{

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, WatermarkWatcher.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, WatermarkWatcher.class);

    private final long watermark;

    private boolean notified;

    /**
     * @param watermark the watermark value in kilobytes. If negative, then
     *            {@link #isWatermarkReached(File)} always return <code>false</code>.
     */
    public WatermarkWatcher(final long watermark)
    {
        this.watermark = watermark;
    }

    private final static String displayKilobyteValue(final long value)
    {
        return FileUtils.byteCountToDisplaySize(value * FileUtils.ONE_KB);
    }

    /**
     * Checks whether the watermark value is reached.
     * <p>
     * If so, then informs (only once!) the administrator (via notification log) about it. The
     * administrator gets also informed when the space is sufficient again.
     * </p>
     */
    public final synchronized boolean isWatermarkReached(final File path)
    {
        if (watermark < 0)
        {
            return false;
        }
        try
        {
            final String canonicalPath = path.getCanonicalPath();
            final long freeSpace = FileSystemUtils.freeSpaceKb(canonicalPath);
            final String freeSpaceDisplayed = displayKilobyteValue(freeSpace);
            if (freeSpace <= watermark)
            {
                if (notified == false)
                {
                    notificationLog.warn(String.format(
                            "The amount of available space (%s) on '%s' "
                                    + "is lower than the specified watermark (%s).",
                            freeSpaceDisplayed, canonicalPath, displayKilobyteValue(watermark)));
                }
                notified = true;
                return true;
            } else
            {
                if (notified == true)
                {
                    notificationLog
                            .info(String
                                    .format(
                                            "The amount of available space (%s) on '%s' "
                                                    + "is again sufficient (greater than the specified watermark: %s).",
                                            freeSpaceDisplayed, canonicalPath,
                                            displayKilobyteValue(watermark)));
                } else
                {
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info(String.format(
                                "Amount of available space on '%s' is: %s.", canonicalPath,
                                freeSpaceDisplayed));
                    }
                }
                notified = false;
                return false;
            }
        } catch (IOException ex)
        {
            operationLog.error("The watermark watcher can not work properly.", ex);
            return false;
        }
    }
}
