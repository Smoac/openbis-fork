/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator;

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.etlserver.IStoreRootDirectoryHolder;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class MarkerFileUtility
{
    private final Logger operationLog;

    private final Logger notificationLog;

    private final IFileOperations fileOperations;

    private final IStoreRootDirectoryHolder storeRootDirectoryHolder;

    public MarkerFileUtility(Logger operationLog, Logger notificationLog,
            IFileOperations fileOperations, IStoreRootDirectoryHolder storeRootDirectoryHolder)
    {
        this.operationLog = operationLog;
        this.notificationLog = notificationLog;
        this.fileOperations = fileOperations;
        this.storeRootDirectoryHolder = storeRootDirectoryHolder;
    }

    /**
     * From given <var>isFinishedPath</var> gets the incoming data set path and checks it.
     * 
     * @return <code>null</code> if a problem has happened. Otherwise a useful and usable incoming
     *         data set path is returned.
     */
    public final File getIncomingDataSetPathFromMarker(final File isFinishedPath)
    {
        final File incomingDataSetPath =
                FileUtilities.removePrefixFromFileName(isFinishedPath, IS_FINISHED_PREFIX);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Getting incoming data set path '%s' from is-finished path '%s'",
                    incomingDataSetPath, isFinishedPath));
        }
        final String errorMsg =
                fileOperations.checkPathFullyAccessible(incomingDataSetPath, "incoming data set");
        if (errorMsg != null)
        {
            fileOperations.delete(isFinishedPath);
            throw EnvironmentFailureException.fromTemplate(String.format(
                    "Error moving path '%s' from '%s' to '%s': %s", incomingDataSetPath.getName(),
                    incomingDataSetPath.getParent(),
                    storeRootDirectoryHolder.getStoreRootDirectory(), errorMsg));
        }
        return incomingDataSetPath;
    }

    public boolean deleteAndLogIsFinishedMarkerFile(File isFinishedFile)
    {
        if (fileOperations.exists(isFinishedFile) == false)
        {
            return false;
        }
        final boolean ok = fileOperations.delete(isFinishedFile);
        final String absolutePath = isFinishedFile.getAbsolutePath();
        if (ok == false)
        {
            notificationLog.error(String.format("Removing file '%s' failed.", absolutePath));
        } else
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("File '%s' has been removed.", absolutePath));
            }
        }
        return ok;
    }
}
