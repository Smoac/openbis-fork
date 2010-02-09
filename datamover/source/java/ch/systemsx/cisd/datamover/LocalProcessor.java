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
import java.io.FileFilter;
import java.util.Properties;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.filesystem.IPathHandler;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IRecoverableTimerTaskFactory;
import ch.systemsx.cisd.datamover.transformation.ITransformator;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * Processing of the files on the local machine. This class does not scan its input directory, all
 * resources must registered with a handler by someone else, also in the case of recovery after
 * shutdown.
 * 
 * @author Tomasz Pylak
 */
public final class LocalProcessor implements IPathHandler, IRecoverableTimerTaskFactory
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, LocalProcessor.class);

    private static final Logger manualInterventionLog = Logger.getLogger("MANUAL_INTERVENTION");

    private static final ISimpleLogger simpleOperationLog = new Log4jSimpleLogger(operationLog);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, LocalProcessor.class);

    private final IImmutableCopier copier;

    private final IPathMover mover;

    // Input: where the data are moved from (for recovery).
    private final File inputDir;

    // Output: from here data are moved when processing is finished.
    private final File outputDir;

    // Auxiliary directory used if we need to make a copy of incoming data. Making a copy can take
    // some time, so we do that in the temporary directory. Than we move it from temporary the final
    // destination. In this way external process can start moving data from final destination as
    // soon as they appear there.
    private final File tempDir;

    private final File extraCopyDirOrNull;

    private final FileFilter manualInterventionFileFilter;

    private final FileFilter cleansingFileFilter;

    private final File manualInterventionDir;

    private boolean stopped = false;

    private final ITransformator transformatorOrNull;

    LocalProcessor(final Parameters parameters, final LocalBufferDirs bufferDirs,
            final IImmutableCopier copier, final IPathMover mover)
    {
        this.inputDir = bufferDirs.getCopyCompleteDir();
        this.outputDir = bufferDirs.getReadyToMoveDir();
        this.tempDir = bufferDirs.getTempDir();
        this.extraCopyDirOrNull = parameters.tryGetExtraCopyDir();
        this.manualInterventionDir = parameters.tryGetManualInterventionDir();
        this.manualInterventionFileFilter = tryCreateManualInterventionFileFilter(parameters);
        this.cleansingFileFilter = tryCreateCleansingFileFilter(parameters);
        this.transformatorOrNull = tryCreateTransformator(parameters);
        this.copier = copier;
        this.mover = mover;
    }

    private final static FileFilter tryCreateCleansingFileFilter(final Parameters parameters)
    {
        final Pattern cleansingRegex = parameters.tryGetCleansingRegex();
        if (cleansingRegex != null)
        {
            return FileFilterUtils.andFileFilter(new RegexFileFilter(cleansingRegex),
                    FileFilterUtils.fileFileFilter());
        }
        return null;
    }

    private final static FileFilter tryCreateManualInterventionFileFilter(
            final Parameters parameters)
    {
        final Pattern manualInterventionRegex = parameters.tryGetManualInterventionRegex();
        if (manualInterventionRegex != null)
        {
            return new RegexFileFilter(manualInterventionRegex);
        }
        return null;
    }

    private final static ITransformator tryCreateTransformator(final Parameters parameters)
    {
        final String className = parameters.tryGetTransformatorClassName();
        final Properties transformatorProperties = parameters.tryGetTransformatorProperties();
        if (className != null)
        {
            try
            {
                return ClassUtils.create(ITransformator.class, className, transformatorProperties);
            } catch (ConfigurationFailureException ex)
            {
                throw ex; // rethrow the exception without changing the message
            } catch (Exception ex)
            {
                throw new ConfigurationFailureException("Cannot find the transformator class '"
                        + className + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
            }
        }
        return null;
    }

    private final void recover()
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

    private final void recoverTemporaryExtraCopy()
    {
        final File[] files = FileUtilities.tryListFiles(tempDir, simpleOperationLog);
        if (files == null || files.length == 0)
        {
            // Directory is empty, no recovery is needed
            return;
        }
        for (int i = 0; i < files.length; i++)
        {
            final File file = files[i];
            if (fileExists(inputDir, file))
            {
                // Partial copy, delete it
                FileUtilities.deleteRecursively(file);
            } else
            {
                // If in previous run we were creating an extra copy, and now we do not, we leave
                // the resource in tmp directory. If now we do create copies, it's not clear what to
                // do, because the destination directory could change. We move the copy to that
                // directory to ensure clean recovery from errors.
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

    /**
     * @return <code>true</code> if processing needs to continue, <code>false</code> otherwise.
     */
    private final boolean doMoveManualOrClean(final File file)
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

    /**
     * @return only returns <code>true</code> if the whole given <var>resource</var> was deleted.
     */
    private final boolean doCleansing(final File resource)
    {
        if (cleansingFileFilter != null)
        {
            log(resource, "Doing cleansing");
            final ISimpleLogger logger =
                    operationLog.isDebugEnabled() ? new Log4jSimpleLogger(operationLog, Level.DEBUG)
                            : null;
            return FileUtilities.deleteRecursively(resource, cleansingFileFilter, logger);
        }
        return false;
    }

    private final EFileManipResult doManualIntervention(final File resource)
    {
        if (manualInterventionDir == null || manualInterventionFileFilter == null)
        {
            return EFileManipResult.CONTINUE;
        }
        final boolean needsManualIntervention = manualInterventionFileFilter.accept(resource);
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

    private final static void log(final File path, final String description)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("%s on %s", description, path.getPath()));
        }
    }

    private final static void logManualIntervention(final File path,
            final boolean needsManualIntervention)
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
        stopped = false;
        try
        {
            final boolean continueProcessing = doMoveManualOrClean(path);
            if (continueProcessing == false)
            {
                // Stop processing
                return;
            }

            // transformation step
            if (transformatorOrNull != null)
            {
                operationLog.info(String
                        .format("START_TRANSFORMATION '%s'", path.getAbsolutePath()));
                Status transformationStatus = transformatorOrNull.transform(path);
                if (transformationStatus.isError())
                {
                    notificationLog.error(String.format("FAILED_TRANSFORMATION '%s': %s", path
                            .getAbsolutePath(), transformationStatus.tryGetErrorMessage()));
                    return;
                } else
                {
                    operationLog.info(String.format("FINISHED_TRANSFORMATION '%s'", path
                            .getAbsolutePath()));
                }
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
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format(
                            "Creating extra copy of directory '%s' to '%s'.", path
                                    .getAbsolutePath(), tempDir.getAbsoluteFile()));
                }
                final boolean ok = copier.copyImmutably(path, tempDir, null);
                if (ok == false)
                {
                    notificationLog.error(String.format("Creating extra copy of '%s' failed.", path
                            .getAbsolutePath()));
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
        } catch (InterruptedExceptionUnchecked ex)
        {
            stopped = true;
        }
    }

    public boolean isStopped()
    {
        return stopped;
    }

    //
    // IRecoverableTimerTaskFactory
    //

    public final TimerTask createRecoverableTimerTask()
    {
        return new TimerTask()
            {

                //
                // TimerTask
                //

                @Override
                public final void run()
                {
                    recover();
                }
            };
    }

    //
    // Helper classes
    //

    private enum EFileManipResult
    {
        CONTINUE, FAILURE, STOP
    }

}
