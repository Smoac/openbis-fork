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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusWithResult;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandBuilder;
import ch.systemsx.cisd.common.filesystem.ssh.SshCommandExecutor;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.datamover.filesystem.intf.AbstractFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;

/**
 * Allows to operate on files by executing all file system operations on a remote machine with ssh
 * command.
 * 
 * @author Tomasz Pylak
 */
public class FileStoreRemote extends AbstractFileStore
{
    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, FileStoreRemote.class);

    private static final String NO_SUCH_FILE_OR_DIRECTORY_MSG = "No such file or directory";

    public static final long QUICK_SSH_TIMEOUT_MILLIS = 15 * 1000;

    public static final long LONG_SSH_TIMEOUT_MILLIS = 120 * 1000;

    // -- bash commands -------------

    // Creates bash command. The command returns the age of the most recently modified file as the
    // number of seconds (note that it's not in miliseconds!) form the epoch
    private static String mkFindYoungestModificationTimestampSecCommand(final String path,
            final String findExec)
    {
        return findExec + " " + path + " -printf \"%T@\\n\" | sort -n | head -1 ";
    }

    private static String mkLastchangedCommand(final String path,
            final long stopWhenFindYoungerMillis, boolean isRelative, final String lastchangedExec)
    {
        return lastchangedExec + " " + path + " " + (isRelative ? "r" : "")
                + Long.toString(stopWhenFindYoungerMillis / 1000);
    }

    // Creates bash command. The command deletes file or recursively deletes the whole directory.
    // Be careful!
    private static String mkDeleteFileCommand(final String pathString)
    {
        return "rm -fr " + pathString;
    }

    // Creates bash command. The command returns 0 if the command exists and is a file
    private static String mkCheckCommandExistsCommand(final String commandName)
    {
        return "type -p " + commandName;
    }

    // Creates bash command. The command returns 0 if the command exists and is a file
    private static String getVersionCommand(final String commandName)
    {
        return commandName + " --version";
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

    private final SshCommandExecutor sshCommandExecutor;

    private final HighwaterMarkWatcher highwaterMarkWatcher;

    private final boolean skipAccessibilityTest;

    private String remoteLastchangedExecutableOrNull;

    private String remoteFindExecutableOrNull;

    /**
     * @param remoteFindExecutableOrNull The executable to use for checking the last modification
     *            time of files on the remote outgoing host. It should be a GNU find supporting
     *            -printf option.
     * @param kind Description of the directory used in logs
     */
    public FileStoreRemote(final HostAwareFileWithHighwaterMark fileWithHighwaterMark,
            final String kind, final IFileSysOperationsFactory factory,
            final boolean skipAccessibilityTest, final String remoteFindExecutableOrNull,
            final String remoteLastchangedExecutableOrNull)
    {
        this(fileWithHighwaterMark, kind, SshCommandExecutor
                .createSshCommandBuilder(findSSHOrDie(factory)), factory, skipAccessibilityTest,
                remoteFindExecutableOrNull, remoteLastchangedExecutableOrNull);
    }

    @Private
    FileStoreRemote(final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark,
            final String kind, final ISshCommandBuilder sshCommandBuilder,
            final IFileSysOperationsFactory factory, final boolean skipAccessibilityTest,
            final String remoteFindExecutableOrNull, final String remoteLastchangedExecutableOrNull)
    {
        super(hostAwareFileWithHighwaterMark, kind, factory);
        assert hostAwareFileWithHighwaterMark.tryGetHost() != null : "Unspecified host";
        this.skipAccessibilityTest = skipAccessibilityTest;
        this.sshCommandExecutor =
                new SshCommandExecutor(sshCommandBuilder, hostAwareFileWithHighwaterMark
                        .tryGetHost());
        this.highwaterMarkWatcher =
                createHighwaterMarkWatcher(hostAwareFileWithHighwaterMark, sshCommandBuilder);
        setAndLogLastchangedExecutable(remoteLastchangedExecutableOrNull);
        setAndLogFindExecutable(remoteFindExecutableOrNull);
        if (remoteFindExecutableOrNull != null)
        {
            ensureSpecifiedFindExists(hostAwareFileWithHighwaterMark.tryGetHost(),
                    remoteFindExecutableOrNull);
        }
    }

    private void ensureSpecifiedFindExists(final String host, final String remoteFindExecutable)
    {
        if (checkFindExecutable(remoteFindExecutableOrNull) == null)
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
            final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark,
            final ISshCommandBuilder sshCommandBuilder)
    {
        final IFreeSpaceProvider freeSpaceProvider = new RemoteFreeSpaceProvider(sshCommandBuilder);
        final HighwaterMarkWatcher highwaterMarkWatcher =
                new HighwaterMarkWatcher(hostAwareFileWithHighwaterMark.getHighwaterMark(),
                        freeSpaceProvider);
        highwaterMarkWatcher.setPath(hostAwareFileWithHighwaterMark);
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
        final ProcessResult result =
                sshCommandExecutor.tryExecuteCommandRemotely(cmd, QUICK_SSH_TIMEOUT_MILLIS);
        final String errMsg = getErrorMessageOrNull(result);
        if (errMsg == null)
        {
            return Status.OK;
        } else
        {
            return Status.createRetriableError(errMsg);
        }
    }

    public final BooleanStatus exists(final StoreItem item)
    {
        final File itemFile = StoreItem.asFile(getPath(), item);
        return sshCommandExecutor.exists(itemFile, QUICK_SSH_TIMEOUT_MILLIS);
    }

    public final IStoreCopier getCopier(final IFileStore destinationDirectory)
    {
        final boolean requiresDeletion = false;
        return constructStoreCopier(destinationDirectory, requiresDeletion);
    }

    public final StatusWithResult<Long> lastChanged(final StoreItem item,
            final long stopWhenFindYounger)
    {
        if (remoteLastchangedExecutableOrNull != null)
        {
            return lastChangedExec(item, stopWhenFindYounger, false);
        } else
        {
            return lastChangedEmulatedGNUFindExec(item);
        }
    }

    private final StatusWithResult<Long> lastChangedExec(final StoreItem item,
            final long stopWhenFindYoungerMillis, boolean isRelative)
    {
        final String itemPath = StoreItem.asFile(getPath(), item).getPath();

        final String cmd =
                mkLastchangedCommand(itemPath, stopWhenFindYoungerMillis, isRelative,
                        remoteLastchangedExecutableOrNull);
        final ProcessResult result =
                sshCommandExecutor.tryExecuteCommandRemotely(cmd, LONG_SSH_TIMEOUT_MILLIS);
        final String errMsg = getErrorMessageOrNullForLastchanged(result);
        if (errMsg == null)
        {
            final String resultLine = result.getOutput().get(0);
            final long lastChanged = tryParseLastChangedMillis(resultLine);
            return StatusWithResult.<Long> create(lastChanged);
        } else
        {
            return createLastChangeError(item, errMsg);
        }
    }

    private final StatusWithResult<Long> lastChangedEmulatedGNUFindExec(final StoreItem item)
    {
        final String itemPath = StoreItem.asFile(getPath(), item).getPath();

        final String findExec = getRemoteFindExecutableOrDie();
        final String cmd = mkFindYoungestModificationTimestampSecCommand(itemPath, findExec);
        final ProcessResult result =
                sshCommandExecutor.tryExecuteCommandRemotely(cmd, LONG_SSH_TIMEOUT_MILLIS);
        final String errMsg = getErrorMessageOrNullForFind(result);
        if (errMsg == null)
        {
            final String resultLine = result.getOutput().get(0);
            final long lastChanged = tryParseLastChangedMillis(resultLine);
            return StatusWithResult.<Long> create(lastChanged);
        } else
        {
            return createLastChangeError(item, errMsg);
        }
    }

    private static StatusWithResult<Long> createLastChangeError(StoreItem item, String errorMsg)
    {
        return StatusWithResult.<Long> createError("Cannot obtain last change time of the item "
                + item + ". Reason: " + errorMsg);
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

    public final StatusWithResult<Long> lastChangedRelative(final StoreItem item,
            final long stopWhenFindYoungerRelative)
    {
        if (remoteLastchangedExecutableOrNull != null)
        {
            return lastChangedExec(item, stopWhenFindYoungerRelative, true);
        } else
        {
            return lastChangedEmulatedGNUFindExec(item);
        }
    }

    // outgoing and self-test
    public final BooleanStatus checkDirectoryFullyAccessible(final long timeOutMillis)
    {
        final BooleanStatus status =
                skipAccessibilityTest ? BooleanStatus.createTrue() : sshCommandExecutor
                        .checkDirectoryAccessible(getPathString(), timeOutMillis);
        if (status.isSuccess())
        {
            if (this.remoteLastchangedExecutableOrNull != null
                    || checkAvailableAndSetLastchangedUtil())
            {
                return BooleanStatus.createTrue();
            } else if (this.remoteFindExecutableOrNull != null || checkAvailableAndSetFindUtil())
            {
                return BooleanStatus.createTrue();
            } else
            {
                return BooleanStatus.createError(createNoFindUtilMessage());
            }
        } else
        {
            return status;
        }
    }

    public boolean isRemote()
    {
        return true;
    }

    private String createNoFindUtilMessage()
    {
        return "No GNU find utility is present on the remote machine '" + getHost() + "'";
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
            final String findExecutableOrNull = checkFindExecutable(findExec);
            if (findExecutableOrNull != null)
            {
                setAndLogFindExecutable(findExecutableOrNull);
                return true;
            }
        }
        return false;
    }

    private String checkFindExecutable(final String findExec)
    {
        final String cmd = mkCheckCommandExistsCommand(findExec);
        final ProcessResult result =
                sshCommandExecutor.tryExecuteCommandRemotely(cmd, QUICK_SSH_TIMEOUT_MILLIS,
                        false);
        if (machineLog.isDebugEnabled())
        {
            result.log();
        }
        if (result.isOK() && hasAnyOutput(result))
        {
            final String findExecutable = result.getOutput().get(0);
            final String verCmd = getVersionCommand(findExec);
            final ProcessResult verResult =
                    sshCommandExecutor.tryExecuteCommandRemotely(verCmd,
                            QUICK_SSH_TIMEOUT_MILLIS, false);
            if (machineLog.isDebugEnabled())
            {
                verResult.log();
            }
            if (verResult.isOK() && isGNUFind(verResult.getOutput()))
            {
                return findExecutable;
            }
        }
        return null;
    }

    private static boolean hasAnyOutput(ProcessResult result)
    {
        return result.getOutput().size() > 0;
    }

    private boolean isGNUFind(List<String> output)
    {
        return output.size() > 0 && output.get(0).contains("GNU") && output.get(0).contains("find");
    }

    private void setAndLogFindExecutable(final String findExecutableOrNull)
    {
        if (findExecutableOrNull != null)
        {
            this.remoteFindExecutableOrNull = findExecutableOrNull;
            machineLog.info("Using GNU find executable '" + findExecutableOrNull + "' on store '"
                    + this + "'.");
        }
    }

    private boolean checkAvailableAndSetLastchangedUtil()
    {
        final String lastchangedExecutableOrNull = checkExecutable("lastchanged");
        if (lastchangedExecutableOrNull != null)
        {
            setAndLogLastchangedExecutable(lastchangedExecutableOrNull);
        }
        return (lastchangedExecutableOrNull != null);
    }

    private String checkExecutable(final String exec)
    {
        final String cmd = mkCheckCommandExistsCommand(exec);
        final ProcessResult result =
                sshCommandExecutor.tryExecuteCommandRemotely(cmd, QUICK_SSH_TIMEOUT_MILLIS,
                        false);
        if (machineLog.isDebugEnabled())
        {
            result.log();
        }
        if (result.isOK() && hasAnyOutput(result))
        {
            return result.getOutput().get(0);
        } else
        {
            return null;
        }
    }

    private void setAndLogLastchangedExecutable(final String lastchangedExecutableOrNull)
    {
        if (lastchangedExecutableOrNull != null)
        {
            this.remoteLastchangedExecutableOrNull = lastchangedExecutableOrNull;
            machineLog.info("Using 'lastchanged' executable '" + lastchangedExecutableOrNull
                    + "' on store '" + this + "'.");
        }
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
        final ProcessResult result =
                sshCommandExecutor.tryExecuteCommandRemotely(simpleCmd,
                        LONG_SSH_TIMEOUT_MILLIS);
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

    private static String getErrorMessageOrNull(final ProcessResult result)
    {
        if (result.isOK() == false)
        {
            return String.format("Command '%s' failed with error exitval=%d, output=[%s]", result
                    .getCommandLine(), result.getExitValue(), StringUtils.join(result.getOutput(),
                    '\n'));
        } else
        {
            return null;
        }
    }

    private static String getErrorMessageOrNullForLastchanged(final ProcessResult result)
    {
        if (result.isOK() == false)
        {
            return String.format("Command '%s' failed with error exitval=%d, output=[%s]", result
                    .getCommandLine(), result.getExitValue(), StringUtils.join(result.getOutput(),
                    '\n'));
        } else if (hasAnyOutput(result) == false)
        {
            return String.format("Command '%s' ended succesfully, but without any output.", result
                    .getCommandLine());
        } else
        {
            return null;
        }
    }

    // Implementation note: As the find command used via ssh is a pipe, the exit value is the exit
    // value of the last command of the pipe. This is a "head" command that usually doesn't fail.
    // Thus we need other means to find out whether the command was successful than the exit value.
    private static String getErrorMessageOrNullForFind(final ProcessResult result)
    {
        if (result.isOK() == false)
        {
            return String.format("Command '%s' failed with error exitval=%d, output=[%s]", result
                    .getCommandLine(), result.getExitValue(), StringUtils.join(result.getOutput(),
                    '\n'));
        } else if (result.getOutput().size() != 1
                || tryParseLastChangedMillis(result.getOutput().get(0)) == null)
        {
            if (result.getOutput().size() > 0
                    && result.getOutput().get(0).indexOf(NO_SUCH_FILE_OR_DIRECTORY_MSG) > -1)
            {
                return NO_SUCH_FILE_OR_DIRECTORY_MSG;
            } else
            {
                return String.format("Command '%s' failed with output=[%s]", result
                        .getCommandLine(), StringUtils.join(result.getOutput(), '\n'));
            }
        } else
        {
            return null;
        }
    }

    private static Long tryParseLastChangedMillis(String numberStr)
    {
        try
        {
            return Long.parseLong(numberStr) * 1000;
        } catch (final NumberFormatException e)
        {
            return null;
        }
    }

    //
    // FileStore
    //

    @Override
    public final String toString()
    {
        final String pathStr = getPathString();
        if (tryGetRsyncModuleName() != null)
        {
            return "[remote fs] " + getHost() + ":" + tryGetRsyncModuleName() + ":" + pathStr;
        } else
        {
            return "[remote fs] " + getHost() + ":" + pathStr;
        }
    }
}
