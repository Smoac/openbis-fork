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

package ch.systemsx.cisd.datamover.filesystem.store;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.highwatermark.FileWithHighwaterMark;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.highwatermark.RemoteFreeSpaceProvider;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathCopier;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;

/**
 * @author Tomasz Pylak
 */
public class FileStoreRemote extends FileStore
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileStoreRemote.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, FileStoreRemote.class);

    private static final long QUICK_SSH_TIMEOUT_MILIS = 5 * 1000;

    private static final long LONG_SSH_TIMEOUT_MILIS = 15 * 1000;

    private static final String DIRECTORY_AVAILABLE_RESULT = "ok";

    private final static String BASH_FIND_PRINT_TIME_FROM_EPOCHE = " -printf \"%T@\\n\" ";

    private final static String BASH_SELECT_YOUNGEST_TIMESTAMP = " | sort -n | head -1 ";

    private final static String BASH_CHECK_IF_COMMAND_EXISTS = "type -p ";

    private static String mkDeleteFileCommand(String pathString)
    {
        return "rm -fr " + pathString;
    }

    private static String mkCheckDirectoryFullyAccessibleCommand(String path)
    {
        // %1$s references always the first argument
        return String.format("if [ -d %1$s -a -w %1$s -a -r %1$s -a -x %1$s ]; then "
                + "echo %2$s; else echo null; fi", path, DIRECTORY_AVAILABLE_RESULT);
    }

    // ---------------

    private final File sshExecutable;

    private final IStoreItemExistsChecker storeItemExistsChecker;

    private final HighwaterMarkWatcher highwaterMarkWatcher;

    private String remoteFindExecutableOrNull;

    public FileStoreRemote(final FileWithHighwaterMark fileWithHighwaterMark, final String host,
            final String kind, final IFileSysOperationsFactory factory)
    {
        super(fileWithHighwaterMark, host, true, kind, factory);
        assert host != null : "Unspecified host";
        this.sshExecutable = findSSHOrDie(factory);
        this.highwaterMarkWatcher =
                createHighwaterMarkWatcher(fileWithHighwaterMark, host, sshExecutable);
        this.remoteFindExecutableOrNull = null;
        this.storeItemExistsChecker = createStoreItemExistsChecker(factory, getPath(), host);
    }

    private static File findSSHOrDie(final IFileSysOperationsFactory factory)
    {
        File ssh = factory.tryFindSshExecutable();
        if (ssh == null)
        {
            throw new EnvironmentFailureException("Cannot find ssh program");
        }
        return ssh;
    }

    private final static HighwaterMarkWatcher createHighwaterMarkWatcher(
            final FileWithHighwaterMark fileWithHighwaterMark, final String host,
            final File sshExecutable)
    {
        final IFreeSpaceProvider freeSpaceProvider =
                new RemoteFreeSpaceProvider(host, sshExecutable);
        final HighwaterMarkWatcher highwaterMarkWatcher =
                new HighwaterMarkWatcher(fileWithHighwaterMark.getHighwaterMark(),
                        freeSpaceProvider);
        highwaterMarkWatcher.setPath(fileWithHighwaterMark.getFile());
        return highwaterMarkWatcher;
    }

    //
    // FileStore
    //

    public final IExtendedFileStore tryAsExtended()
    {
        return null;
    }

    public final Status delete(final StoreItem item)
    {
        String pathString = StoreItem.asFile(getPath(), item).getPath();
        String simpleCmd = mkDeleteFileCommand(pathString);
        List<String> cmdLine = createSshCommand(simpleCmd);
        ProcessResult result = tryExecuteCommand(cmdLine, QUICK_SSH_TIMEOUT_MILIS);
        String errMsg = tryGetErrorMessage(result);
        if (errMsg == null)
        {
            return Status.OK;
        } else
        {
            return new Status(StatusFlag.RETRIABLE_ERROR, errMsg);
        }
    }

    private interface IStoreItemExistsChecker
    {
        boolean exists(final StoreItem item);
    }

    private static IStoreItemExistsChecker createStoreItemExistsChecker(
            final IFileSysOperationsFactory factory, final File parentPath, final String host)
    {
        return new IStoreItemExistsChecker()
            {
                private final IPathCopier copier = factory.getCopier(false);

                public boolean exists(StoreItem item)
                {
                    File itemFile = StoreItem.asFile(parentPath, item);
                    return copier.existsRemotely(itemFile, host);
                }
            };
    }

    public final boolean exists(final StoreItem item)
    {
        return storeItemExistsChecker.exists(item);
    }

    public final IStoreCopier getCopier(final IFileStore destinationDirectory)
    {
        final boolean requiresDeletion = false;
        return constructStoreCopier(destinationDirectory, requiresDeletion);
    }

    public final long lastChanged(final StoreItem item, final long stopWhenFindYounger)
    {
        return lastChanged(item);
    }

    private final long lastChanged(final StoreItem item)
    {
        String itemPath = StoreItem.asFile(getPath(), item).getPath();

        String findExec = getRemoteFindExecutableOrDie();
        String localCmd =
                findExec + " " + itemPath + BASH_FIND_PRINT_TIME_FROM_EPOCHE
                        + BASH_SELECT_YOUNGEST_TIMESTAMP;
        List<String> cmdLine = createSshCommand(localCmd);
        ProcessResult result = tryExecuteCommand(cmdLine, LONG_SSH_TIMEOUT_MILIS);
        String errMsg = tryGetErrorMessage(result);
        if (errMsg == null)
        {
            String resultLine = result.getProcessOutput().get(0);
            try
            {
                return Long.parseLong(resultLine);
            } catch (NumberFormatException e)
            {
                throw new EnvironmentFailureException("The result of " + cmdLine
                        + " should be a number but was: " + result.getProcessOutput());
            }
        } else
        {
            throw new EnvironmentFailureException(errMsg);
        }
    }

    private String getRemoteFindExecutableOrDie()
    {
        if (remoteFindExecutableOrNull == null)
        {
            checkAvailableAndSetFindUtil();
        }
        if (remoteFindExecutableOrNull == null)
        {
            throw new EnvironmentFailureException(createNoFindUtilMessage());
        } else
        {
            return remoteFindExecutableOrNull;
        }
    }

    public final long lastChangedRelative(final StoreItem item,
            final long stopWhenFindYoungerRelative)
    {
        return lastChanged(item);
    }

    // outgoing and self-test
    public final String tryCheckDirectoryFullyAccessible(final long timeOutMillis)
    {
        String errMsg = tryCheckDirectoryAccessible(getPathString(), timeOutMillis);
        if (errMsg == null)
        {
            if (this.remoteFindExecutableOrNull != null || checkAvailableAndSetFindUtil())
            {
                return null;
            } else
            {
                return createNoFindUtilMessage();
            }
        } else
        {
            return errMsg;
        }
    }

    private String createNoFindUtilMessage()
    {
        return "No find utility is present on the remote machine " + getHost();
    }

    private String getPathString()
    {
        return getPath().getPath();
    }

    // tries to execute different find versions with appropriate options on the remote host. If
    // successful sets the executable script name and returns null. Otherwise returns error message.
    private boolean checkAvailableAndSetFindUtil()
    {
        final String[] findExecutables =
            { "gfind", "find" };
        for (String findExec : findExecutables)
        {
            List<String> cmdLine = createSshCommand(BASH_CHECK_IF_COMMAND_EXISTS + findExec);
            ProcessResult result = tryExecuteCommand(cmdLine, QUICK_SSH_TIMEOUT_MILIS);
            if (result.isOK())
            {
                setFindExecutable(findExec);
                return true;
            }
        }
        return false;
    }

    private void setFindExecutable(String findExecutable)
    {
        this.remoteFindExecutableOrNull = findExecutable;
    }

    private String tryCheckDirectoryAccessible(String pathString, final long timeOutMillis)
    {
        String simpleCmd = mkCheckDirectoryFullyAccessibleCommand(pathString);
        List<String> cmdLine = createSshCommand(simpleCmd);
        ProcessResult result = tryExecuteCommand(cmdLine, timeOutMillis);
        String errMsg = tryGetErrorMessage(result);
        if (errMsg == null)
        {
            return isDirectoryFullyAccessibleParseResult(result) ? null
                    : "Directory not accesible: " + getHost() + ":" + pathString;
        } else
        {
            return errMsg;
        }
    }

    private List<String> createSshCommand(String cmd)
    {
        return ProcessExecutionHelper.createSshCommand(cmd, sshExecutable, getHost());
    }

    private boolean isDirectoryFullyAccessibleParseResult(ProcessResult result)
    {
        List<String> processOutput = result.getProcessOutput();
        if (processOutput.size() != 1)
        {
            machineLog.error("Unexpected output of '" + result.getCommandLine() + "' command: "
                    + processOutput);
        }
        String resultLine = processOutput.get(0);
        return resultLine.equals(DIRECTORY_AVAILABLE_RESULT);
    }

    @Override
    public final String toString()
    {
        final String pathStr = getPathString();
        return "[remote fs]" + getHost() + ":" + pathStr;
    }

    public final String getLocationDescription(final StoreItem item)
    {
        return getHost() + ":" + getChildFile(item).getPath();
    }

    private String getHost()
    {
        String host = tryGetHost();
        assert host != null : "host cannot be null";
        return host;
    }

    public final StoreItem[] tryListSortByLastModified(final ISimpleLogger loggerOrNull)
    {
        // TODO 2008-05-22, Tomasz Pylak: implement this to have ssh tunelling for incoming
        // directories too
        throw new NotImplementedException();
    }

    public final HighwaterMarkWatcher getHighwaterMarkWatcher()
    {
        return highwaterMarkWatcher;
    }

    // -----------------------

    // null if fails
    private ProcessResult tryExecuteCommand(final List<String> cmdLine, final long timeOutMillis)
    {
        ProcessResult result =
                ProcessExecutionHelper.run(cmdLine, timeOutMillis, operationLog, machineLog);
        result.log();
        return result;
    }

    private static String tryGetErrorMessage(ProcessResult result)
    {
        if (result.isOK() == false)
        {
            return "Command '" + result.getCommandLine() + "' failed with error result "
                    + result.exitValue();
        } else
        {
            return null;
        }
    }
}
