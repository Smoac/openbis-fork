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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.IPathHandler;
import ch.systemsx.cisd.common.utilities.IPathImmutableCopier;
import ch.systemsx.cisd.common.utilities.RegexFileFilter;
import ch.systemsx.cisd.common.utilities.RegexFileFilter.PathType;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IRecoverableTimerTaskFactory;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * Processing of the files on the local machine. This class does not scan its input directory, all
 * resources must registered with a handler by someone else, also in the case of recovery after
 * shutdown.
 * 
 * @author Tomasz Pylak
 */
public class LocalProcessor implements IPathHandler, IRecoverableTimerTaskFactory
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, LocalProcessor.class);

    private static final Logger manualInterventionLog = Logger.getLogger("MANUAL_INTERVENTION");

    private static final ISimpleLogger simpleOperationLog = new Log4jSimpleLogger(operationLog);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, LocalProcessor.class);

    private final Parameters parameters;

    private final IPathImmutableCopier copier;

    private final IPathMover mover;

    // input: where the data are moved from (for recovery).
    private final File inputDir;

    // output: from here data are moved when processing is finished.
    private final File outputDir;

    // auxiliary directory used if we need to make a copy of incoming data
    // Making a copy can take some time, so we do that in the temporary directory. Than we move it
    // from
    // temporary the final destination. In this way external process can start moving data from
    // final
    // destination as soon as they appear there.
    private final File tempDir;

    private final File extraCopyDirOrNull;

    private final HighwaterMarkWatcher highwaterMarkWatcher;

    LocalProcessor(final Parameters parameters, final LocalBufferDirs bufferDirs,
            final IPathImmutableCopier copier, final IPathMover mover,
            final HighwaterMarkWatcher highwaterMarkWatcher)
    {
        this.parameters = parameters;
        this.inputDir = bufferDirs.getCopyCompleteDir();
        this.outputDir = bufferDirs.getReadyToMoveDir();
        this.tempDir = bufferDirs.getTempDir();
        this.highwaterMarkWatcher = highwaterMarkWatcher;
        this.extraCopyDirOrNull = parameters.tryGetExtraCopyDir();
        this.copier = copier;
        this.mover = mover;
    }

    // ----------------

    public TimerTask createRecoverableTimerTask()
    {
        return new TimerTask()
            {
                @Override
                public void run()
                {
                    recover();
                }
            };
    }

    private void recover()
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Recovery starts.");
        }
        recoverTemporaryExtraCopy();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Recovery is finished.");
        }
    }

    private void recoverTemporaryExtraCopy()
    {
        final File[] files = FileUtilities.tryListFiles(tempDir, simpleOperationLog);
        if (files == null || files.length == 0)
        {
            return; // directory is empty, no recovery is needed
        }

        for (int i = 0; i < files.length; i++)
        {
            final File file = files[i];
            if (fileExists(inputDir, file))
            {
                FileUtilities.deleteRecursively(file); // partial copy, delete it
            } else
            {
                // if in previous run we were creating an extra copy, and now we do not, we leave
                // the resource in tmp
                // directory. If now we do create copies, it's not clear what to do, because the
                // destination directory
                // could change. We move the copy to that directory to ensure clean recovery from
                // errors.
                if (extraCopyDirOrNull != null)
                {
                    mover.tryMove(file, extraCopyDirOrNull);
                }
            }
        }
    }

    private static boolean fileExists(final File inputDir, final File file)
    {
        return new File(inputDir, file.getName()).exists();
    }

    // @return true if processing needs to continue, false otherwise
    private boolean doMoveManualOrClean(final File file)
    {
        final EFileManipResult manualMoveStatus = doManualIntervention(file);
        if (manualMoveStatus == EFileManipResult.FAILURE)
        {
            return false; // stop processing
        } else if (manualMoveStatus == EFileManipResult.STOP)
        {
            return false; // stop processing
        } else if (manualMoveStatus == EFileManipResult.CONTINUE)
        {
            // continue processing
        }
        final boolean wholeDeleted = doCleansing(file);
        if (wholeDeleted)
        {
            return false; // stop processing
        }
        return true; // continue processing

    }

    // @return true if the whole resource was deleted
    private boolean doCleansing(final File resource)
    {
        final RegexFileFilter cleansingFilter = new RegexFileFilter();
        final Pattern cleansingRegex = parameters.tryGetCleansingRegex();
        if (cleansingRegex != null)
        {
            log(resource, "Doing cleansing");
            cleansingFilter.add(PathType.FILE, cleansingRegex);
        }
        final ISimpleLogger logger =
                operationLog.isDebugEnabled() ? new Log4jSimpleLogger(operationLog, Level.DEBUG)
                        : null;
        final boolean pathDeleted =
                FileUtilities.deleteRecursively(resource, cleansingFilter, logger);
        return pathDeleted;
    }

    private EFileManipResult doManualIntervention(final File resource)
    {
        final File manualInterventionDir = parameters.tryGetManualInterventionDir();
        if (manualInterventionDir == null)
        {
            return EFileManipResult.CONTINUE;
        }
        final RegexFileFilter manualInterventionFilter = new RegexFileFilter();
        final Pattern manualInterventionRegex = parameters.tryGetManualInterventionRegex();
        if (manualInterventionRegex != null)
        {
            manualInterventionFilter.add(PathType.ALL, manualInterventionRegex);
        }

        final boolean needsManualIntervention = manualInterventionFilter.accept(resource);
        logManualIntervention(resource, needsManualIntervention);
        if (needsManualIntervention)
        {
            log(resource, "Moving to manual intervention directory");
            final File movedFile = mover.tryMove(resource, manualInterventionDir);
            return (movedFile != null) ? EFileManipResult.STOP : EFileManipResult.FAILURE;
        } else
        {
            return EFileManipResult.CONTINUE;
        }
    }

    private static void log(final File path, final String description)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("%s on %s", description, path.getPath()));
        }
    }

    private static void logManualIntervention(final File path, final boolean needsManualIntervention)
    {
        if (manualInterventionLog.isInfoEnabled())
        {
            manualInterventionLog.info(String.format(
                    "%s %s [created: %3$tY-%3$tm-%3$td %3$tH:%3$tM:%3$tS]",
                    needsManualIntervention ? "ATTENTION" : "DEFAULT", path.getAbsolutePath(), path
                            .lastModified()));
        }
    }

    //
    // IPathHandler
    //

    public final void handle(final File path)
    {
        final boolean continueProcessing = doMoveManualOrClean(path);
        if (continueProcessing == false)
        {
            return; // stop processing
        }

        File extraTmpCopy = null;
        if (extraCopyDirOrNull != null)
        {
            extraTmpCopy = new File(tempDir, path.getName());
            if (extraTmpCopy.exists())
            {
                operationLog.warn(String.format(
                        "Half-finished extra copy directory '%s' exists - removing it.",
                        extraTmpCopy.getAbsolutePath()));
                if (FileUtilities.deleteRecursively(extraTmpCopy) == false)
                {
                    notificationLog.error(String.format(
                            "Removal of half-finished extra copy directory '%s' failed.",
                            extraTmpCopy.getAbsolutePath()));
                    return;
                }
            }
            extraTmpCopy = copier.tryCopy(path, tempDir, null);
            if (extraTmpCopy == null)
            {
                notificationLog.error(String.format("Creating extra copy of '%s' failed.", path));
                return;
            }
        }

        final File movedFile = mover.tryMove(path, outputDir);
        if (movedFile == null)
        {
            notificationLog.error(String.format(
                    "Moving '%s' to '%s' for final moving process failed.", path, outputDir));
            return;
        }

        if (extraTmpCopy != null)
        {
            assert extraCopyDirOrNull != null;
            final File extraCopy = mover.tryMove(extraTmpCopy, extraCopyDirOrNull);
            if (extraCopy == null)
            {
                notificationLog.error(String.format(
                        "Moving temporary extra copy '%s' to destination '%s' failed.",
                        extraTmpCopy, extraCopyDirOrNull));
            }
        }
    }

    public final boolean mayHandle(final File path)
    {
        highwaterMarkWatcher.run();
        return highwaterMarkWatcher.isBelow() == false;
    }

    //
    // Helper classes
    //

    private enum EFileManipResult
    {
        CONTINUE, FAILURE, STOP
    }

}
