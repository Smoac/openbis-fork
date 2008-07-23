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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.CallableExecutor;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;

/**
 * @author Bernd Rinn
 */
public class HardLinkMaker implements IFileImmutableCopier
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HardLinkMaker.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, HardLinkMaker.class);

    private static final String HARD_LINK_EXEC = "ln";

    private final String linkExecPath;

    private final RetryingOperationTimeout singleFileLinkTimeout;

    private static class RetryingOperationTimeout
    {
        private final long millisToWaitForCompletion;

        private final int maxRetryOnFailure;

        private final long millisToSleepOnFailure;

        private RetryingOperationTimeout(long millisToWaitForCompletion, int maxRetryOnFailure,
                long millisToSleepOnFailure)
        {
            this.millisToWaitForCompletion = millisToWaitForCompletion;
            this.maxRetryOnFailure = maxRetryOnFailure;
            this.millisToSleepOnFailure = millisToSleepOnFailure;
        }

        public long getMillisToWaitForCompletion()
        {
            return millisToWaitForCompletion;
        }

        public int getMaxRetryOnFailure()
        {
            return maxRetryOnFailure;
        }

        public long getMillisToSleepOnFailure()
        {
            return millisToSleepOnFailure;
        }
    }

    private HardLinkMaker(final String linkExecPath,
            RetryingOperationTimeout singleFileLinkTimeoutOrNull)
    {
        this.linkExecPath = linkExecPath;
        this.singleFileLinkTimeout =
                singleFileLinkTimeoutOrNull != null ? singleFileLinkTimeoutOrNull
                        : createNoTimeout();
    }

    //
    // Factory methods
    //

    /**
     * Creates copier which won't retry an operation if it fails.
     * 
     * @param linkExecPath The path to the <code>ln</code> executable.
     */
    public static final IFileImmutableCopier create(final String linkExecPath)
    {
        return new HardLinkMaker(linkExecPath, null);
    }

    /**
     * Creates copier trying to find the path to the <code>ln</code> executable.
     * 
     * @return <code>null</code> if the <code>ln</code> executable was not found.
     */
    public static final IFileImmutableCopier tryCreate()
    {
        return tryCreate(null);
    }

    /**
     * Creates copier which is able to retry the operation of creating the hard link of a file if
     * it does not complete after a specified timeout.
     * 
     * @param millisToWaitForCompletion The time to wait for the process creating one hard link to a
     *            file to complete in milli seconds. If the process is not finished after that time,
     *            it will be terminated.
     * @param maxRetryOnFailure The number of times we should try to create each hard link if copy
     *            operation fails.
     * @param millisToSleepOnFailure The number of milliseconds we should wait before re-executing
     *            the copy of a single file. Specify 0 to wait till the first operation completes.
     */
    public static final IFileImmutableCopier tryCreateRetrying(
            final long millisToWaitForCompletion, final int maxRetryOnFailure,
            final long millisToSleepOnFailure)
    {
        RetryingOperationTimeout timeout =
                new RetryingOperationTimeout(millisToWaitForCompletion, maxRetryOnFailure,
                        millisToSleepOnFailure);
        return tryCreate(timeout);
    }

    private static final IFileImmutableCopier tryCreate(
            RetryingOperationTimeout singleFileLinkTimeoutOrNull)
    {
        final File lnExec = OSUtilities.findExecutable(HARD_LINK_EXEC);
        if (lnExec == null)
        {
            return null;
        }
        return new HardLinkMaker(lnExec.getAbsolutePath(), singleFileLinkTimeoutOrNull);
    }

    private RetryingOperationTimeout createNoTimeout()
    {
        return new RetryingOperationTimeout(0, 1, 0);
    }

    public boolean copyFileImmutably(final File source, final File destinationDirectory,
            final String nameOrNull)
    {
        assert source.isFile() : String
                .format("Given file '%s' must be a file and is not.", source);
        final File destFile =
                new File(destinationDirectory, nameOrNull == null ? source.getName() : nameOrNull);
        final List<String> cmd = createLnCmdLine(source, destFile);
        final Callable<Boolean> processTask = new Callable<Boolean>()
            {
                public final Boolean call()
                {
                    boolean result =
                            ProcessExecutionHelper.runAndLog(cmd, operationLog, machineLog,
                                    singleFileLinkTimeout.getMillisToWaitForCompletion());
                    // NOTE: we have noticed that sometimes the result is false although the file
                    // have been copied
                    if (result == false && destFile.exists()
                            && checkIfIdenticalContent(source, destFile))
                    {
                        machineLog
                                .warn("Link creator reported failure, but the exact copy of the file '"
                                        + source.getPath()
                                        + "' seems to exist in '"
                                        + destFile.getPath() + "'. Error will be ignored.");
                        result = true;
                    }
                    return result;
                }
            };
        boolean ok =
                runRepeatableProcess(processTask, singleFileLinkTimeout.getMaxRetryOnFailure(),
                        singleFileLinkTimeout.getMillisToSleepOnFailure());
        return ok;
    }

    private static boolean checkIfIdenticalContent(final File file1, final File file2)
    {
        StopException.check();
        try
        {
            return FileUtils.contentEquals(file1, file2);
        } catch (IOException e)
        {
            machineLog.warn("Error when comparing the content of a file and its link: "
                    + e.getMessage());
        }
        return false;
    }

    private final List<String> createLnCmdLine(final File srcFile, final File destFile)
    {
        final List<String> tokens = new ArrayList<String>();
        tokens.add(linkExecPath);
        tokens.add(srcFile.getAbsolutePath());
        tokens.add(destFile.getAbsolutePath());
        return tokens;
    }

    private static boolean runRepeatableProcess(final Callable<Boolean> task,
            final int maxRetryOnFailure, final long millisToSleepOnFailure)
    {
        return new CallableExecutor(maxRetryOnFailure, millisToSleepOnFailure)
                .executeCallable(task);
    }
}
