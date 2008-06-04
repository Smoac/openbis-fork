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
import java.util.ArrayList;
import java.util.Arrays;
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
import ch.systemsx.cisd.common.process.ProcessExecutionHelper.OutputReadingStrategy;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;
import ch.systemsx.cisd.datamover.filesystem.intf.UnknownLastChangedException;

/**
 * @author Tomasz Pylak
 */
public class FileStoreRemote extends FileStore
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileStoreRemote.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, FileStoreRemote.class);

    private static final long QUICK_SSH_TIMEOUT_MILIS = 15 * 1000;

    private static final long LONG_SSH_TIMEOUT_MILIS = 120 * 1000;

    // -- bash commands -------------

    // Creates bash command. The command returns the age of the most recently modified file as the
    // number of seconds (note that it's not in miliseconds!) form the epoch
    private static String mkFindYoungestModificationTimestampSecCommand(final String path,
            final String findExec)
    {
        return findExec + " " + path + " -printf \"%T@\\n\" | sort -n | head -1 ";
    }

    // Creates bash command. The command deletes file or recursively deletes the whole directory.
    // Be careful!
    private static String mkDeleteFileCommand(final String pathString)
    {
        return "rm -fr " + pathString;
    }

    // Creates bash command. The command returns 0 and its output is empty if the path is a readable
    // and writable directory
    private static String mkCheckDirectoryFullyAccessibleCommand(final String path)
    {
        // %1$s references always the first argument
        return String.format("if [ -d %1$s -a -w %1$s -a -r %1$s -a -x %1$s ]; then "
                + "exit 0; else echo false; fi", path);
    }

    // Creates bash command. The command returns 0 and its output is empty if the path is an
    // existing file or directory
    private static String mkCheckFileExistsCommand(final String path)
    {
        return String.format("if [ -e %s ]; then exit 0; else echo false; fi", path);
    }

    // Creates bash command. The command returns 0 if the command exists and is a file
    private static String mkCheckCommandExistsCommand(final String commandName)
    {
        return "type -p " + commandName;
    }

    // Creates bash command. The command returns the list of files inside the directory, sorted by
    // modification time, oldest first
    private static String mkListByOldestModifiedCommand(final String directoryPath)
    {
        // -A: show all entries except of . and ..
        // -1: show one entry per line, nams only
        // -t -r: sort by modification time (the oldest forst thanx to -r)
        return "ls -1 -A -t -r " + directoryPath;
    }

    // ---------------

    private final ISshCommandBuilder sshCommandBuilder;

    private final HighwaterMarkWatcher highwaterMarkWatcher;

    private String remoteFindExecutableOrNull;

    /**
     * @param remoteFindExecutableOrNull The executable to use for checking the last modification
     *            time of files on the remote outgoing host. It should be a GNU find supporting
     *            -printf option.
     * @param kind Description of the directory used in logs
     */
    public FileStoreRemote(final FileWithHighwaterMark fileWithHighwaterMark, final String host,
            final String kind, final IFileSysOperationsFactory factory,
            String remoteFindExecutableOrNull)
    {
        this(fileWithHighwaterMark, host, kind, createSshCommandBuilder(findSSHOrDie(factory)),
                factory, remoteFindExecutableOrNull);
    }

    // exposed for tests
    FileStoreRemote(final FileWithHighwaterMark fileWithHighwaterMark, final String host,
            final String kind, final ISshCommandBuilder sshCommandBuilder,
            final IFileSysOperationsFactory factory, final String remoteFindExecutableOrNull)
    {
        super(fileWithHighwaterMark, host, kind, factory);
        assert host != null : "Unspecified host";
        this.sshCommandBuilder = sshCommandBuilder;
        this.highwaterMarkWatcher =
                createHighwaterMarkWatcher(fileWithHighwaterMark, host, sshCommandBuilder);
        this.remoteFindExecutableOrNull = remoteFindExecutableOrNull;
        if (remoteFindExecutableOrNull != null)
        {
            ensureSpecifiedFindExists(host, remoteFindExecutableOrNull);
        }
    }

    private void ensureSpecifiedFindExists(final String host, final String remoteFindExecutable)
    {
        if (checkFindExecutable(remoteFindExecutableOrNull) == false)
        {
            throw new EnvironmentFailureException("Cannot find specified find executable '"
                    + remoteFindExecutableOrNull + "' on the remote host '" + host + "'.");
        }
    }

    private static File findSSHOrDie(final IFileSysOperationsFactory factory)
    {
        final File ssh = factory.tryFindSshExecutable();
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
        final String pathString = StoreItem.asFile(getPath(), item).getPath();
        final String cmd = mkDeleteFileCommand(pathString);
        final ProcessResult result = tryExecuteCommandRemotely(cmd, QUICK_SSH_TIMEOUT_MILIS);
        final String errMsg = tryGetErrorMessage(result);
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
        final File itemFile = StoreItem.asFile(getPath(), item);
        final String cmd = mkCheckFileExistsCommand(itemFile.getPath());
        final ProcessResult result = tryExecuteCommandRemotely(cmd, QUICK_SSH_TIMEOUT_MILIS);
        return isSuccessfulCheck(result);
    }

    private boolean isSuccessfulCheck(final ProcessResult result)
    {
        return result.isOK() && result.getOutput().size() == 0;
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
        final String itemPath = StoreItem.asFile(getPath(), item).getPath();

        final String findExec = getRemoteFindExecutableOrDie();
        final String cmd = mkFindYoungestModificationTimestampSecCommand(itemPath, findExec);
        final ProcessResult result = tryExecuteCommandRemotely(cmd, LONG_SSH_TIMEOUT_MILIS);
        final String errMsg = tryGetErrorMessage(result);
        if (errMsg == null)
        {
            final String resultLine = result.getOutput().get(0);
            try
            {
                return Long.parseLong(resultLine) * 1000;
            } catch (final NumberFormatException e)
            {
                throw createLastChangeException(item, "The result of " + cmd + " on remote host "
                        + getHost() + "should be a number but was: " + result.getOutput());
            }
        } else
        {
            throw createLastChangeException(item, errMsg);
        }
    }

    private static UnknownLastChangedException createLastChangeException(StoreItem item,
            String errorMsg)
    {
        return new UnknownLastChangedException("Cannot obtain last change time of the item " + item
                + ". Reason: " + errorMsg);
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
        final String errMsg = tryCheckDirectoryAccessible(getPathString(), timeOutMillis);
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
        for (final String findExec : findExecutables)
        {
            boolean ok = checkFindExecutable(findExec);
            if (ok)
            {
                setFindExecutable(findExec);
                return true;
            }
        }
        return false;
    }

    private boolean checkFindExecutable(final String findExec)
    {
        final String cmd = mkCheckCommandExistsCommand(findExec);
        final ProcessResult result = tryExecuteCommandRemotely(cmd, QUICK_SSH_TIMEOUT_MILIS);
        return result.isOK();
    }

    private void setFindExecutable(final String findExecutable)
    {
        this.remoteFindExecutableOrNull = findExecutable;
    }

    private String tryCheckDirectoryAccessible(final String pathString, final long timeOutMillis)
    {
        final String cmd = mkCheckDirectoryFullyAccessibleCommand(pathString);
        final ProcessResult result = tryExecuteCommandRemotely(cmd, timeOutMillis);
        return isSuccessfulCheck(result) ? null
                : ("Directory not accesible: " + getHost() + ":" + pathString);
    }

    private final static List<String> createSshCommand(final String command,
            final File sshExecutable, final String host)
    {
        final ArrayList<String> wrappedCmd = new ArrayList<String>();
        final List<String> sshCommand = Arrays.asList(sshExecutable.getPath(), "-T", host);
        wrappedCmd.addAll(sshCommand);
        wrappedCmd.add(command);
        return wrappedCmd;
    }

    private static ISshCommandBuilder createSshCommandBuilder(final File sshExecutable)
    {
        return new ISshCommandBuilder()
            {

                public List<String> createSshCommand(final String cmd, final String host)
                {
                    return FileStoreRemote.createSshCommand(cmd, sshExecutable, host);
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
        final String host = tryGetHost();
        assert host != null : "host cannot be null";
        return host;
    }

    public final StoreItem[] tryListSortByLastModified(final ISimpleLogger loggerOrNull)
    {
        final String simpleCmd = mkListByOldestModifiedCommand(getPathString());
        final ProcessResult result = tryExecuteCommandRemotely(simpleCmd, LONG_SSH_TIMEOUT_MILIS);
        if (result.isOK())
        {
            return asStoreItems(result.getOutput());
        } else
        {
            return null;
        }
    }

    private static StoreItem[] asStoreItems(final List<String> lines)
    {
        final StoreItem[] items = new StoreItem[lines.size()];
        int i = 0;
        for (final String line : lines)
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

    private ProcessResult tryExecuteCommandRemotely(final String localCmd, final long timeOutMillis)
    {
        final List<String> cmdLine = sshCommandBuilder.createSshCommand(localCmd, getHost());
        final ProcessResult result =
                ProcessExecutionHelper.run(cmdLine, operationLog, machineLog, timeOutMillis,
                        OutputReadingStrategy.ALWAYS, false);
        result.log();
        return result;
    }

    private static String tryGetErrorMessage(final ProcessResult result)
    {
        if (result.isOK() == false)
        {
            return "Command '" + result.getCommandLine() + "' failed with error result "
                    + result.getExitValue();
        } else
        {
            return null;
        }
    }
}
