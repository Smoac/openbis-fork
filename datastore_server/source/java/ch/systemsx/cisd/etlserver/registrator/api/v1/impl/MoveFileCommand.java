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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.io.IOException;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;

/**
 * Does a move if the destination is an existing directory, a rename otherwise.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class MoveFileCommand extends AbstractTransactionalCommand
{
    private static final long serialVersionUID = 1L;

    private final String srcParentDirAbsolutePath;

    private final String srcFileName;

    private final String dstParentDirAbsolutePath;

    private final String dstFileName;

    public MoveFileCommand(String srcParentDirAbsolutePath, String srcFileName,
            String dstParentDirAbsolutePath, String dstFileName)
    {
        this.srcParentDirAbsolutePath = srcParentDirAbsolutePath;
        this.srcFileName = srcFileName;
        this.dstParentDirAbsolutePath = dstParentDirAbsolutePath;
        this.dstFileName = dstFileName;
    }

    public void execute()
    {
        File src = new File(srcParentDirAbsolutePath, srcFileName);
        File dst = new File(dstParentDirAbsolutePath, dstFileName);

        if (false == src.exists())
        {
            IOException checkedException = new IOException("Source file for move does not exist");
            throw new IOExceptionUnchecked(checkedException);
        }

        moveFile(src, dst);
    }

    public void rollback()
    {
        // The src is the original location, dst is the location we moved it to. We want to undo the
        // move (mv dst src).
        File src = new File(srcParentDirAbsolutePath, srcFileName);
        File dst = new File(dstParentDirAbsolutePath, dstFileName);
        if (false == dst.exists())
        {
            if (true == src.exists())
            {
                // This has already been rolled back.
            } else
            {
                getOperationLog().error(
                        "Could not undo move command. The file move source file no longer exists.");
            }
            return;
        }

        moveFile(dst, src);
    }

    private void moveFile(File from, File to)
    {
        String entity = from.isFile() ? "file" : "direcrtory";
        getOperationLog().info(
                String.format("Moving %s '%s' to '%s'", entity, from.getAbsolutePath(),
                        to.getAbsolutePath()));
        IFileOperations fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
        fileOperations.move(from, to);
    }

    @Override
    public String toString()
    {
        return "MoveFileCommand [srcParentDirAbsolutePath=" + srcParentDirAbsolutePath
                + ", srcFileName=" + srcFileName + ", dstParentDirAbsolutePath="
                + dstParentDirAbsolutePath + ", dstFileName=" + dstFileName + "]";
    }

}
