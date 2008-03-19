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

package ch.systemsx.cisd.common.compression.file;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * An {@link ICompressionMethod} that performs in-place compression of a bulk of files by means of calling an external
 * compression program and running it per file in an external process.
 * 
 * @author Bernd Rinn
 */
public abstract class InPlaceCompressionMethod implements ICompressionMethod, ISelfTestable
{

    private static final String INPROGRESS_MARKER = ".COMPRESSION_IN_PROGRESS_";

    private static final String COMPRESSED_MARKER = ".COMPRESSED_";

    protected static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, InPlaceCompressionMethod.class);

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, InPlaceCompressionMethod.class);

    private File prefixInProgress(File file)
    {
        assert file != null;

        return new File(file.getParent(), INPROGRESS_MARKER + file.getName());
    }

    private File prefixCompressed(File file)
    {
        assert file != null;

        return new File(file.getParent(), COMPRESSED_MARKER + file.getName());
    }

    private File tryRemovePrefix(File file)
    {
        assert file != null;

        final String name = file.getName();
        if (name.startsWith(INPROGRESS_MARKER))
        {
            return new File(file.getParent(), name.substring(INPROGRESS_MARKER.length()));
        } else if (name.startsWith(COMPRESSED_MARKER))
        {
            return new File(file.getParent(), name.substring(COMPRESSED_MARKER.length()));
        } else
        {
            return null;
        }
    }

    private boolean isCompressedFile(File fileToCompress)
    {
        return fileToCompress.getName().startsWith(COMPRESSED_MARKER);
    }

    private boolean isInProgressFile(File fileToCompress)
    {
        return fileToCompress.getName().startsWith(INPROGRESS_MARKER);
    }

    private Status createStatusAndLog(String msgTemplate, Object... params)
    {
        final String msg = String.format(msgTemplate, params);
        operationLog.error(msg);
        return new Status(StatusFlag.FATAL_ERROR, msg);
    }

    /**
     * Creates the command line of the external program to call in order to perform the compression.
     */
    protected abstract List<String> createCommandLine(File fileToCompress, File inProgressFile);

    /**
     * Returns the file extensions of files that this compression method can compress.
     * <p>
     * All extensions need to be returned in lower case.
     */
    protected abstract List<String> getAcceptedExtensions();

    /**
     * Perform any check necessary to see whether the external program that has been found is suitable for the
     * compression task (e.g. program version).
     */
    public abstract void check() throws EnvironmentFailureException, ConfigurationFailureException;

    public boolean accept(File pathname)
    {
        if (pathname.isFile() == false)
        {
            return false;
        }
        final String name = pathname.getName().toLowerCase();
        for (String extension : getAcceptedExtensions())
        {
            if (name.endsWith(extension))
            {
                return true;
            }
        }
        return false;
    }

    public Status compress(File fileToCompress)
    {
        assert fileToCompress != null;

        // Clean up
        if (isInProgressFile(fileToCompress))
        {
            final boolean ok = fileToCompress.delete();
            if (ok)
            {
                operationLog.warn(String.format("Clean up: deleting left-over file '%s'",
                        fileToCompress.getAbsolutePath()));
                return Status.OK;
            } else
            {
                return createStatusAndLog("Clean up: Unable to delete left-over file '%s'",
                        fileToCompress.getAbsolutePath());
            }
        }
        if (isCompressedFile(fileToCompress))
        {
            final File originalFile = tryRemovePrefix(fileToCompress);
            assert originalFile != null;
            if (originalFile.exists())
            {
                final boolean ok = originalFile.delete();
                if (ok == false)
                {
                    return createStatusAndLog("Clean up: Unable to delete uncompressed file '%s'",
                            originalFile);
                }
            }
            if (fileToCompress.renameTo(originalFile))
            {
                return Status.OK;
            } else
            {
                return createStatusAndLog(
                        "Renaming compressed file '%s' to original name '%s' failed.",
                        fileToCompress, originalFile);
            }
        }
        final File inProgressFile = prefixInProgress(fileToCompress);
        final File compressionFinishedFile = prefixCompressed(fileToCompress);
        final boolean runOK =
                ProcessExecutionHelper.runAndLog(createCommandLine(fileToCompress, inProgressFile),
                        operationLog, machineLog);
        if (runOK == false)
        {
            return createStatusAndLog("Unable to compress '%s'.", fileToCompress.getAbsolutePath());
        }
        final boolean firstRenameOK = inProgressFile.renameTo(compressionFinishedFile);
        if (firstRenameOK == false)
        {
            return createStatusAndLog("Unable to rename '%s' to '%s'.", inProgressFile
                    .getAbsolutePath(), compressionFinishedFile.getAbsolutePath());
        }
        final boolean removalOfOriginalOK = fileToCompress.delete();
        if (removalOfOriginalOK == false)
        {
            return createStatusAndLog("Unable to delete original file '%s'", fileToCompress
                    .getAbsolutePath());
        }
        final boolean secondRenameOK = compressionFinishedFile.renameTo(fileToCompress);
        if (secondRenameOK == false)
        {
            return createStatusAndLog("Unable to rename '%s' to '%s'.", compressionFinishedFile
                    .getAbsolutePath(), fileToCompress.getAbsolutePath());
        }
        return Status.OK;
    }

}