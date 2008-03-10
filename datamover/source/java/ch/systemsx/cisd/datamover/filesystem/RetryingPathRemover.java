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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;

/**
 * A class which supports removal of paths on a file system that is mounted locally.
 * <p>
 * Supports retrying if the operation fails for the first time.
 * 
 * @author Bernd Rinn
 */
final class RetryingPathRemover implements IPathRemover
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, RetryingPathMover.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, RetryingPathMover.class);

    private final int maxRetriesOnFailure;

    private final long millisToSleepOnFailure;

    RetryingPathRemover(int maxRetriesOnFailure, long millisToSleepOnFailure)
    {
        this.maxRetriesOnFailure = maxRetriesOnFailure;
        this.millisToSleepOnFailure = millisToSleepOnFailure;
    }

    private final static Status STATUS_FAILED_DELETION = new Status(StatusFlag.FATAL_ERROR, "Failed to remove path.");

    public Status remove(File path)
    {
        assert path != null;

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Removing path '%s'", path.getPath()));
        }
        int failures = 0;
        boolean deletionOK = false;
        while (true)
        {
            deletionOK = FileUtilities.deleteRecursively(path);
            if (deletionOK)
            {
                break;
            } else
            {
                if (path.exists() == false)
                {
                    operationLog.warn(String.format("Path '%s' doesn't exist, so it can't be removed.", path));
                    break;
                }
                ++failures;
                operationLog.warn(String.format("Removing path '%s' failed (attempt %d).", path, failures));
                if (failures >= maxRetriesOnFailure)
                {
                    break;
                }
                try
                {
                    Thread.sleep(millisToSleepOnFailure);
                } catch (InterruptedException ex)
                {
                    break;
                }
            }
        }

        if (deletionOK == false)
        {
            notificationLog.error(String.format("Removing path '%s' failed, giving up.", path));
            return STATUS_FAILED_DELETION;
        } else
        {
            return Status.OK;
        }
    }
}