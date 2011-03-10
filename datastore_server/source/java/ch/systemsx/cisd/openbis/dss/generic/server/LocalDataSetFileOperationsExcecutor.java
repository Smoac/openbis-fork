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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ExceptionWithStatus;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

public final class LocalDataSetFileOperationsExcecutor implements IDataSetFileOperationsExecutor
{
    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            LocalDataSetFileOperationsExcecutor.class);

    private final IFileOperations fileOperations;

    public LocalDataSetFileOperationsExcecutor(IFileOperations fileOperations)
    {
        this.fileOperations = fileOperations;
    }

    public BooleanStatus exists(File file)
    {
        return BooleanStatus.createFromBoolean(fileOperations.exists(file));
    }

    public void deleteFolder(File folder)
    {
        try
        {
            fileOperations.deleteRecursively(folder);
        } catch (Exception ex)
        {
            operationLog.error("Deletion of '" + folder + "' failed.", ex);
            throw new ExceptionWithStatus(Status.createError("couldn't delete"));
        }
    }

    public void copyDataSetToDestination(File dataSet, File destination)
    {
        try
        {
            if (dataSet.isFile())
            {
                fileOperations.copyFileToDirectory(dataSet, destination);
            } else
            {
                fileOperations.copyDirectoryToDirectory(dataSet, destination);
            }
            new File(destination, dataSet.getName()).setLastModified(dataSet.lastModified());
        } catch (Exception ex)
        {
            operationLog.error("Couldn't copy '" + dataSet + "' to '" + destination + "'", ex);
            throw new ExceptionWithStatus(Status.createError("copy failed"), ex);
        }
    }

    public void retrieveDataSetFromDestination(File dataSet, File destination)
    {
        try
        {
            if (destination.isFile())
            {
                fileOperations.copyFileToDirectory(destination, dataSet);
            } else
            {
                fileOperations.copyDirectoryToDirectory(destination, dataSet);
            }
            new File(dataSet, destination.getName()).setLastModified(destination.lastModified());
        } catch (Exception ex)
        {
            operationLog.error("Couldn't retrieve '" + destination + "' to '" + dataSet + "'", ex);
            throw new ExceptionWithStatus(Status.createError("retrieve failed"), ex);
        }
    }

    public void renameTo(File newFile, File oldFile)
    {
        boolean result = oldFile.renameTo(newFile);
        if (result == false)
        {
            operationLog.error("Couldn't rename '" + oldFile + "' to '" + newFile + "'.");
            throw new ExceptionWithStatus(Status.createError("rename failed"));
        }
    }

    public void createMarkerFile(File markerFile)
    {
        try
        {
            boolean result = markerFile.createNewFile();
            if (result == false)
            {
                throw new IOException("File '" + markerFile + "' already exists.");
            }
        } catch (IOException ex)
        {
            operationLog.error("Couldn't create marker file '" + markerFile + "'.", ex);
            throw new ExceptionWithStatus(Status.createError("creating a marker file failed"), ex);
        }
    }

}