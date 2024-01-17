package ch.ethz.sis.transaction;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

public class TransactionLog implements ITransactionLog
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionLog.class);

    private final File logFolder;

    private final Map<UUID, TransactionStatus> lastStatuses;

    public TransactionLog(File logFolder)
    {
        if (logFolder == null)
        {
            throw new IllegalArgumentException("Transactions log folder cannot be null");
        }

        try
        {
            createOrCheckFolder(logFolder);
        } catch (Exception e)
        {
            throw new RuntimeException("Could not prepare transactions log folder '" + logFolder + "'.", e);
        }

        this.logFolder = logFolder;
        this.lastStatuses = loadLastStatuses(logFolder);
    }

    @Override public void logStatus(final UUID transactionId, final TransactionStatus transactionStatus)
    {
        File transactionLogFolder = new File(logFolder, transactionId.toString());

        try
        {
            createOrCheckFolder(transactionLogFolder);
        } catch (Exception e)
        {
            throw new RuntimeException(
                    "Could not prepare transaction log folder '" + transactionLogFolder + "' for transaction '" + transactionId + "'.", e);
        }

        File transactionStatusFile = new File(transactionLogFolder, transactionStatus.name());

        try
        {
            createOrCheckAndTouchFile(transactionStatusFile);
        } catch (Exception e)
        {
            throw new RuntimeException(
                    "Could not prepare transaction status file '" + transactionStatusFile + "' for transaction '" + transactionId + "'.",
                    e);
        }

        lastStatuses.put(transactionId, transactionStatus);

        operationLog.info("Logged transaction '" + transactionId + "' status '" + transactionStatus + "'.");
    }

    @Override public Map<UUID, TransactionStatus> getLastStatuses()
    {
        return Collections.unmodifiableMap(lastStatuses);
    }

    private static Map<UUID, TransactionStatus> loadLastStatuses(File logFolder)
    {
        operationLog.info("Loading last transaction statuses from folder '" + logFolder + "'.");

        if (!logFolder.exists() || !logFolder.isDirectory())
        {
            throw new RuntimeException("Transactions log folder '" + logFolder + "' does not exist or is not a directory.");
        }

        Map<UUID, TransactionStatus> lastStatuses = new HashMap<>();
        File[] transactionFolders = logFolder.listFiles();

        if (transactionFolders == null)
        {
            throw new RuntimeException("Could not load the contents of the transaction log folder '" + logFolder + "'.");
        }

        for (File transactionFolder : transactionFolders)
        {
            if (transactionFolder.isDirectory())
            {
                File[] statusFiles = transactionFolder.listFiles();

                if (statusFiles == null)
                {
                    throw new RuntimeException("Could not load the contents of the transaction log folder '" + transactionFolder + "'.");
                }

                for (File statusFile : statusFiles)
                {
                    if (statusFile.isFile())
                    {
                        try
                        {
                            TransactionStatus previousLastStatus = lastStatuses.get(UUID.fromString(transactionFolder.getName()));
                            TransactionStatus lastStatus = TransactionStatus.valueOf(statusFile.getName());

                            if (previousLastStatus == null || previousLastStatus.isPreviousStatusOf(lastStatus))
                            {
                                lastStatuses.put(UUID.fromString(transactionFolder.getName()), lastStatus);
                            }
                        } catch (Exception e)
                        {
                            operationLog.info("Ignoring file '" + statusFile + "'. It's name '" + statusFile.getName()
                                    + "' does not match any of the transaction statuses '" + Arrays.toString(TransactionStatus.values()) + "'.");
                        }
                    } else
                    {
                        operationLog.info(
                                "Ignoring non-regular file '" + statusFile
                                        + "'. Only regular files that represent transaction statuses are expected to be found in '"
                                        + transactionFolder + "'.");
                    }
                }
            } else
            {
                operationLog.info(
                        "Ignoring regular file '" + transactionFolder + "'. Only folders that represent transactions are expected to be found in '"
                                + logFolder + "'.");
            }
        }

        return lastStatuses;
    }

    private static void createOrCheckFolder(File folder)
    {
        if (folder.exists() && !folder.isDirectory())
        {
            throw new IllegalArgumentException("Folder '" + folder.getAbsolutePath() + "' is not a directory");
        }

        if (!folder.exists())
        {
            boolean created = false;
            Exception exception = null;

            try
            {
                created = folder.mkdir();
            } catch (Exception e)
            {
                exception = e;
            }

            if (!created)
            {
                throw new RuntimeException("Could not create folder '" + folder.getAbsolutePath() + "'.", exception);
            }
        }

        if (!folder.canWrite())
        {
            throw new IllegalArgumentException("Cannot write to folder '" + folder.getAbsolutePath() + "'.");
        }
    }

    private static void createOrCheckAndTouchFile(File file)
    {
        boolean success = false;
        Exception exception = null;

        try
        {
            if (file.exists())
            {
                if (!file.isFile())
                {
                    throw new RuntimeException("File '" + file + "' is not a regular file.");
                }
                success = file.setLastModified(System.currentTimeMillis());
            } else
            {
                success = file.createNewFile();
            }
        } catch (Exception e)
        {
            exception = e;
        }

        if (!success)
        {
            throw new RuntimeException("Could not create or touch file '" + file + "'.", exception);
        }
    }

}
