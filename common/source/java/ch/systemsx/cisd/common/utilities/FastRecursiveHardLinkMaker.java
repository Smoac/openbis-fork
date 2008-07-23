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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A fast {@link IImmutableCopier} that uses a fallback option whenever one of the fast copiers for
 * either files or directories is not available.
 * 
 * @author Bernd Rinn
 */
public class FastRecursiveHardLinkMaker implements IImmutableCopier
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FastRecursiveHardLinkMaker.class);

    private static final String RSYNC_EXEC = "rsync";

    private final static long MILLIS = 1000L;

    private final static long DEFAULT_INACTIVITY_TRESHOLD_MILLIS = 300 * MILLIS;

    private final static int DEFAULT_MAX_ERRORS_TO_IGNORE = 3;

    private final static int DEFAULT_MAX_ATTEMPTS = 10;

    private final static long DEFAULT_TIME_TO_SLEEP_AFTER_COPY_FAILS = 5 * MILLIS;

    private final IImmutableCopier fallbackCopierOrNull;

    private final IFileImmutableCopier fastFileCopierOrNull;

    private final IDirectoryImmutableCopier fastDirectoryCopierOrNull;

    public final static IImmutableCopier tryCreate(final File rsyncExecutable)
    {
        return tryCreate(rsyncExecutable, DEFAULT_INACTIVITY_TRESHOLD_MILLIS, DEFAULT_MAX_ATTEMPTS,
                DEFAULT_TIME_TO_SLEEP_AFTER_COPY_FAILS);
    }

    public final static IImmutableCopier tryCreate(final long millisToWaitForCompletion,
            final int maxRetryOnFailure, final long millisToSleepOnFailure)
    {
        return tryCreate(OSUtilities.findExecutable(RSYNC_EXEC), millisToWaitForCompletion,
                maxRetryOnFailure, millisToSleepOnFailure);
    }

    public final static IImmutableCopier tryCreate(final File rsyncExecutable,
            final long millisToWaitForCompletion, final int maxRetryOnFailure,
            final long millisToSleepOnFailure)
    {
        try
        {
            return new FastRecursiveHardLinkMaker(rsyncExecutable, millisToSleepOnFailure,
                    maxRetryOnFailure, millisToSleepOnFailure);
        } catch (ConfigurationFailureException ex)
        {
            return null;
        }
    }

    private FastRecursiveHardLinkMaker(final File rsyncExcutable,
            final long inactivityThresholdMillis, final int maxRetryOnFailure,
            final long millisToSleepOnFailure) throws ConfigurationFailureException
    {
        this.fastFileCopierOrNull =
                FastHardLinkMaker.tryCreate(inactivityThresholdMillis, maxRetryOnFailure,
                        millisToSleepOnFailure);
        this.fastDirectoryCopierOrNull =
                new RsyncBasedRecursiveHardLinkMaker(rsyncExcutable, inactivityThresholdMillis,
                        DEFAULT_MAX_ERRORS_TO_IGNORE, maxRetryOnFailure, millisToSleepOnFailure);
        if (fastFileCopierOrNull == null)
        {
            this.fallbackCopierOrNull =
                    RecursiveHardLinkMaker.tryCreate(HardLinkMaker.tryCreateRetrying(
                            inactivityThresholdMillis, maxRetryOnFailure, millisToSleepOnFailure));
        } else
        {
            this.fallbackCopierOrNull = RecursiveHardLinkMaker.tryCreate(fastFileCopierOrNull);
        }
        if ((fastFileCopierOrNull == null && fallbackCopierOrNull == null)
                || (fastDirectoryCopierOrNull == null && fallbackCopierOrNull == null))
        {
            throw new ConfigurationFailureException("Not operational");
        }
        if (operationLog.isInfoEnabled())
        {
            if (fastFileCopierOrNull != null)
            {
                operationLog.info("Using native library to create hard link copies of files.");
            } else
            {
                operationLog.info("Using 'ln' to create hard link copies of files.");
            }
            if (fastDirectoryCopierOrNull != null)
            {
                operationLog.info("Using 'rsync' to traverse directories when making recursive "
                        + "hard links copies.");
            } else
            {
                operationLog.info("Using Java to traverse directories when making recursive hard "
                        + "link copies");
            }
        }
    }

    public boolean copyImmutably(File source, File destinationDirectory, String nameOrNull)
    {
        if (source.isDirectory())
        {
            if (fastDirectoryCopierOrNull != null)
            {
                return fastDirectoryCopierOrNull.copyDirectoryImmutably(source,
                        destinationDirectory, nameOrNull);
            } else
            {
                return fallbackCopierOrNull.copyImmutably(source, destinationDirectory, nameOrNull);
            }
        } else
        {
            if (fastFileCopierOrNull != null)
            {
                return fastFileCopierOrNull.copyFileImmutably(source, destinationDirectory,
                        nameOrNull);
            } else
            {
                return fallbackCopierOrNull.copyImmutably(source, destinationDirectory, nameOrNull);
            }
        }
    }

}
