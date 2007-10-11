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

    private final Status STATUS_FAILED_DELETION = new Status(StatusFlag.FATAL_ERROR, "Failed to remove path.");

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