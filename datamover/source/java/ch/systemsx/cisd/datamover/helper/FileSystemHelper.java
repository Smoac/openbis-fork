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

package ch.systemsx.cisd.datamover.helper;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.datamover.IntraFSPathMover;
import ch.systemsx.cisd.datamover.LocalProcessorHandler;
import ch.systemsx.cisd.datamover.intf.IReadPathOperations;

/**
 * Basic file system operations helper.
 * 
 * @author Tomasz Pylak on Aug 27, 2007
 */
public class FileSystemHelper
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, LocalProcessorHandler.class);

    public static File ensureDirectoryExists(File dir, String newDirName)
    {
        File dataDir = new File(dir, newDirName);
        if (dataDir.exists() == false)
        {
            if (dataDir.mkdir() == false)
                throw new EnvironmentFailureException("Could not create directory " + dataDir);
        }
        return dataDir;
    }

    /**
     * Lists all resources in a given directory, logs errors.
     */
    public static File[] listFiles(File directory)
    {
        final ISimpleLogger errorLogger = new Log4jSimpleLogger(Level.ERROR, operationLog);
        /**
         * Lists all resources in a given directory, logs errors.
         */
        FileFilter acceptAll = new FileFilter()
            {
                public boolean accept(File file)
                {
                    return true;
                }
            };
        return FileUtilities.listFiles(directory, acceptAll, errorLogger);
    }

    public static IReadPathOperations createPathReadOperations()
    {
        return new IReadPathOperations()
            {

                public boolean exists(File file)
                {
                    return file.exists();
                }

                public long lastChanged(File path)
                {
                    return FileUtilities.lastChanged(path);
                }

                public File[] listFiles(File directory, FileFilter filter, ISimpleLogger loggerOrNull)
                {
                    return FileUtilities.listFiles(directory, filter, loggerOrNull);
                }

                public File[] listFiles(File directory, ISimpleLogger logger)
                {
                    return FileSystemHelper.listFiles(directory);
                }
            };
    }

    /**
     * Moves source file to destination directory.
     */
    public static File tryMoveLocal(File sourceFile, File destinationDir)
    {
        return tryMoveLocal(sourceFile, destinationDir, "");
    }

    /**
     * Moves source file to destination directory, putting <var>prefixTemplate</var> in front of its name after
     * replacing '%t' with the current time stamp.
     */
    public static File tryMoveLocal(File sourceFile, File destinationDirectory, String prefixTemplate)
    {
        boolean ok = new IntraFSPathMover(destinationDirectory, prefixTemplate).handle(sourceFile);
        if (ok == false)
        {
            return null;
        }
        return new File(createDestinationPath(sourceFile, null, destinationDirectory, prefixTemplate));
    }

    /**
     * Creates a destination path for copying <var>sourcePath</var> to <var>destinationDirectory</var> with prefix
     * defined by <var>prefixTemplate</var>. Note that '%t' in <var>prefixTemplate</var> will be replaced by the
     * current time stamp in format YYYYmmddhhMMss.
     */
    public static String createDestinationPath(File sourcePath, String destinationHostOrNull,
            File destinationDirectory, String prefixTemplate)
    {
        assert sourcePath != null;
        assert destinationDirectory != null;
        assert prefixTemplate != null;

        if (destinationHostOrNull != null)
        {
            return destinationHostOrNull + ":" + destinationDirectory.getPath() + File.separator
                    + createPrefix(prefixTemplate) + sourcePath.getName();

        } else
        {
            return destinationDirectory.getAbsolutePath() + File.separator + createPrefix(prefixTemplate)
                    + sourcePath.getName();
        }
    }

    private static String createPrefix(String prefixTemplate)
    {
        return StringUtils.replace(prefixTemplate, "%t", DateFormatUtils.format(System.currentTimeMillis(),
                "yyyyMMddHHmmss"));
    }

}