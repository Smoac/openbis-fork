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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Utility to create a hard link of a file or copy recursively a directories structure, creating a hard link for each
 * file inside. Note that presence of <code>ln</code> executable is required, which is not available under Windows.
 * 
 * @author Tomasz Pylak
 */
public final class RecursiveHardLinkMaker implements IPathImmutableCopier
{
    private static final String HARD_LINK_EXEC = "ln";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RecursiveHardLinkMaker.class);

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, RecursiveHardLinkMaker.class);

    private final String linkExecPath;

    private RecursiveHardLinkMaker(final String linkExecPath)
    {
        this.linkExecPath = linkExecPath;
    }

    public static final IPathImmutableCopier create(final String linkExecPath)
    {
        return new RecursiveHardLinkMaker(linkExecPath);
    }

    public static final IPathImmutableCopier tryCreate()
    {
        final File lnExec = OSUtilities.findExecutable(HARD_LINK_EXEC);
        if (lnExec == null)
        {
            return null;
        }
        return new RecursiveHardLinkMaker(lnExec.getAbsolutePath());
    }

    /**
     * Copies <var>path</var> (file or directory) to <var>destinationDirectory</var> by duplicating directory
     * structure and creating hard link for each file.
     * <p>
     * <i>Note that <var>nameOrNull</var> cannot already exist in given <var>destinationDirectory</var>.</i>
     * </p>
     */
    public final File tryCopy(final File path, final File destinationDirectory, final String nameOrNull)
    {
        assert path != null : "Given path can not be null.";
        assert destinationDirectory != null && destinationDirectory.isDirectory() : "Given destination directory can not be null and must be a directory.";
        final String destName = nameOrNull == null ? path.getName() : nameOrNull;
        final File destFile = new File(destinationDirectory, destName);
        assert destFile.exists() == false : String.format(
                "File '%s' already exists in given destination directory '%s'", destName, destinationDirectory);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Creating a hard link copy of '%s' in '%s'.", path.getPath(),
                    destinationDirectory.getPath()));
        }
        return tryMakeCopy(path, destinationDirectory, nameOrNull);
    }

    private final File tryMakeCopy(final File resource, final File destinationDirectory, final String nameOrNull)
    {
        if (resource.isFile())
        {
            return tryCreateHardLinkIn(resource, destinationDirectory, nameOrNull);
        } else
        {
            final String name = nameOrNull == null ? resource.getName() : nameOrNull;
            final File dir = tryCreateDir(name, destinationDirectory);
            if (dir == null)
            {
                return null;
            }
            final File[] files = resource.listFiles();
            if (files != null)
            {
                for (final File file : files)
                {
                    if (tryMakeCopy(file, dir, null) == null)
                    {
                        return null;
                    }
                }
            } else
            // Shouldn't happen, but just to be sure.
            {
                if (resource.exists() == false)
                {
                    operationLog.error(String.format("Path '%s' vanished during processing.", resource));
                } else
                {
                    operationLog.error(String.format("Found path '%s' that is neither a file nor a directory.",
                            resource));
                }
            }
            return dir;
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
                machineLog.error(String.format("Directory %s already exists in %s", name, destDir.getAbsolutePath()));
                ok = true;
            } else
            {
                machineLog.error(String.format("Could not create directory %s inside %s.", name, destDir
                        .getAbsolutePath()));
                if (dir.isFile())
                {
                    machineLog.error("There is a file with a same name.");
                }
            }
        }
        return ok ? dir : null;
    }

    private final File tryCreateHardLinkIn(final File file, final File destDir, final String nameOrNull)
    {
        assert file.isFile() : String.format("Given file '%s' must be a file and is not.", file);
        final File destFile = new File(destDir, nameOrNull == null ? file.getName() : nameOrNull);
        final List<String> cmd = createLnCmdLine(file, destFile);
        final boolean ok = ProcessExecutionHelper.runAndLog(cmd, operationLog, machineLog);
        return ok ? destFile : null;
    }

    private final List<String> createLnCmdLine(final File srcFile, final File destFile)
    {
        final List<String> tokens = new ArrayList<String>();
        tokens.add(linkExecPath);
        tokens.add(srcFile.getAbsolutePath());
        tokens.add(destFile.getAbsolutePath());
        return tokens;
    }
}
