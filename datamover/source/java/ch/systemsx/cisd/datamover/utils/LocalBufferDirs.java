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

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;

/**
 * Paths to different local buffer directories.
 * <p>
 * All these local directories are sub-directories of <code>bufferDir</code> specified in the
 * constructor.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class LocalBufferDirs
{
    private final File copyInProgressDir;

    private final File copyCompleteDir;

    private final File readyToMoveDir;

    private final File tempDir;

    private final long bufferDirHighwaterMark;

    public LocalBufferDirs(final HostAwareFileWithHighwaterMark bufferDir,
            final String copyInProgressDirName, final String copyCompleteDirName,
            final String readyToMoveDirName, final String tempDirName)
    {
        final File bufferDirPath = bufferDir.getFile();
        this.bufferDirHighwaterMark = bufferDir.getHighwaterMark();
        this.copyInProgressDir = ensureDirectoryExists(bufferDirPath, copyInProgressDirName);
        this.copyCompleteDir = ensureDirectoryExists(bufferDirPath, copyCompleteDirName);
        this.readyToMoveDir = ensureDirectoryExists(bufferDirPath, readyToMoveDirName);
        this.tempDir = ensureDirectoryExists(bufferDirPath, tempDirName);
    }

    private final static File ensureDirectoryExists(final File dir, final String newDirName)
    {
        final File dataDir = new File(dir, newDirName);
        if (dataDir.exists() == false)
        {
            if (dataDir.mkdir() == false)
            {
                throw new EnvironmentFailureException("Could not create directory " + dataDir);
            }
        }
        return dataDir;
    }

    /** here data are copied from incoming */
    public final File getCopyInProgressDir()
    {
        return copyInProgressDir;
    }

    /** here data are moved when copy is complete */
    public final File getCopyCompleteDir()
    {
        return copyCompleteDir;
    }

    /** from here data are moved to outgoing directory */
    public final File getReadyToMoveDir()
    {
        return readyToMoveDir;
    }

    /** auxiliary directory used if we need to make a copy of incoming data */
    public final File getTempDir()
    {
        return tempDir;
    }

    public final long getBufferDirHighwaterMark()
    {
        return bufferDirHighwaterMark;
    }
}
