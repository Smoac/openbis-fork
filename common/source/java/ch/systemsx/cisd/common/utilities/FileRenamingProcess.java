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

package ch.systemsx.cisd.common.utilities;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A <code>IProcess</code> implementation for file renaming.
 * 
 * @author Christian Ribeaud
 */
public final class FileRenamingProcess implements IProcess
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, FileRenamingProcess.class);

    public static final long DEFAULT_MILLIS_TO_SLEEP = 5000L;

    public static final int DEFAULT_MAX_RETRIES = 12;

    private final File sourceFile;

    private final File destinationFile;

    private final int maxRetries;

    private final long millisToSleep;

    private int failures;

    private boolean renamed;

    public FileRenamingProcess(final File sourceFile, final File destinationFile)
    {
        this(DEFAULT_MAX_RETRIES, DEFAULT_MILLIS_TO_SLEEP, sourceFile, destinationFile);
    }

    public FileRenamingProcess(final int maxRetries, final long millisToSleep, final File sourceFile,
            final File destinationFile)
    {
        this.sourceFile = sourceFile;
        this.maxRetries = maxRetries;
        this.millisToSleep = millisToSleep;
        this.destinationFile = destinationFile;
    }

    /**
     * Whether the file has been renamed.
     * <p>
     * This is the return value of {@link File#renameTo(File)}.
     * </p>
     */
    public final boolean isRenamed()
    {
        return renamed;
    }

    //
    // IProcess
    //

    public final int getMaxRetryOnFailure()
    {
        return maxRetries;
    }

    public final long getMillisToSleepOnFailure()
    {
        return millisToSleep;
    }

    public final void run()
    {
        renamed = sourceFile.renameTo(destinationFile);
    }

    public final boolean succeeded()
    {
        if (renamed == false)
        {
            if (sourceFile.exists() == false)
            {
                operationLog.error(String.format("Path '%s' doesn't exist, so it can't be moved to '%s'.", sourceFile,
                        destinationFile));
                // Nothing to do here. So exit the looping by returning true.
                return true;
            }
            operationLog.warn(String.format("Moving path '%s' to directory '%s' failed (attempt %d).", sourceFile,
                    destinationFile, ++failures));
        }
        return true;
    }
}