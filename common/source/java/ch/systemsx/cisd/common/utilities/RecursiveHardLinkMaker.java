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

package ch.systemsx.cisd.common.utilities;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Utility to create a hard link of a file or copy recursively a directories structure, creating a
 * hard link for each file inside. Note that presence of <code>ln</code> executable is required,
 * which is not available under Windows.
 * 
 * @author Tomasz Pylak
 */
public final class RecursiveHardLinkMaker implements IImmutableCopier
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RecursiveHardLinkMaker.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, RecursiveHardLinkMaker.class);

    private final IFileImmutableCopier fileCopier;

    private RecursiveHardLinkMaker(final IFileImmutableCopier fileCopier)
    {
        this.fileCopier = fileCopier;
    }

    //
    // Factory methods
    //
    public static IImmutableCopier tryCreate(final IFileImmutableCopier fileCopierOrNull)
    {
        if (fileCopierOrNull == null)
        {
            return null;
        } else
        {
            return new RecursiveHardLinkMaker(fileCopierOrNull);
        }
    }
    
    //
    // IImmutableCopier
    //

    /**
     * Copies <var>source</var> (file or directory) to <var>destinationDirectory</var> by
     * duplicating the directory structure and creating a hard link for each file.
     * <p>
     * <i>Note that <var>nameOrNull</var> cannot already exist in given
     * <var>destinationDirectory</var>.</i>
     * </p>
     */
    public boolean copyImmutably(File source, File destinationDirectory, String nameOrNull)
    {
        assert source != null && source.exists();
        assert destinationDirectory != null && destinationDirectory.isDirectory();
        final String destName = (nameOrNull == null) ? source.getName() : nameOrNull;
        final File destFile = new File(destinationDirectory, destName);
        if (destFile.exists())
        {
            operationLog.error(String.format(
                    "File '%s' already exists in given destination directory '%s'", destName,
                    destinationDirectory));
            return false;
        }
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace(String.format("Creating a hard link copy of '%s' in '%s'.", source
                    .getPath(), destinationDirectory.getPath()));
        }
        return primCopyImmutably(source, destinationDirectory, nameOrNull);
    }

    private final boolean primCopyImmutably(final File source, final File destinationDirectory,
            final String nameOrNull)
    {
        if (source.isFile())
        {
            return fileCopier.copyFileImmutably(source, destinationDirectory, nameOrNull);
        } else
        {
            final String name = nameOrNull == null ? source.getName() : nameOrNull;
            final File dir = tryCreateDir(name, destinationDirectory);
            if (dir == null)
            {
                return false;
            }
            final File[] files = source.listFiles();
            if (files != null)
            {
                for (final File file : files)
                {
                    StopException.check();
                    if (primCopyImmutably(file, dir, null) == false)
                    {
                        return false;
                    }
                }
            } else
            // Shouldn't happen, but just to be sure.
            {
                if (source.exists() == false)
                {
                    operationLog.error(String.format("Path '%s' vanished during processing.",
                            source));
                } else
                {
                    operationLog.error(String.format(
                            "Found path '%s' that is neither a file nor a directory.", source));
                }
            }
            return true;
        }
    }

    private final static File tryCreateDir(final String name, final File destDir)
    {
        final File dir = new File(destDir, name);
        boolean ok = dir.mkdir();
        if (ok == false)
        {
            if (dir.isDirectory())
            {
                machineLog.error(String.format("Directory %s already exists in %s", name, destDir
                        .getAbsolutePath()));
                ok = true;
            } else
            {
                machineLog.error(String.format("Could not create directory %s inside %s.", name,
                        destDir.getAbsolutePath()));
                if (dir.isFile())
                {
                    machineLog.error("There is a file with a same name.");
                }
            }
        }
        return ok ? dir : null;
    }

}
