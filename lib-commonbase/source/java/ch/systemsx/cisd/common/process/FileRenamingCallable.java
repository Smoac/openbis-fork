/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.process;

import java.io.File;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.concurrent.Callable;

import ch.systemsx.cisd.common.io.Posix;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A <code>IProcess</code> implementation for file renaming.
 * 
 * @author Christian Ribeaud
 */
public final class FileRenamingCallable implements Callable<Boolean>
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FileRenamingCallable.class);

    private final File sourceFile;

    private final File destinationFile;

    private int failures;

    public FileRenamingCallable(final File sourceFile, final File destinationFile)
    {
        assert sourceFile != null : "Unspecified source file";
        assert destinationFile != null : "Unspecified d file";
        this.sourceFile = sourceFile;
        this.destinationFile = destinationFile;
    }

    //
    // Callable
    //

    @Override
    public final Boolean call() throws Exception
    {
        if (sourceFile.exists() == false)
        {
            operationLog.error(String.format(
                    "Path '%s' doesn't exist, so it can't be moved to '%s'.", sourceFile,
                    destinationFile));
            return false;
        }
        if (destinationFile.exists())
        {
            operationLog.error(String.format("Destination path '%s' already exists.",
                    destinationFile));
            return false;
        }
        boolean renamed = sourceFile.renameTo(destinationFile);
        if (renamed == false)
        {
            if (Posix.isOperational())
            {
                try
                {
                    // Try to set the permissions to "all can write"
                    Set<PosixFilePermission> permissions =
                            Posix.getPermissions(sourceFile.getPath());
                    Posix.setAccessMode(sourceFile.getPath(), (short) 0777);
                    renamed = sourceFile.renameTo(destinationFile);
                    Posix.setAccessMode(destinationFile.getPath(), permissions);
                } catch (IOExceptionUnchecked ex)
                {
                    operationLog.warn(String.format(
                            "Moving path '%s' to directory '%s' failed (attempt %d).", sourceFile,
                            destinationFile, ++failures), ex.getCause());
                    return null; // Return null to make CallableExecutor try to repeat operation
                }
            }
            if (renamed == false)
            {
                operationLog.warn(String.format(
                        "Moving path '%s' to directory '%s' failed (attempt %d).", sourceFile,
                        destinationFile, ++failures));
                return null; // Return null to make CallableExecutor try to repeat operation
            }
        }
        return true;
    }
}