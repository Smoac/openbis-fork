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
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.bds.StringUtils;
import ch.systemsx.cisd.common.exceptions.ExceptionWithStatus;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
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

    public BooleanStatus checkSame(File dataSet, File destination)
    {
        if (false == fileOperations.exists(dataSet))
        {
            return BooleanStatus.createFalse("Data set location '" + dataSet + "' doesn't exist");
        } else if (false == fileOperations.exists(destination))
        {
            return BooleanStatus.createFalse("Destination location '" + destination
                    + "' doesn't exist");
        }

        if (dataSet.isDirectory())
        {
            if (destination.isDirectory())
            {
                FileFilter nullFilter = null;
                List<File> storeFiles = FileUtilities.listFiles(dataSet, nullFilter, true);
                List<File> destFiles = FileUtilities.listFiles(destination, nullFilter, true);

                Map<String, Long> dataSetFileSizesByPaths =
                        FolderFileSizesReportGenerator.extractSizesByPaths(storeFiles, dataSet);
                Map<String, Long> destinationFileSizesByPaths =
                        FolderFileSizesReportGenerator.extractSizesByPaths(destFiles, destination);
                String inconsistenciesReport =
                        FolderFileSizesReportGenerator.findInconsistencies(dataSetFileSizesByPaths,
                                destinationFileSizesByPaths);
                if (StringUtils.isBlank(inconsistenciesReport))
                {
                    return BooleanStatus.createTrue();
                } else
                {
                    return BooleanStatus.createFalse("Inconsistencies:\n" + inconsistenciesReport);
                }
            } else
            {
                return BooleanStatus.createFalse("Data set location '" + dataSet
                        + "' is a directory while destination location '" + destination
                        + "' isn't.\n");
            }
        } else if (destination.isDirectory())
        {
            return BooleanStatus.createFalse("Destination location '" + destination
                    + "' is a directory while data set location '" + dataSet + "' isn't.\n");
        } else
        // compare 2 files
        {
            if (dataSet.length() != destination.length())
            {
                return BooleanStatus.createFalse(FolderFileSizesReportGenerator
                        .createDifferentSizesMsg(dataSet.getPath(), dataSet.length(),
                                destination.length()));
            } else
            {
                return BooleanStatus.createTrue();
            }
        }
    }

    public BooleanStatus exists(File file)
    {
        return BooleanStatus.createFromBoolean(fileOperations.exists(file));
    }

    public void createFolder(File folder)
    {
        try
        {
            fileOperations.mkdirs(folder);
        } catch (Exception ex)
        {
            operationLog.error("Creation of '" + folder + "' failed.", ex);
            throw new ExceptionWithStatus(Status.createError("couldn't create directory"));
        }
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

    /** Helper class for generating report about inconsistencies with file sizes in given folders. */
    static class FolderFileSizesReportGenerator
    {
        private static String DESTINATION = "destination";

        private static String STORE = "store";

        private static String DIFFERENT_SIZES_MSG =
                "'%s' - different file sizes; store: %d, destination: %d\n";

        private static String NOT_EXISTS_MSG = "'%s' - exists in %s but is missing in %s\n";

        private static String createMissingFileMsg(String path, String existsIn, String missingIn)
        {
            return String.format(NOT_EXISTS_MSG, path, existsIn, missingIn);
        }

        /**
         * Returns a message about different sizes of given <var>path</var> in store and
         * destination.
         */
        public static String createDifferentSizesMsg(String path, Long storeFileSize,
                Long destinationFileSize)
        {
            return String.format(DIFFERENT_SIZES_MSG, path, storeFileSize, destinationFileSize);
        }

        /**
         * Returns a map from relative file path to file size for given list of files.
         */
        public static Map<String, Long> extractSizesByPaths(List<File> files, File root)
        {
            Map<String, Long> result = new LinkedHashMap<String, Long>();
            for (File file : files)
            {
                String relativePath = FileUtilities.getRelativeFile(root, file);
                result.put(relativePath, file.length());
            }
            return result;
        }

        /**
         * Returns report about inconsistencies with file sizes in given maps or empty string if no
         * inconsistencies were found. The maps contain entries from relative file path to file
         * size.
         */
        public static String findInconsistencies(
                Map<String /* path */, Long /* size */> dataSetFileSizesByPaths,
                Map<String /* path */, Long /* size */> destinationFileSizesByPaths)
        {
            StringBuilder inconsistenciesBuilder = new StringBuilder();
            for (Entry<String, Long> sizeByPath : dataSetFileSizesByPaths.entrySet())
            {
                String path = sizeByPath.getKey();
                Long size = sizeByPath.getValue();
                if (destinationFileSizesByPaths.containsKey(path))
                {
                    Long destinationSize = destinationFileSizesByPaths.get(path);
                    if (false == size.equals(destinationSize))
                    {
                        inconsistenciesBuilder.append(createDifferentSizesMsg(path, size,
                                destinationSize));
                    }
                    // at the end we want only remaining destination files
                    destinationFileSizesByPaths.remove(path);
                } else
                {
                    inconsistenciesBuilder.append(createMissingFileMsg(path, STORE, DESTINATION));
                }
            }
            for (String remainingDestinationPath : destinationFileSizesByPaths.keySet())
            {
                inconsistenciesBuilder.append(createMissingFileMsg(remainingDestinationPath,
                        DESTINATION, STORE));
            }
            return inconsistenciesBuilder.toString();
        }
    }

}