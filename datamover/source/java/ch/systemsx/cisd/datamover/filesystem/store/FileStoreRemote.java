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
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.highwatermark.FileWithHighwaterMark;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
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

    // -- bash commands -------------

    // Creates bash command. The command returns the age of the most recently modified file as the
    // number of seconds (note that it's not in miliseconds!) form the epoch
    private static String mkFindYoungestModificationTimestampSecCommand(String path, String findExec)
    {
        return findExec + " " + path + " -printf \"%T@\\n\" | sort -n | head -1 ";
    }

    // Creates bash command. The command deletes file or recursively deletes the whole directory.
    // Be careful!
    private static String mkDeleteFileCommand(String pathString)
    {
        return "rm -fr " + pathString;
    }

    // Creates bash command. The command returns 0 and its output is empty if the path is a readable
    // and writable directory
    private static String mkCheckDirectoryFullyAccessibleCommand(String path)
    {
        // %1$s references always the first argument
        return String.format("if [ -d %1$s -a -w %1$s -a -r %1$s -a -x %1$s ]; then "
                + "exit 0; else echo false; fi", path);
    }

    // Creates bash command. The command returns 0 and its output is empty if the path is an
    // existing file or directory
    private static String mkCheckFileExistsCommand(String path)
    {
        return String.format("if [ -e %s ]; then exit 0; else echo false; fi", path);
    }

    // Creates bash command. The command returns 0 if the command exists and is a file
    private static String mkCheckCommandExistsCommand(String commandName)
    {
        return "type -p " + commandName;
    }

    // Creates bash command. The command returns the list of files inside the directory, sorted by
    // modification time, oldest first
    private static String mkListByOldestModifiedCommand(String directoryPath)
    {
        return "ls -1 -t -r " + directoryPath;
    }

    // ---------------

    private final ISshCommandBuilder sshCommandBuilder;

    private final HighwaterMarkWatcher highwaterMarkWatcher;

    private String remoteFindExecutableOrNull;

    public FileStoreRemote(final FileWithHighwaterMark fileWithHighwaterMark, final String host,
            final String kind, final IFileSysOperationsFactory factory)
    {
        this(fileWithHighwaterMark, host, kind, createSshCommandBuilder(findSSHOrDie(factory)),
                factory);
    }

    // exposed for tests
    FileStoreRemote(final FileWithHighwaterMark fileWithHighwaterMark, final String host,
            final String kind, final ISshCommandBuilder sshCommandBuilder,
            final IFileSysOperationsFactory factory)
    {
        super(fileWithHighwaterMark, host, kind, factory);
        assert host != null : "Unspecified host";
        this.sshCommandBuilder = sshCommandBuilder;
        this.highwaterMarkWatcher =
                createHighwaterMarkWatcher(fileWithHighwaterMark, host, sshCommandBuilder);
        this.remoteFindExecutableOrNull = null;
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
            final ISshCommandBuilder sshCommandBuilder)
    {
        final IFreeSpaceProvider freeSpaceProvider =
                new RemoteFreeSpaceProvider(host, sshCommandBuilder);
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
        String cmd = mkDeleteFileCommand(pathString);
        ProcessResult result = tryExecuteCommandRemotely(cmd, QUICK_SSH_TIMEOUT_MILIS);
        String errMsg = tryGetErrorMessage(result);
        if (errMsg == null)
        {
            return Status.OK;
        } else
        {
            return new Status(StatusFlag.RETRIABLE_ERROR, errMsg);
        }
    }

    public final boolean exists(final StoreItem item)
    {
        File itemFile = StoreItem.asFile(getPath(), item);
        String cmd = mkCheckFileExistsCommand(itemFile.getPath());
        ProcessResult result = tryExecuteCommandRemotely(cmd, QUICK_SSH_TIMEOUT_MILIS);
        return isSuccessfulCheck(result);
    }

    private boolean isSuccessfulCheck(ProcessResult result)
    {
        return result.isOK() && result.getProcessOutput().size() == 0;
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
        String cmd = mkFindYoungestModificationTimestampSecCommand(itemPath, findExec);
        ProcessResult result = tryExecuteCommandRemotely(cmd, LONG_SSH_TIMEOUT_MILIS);
        String errMsg = tryGetErrorMessage(result);
        if (errMsg == null)
        {
            String resultLine = result.getProcessOutput().get(0);
            try
            {
                return Long.parseLong(resultLine) * 1000;
            } catch (NumberFormatException e)
            {
                throw new EnvironmentFailureException("The result of " + cmd + " on remote host "
                        + getHost() + "should be a number but was: " + result.getProcessOutput());
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
            String cmd = mkCheckCommandExistsCommand(findExec);
            ProcessResult result = tryExecuteCommandRemotely(cmd, QUICK_SSH_TIMEOUT_MILIS);
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
        String cmd = mkCheckDirectoryFullyAccessibleCommand(pathString);
        ProcessResult result = tryExecuteCommandRemotely(cmd, timeOutMillis);
        return isSuccessfulCheck(result) ? null
                : ("Directory not accesible: " + getHost() + ":" + pathString);
    }

    private static ISshCommandBuilder createSshCommandBuilder(final File sshExecutable)
    {
        return new ISshCommandBuilder()
            {
                public List<String> createSshCommand(String cmd, String host)
                {
                    return ProcessExecutionHelper.createSshCommand(cmd, sshExecutable, host);
                }
            };
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
        String simpleCmd = mkListByOldestModifiedCommand(getPathString());
        ProcessResult result = tryExecuteCommandRemotely(simpleCmd, LONG_SSH_TIMEOUT_MILIS);
        if (result.isOK())
        {
            return asStoreItems(result.getProcessOutput());
        } else
        {
            return null;
        }
    }

    private static StoreItem[] asStoreItems(List<String> lines)
    {
        StoreItem[] items = new StoreItem[lines.size()];
        int i = 0;
        for (String line : lines)
        {
            items[i] = new StoreItem(line);
            i++;
        }
        return items;
    }

    public final HighwaterMarkWatcher getHighwaterMarkWatcher()
    {
        return highwaterMarkWatcher;
    }

    // -----------------------

    private ProcessResult tryExecuteCommandRemotely(String localCmd, long timeOutMillis)
    {
        List<String> cmdLine = sshCommandBuilder.createSshCommand(localCmd, getHost());
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
