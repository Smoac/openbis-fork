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

package ch.systemsx.cisd.datamover.utils;

import java.io.File;

import ch.systemsx.cisd.datamover.Parameters;
import ch.systemsx.cisd.datamover.filesystem.LocalFileSystem;

/**
 * Paths to different local buffer directories.
 * @author Tomasz Pylak on Aug 29, 2007
 */
public class LocalBufferDirs
{
    private final File copyInProgressDir;

    private final File copyCompleteDir;

    private final File readyToMoveDir;

    private final File tempDir;

    public LocalBufferDirs(Parameters parameters, String copyInProgressDirName, String copyCompleteDirName,
            String readyToMoveDirName, String tempDirName)
    {
        File buffer = parameters.getBufferStore().getPath();
        this.copyInProgressDir = LocalFileSystem.ensureDirectoryExists(buffer, copyInProgressDirName);
        this.copyCompleteDir = LocalFileSystem.ensureDirectoryExists(buffer, copyCompleteDirName);
        this.readyToMoveDir = LocalFileSystem.ensureDirectoryExists(buffer, readyToMoveDirName);
        this.tempDir = LocalFileSystem.ensureDirectoryExists(buffer, tempDirName);
    }

    /** here data are copied from incoming */
    public File getCopyInProgressDir()
    {
        return copyInProgressDir;
    }

    /** here data are moved when copy is complete */
    public File getCopyCompleteDir()
    {
        return copyCompleteDir;
    }

    /** from here data are moved to outgoing directory */
    public File getReadyToMoveDir()
    {
        return readyToMoveDir;
    }

    /** auxiliary directory used if we need to make a copy of incoming data */
    public File getTempDir()
    {
        return tempDir;
    }
}
