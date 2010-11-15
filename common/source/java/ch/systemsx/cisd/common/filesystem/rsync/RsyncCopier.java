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

package ch.systemsx.cisd.common.filesystem.rsync;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.filesystem.IDirectoryImmutableCopier;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncVersionChecker.RsyncVersion;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper.IProcessHandler;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper.OutputReadingStrategy;
import ch.systemsx.cisd.common.utilities.ITerminable;

/**
 * A class that encapsulates the <code>rsync</code> call for doing an archive copy.
 * 
 * @author Bernd Rinn
 */
public final class RsyncCopier implements IPathCopier, IDirectoryImmutableCopier
{
    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            RsyncCopier.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            RsyncCopier.class);

    /**
     * The {@link Status} returned if the process was terminated by {@link Process#destroy()}.
     */
    @Private
    static final Status TERMINATED_STATUS = Status.createRetriableError("Process was terminated.");

    private static final Status INTERRUPTED_STATUS = Status
            .createRetriableError("Process was interrupted.");

    private static final Status TIMEOUT_STATUS = Status
            .createRetriableError("Process has stopped because of timeout.");

    private final String rsyncExecutable;

    /**
     * Record for storing information about an rsync executable.
     */
    private static class RsyncRecord
    {
        private final String rsyncExecutable;

        private final RsyncVersion rsyncVersion;

        RsyncRecord(String rsyncExecutable, RsyncVersion rsyncVersion)
        {
            this.rsyncExecutable = rsyncExecutable;
            this.rsyncVersion = rsyncVersion;
        }

        public final String getRsyncExecutable()
        {
            return rsyncExecutable;
        }

        public final RsyncVersion getRsyncVersion()
        {
            return rsyncVersion;
        }
    }

    private Map<String, RsyncRecord> remoteHostRsyncMap = new HashMap<String, RsyncRecord>();

    private final RsyncVersion rsyncVersion;

    private final String sshExecutable;

    private final List<String> additionalCmdLineFlagsOrNull;

    /** If set, overrides all command line parameters for mutable copying. */
    private final List<String> cmdLineFlagsOrNull;

    private final boolean overwrite;

    /**
     * If <code>true</code>, the file system of the destination directory requires that already
     * existing files and directories on the remote side are removed before the copy process is
     * started.
     */
    private final boolean destinationDirectoryRequiresDeletionBeforeCreation;

    // stores the handler to stop the copy process if it has been launched or null otherwise.
    private final AtomicReference<ITerminable> rsyncTerminator;

    /**
     * Constructs an <code>RsyncCopier</code> for use as {@link IDirectoryImmutableCopier}.
     * 
     * @param rsyncExecutable The <code>rsync</code> binary to call for copying.
     */
    public RsyncCopier(final File rsyncExecutable)
    {
        this(rsyncExecutable, null, false, false);
    }

    /**
     * Constructs an <code>RsyncCopier</code> with fully custom command line flags.
     * 
     * @param rsyncExecutable The <code>rsync</code> binary to call for copying.
     * @param sshExecutableOrNull The <code>ssh</code> binary to use for creating tunnels, or
     *            <code>null</code>, if no <code>ssh</code> is available on this machine.
     * @param cmdLineFlags The command line flags to use for the rsync command.
     */
    public RsyncCopier(final File rsyncExecutable, final File sshExecutableOrNull,
            String... cmdLineFlags)
    {
        assert rsyncExecutable != null && rsyncExecutable.exists();
        assert sshExecutableOrNull == null || rsyncExecutable.exists();

        this.rsyncExecutable = rsyncExecutable.getAbsolutePath();
        this.rsyncVersion = RsyncVersionChecker.getVersion(rsyncExecutable.getAbsolutePath());
        this.sshExecutable = (sshExecutableOrNull != null) ? sshExecutableOrNull.getPath() : null;
        this.rsyncTerminator = new AtomicReference<ITerminable>(null);
        this.overwrite = false;
        this.destinationDirectoryRequiresDeletionBeforeCreation = false;
        this.additionalCmdLineFlagsOrNull = null;
        this.cmdLineFlagsOrNull = Arrays.asList(cmdLineFlags);
    }

    /**
     * Constructs an <code>RsyncCopier</code>.
     * 
     * @param rsyncExecutable The <code>rsync</code> binary to call for copying.
     * @param sshExecutableOrNull The <code>ssh</code> binary to use for creating tunnels, or
     *            <code>null</code>, if no <code>ssh</code> is available on this machine.
     * @param destinationDirectoryRequiresDeletionBeforeCreation If <code>true</code>, already
     *            existing files and directories on the remote side will be deleted before starting
     *            the copy process (no overwriting of paths).
     */
    public RsyncCopier(final File rsyncExecutable, final File sshExecutableOrNull,
            final boolean destinationDirectoryRequiresDeletionBeforeCreation,
            final boolean overwrite, final String... cmdLineFlags)
    {
        assert rsyncExecutable != null && rsyncExecutable.exists();
        assert sshExecutableOrNull == null || rsyncExecutable.exists();

        this.rsyncExecutable = rsyncExecutable.getAbsolutePath();
        this.rsyncVersion = RsyncVersionChecker.getVersion(rsyncExecutable.getAbsolutePath());
        this.sshExecutable = (sshExecutableOrNull != null) ? sshExecutableOrNull.getPath() : null;
        this.destinationDirectoryRequiresDeletionBeforeCreation =
                destinationDirectoryRequiresDeletionBeforeCreation;
        this.overwrite = overwrite;
        this.rsyncTerminator = new AtomicReference<ITerminable>(null);
        if (cmdLineFlags.length > 0)
        {
            this.additionalCmdLineFlagsOrNull = Arrays.asList(cmdLineFlags);
        } else
        {
            this.additionalCmdLineFlagsOrNull = null;
        }
        this.cmdLineFlagsOrNull = null;
    }

    private boolean rsyncSupportsAppend(RsyncVersion versionOrNull)
    {
        return versionOrNull == null || versionOrNull.isNewerOrEqual(2, 6, 7);
    }

    private boolean isGoodEnough(RsyncVersion versionOrNull)
    {
        return versionOrNull != null && versionOrNull.isNewerOrEqual(2, 6, 0);
    }

    private boolean isOverwriteMode(final RsyncRecord remoteRsyncOrNull)
    {
        return overwrite
                || destinationDirectoryRequiresDeletionBeforeCreation
                || (rsyncSupportsAppend(rsyncVersion) == false)
                || (remoteRsyncOrNull != null && (rsyncSupportsAppend(remoteRsyncOrNull
                        .getRsyncVersion()) == false));
    }

    //
    // IPathCopier
    //

    public final Status copy(final File sourcePath, final File destinationDirectory)
    {
        return copy(sourcePath, null, destinationDirectory, null, null, null, false);
    }

    public final Status copyContent(final File sourcePath, final File destinationDirectory)
    {
        return copy(sourcePath, null, destinationDirectory, null, null, null, true);
    }

    public final Status copyFromRemote(final File sourcePath, final String sourceHost,
            final File destinationDirectory, String rsyncModuleNameOrNull,
            String rsyncPasswordFileOrNull)
    {
        return copy(sourcePath, sourceHost, destinationDirectory, null, rsyncModuleNameOrNull,
                rsyncPasswordFileOrNull, false);
    }

    public Status copyContentFromRemote(File sourcePath, String sourceHost,
            File destinationDirectory, String rsyncModuleNameOrNull, String rsyncPasswordFileOrNull)
    {
        return copy(sourcePath, sourceHost, destinationDirectory, null, rsyncModuleNameOrNull,
                rsyncPasswordFileOrNull, true);
    }

    public final Status copyToRemote(final File sourcePath, final File destinationDirectory,
            final String destinationHost, String rsyncModuleNameOrNull,
            String rsyncPasswordFileOrNull)
    {
        return copy(sourcePath, null, destinationDirectory, destinationHost, rsyncModuleNameOrNull,
                rsyncPasswordFileOrNull, false);
    }

    public Status copyContentToRemote(File sourcePath, File destinationDirectory,
            String destinationHostOrNull, String rsyncModuleNameOrNull,
            String rsyncPasswordFileOrNull)
    {
        return copy(sourcePath, null, destinationDirectory, destinationHostOrNull,
                rsyncModuleNameOrNull, rsyncPasswordFileOrNull, true);
    }

    //
    // IDirectoryImmutableCopier
    //

    public boolean copyDirectoryImmutably(File sourceDirectory, File destinationDirectory,
            String targetNameOrNull)
    {
        assert sourceDirectory != null;
        assert sourceDirectory.isDirectory() : sourceDirectory.getAbsolutePath();
        assert destinationDirectory != null;
        assert destinationDirectory.isDirectory() : destinationDirectory.getAbsolutePath();

        final List<String> commandLine =
                createCommandLineForImmutableCopy(
                        sourceDirectory,
                        createTargetDirectory(sourceDirectory, destinationDirectory,
                                targetNameOrNull));
        final ProcessResult processResult =
                runCommand(commandLine, ProcessExecutionHelper.DEFAULT_OUTPUT_READING_STRATEGY);
        return processResult.isOK();
    }

    private File createTargetDirectory(final File sourceDirectory, final File destinationDirectory,
            final String targetNameOrNull)
    {
        if (targetNameOrNull == null)
        {
            return new File(destinationDirectory, sourceDirectory.getName());
        } else
        {
            return new File(destinationDirectory, targetNameOrNull);
        }
    }

    /**
     * Terminates the copy process if it is still currently running. If no copy process is running,
     * the method will return immediately. If many copy processes has been launched, only the last
     * one will be terminated. No more copy operations can be started from that point.
     */
    synchronized public final boolean terminate()
    {
        final ITerminable copyProcess = rsyncTerminator.get();
        if (copyProcess != null)
        {
            return copyProcess.terminate();
        } else
        {
            return false;
        }
    }

    private final List<String> createCommandLineForImmutableCopy(final File sourcePath,
            final File destinationPath)
    {
        assert sourcePath != null;
        assert destinationPath != null;
        assert destinationPath.getParentFile().isDirectory() : destinationPath.getParentFile()
                .getAbsolutePath();

        final String absoluteSource = sourcePath.getAbsolutePath();
        final List<String> commandLineList = new ArrayList<String>();
        commandLineList.add(rsyncExecutable);
        commandLineList.add("--archive");
        commandLineList.add("--link-dest=" + toUnix(absoluteSource));
        commandLineList.add(toUnix(absoluteSource) + "/");
        commandLineList.add(toUnix(destinationPath.getAbsolutePath()));

        return commandLineList;
    }

    /**
     * Checks whether the <code>rsync</code> can be executed and has a version >= 2.6.0.
     * 
     * @throws ConfigurationFailureException If the check fails.
     */
    public final void check()
    {
        if (machineLog.isDebugEnabled())
        {
            machineLog.debug(String.format("Testing rsync executable '%s'", rsyncExecutable));
        }
        if (rsyncVersion == null)
        {
            if (OSUtilities.executableExists(rsyncExecutable))
            {
                throw new ConfigurationFailureException(String.format(
                        "Rsync executable '%s' is invalid.", rsyncExecutable));
            } else
            {
                throw new ConfigurationFailureException(String.format(
                        "Rsync executable '%s' does not exist.", rsyncExecutable));
            }
        }
        if (isGoodEnough(rsyncVersion) == false)
        {
            throw new ConfigurationFailureException(String.format(
                    "Rsync executable '%s' is too old (required: 2.6.0, found: %s)",
                    rsyncExecutable, rsyncVersion.getVersionString()));
        }
        if (machineLog.isInfoEnabled())
        {
            machineLog.info(String.format("Using rsync executable '%s', version %s, mode: %s",
                    rsyncExecutable, rsyncVersion.getVersionString(),
                    (isOverwriteMode(null) ? "overwrite" : "append")));
        }
        if (rsyncVersion.isRsyncPreReleaseVersion())
        {
            machineLog
                    .warn(String.format(
                            "The rsync executable '%s' is a pre-release version. It is not recommended "
                                    + "to use such a version in a production environment.",
                            rsyncExecutable));
        }
    }

    public boolean isRemote()
    {
        return false;
    }

    public boolean checkRsyncConnectionViaRsyncServer(String host, String rsyncModule,
            String rsyncPasswordFileOrNull)
    {
        final List<String> commandLineList = new ArrayList<String>();
        commandLineList.add(rsyncExecutable);
        if (rsyncPasswordFileOrNull != null && new File(rsyncPasswordFileOrNull).exists())
        {
            commandLineList.add("--password-file");
            commandLineList.add(rsyncPasswordFileOrNull);
        }
        commandLineList.add(buildUnixPathForServer(host, new File("/"), rsyncModule, false));
        final ProcessResult processResult =
                runCommand(commandLineList, ProcessExecutionHelper.DEFAULT_OUTPUT_READING_STRATEGY);
        processResult.log();
        return processResult.isOK();
    }

    private final static List<String> createSshCommand(final String host,
            final String sshExecutable, final String command)
    {
        final ArrayList<String> wrappedCmd = new ArrayList<String>();
        final List<String> sshCommand = Arrays.asList(sshExecutable, "-T", host);
        wrappedCmd.addAll(sshCommand);
        wrappedCmd.add(command);
        return wrappedCmd;
    }

    public boolean checkRsyncConnectionViaSsh(String host, String rsyncExecutableOnHostOrNull)
    {
        if (remoteHostRsyncMap.containsKey(host))
        {
            return true;
        }
        final String rsyncExec;
        if (rsyncExecutableOnHostOrNull == null)
        {
            rsyncExec = tryFindRemoteRsyncExecutable(host);
        } else
        {
            rsyncExec = rsyncExecutableOnHostOrNull;
        }
        if (rsyncExec == null)
        {
            return false;
        }
        final List<String> commandLineList =
                createSshCommand(host, sshExecutable, rsyncExec + " --version");
        final ProcessResult verResult = runCommand(commandLineList, OutputReadingStrategy.ALWAYS);
        verResult.log();
        if (verResult.isOK() == false || verResult.getOutput().size() == 0)
        {
            return false;
        }
        final RsyncVersion versionOrNull =
                RsyncVersionChecker.tryParseVersionLine(verResult.getOutput().get(0));
        final boolean ok = isGoodEnough(versionOrNull);
        if (ok)
        {
            remoteHostRsyncMap.put(host, new RsyncRecord(rsyncExec, versionOrNull));
            if (machineLog.isInfoEnabled())
            {
                machineLog.info(String.format(
                        "On host '%s': using rsync executable '%s', version %s", host, rsyncExec,
                        ObjectUtils.toString(versionOrNull, "UNKNOWN")));
            }
        } else
        {
            machineLog.error(String.format(
                    "On host '%s': rsync executable '%s', version %s is too old", host, rsyncExec,
                    ObjectUtils.toString(versionOrNull, "UNKNOWN")));
        }
        return ok;
    }

    private String tryFindRemoteRsyncExecutable(String host)
    {
        List<String> commandLineList = createSshCommand(host, sshExecutable, "type -p rsync");
        final ProcessResult result = runCommand(commandLineList, OutputReadingStrategy.ALWAYS);
        result.log();
        if (result.isOK() && result.getOutput().size() != 1)
        {
            if (result.getOutput().size() == 0)
            {
                machineLog.warn(String.format("No output on command '%s'.",
                        StringUtils.join(commandLineList, ' ')));
            } else
            {
                machineLog.warn(String.format("Unexpected output on command '%s':\n%s",
                        StringUtils.join(commandLineList, ' '),
                        StringUtils.join(result.getOutput(), '\n')));
            }
        }
        if (result.isOK() && result.getOutput().size() == 1)
        {
            return result.getOutput().get(0);
        } else
        {
            return null;
        }
    }

    private final Status copy(final File sourcePath, final String sourceHostOrNull,
            final File destinationDirectory, final String destinationHostOrNull,
            final String rsyncModuleNameOrNull, final String rsyncPasswordFileOrNull,
            final boolean copyDirectoryContent)
    {
        assert sourcePath != null;
        assert sourceHostOrNull != null || sourcePath.exists() : logNonExistent(sourcePath);
        assert destinationDirectory != null;
        assert destinationHostOrNull != null || destinationDirectory.isDirectory() : logNonExistent(destinationDirectory);
        // Only one side can be remote
        assert sourceHostOrNull == null || destinationHostOrNull == null;
        final List<String> commandLine =
                createCommandLineForMutableCopy(sourcePath, sourceHostOrNull, destinationDirectory,
                        destinationHostOrNull, rsyncModuleNameOrNull, rsyncPasswordFileOrNull,
                        copyDirectoryContent);
        return createStatus(runCommand(commandLine,
                ProcessExecutionHelper.DEFAULT_OUTPUT_READING_STRATEGY));
    }

    private final String logNonExistent(final File path)
    {
        if (path == null)
        {
            return "null";
        } else
        {
            return "path '" + path.getAbsolutePath() + "' does not exist";
        }
    }

    @Private
    final List<String> createCommandLineForMutableCopy(final File sourcePath,
            final String sourceHostOrNull, final File destinationDirectory,
            final String destinationHostOrNull, final String rsyncModuleNameOrNull,
            final String rsyncPasswordFileOrNull, final boolean copyDirectoryContent)
    {
        assert sourcePath != null && (sourceHostOrNull != null || sourcePath.exists());
        assert destinationDirectory != null
                && (destinationHostOrNull != null || destinationDirectory.isDirectory());
        assert (destinationHostOrNull != null && sshExecutable != null)
                || (destinationHostOrNull == null);
        assert (sourceHostOrNull != null && sshExecutable != null) || (sourceHostOrNull == null);

        final List<String> commandLineList = new ArrayList<String>();
        final RsyncRecord remoteRsyncOrNull =
                tryGetRemoteRsync(sourceHostOrNull, destinationHostOrNull);
        commandLineList.add(rsyncExecutable);
        if (cmdLineFlagsOrNull != null)
        {
            commandLineList.addAll(cmdLineFlagsOrNull);
        } else
        {
            final List<String> standardParameters =
                    Arrays.asList("--archive", "--delete", "--inplace");

            commandLineList.addAll(standardParameters);
            if (isOverwriteMode(remoteRsyncOrNull))
            {
                commandLineList.add("--whole-file");
            } else
            {
                commandLineList.add("--append");
            }
        }
        if (sshExecutable != null && (destinationHostOrNull != null || sourceHostOrNull != null)
                && rsyncModuleNameOrNull == null)
        {
            commandLineList.add("--rsh");
            commandLineList.add(getSshExecutableArgument(sshExecutable));
            if (remoteRsyncOrNull != null)
            {
                commandLineList.add("--rsync-path");
                commandLineList.add(remoteRsyncOrNull.getRsyncExecutable());
            }
        }
        if (rsyncModuleNameOrNull != null && rsyncPasswordFileOrNull != null
                && new File(rsyncPasswordFileOrNull).exists())
        {
            commandLineList.add("--password-file");
            commandLineList.add(rsyncPasswordFileOrNull);
        }
        if (additionalCmdLineFlagsOrNull != null)
        {
            commandLineList.addAll(additionalCmdLineFlagsOrNull);
        }
        commandLineList.add(buildUnixPathForServer(sourceHostOrNull, sourcePath,
                rsyncModuleNameOrNull, copyDirectoryContent));
        commandLineList.add(buildUnixPathForServer(destinationHostOrNull, destinationDirectory,
                rsyncModuleNameOrNull, true));

        return commandLineList;
    }

    private RsyncRecord tryGetRemoteRsync(final String sourceHostOrNull,
            final String destinationHostOrNull)
    {
        final RsyncRecord sourceRemoteRsyncOrNull =
                (sourceHostOrNull == null) ? null : remoteHostRsyncMap.get(sourceHostOrNull);
        final RsyncRecord destinationRemoteRsyncOrNull =
                (destinationHostOrNull == null) ? null : remoteHostRsyncMap
                        .get(destinationHostOrNull);
        final RsyncRecord remoteRsyncOrNull =
                (sourceRemoteRsyncOrNull != null) ? sourceRemoteRsyncOrNull
                        : destinationRemoteRsyncOrNull;
        return remoteRsyncOrNull;
    }

    private final static String getSshExecutableArgument(final String sshExecutable)
    {
        return toUnix(sshExecutable) + " -oBatchMode=yes";
    }

    /**
     * Builds the path for SSH/RSYNC server. <i>As the server is generally expected to be a Unix
     * machine, we build a Unix path regardless of the client platform.</i>
     */
    private static String buildUnixPathForServer(final String host, final File resource,
            final String rsyncModule, final boolean appendSlash)
    {
        if (null == host)
        {
            String path = resource.getAbsolutePath();
            if (appendSlash)
            {
                path += File.separator;
            }
            return toUnix(path);
        } else
        {
            String sep = "::";
            String path = rsyncModule;
            if (path == null)
            {
                sep = ":";
                path = resource.getPath();
            }
            if (appendSlash)
            {
                path += '/';
            }
            // We must not use the absolute path here because that is the business of the
            // destination host.
            return host + sep + path;
        }
    }

    /**
     * Since <code>rsync</code> under Windows is from Cygwin, we need to translate the path into a
     * Cygwin path.
     */
    private static String toUnix(final String path)
    {
        if (OSUtilities.isWindows() == false)
        {
            return path;
        }
        String resultPath = path.replace('\\', '/');
        // Get rid of drive letters.
        if (resultPath.charAt(1) == ':')
        {
            resultPath = "/cygdrive/" + resultPath.charAt(0) + resultPath.substring(2);
        }
        return resultPath;
    }

    private final static Status createStatus(final ProcessResult processResult)
    {
        if (processResult.isTerminated())
        {
            return TERMINATED_STATUS;
        }
        if (processResult.isInterruped())
        {
            return INTERRUPTED_STATUS;
        }
        if (processResult.isTimedOut())
        {
            return TIMEOUT_STATUS;
        }
        int exitValue = processResult.getExitValue();
        final StatusFlag flag = RsyncExitValueTranslator.getStatus(exitValue);
        if (flag == StatusFlag.OK)
        {
            return Status.OK;
        }
        final boolean retriableError = (flag == StatusFlag.RETRIABLE_ERROR);
        return Status.createError(retriableError, RsyncExitValueTranslator.getMessage(exitValue));
    }

    private ProcessResult runCommand(final List<String> commandLine,
            OutputReadingStrategy outputReadingStrategy)
    {
        IProcessHandler processHandler;
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace(String.format("Trying to get lock for running command '%s'",
                    commandLine));
        }
        synchronized (this)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Running command '%s'", commandLine));
            }
            processHandler =
                    ProcessExecutionHelper.runUnblocking(commandLine, outputReadingStrategy,
                            operationLog, machineLog);
            rsyncTerminator.set(processHandler);
        }
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace(String.format("Waiting for process of command '%s' to finish.",
                    commandLine));
        }
        final ProcessResult processResult = processHandler.getResult();
        processResult.log();
        return processResult;
    }

}
