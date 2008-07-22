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

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.InactivityMonitor;
import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IInactivityObserver;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncCopier;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A hard link maker based on <code>rsync</code>.
 * 
 * @author Bernd Rinn
 */
public class RsyncBasedRecursiveHardLinkMaker implements IDirectoryImmutableCopier
{

    private static final String RSYNC_EXEC = "rsync";

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, RsyncBasedRecursiveHardLinkMaker.class);

    private final static long MILLIS = 1000L;

    private final static long DEFAULT_INACTIVITY_TRESHOLD_MILLIS = 300 * MILLIS;

    private final static int DEFAULT_MAX_ERRORS_TO_IGNORE = 3;

    private final static int DEFAULT_MAX_ATTEMPTS = 10;

    private final static long DEFAULT_TIME_TO_SLEEP_AFTER_COPY_FAILS = 5 * MILLIS;

    private final long inactivityThresholdMillis;

    private final int maxErrorsToIgnore;

    private final int maxAttempts;

    private final long timeToSleepAfterCopyFails;

    private final RsyncCopier rsyncCopier;

    public interface ILastChangedChecker
    {
        long lastChangedRelative(File destinationDirectory, long thresholdMillis);
    }

    public RsyncBasedRecursiveHardLinkMaker()
    {
        this(null, DEFAULT_INACTIVITY_TRESHOLD_MILLIS, DEFAULT_MAX_ERRORS_TO_IGNORE,
                DEFAULT_MAX_ATTEMPTS, DEFAULT_TIME_TO_SLEEP_AFTER_COPY_FAILS);
    }

    public RsyncBasedRecursiveHardLinkMaker(File rsyncExecutableOrNull)
    {
        this(rsyncExecutableOrNull, DEFAULT_INACTIVITY_TRESHOLD_MILLIS,
                DEFAULT_MAX_ERRORS_TO_IGNORE, DEFAULT_MAX_ATTEMPTS,
                DEFAULT_TIME_TO_SLEEP_AFTER_COPY_FAILS);
    }

    public RsyncBasedRecursiveHardLinkMaker(File rsyncExecutableOrNull,
            long inactivityThresholdMillis, int maxErrorsToIgnore, int maxAttempts,
            long timeToSleepAfterCopyFails)
    {
        if (rsyncExecutableOrNull == null)
        {
            rsyncCopier = new RsyncCopier(OSUtilities.findExecutable(RSYNC_EXEC));
        } else
        {
            rsyncCopier = new RsyncCopier(rsyncExecutableOrNull);
        }
        this.inactivityThresholdMillis = inactivityThresholdMillis;
        this.timeToSleepAfterCopyFails = timeToSleepAfterCopyFails;
        this.maxErrorsToIgnore = maxErrorsToIgnore;
        this.maxAttempts = maxAttempts;
    }

    public boolean copyDirectoryImmutably(final File sourceDirectory,
            final File destinationDirectory, final String targetNameOrNull)
    {
        final File target =
                new File(destinationDirectory, (targetNameOrNull == null) ? sourceDirectory
                        .getName() : targetNameOrNull);
        final IInactivityObserver observer = new IInactivityObserver()
            {
                public void update(long inactiveSinceMillis, String descriptionOfInactivity)
                {
                    machineLog.error(descriptionOfInactivity + ", terminating rsync");
                    rsyncCopier.terminate();
                }
            };
        boolean ok;
        int attempt = 0;
        while (true)
        {
            ok =
                    createHardLinks(sourceDirectory, destinationDirectory, targetNameOrNull,
                            target, observer);
            if (ok || attempt++ > maxAttempts)
            {
                break;
            }
            if (ok == false)
            {
                ConcurrencyUtilities.sleep(timeToSleepAfterCopyFails);
            }
        }
        return ok;
    }

    private boolean createHardLinks(final File sourceDirectory, final File destinationDirectory,
            final String targetNameOrNull, final File target, final IInactivityObserver observer)
    {
        final InactivityMonitor monitor =
                new InactivityMonitor(new RemoteDirectoryCopyActivitySensor(target,
                        maxErrorsToIgnore), observer, inactivityThresholdMillis, true);
        final boolean result =
                rsyncCopier.copyDirectoryImmutably(sourceDirectory, destinationDirectory,
                        targetNameOrNull);
        monitor.stop();
        return result;
    }

}
