/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.impl;

import java.io.File;

import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CopyFileCommand extends MoveFileCommand
{

    private boolean hardLink;

    public CopyFileCommand(String srcParentDirAbsolutePath, String srcFileName,
            String dstParentDirAbsolutePath, String dstFileName, boolean hardLink)
    {
        super(srcParentDirAbsolutePath, srcFileName, dstParentDirAbsolutePath, dstFileName);
        this.hardLink = hardLink;
    }

    private static final long serialVersionUID = 1L;

    @Override
    public void execute()
    {
        File src = getSrc();
        File dst = getDst();
        if (hardLink)
        {
            IImmutableCopier copier = FastRecursiveHardLinkMaker.tryCreate();
            copier.copyImmutably(src, dst.getParentFile(), dst.getName());
        } else
        {
            IFileOperations fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
            fileOperations.copy(src, dst);
        }
    }

    @Override
    public void rollback()
    {
        File dst = getDst();
        if (dst.exists())
        {
            IFileOperations fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
            fileOperations.delete(dst);
            if (dst.exists())
            {
                getOperationLog().error("Could not delete file '" + dst + "'.");
            }
        }

    }

    @Override
    public String toString()
    {
        return render("CopyFileCommand");
    }
    

}
